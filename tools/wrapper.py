#!/usr/bin/env python3
"""Validated profile generator and build/run entry point for ProjectGeminiApps."""

from __future__ import annotations

import argparse
import fcntl
import hashlib
import json
import os
from pathlib import Path
import plistlib
import re
import shutil
import struct
import subprocess
import sys
import tempfile
import time
import tomllib
from typing import Any
from urllib.parse import urlsplit
import xml.etree.ElementTree as ElementTree


ROOT = Path(__file__).resolve().parents[1]
PROFILES_DIR = ROOT / "profiles"
GENERATED_DIR = ROOT / "config" / "generated"
DIST_DIR = ROOT / "dist"
SUPPORTED_CAPABILITIES = {
    "external_navigation",
    "offline_retry",
    "persistent_session",
    "web_history",
}
TOP_LEVEL_KEYS = {
    "schema_version",
    "id",
    "display_name",
    "subtitle",
    "start_url",
    "allowed_origins",
    "icon",
    "version_name",
    "version_code",
    "required_capabilities",
    "android",
    "ios",
}
ANDROID_KEYS = {"application_id"}
IOS_KEYS = {"bundle_id", "scheme", "url_scheme", "associated_domain"}
PROFILE_ID_RE = re.compile(r"^[a-z][a-z0-9_]*$")
ANDROID_ID_RE = re.compile(r"^[a-z][a-z0-9_]*(?:\.[a-z][a-z0-9_]*)+$")
IOS_ID_RE = re.compile(r"^[A-Za-z0-9][A-Za-z0-9-]*(?:\.[A-Za-z0-9][A-Za-z0-9-]*)+$")
SCHEME_RE = re.compile(r"^[A-Za-z][A-Za-z0-9_-]*$")
URL_SCHEME_RE = re.compile(r"^[a-z][a-z0-9+.-]*$")
VERSION_RE = re.compile(r"^[0-9]+\.[0-9]+\.[0-9]+(?:[-+][0-9A-Za-z.-]+)?$")


class ProfileError(ValueError):
    pass


def fail(message: str) -> None:
    raise ProfileError(message)


def run(command: list[str], *, cwd: Path = ROOT, env: dict[str, str] | None = None) -> None:
    print("+", " ".join(command), flush=True)
    subprocess.run(command, cwd=cwd, env=env, check=True)


def output(command: list[str], *, cwd: Path = ROOT, env: dict[str, str] | None = None) -> str:
    return subprocess.check_output(command, cwd=cwd, env=env, text=True).strip()


def require_type(value: Any, expected: type, field: str) -> None:
    if expected is int:
        valid = isinstance(value, int) and not isinstance(value, bool)
    else:
        valid = isinstance(value, expected)
    if not valid:
        fail(f"{field} must be {expected.__name__}")


def canonical_origin(raw: str, field: str) -> str:
    require_type(raw, str, field)
    parsed = urlsplit(raw)
    if parsed.scheme.lower() != "https":
        fail(f"{field} must use https")
    if parsed.username is not None or parsed.password is not None:
        fail(f"{field} must not contain userinfo")
    if not parsed.hostname:
        fail(f"{field} must contain a host")
    if parsed.path not in ("", "/") or parsed.query or parsed.fragment:
        fail(f"{field} must be a canonical origin without path, query, or fragment")
    try:
        host = parsed.hostname.encode("idna").decode("ascii").lower().rstrip(".")
        port = parsed.port
    except (UnicodeError, ValueError) as error:
        fail(f"{field} is invalid: {error}")
    suffix = "" if port in (None, 443) else f":{port}"
    return f"https://{host}{suffix}"


def start_url_origin(raw: str, field: str) -> str:
    require_type(raw, str, field)
    parsed = urlsplit(raw)
    if parsed.scheme.lower() != "https" or not parsed.hostname:
        fail(f"{field} must be an absolute https URL")
    if parsed.username is not None or parsed.password is not None:
        fail(f"{field} must not contain userinfo")
    try:
        host = parsed.hostname.encode("idna").decode("ascii").lower().rstrip(".")
        port = parsed.port
    except (UnicodeError, ValueError) as error:
        fail(f"{field} is invalid: {error}")
    suffix = "" if port in (None, 443) else f":{port}"
    return f"https://{host}{suffix}"


def validate_plain_text(value: str, field: str) -> None:
    if not value.strip() and field != "subtitle":
        fail(f"{field} must not be blank")
    if any(token in value for token in ("\n", "\r", "\x00", "$(", "//", "/*")):
        fail(f"{field} contains characters unsafe for generated build settings")


def validate_png(path: Path, field: str) -> None:
    if not path.is_file():
        fail(f"{field} does not exist: {path}")
    data = path.read_bytes()[:33]
    if len(data) < 33 or data[:8] != b"\x89PNG\r\n\x1a\n" or data[12:16] != b"IHDR":
        fail(f"{field} must be a PNG")
    width, height, _, color_type = struct.unpack(">IIBB", data[16:26])
    if (width, height) != (1024, 1024):
        fail(f"{field} must be exactly 1024x1024")
    if color_type not in (0, 2, 3):
        fail(f"{field} must not contain an alpha channel")


def validated_icon_path(raw: str, profile_id: str) -> Path:
    require_type(raw, str, f"{profile_id}.icon")
    relative = Path(raw)
    if relative.is_absolute() or ".." in relative.parts:
        fail(f"{profile_id}.icon must be a relative path without traversal")
    icons_root = (PROFILES_DIR / "icons").resolve()
    resolved = (PROFILES_DIR / relative).resolve()
    if icons_root != resolved.parent and icons_root not in resolved.parents:
        fail(f"{profile_id}.icon must stay inside profiles/icons")
    validate_png(resolved, f"{profile_id}.icon")
    return resolved


def load_profiles() -> dict[str, dict[str, Any]]:
    profiles: dict[str, dict[str, Any]] = {}
    normalized_ids: dict[str, str] = {}
    android_ids: dict[str, str] = {}
    ios_ids: dict[str, str] = {}
    schemes: dict[str, str] = {}

    files = sorted(PROFILES_DIR.glob("*.toml"))
    if not files:
        fail("No profiles found")

    for path in files:
        try:
            profile = tomllib.loads(path.read_text(encoding="utf-8"))
        except (tomllib.TOMLDecodeError, OSError) as error:
            fail(f"Unable to read {path.name}: {error}")
        unknown = set(profile) - TOP_LEVEL_KEYS
        missing = TOP_LEVEL_KEYS - set(profile)
        if unknown:
            fail(f"{path.name} has unknown fields: {', '.join(sorted(unknown))}")
        if missing:
            fail(f"{path.name} is missing fields: {', '.join(sorted(missing))}")

        profile_id = profile["id"]
        require_type(profile_id, str, f"{path.name}.id")
        if not PROFILE_ID_RE.fullmatch(profile_id):
            fail(f"{path.name}.id must match {PROFILE_ID_RE.pattern}")
        if path.stem != profile_id:
            fail(f"{path.name} must be named {profile_id}.toml")
        normalized = profile_id.replace("-", "_").lower()
        if normalized in normalized_ids:
            fail(f"Profile normalization collision: {profile_id} and {normalized_ids[normalized]}")
        normalized_ids[normalized] = profile_id
        if profile_id in profiles:
            fail(f"Duplicate profile id: {profile_id}")

        if profile["schema_version"] != 1:
            fail(f"{profile_id}.schema_version must be 1")
        for field in ("display_name", "subtitle", "start_url", "icon", "version_name"):
            require_type(profile[field], str, f"{profile_id}.{field}")
        validate_plain_text(profile["display_name"], f"{profile_id}.display_name")
        validate_plain_text(profile["subtitle"], "subtitle")
        if not VERSION_RE.fullmatch(profile["version_name"]):
            fail(f"{profile_id}.version_name must be semantic version text")
        require_type(profile["version_code"], int, f"{profile_id}.version_code")
        if profile["version_code"] <= 0:
            fail(f"{profile_id}.version_code must be positive")

        require_type(profile["allowed_origins"], list, f"{profile_id}.allowed_origins")
        if not profile["allowed_origins"]:
            fail(f"{profile_id}.allowed_origins must not be empty")
        origins = [canonical_origin(item, f"{profile_id}.allowed_origins") for item in profile["allowed_origins"]]
        if len(origins) != len(set(origins)):
            fail(f"{profile_id}.allowed_origins contains duplicates after normalization")
        profile["allowed_origins"] = origins
        profile["start_url_origin"] = start_url_origin(profile["start_url"], f"{profile_id}.start_url")
        if profile["start_url_origin"] not in origins:
            fail(f"{profile_id}.start_url origin is not allowlisted")

        require_type(profile["required_capabilities"], list, f"{profile_id}.required_capabilities")
        if not all(isinstance(item, str) for item in profile["required_capabilities"]):
            fail(f"{profile_id}.required_capabilities must contain strings")
        unsupported = set(profile["required_capabilities"]) - SUPPORTED_CAPABILITIES
        if unsupported:
            fail(f"{profile_id} requests unsupported capabilities: {', '.join(sorted(unsupported))}")
        profile["icon_path"] = validated_icon_path(profile["icon"], profile_id)

        for section, allowed_keys in (("android", ANDROID_KEYS), ("ios", IOS_KEYS)):
            require_type(profile[section], dict, f"{profile_id}.{section}")
            unknown_nested = set(profile[section]) - allowed_keys
            missing_nested = allowed_keys - set(profile[section])
            if unknown_nested or missing_nested:
                fail(
                    f"{profile_id}.{section} schema mismatch; unknown={sorted(unknown_nested)}, "
                    f"missing={sorted(missing_nested)}"
                )

        android_id = profile["android"]["application_id"]
        ios_id = profile["ios"]["bundle_id"]
        scheme = profile["ios"]["scheme"]
        url_scheme = profile["ios"]["url_scheme"]
        associated_domain = profile["ios"]["associated_domain"].lower().rstrip(".")
        if not isinstance(android_id, str) or not ANDROID_ID_RE.fullmatch(android_id):
            fail(f"{profile_id}.android.application_id is invalid")
        if not isinstance(ios_id, str) or not IOS_ID_RE.fullmatch(ios_id):
            fail(f"{profile_id}.ios.bundle_id is invalid")
        if not isinstance(scheme, str) or not SCHEME_RE.fullmatch(scheme):
            fail(f"{profile_id}.ios.scheme is invalid")
        if not isinstance(url_scheme, str) or not URL_SCHEME_RE.fullmatch(url_scheme):
            fail(f"{profile_id}.ios.url_scheme is invalid")
        if not isinstance(associated_domain, str) or associated_domain != urlsplit(profile["start_url"]).hostname:
            fail(f"{profile_id}.ios.associated_domain must match the start URL host")

        for value, registry, label in (
            (android_id.lower(), android_ids, "Android application id"),
            (ios_id.lower(), ios_ids, "iOS bundle id"),
            (scheme.lower(), schemes, "iOS scheme"),
        ):
            if value in registry:
                fail(f"Duplicate {label}: {profile_id} and {registry[value]}")
            registry[value] = profile_id

        profiles[profile_id] = profile

    return profiles


def atomic_write(path: Path, content: str | bytes) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    mode = "wb" if isinstance(content, bytes) else "w"
    kwargs = {} if isinstance(content, bytes) else {"encoding": "utf-8", "newline": "\n"}
    with tempfile.NamedTemporaryFile(mode=mode, dir=path.parent, delete=False, **kwargs) as handle:
        handle.write(content)
        temporary = Path(handle.name)
    os.replace(temporary, path)


def java_property(value: Any) -> str:
    text = str(value).replace("\\", "\\\\").replace("\n", "\\n")
    return text.replace("=", "\\=").replace(":", "\\:")


def xcconfig_url(value: str) -> str:
    return value.replace("://", ":$(SLASH)$(SLASH)")


def generate_android(profile: dict[str, Any]) -> None:
    profile_id = profile["id"]
    destination = GENERATED_DIR / "profiles" / profile_id
    values = {
        "profile.id": profile_id,
        "app.name": profile["display_name"],
        "app.subtitle": profile["subtitle"],
        "app.startUrl": profile["start_url"],
        "app.allowedOrigins": "|".join(profile["allowed_origins"]),
        "app.enableJavaScriptBridge": "false",
        "app.versionName": profile["version_name"],
        "app.versionCode": profile["version_code"],
        "android.applicationId": profile["android"]["application_id"],
        "android.deepLinkHost": urlsplit(profile["start_url"]).hostname or "",
    }
    content = "# Generated from profiles/%s.toml; do not edit.\n" % profile_id
    content += "".join(f"{key}={java_property(value)}\n" for key, value in sorted(values.items()))
    atomic_write(destination / "android.properties", content)
    icon = profile["icon_path"].read_bytes()
    atomic_write(destination / "android" / "res" / "mipmap-xxxhdpi" / "ic_launcher.png", icon)
    atomic_write(destination / "android" / "res" / "mipmap-xxxhdpi" / "ic_launcher_round.png", icon)


def generate_ios_config(profile: dict[str, Any]) -> None:
    profile_id = profile["id"]
    scheme = profile["ios"]["scheme"]
    generated_config = ROOT / "iosApp" / "Config" / "Generated"
    common = f"""// Generated from profiles/{profile_id}.toml; do not edit.
#include "../Base.xcconfig"

APP_NAME = {profile['display_name']}
APP_SUBTITLE = {profile['subtitle']}
APP_BUNDLE_ID = {profile['ios']['bundle_id']}
PRODUCT_BUNDLE_IDENTIFIER = $(APP_BUNDLE_ID)
APP_URL_SCHEME = {profile['ios']['url_scheme']}
APP_START_URL = {xcconfig_url(profile['start_url'])}
APP_ALLOWED_ORIGINS = {'|'.join(xcconfig_url(item) for item in profile['allowed_origins'])}
APP_ASSOCIATED_DOMAIN = applinks:{profile['ios']['associated_domain']}
ASSETCATALOG_COMPILER_APPICON_NAME = {scheme}AppIcon
MARKETING_VERSION = {profile['version_name']}
CURRENT_PROJECT_VERSION = {profile['version_code']}
"""
    atomic_write(generated_config / f"{profile_id}.xcconfig", common)
    for configuration in ("Debug", "Release"):
        pod_target = f"Pods-ProjectGeminiApps-{scheme}"
        pod_name = f"{pod_target}.{configuration.lower()}.xcconfig"
        content = (
            f"// Generated from profiles/{profile_id}.toml; do not edit.\n"
            f"#include? \"../../Pods/Target Support Files/{pod_target}/{pod_name}\"\n"
            f"#include \"{profile_id}.xcconfig\"\n"
        )
        atomic_write(generated_config / f"{profile_id}-{configuration}.xcconfig", content)
    icon = profile["icon_path"].read_bytes()
    app_icon = ROOT / "iosApp" / "iosApp" / "Assets.xcassets" / f"{scheme}AppIcon.appiconset"
    contents = {
        "images": [{
            "filename": f"{scheme}AppIcon.png",
            "idiom": "universal",
            "platform": "ios",
            "size": "1024x1024",
        }],
        "info": {"author": "xcode", "version": 1},
    }
    atomic_write(app_icon / "Contents.json", json.dumps(contents, indent=2) + "\n")
    atomic_write(app_icon / f"{scheme}AppIcon.png", icon)


def generate_xcode_project(profiles: dict[str, dict[str, Any]]) -> None:
    lines = [
        "# Generated from profiles/*.toml; do not edit.",
        "name: ProjectGeminiApps",
        "options:",
        '  xcodeVersion: "15.0"',
        "  deploymentTarget:",
        '    iOS: "14.0"',
        "configs:",
        "  Debug: debug",
        "  Release: release",
        "targetTemplates:",
        "  WebWrapperApp:",
        "    type: application",
        "    platform: iOS",
        '    deploymentTarget: "14.0"',
        "    sources:",
        "      - path: iosApp",
        "        excludes:",
        "          - Info.plist",
        "          - iosApp.entitlements",
        "    dependencies:",
        "      - sdk: WebKit.framework",
        "    settings:",
        "      base:",
        "        CODE_SIGN_ENTITLEMENTS: iosApp/iosApp.entitlements",
        "        GENERATE_INFOPLIST_FILE: NO",
        "        INFOPLIST_FILE: iosApp/Info.plist",
        '        PRODUCT_NAME: "$(APP_NAME)"',
        '        SWIFT_VERSION: "5.0"',
        '        TARGETED_DEVICE_FAMILY: "1,2"',
        "targets:",
    ]
    for profile in profiles.values():
        scheme = profile["ios"]["scheme"]
        profile_id = profile["id"]
        lines.extend([
            f"  {scheme}:",
            "    templates:",
            "      - WebWrapperApp",
            "    configFiles:",
            f"      Debug: Config/Generated/{profile_id}-Debug.xcconfig",
            f"      Release: Config/Generated/{profile_id}-Release.xcconfig",
        ])
    lines.append("schemes:")
    for profile in profiles.values():
        scheme = profile["ios"]["scheme"]
        lines.extend([
            f"  {scheme}:",
            "    build:",
            "      targets:",
            f"        {scheme}: all",
            "    run:",
            "      config: Debug",
            "    profile:",
            "      config: Release",
            "    analyze:",
            "      config: Debug",
            "    archive:",
            "      config: Release",
        ])
    atomic_write(ROOT / "iosApp" / "project.yml", "\n".join(lines) + "\n")

    podfile = [
        "# Generated from profiles/*.toml; do not edit.",
        "platform :ios, '14.0'",
        "project 'ProjectGeminiApps.xcodeproj'",
        "",
        "abstract_target 'ProjectGeminiApps' do",
        "  use_frameworks! :linkage => :static",
        "  pod 'shared', :path => '../shared'",
        "",
    ]
    podfile.extend(f"  target '{profile['ios']['scheme']}'" for profile in profiles.values())
    podfile.extend(["end", ""])
    atomic_write(ROOT / "iosApp" / "Podfile", "\n".join(podfile))
    if shutil.which("xcodegen"):
        run(["xcodegen", "generate", "--spec", "project.yml"], cwd=ROOT / "iosApp")
        project_file = ROOT / "iosApp" / "ProjectGeminiApps.xcodeproj" / "project.pbxproj"
        project_text = project_file.read_text(encoding="utf-8")
        # Xcode 26 defaults to object version 77, which CocoaPods 1.12/xcodeproj 1.22
        # cannot parse. This project uses no version-77-only objects, so emit the
        # Xcode 14 format supported by both toolchains.
        project_text = project_text.replace("objectVersion = 77;", "objectVersion = 56;")
        project_text = "\n".join(
            line for line in project_text.splitlines()
            if "preferredProjectObjectVersion =" not in line
            and "minimizedProjectReferenceProxies =" not in line
        ) + "\n"
        atomic_write(project_file, project_text)


def generate(profiles: dict[str, dict[str, Any]], selected: str, platform: str) -> None:
    GENERATED_DIR.mkdir(parents=True, exist_ok=True)
    lock_path = GENERATED_DIR / ".generate.lock"
    with lock_path.open("w") as lock:
        fcntl.flock(lock, fcntl.LOCK_EX)
        chosen = profiles.values() if selected == "all" else [profiles[selected]]
        if platform in ("android", "all"):
            for profile in chosen:
                generate_android(profile)
        if platform in ("ios", "all"):
            for profile in profiles.values():
                generate_ios_config(profile)
            generate_xcode_project(profiles)
        manifest = {
            "profiles": sorted(profiles),
            "schema_version": 1,
            "source_hash": hashlib.sha256(
                b"".join(path.read_bytes() for path in sorted(PROFILES_DIR.glob("*.toml")))
            ).hexdigest(),
        }
        atomic_write(GENERATED_DIR / "manifest.json", json.dumps(manifest, indent=2) + "\n")
    print(f"Generated {platform} configuration for {selected}")


def environment() -> dict[str, str]:
    env = os.environ.copy()
    sdk_candidates = [
        env.get("ANDROID_HOME"),
        env.get("ANDROID_SDK_ROOT"),
        str(Path.home() / "Library" / "Android" / "sdk"),
    ]
    sdk = next((item for item in sdk_candidates if item and Path(item).is_dir()), None)
    if sdk:
        env["ANDROID_HOME"] = sdk
        env["ANDROID_SDK_ROOT"] = sdk
        env["PATH"] = os.pathsep.join([
            str(Path(sdk) / "platform-tools"),
            str(Path(sdk) / "emulator"),
            env.get("PATH", ""),
        ])
    xcode = Path("/Applications/Xcode.app/Contents/Developer")
    if xcode.is_dir() and "DEVELOPER_DIR" not in env:
        env["DEVELOPER_DIR"] = str(xcode)
    return env


def resolve_android_artifact(build_root: Path, configuration: str, artifact: str) -> Path:
    artifact_directory = "apk" if artifact == "apk" else "bundle"
    variant_directory = build_root / artifact_directory / configuration
    candidates = sorted(variant_directory.glob(f"*.{artifact}"))
    if not candidates:
        fail(
            f"Gradle completed without a {configuration} {artifact} artifact in "
            f"{variant_directory}"
        )
    if len(candidates) != 1:
        fail(
            f"Gradle produced multiple {configuration} {artifact} artifacts in "
            f"{variant_directory}; split artifacts are not supported"
        )
    return candidates[0]


def resolve_ios_simulator_app(
    products_root: Path,
    configuration: str,
    display_name: str,
) -> Path:
    app = products_root / f"{configuration.capitalize()}-iphonesimulator" / f"{display_name}.app"
    if not app.is_dir():
        fail(f"Xcode completed without the requested simulator app at {app}")
    return app


def read_simple_properties(path: Path) -> dict[str, str]:
    values: dict[str, str] = {}
    for raw_line in path.read_text(encoding="utf-8").splitlines():
        line = raw_line.strip()
        if not line or line.startswith(("#", "!")):
            continue
        separator = "=" if "=" in line else ":" if ":" in line else None
        if separator is None:
            continue
        key, value = line.split(separator, 1)
        values[key.strip()] = value.strip()
    return values


def require_android_release_signing(root: Path = ROOT) -> Path:
    properties_path = root / "keystore.properties"
    if not properties_path.is_file():
        fail("Android release builds require an ignored keystore.properties file")
    values = read_simple_properties(properties_path)
    required = ("storeFile", "storePassword", "keyAlias", "keyPassword")
    missing = [key for key in required if not values.get(key)]
    if missing:
        fail(f"keystore.properties is missing required signing fields: {', '.join(missing)}")
    store_file = Path(values["storeFile"])
    if not store_file.is_absolute():
        store_file = root / store_file
    if not store_file.is_file():
        fail("keystore.properties storeFile does not resolve to a real signing key")
    print("Verified Android release signing configuration")
    return store_file


def build_android(profile: dict[str, Any], configuration: str, artifact: str) -> Path:
    profile_id = profile["id"]
    if configuration == "release":
        require_android_release_signing()
    task = "assemble" if artifact == "apk" else "bundle"
    task += configuration.capitalize()
    run(
        [str(ROOT / "gradlew"), "--no-parallel", f"-PappProfile={profile_id}", f":androidApp:{task}"],
        env=environment(),
    )
    extension = artifact
    build_root = ROOT / ".build" / "android" / profile_id / "androidApp" / "outputs"
    source = resolve_android_artifact(build_root, configuration, artifact)
    destination = DIST_DIR / profile_id / "android" / f"{profile_id}-{configuration}.{extension}"
    atomic_write(destination, source.read_bytes())
    inspect_android_artifact(destination, profile, configuration, artifact)
    return destination


def inspect_android_artifact(
    path: Path,
    profile: dict[str, Any],
    configuration: str,
    artifact: str,
) -> None:
    env = environment()
    if artifact == "apk":
        sdk = Path(env.get("ANDROID_HOME", ""))
        analyzers = sorted(sdk.glob("cmdline-tools/*/bin/apkanalyzer"))
        if not analyzers:
            fail("apkanalyzer is required to inspect packaged APK identity")
        packaged_id = output([
            str(analyzers[-1]),
            "manifest",
            "application-id",
            str(path),
        ], env=env)
        signers = sorted(sdk.glob("build-tools/*/apksigner"))
        if not signers:
            fail("apksigner is required to verify packaged APK signing")
        run([str(signers[-1]), "verify", "--verbose", str(path)], env=env)
    elif artifact == "aab":
        bundletool = shutil.which("bundletool", path=env.get("PATH"))
        if not bundletool:
            fail("bundletool is required to inspect packaged AAB identity")
        manifest = output([
            bundletool,
            "dump",
            "manifest",
            f"--bundle={path}",
            "--module=base",
        ], env=env)
        try:
            packaged_id = ElementTree.fromstring(manifest).attrib["package"]
        except (ElementTree.ParseError, KeyError) as error:
            fail(f"Unable to read application id from AAB manifest: {error}")
        jarsigner = shutil.which("jarsigner", path=env.get("PATH"))
        if not jarsigner:
            fail("jarsigner is required to verify packaged AAB signing")
        verification = output([jarsigner, "-verify", str(path)], env=env)
        if "jar verified." not in verification.lower():
            fail("jarsigner did not confirm a valid AAB signature")
    else:
        fail(f"Unsupported Android artifact inspection type: {artifact}")

    expected = profile["android"]["application_id"]
    if configuration == "debug":
        expected += ".debug"
    if packaged_id != expected:
        fail(f"Packaged Android id {packaged_id!r} does not match {expected!r}")
    print(f"Verified packaged Android application id: {packaged_id}")
    print(f"Verified packaged Android {artifact.upper()} signature")


def prepare_ios(profiles: dict[str, dict[str, Any]]) -> None:
    run([str(ROOT / "gradlew"), "--no-parallel", ":shared:generateDummyFramework"], env=environment())
    run(["pod", "install"], cwd=ROOT / "iosApp", env=environment())


def build_ios(profile: dict[str, Any], configuration: str, artifact: str) -> Path:
    if artifact == "ipa":
        fail(
            "Signed IPA export is intentionally unavailable without an Apple-signed archive "
            "and explicit export options"
        )
    prepare_ios({profile["id"]: profile})
    profile_id = profile["id"]
    scheme = profile["ios"]["scheme"]
    derived = ROOT / ".build" / "ios" / profile_id
    base = [
        "xcodebuild",
        "-workspace", str(ROOT / "iosApp" / "ProjectGeminiApps.xcworkspace"),
        "-scheme", scheme,
        "-configuration", configuration.capitalize(),
        "-derivedDataPath", str(derived),
    ]
    if artifact == "simulator-app":
        run(base + ["-sdk", "iphonesimulator", "CODE_SIGNING_ALLOWED=NO", "build"], env=environment())
        source = resolve_ios_simulator_app(
            derived / "Build" / "Products",
            configuration,
            profile["display_name"],
        )
        destination = DIST_DIR / profile_id / "ios" / f"{scheme}-simulator.app"
        if destination.exists():
            shutil.rmtree(destination)
        destination.parent.mkdir(parents=True, exist_ok=True)
        shutil.copytree(source, destination)
        inspect_ios_app(destination, profile)
        return destination
    if artifact == "xcarchive":
        destination = DIST_DIR / profile_id / "ios" / f"{scheme}.xcarchive"
        run(base + [
            "-destination", "generic/platform=iOS",
            "-archivePath", str(destination),
            "CODE_SIGNING_ALLOWED=NO",
            "archive",
        ], env=environment())
        return destination
    fail(f"Unsupported iOS artifact: {artifact}")


def inspect_ios_app(app: Path, profile: dict[str, Any]) -> None:
    info = plistlib.loads((app / "Info.plist").read_bytes())
    checks = {
        "CFBundleIdentifier": profile["ios"]["bundle_id"],
        "CFBundleDisplayName": profile["display_name"],
        "CFBundleShortVersionString": profile["version_name"],
        "CFBundleVersion": str(profile["version_code"]),
        "CADisableMinimumFrameDurationOnPhone": True,
        "AppStartURL": profile["start_url"],
        "AppAllowedOrigins": "|".join(profile["allowed_origins"]),
    }
    for key, expected in checks.items():
        if info.get(key) != expected:
            fail(f"Packaged iOS {key}={info.get(key)!r}, expected {expected!r}")
    print(f"Verified packaged iOS profile: {profile['id']}")


def select_device(platform: str, requested: str | None, env: dict[str, str]) -> str:
    if requested:
        if platform == "android":
            devices = output(["adb", "devices"], env=env).splitlines()[1:]
            ready = [line.split()[0] for line in devices if line.endswith("\tdevice")]
            if requested not in ready:
                fail(f"Android device {requested!r} is not ready")
        else:
            data = json.loads(output(["xcrun", "simctl", "list", "devices", "available", "--json"], env=env))
            matches = [item for runtime in data["devices"].values() for item in runtime if item["udid"] == requested]
            if not matches:
                fail(f"iOS simulator {requested!r} is not available")
            if matches[0]["state"] != "Booted":
                run(["xcrun", "simctl", "boot", requested], env=env)
                run(["xcrun", "simctl", "bootstatus", requested, "-b"], env=env)
        return requested
    if platform == "android":
        devices = output(["adb", "devices"], env=env).splitlines()[1:]
        ready = [line.split()[0] for line in devices if line.endswith("\tdevice")]
        if not ready:
            fail("No running Android emulator/device; pass --device after starting one")
        return ready[0]
    data = json.loads(output(["xcrun", "simctl", "list", "devices", "available", "--json"], env=env))
    booted = [item["udid"] for runtime in data["devices"].values() for item in runtime if item["state"] == "Booted"]
    if booted:
        return booted[0]
    available = [item["udid"] for runtime in data["devices"].values() for item in runtime]
    if not available:
        fail("No available iOS simulator")
    run(["xcrun", "simctl", "boot", available[0]], env=env)
    run(["xcrun", "simctl", "bootstatus", available[0], "-b"], env=env)
    return available[0]


def run_profile(
    profile: dict[str, Any],
    platform: str,
    device: str | None,
    screenshot: str | None,
    wait_seconds: float,
) -> None:
    env = environment()
    selected = select_device(platform, device, env)
    screenshot_path = Path(screenshot).resolve() if screenshot else None
    if screenshot_path:
        screenshot_path.parent.mkdir(parents=True, exist_ok=True)
    if platform == "android":
        artifact = build_android(profile, "debug", "apk")
        run(["adb", "-s", selected, "install", "-r", str(artifact)], env=env)
        app_id = profile["android"]["application_id"] + ".debug"
        run(["adb", "-s", selected, "shell", "am", "start", "-n", f"{app_id}/com.projectduoplay.android.MainActivity"], env=env)
        time.sleep(wait_seconds)
        if screenshot_path:
            with screenshot_path.open("wb") as handle:
                subprocess.run(["adb", "-s", selected, "exec-out", "screencap", "-p"], env=env, check=True, stdout=handle)
    else:
        app = build_ios(profile, "debug", "simulator-app")
        run(["xcrun", "simctl", "install", selected, str(app)], env=env)
        run(["xcrun", "simctl", "launch", selected, profile["ios"]["bundle_id"]], env=env)
        time.sleep(wait_seconds)
        if screenshot_path:
            run(["xcrun", "simctl", "io", selected, "screenshot", str(screenshot_path)], env=env)


def doctor(platform: str) -> int:
    env = environment()
    tools = {
        "common": ["python3", "java"],
        "android": ["adb"],
        "ios": ["xcodebuild", "xcodegen", "pod"],
    }
    requested = tools["common"] + ([] if platform == "common" else tools[platform])
    failed = False
    for tool in requested:
        location = shutil.which(tool, path=env.get("PATH"))
        print(f"{'OK' if location else 'MISSING'} {tool}: {location or '-'}")
        failed |= location is None
    if platform == "android":
        sdk = env.get("ANDROID_HOME")
        print(f"{'OK' if sdk else 'MISSING'} Android SDK: {sdk or '-'}")
        failed |= not sdk
    if platform == "ios" and env.get("DEVELOPER_DIR"):
        print(f"OK Xcode developer dir: {env['DEVELOPER_DIR']}")
    return 1 if failed else 0


def parser() -> argparse.ArgumentParser:
    result = argparse.ArgumentParser(prog="wrapper")
    subcommands = result.add_subparsers(dest="command", required=True)
    doctor_parser = subcommands.add_parser("doctor")
    doctor_parser.add_argument("--platform", choices=("common", "android", "ios"), required=True)
    subcommands.add_parser("list")
    validate_parser = subcommands.add_parser("validate")
    validate_parser.add_argument("--profile", required=True)
    generate_parser = subcommands.add_parser("generate")
    generate_parser.add_argument("--profile", required=True)
    generate_parser.add_argument("--platform", choices=("android", "ios", "all"), required=True)
    build_parser = subcommands.add_parser("build")
    build_parser.add_argument("--profile", required=True)
    build_parser.add_argument("--platform", choices=("android", "ios"), required=True)
    build_parser.add_argument("--configuration", choices=("debug", "release"), required=True)
    build_parser.add_argument("--artifact", choices=("apk", "aab", "simulator-app", "xcarchive", "ipa"), required=True)
    run_parser = subcommands.add_parser("run")
    run_parser.add_argument("--profile", required=True)
    run_parser.add_argument("--platform", choices=("android", "ios"), required=True)
    run_parser.add_argument("--device")
    run_parser.add_argument("--screenshot")
    run_parser.add_argument("--wait-seconds", type=float, default=8.0)
    return result


def selected_profile(profiles: dict[str, dict[str, Any]], selected: str, allow_all: bool = False) -> dict[str, Any]:
    if allow_all and selected == "all":
        return {}
    if selected not in profiles:
        fail(f"Unknown profile {selected!r}; available: {', '.join(profiles)}")
    return profiles[selected]


def main() -> int:
    args = parser().parse_args()
    if args.command == "doctor":
        return doctor(args.platform)
    profiles = load_profiles()
    if args.command == "list":
        for profile in profiles.values():
            print(f"{profile['id']}\t{profile['display_name']}\t{profile['start_url']}")
        return 0
    if args.command == "validate":
        selected_profile(profiles, args.profile, allow_all=True)
        print(f"Validated {len(profiles) if args.profile == 'all' else 1} profile(s)")
        return 0
    if args.command == "generate":
        selected_profile(profiles, args.profile, allow_all=True)
        generate(profiles, args.profile, args.platform)
        return 0
    profile = selected_profile(profiles, args.profile)
    if args.command == "build":
        if args.platform == "android" and args.artifact not in ("apk", "aab"):
            fail("Android artifacts are apk or aab")
        if args.platform == "ios" and args.artifact not in ("simulator-app", "xcarchive", "ipa"):
            fail("iOS artifacts are simulator-app, xcarchive, or ipa")
        generate(profiles, profile["id"], args.platform)
        artifact = (
            build_android(profile, args.configuration, args.artifact)
            if args.platform == "android"
            else build_ios(profile, args.configuration, args.artifact)
        )
        print(artifact)
        return 0
    if args.command == "run":
        if args.wait_seconds < 0 or args.wait_seconds > 120:
            fail("--wait-seconds must be between 0 and 120")
        generate(profiles, profile["id"], args.platform)
        run_profile(profile, args.platform, args.device, args.screenshot, args.wait_seconds)
        return 0
    return 2


if __name__ == "__main__":
    try:
        raise SystemExit(main())
    except ProfileError as error:
        print(f"ERROR: {error}", file=sys.stderr)
        raise SystemExit(2)
