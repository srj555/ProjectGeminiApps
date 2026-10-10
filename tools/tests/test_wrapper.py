from __future__ import annotations

import hashlib
import importlib.util
from pathlib import Path
import subprocess
import tempfile
import unittest
from unittest import mock


ROOT = Path(__file__).resolve().parents[2]
SPEC = importlib.util.spec_from_file_location("wrapper_cli", ROOT / "tools" / "wrapper.py")
assert SPEC and SPEC.loader
wrapper_cli = importlib.util.module_from_spec(SPEC)
SPEC.loader.exec_module(wrapper_cli)


def digest_tree(path: Path) -> str:
    digest = hashlib.sha256()
    for file in sorted(item for item in path.rglob("*") if item.is_file()):
        digest.update(file.relative_to(path).as_posix().encode())
        digest.update(file.read_bytes())
    return digest.hexdigest()


def digest_paths(paths: list[Path]) -> str:
    digest = hashlib.sha256()
    for path in paths:
        if path.is_file():
            digest.update(path.relative_to(ROOT).as_posix().encode())
            digest.update(path.read_bytes())
        elif path.is_dir():
            digest.update(path.relative_to(ROOT).as_posix().encode())
            digest.update(digest_tree(path).encode())
    return digest.hexdigest()


class ProfileValidationTest(unittest.TestCase):
    def test_repository_profiles_are_valid(self) -> None:
        self.assertEqual({"duoplay", "vman"}, set(wrapper_cli.load_profiles()))

    def test_origins_are_canonical_and_exact(self) -> None:
        self.assertEqual(
            "https://example.com",
            wrapper_cli.canonical_origin("https://EXAMPLE.com:443", "origin"),
        )
        self.assertEqual(
            "https://example.com:8443",
            wrapper_cli.canonical_origin("https://example.com:8443", "origin"),
        )
        for malicious in (
            "http://example.com",
            "https://user@example.com",
            "https://example.com.attacker.invalid/path",
            "https://example.com/path",
        ):
            with self.subTest(malicious=malicious):
                with self.assertRaises(wrapper_cli.ProfileError):
                    wrapper_cli.canonical_origin(malicious, "origin")

    def test_unknown_fields_and_unsupported_capabilities_fail_closed(self) -> None:
        original = (ROOT / "profiles" / "duoplay.toml").read_text()
        with tempfile.TemporaryDirectory() as directory:
            profiles = Path(directory)
            (profiles / "icons").mkdir()
            (profiles / "icons" / "duoplay.png").write_bytes(
                (ROOT / "profiles" / "icons" / "duoplay.png").read_bytes()
            )
            with mock.patch.object(wrapper_cli, "PROFILES_DIR", profiles):
                (profiles / "duoplay.toml").write_text(original + "\nunknown = true\n")
                with self.assertRaisesRegex(wrapper_cli.ProfileError, "unknown"):
                    wrapper_cli.load_profiles()

                (profiles / "duoplay.toml").write_text(
                    original.replace(
                        '"persistent_session"]',
                        '"persistent_session", "file_upload"]',
                    )
                )
                with self.assertRaisesRegex(wrapper_cli.ProfileError, "unsupported capabilities"):
                    wrapper_cli.load_profiles()

    def test_icon_path_traversal_fails(self) -> None:
        original = (ROOT / "profiles" / "duoplay.toml").read_text()
        with tempfile.TemporaryDirectory() as directory:
            profiles = Path(directory)
            (profiles / "duoplay.toml").write_text(
                original.replace('icon = "icons/duoplay.png"', 'icon = "../duoplay.png"')
            )
            with mock.patch.object(wrapper_cli, "PROFILES_DIR", profiles):
                with self.assertRaisesRegex(wrapper_cli.ProfileError, "without traversal"):
                    wrapper_cli.load_profiles()


class GeneratorIntegrationTest(unittest.TestCase):
    def test_a_b_a_and_concurrent_generation_are_deterministic(self) -> None:
        command = [str(ROOT / "wrapper"), "generate", "--platform", "android", "--profile"]
        subprocess.run(command + ["duoplay"], cwd=ROOT, check=True, capture_output=True)
        first = digest_tree(ROOT / "config" / "generated" / "profiles" / "duoplay")
        subprocess.run(command + ["vman"], cwd=ROOT, check=True, capture_output=True)
        subprocess.run(command + ["duoplay"], cwd=ROOT, check=True, capture_output=True)
        self.assertEqual(first, digest_tree(ROOT / "config" / "generated" / "profiles" / "duoplay"))

        duo = subprocess.Popen(command + ["duoplay"], cwd=ROOT, stdout=subprocess.PIPE, stderr=subprocess.PIPE)
        vman = subprocess.Popen(command + ["vman"], cwd=ROOT, stdout=subprocess.PIPE, stderr=subprocess.PIPE)
        duo.communicate()
        vman.communicate()
        self.assertEqual(0, duo.returncode)
        self.assertEqual(0, vman.returncode)

    def test_cli_works_outside_repo_and_does_not_invoke_shell(self) -> None:
        with tempfile.TemporaryDirectory(prefix="Project Gemini ") as directory:
            result = subprocess.run(
                [str(ROOT / "wrapper"), "list"],
                cwd=directory,
                text=True,
                capture_output=True,
                check=True,
            )
            self.assertIn("duoplay", result.stdout)
            marker = Path(directory) / "should-not-exist"
            malicious = f"duoplay;touch {marker}"
            rejected = subprocess.run(
                [str(ROOT / "wrapper"), "validate", "--profile", malicious],
                cwd=directory,
                text=True,
                capture_output=True,
            )
            self.assertNotEqual(0, rejected.returncode)
            self.assertFalse(marker.exists())

    def test_ios_generation_is_deterministic_and_concurrency_safe(self) -> None:
        command = [str(ROOT / "wrapper"), "generate", "--platform", "ios", "--profile"]
        generated_paths = [
            ROOT / "iosApp" / "Config" / "Generated",
            ROOT / "iosApp" / "project.yml",
            ROOT / "iosApp" / "Podfile",
            ROOT / "iosApp" / "ProjectGeminiApps.xcodeproj",
            ROOT / "iosApp" / "iosApp" / "Assets.xcassets" / "DuoPlayAppIcon.appiconset",
            ROOT / "iosApp" / "iosApp" / "Assets.xcassets" / "VManAppIcon.appiconset",
        ]
        subprocess.run(command + ["duoplay"], cwd=ROOT, check=True, capture_output=True)
        first = digest_paths(generated_paths)
        subprocess.run(command + ["vman"], cwd=ROOT, check=True, capture_output=True)
        subprocess.run(command + ["duoplay"], cwd=ROOT, check=True, capture_output=True)
        self.assertEqual(first, digest_paths(generated_paths))

        duo = subprocess.Popen(command + ["duoplay"], cwd=ROOT, stdout=subprocess.PIPE, stderr=subprocess.PIPE)
        vman = subprocess.Popen(command + ["vman"], cwd=ROOT, stdout=subprocess.PIPE, stderr=subprocess.PIPE)
        duo.communicate()
        vman.communicate()
        self.assertEqual(0, duo.returncode)
        self.assertEqual(0, vman.returncode)
        self.assertEqual(first, digest_paths(generated_paths))

    def test_unicode_branding_is_emitted_as_utf8(self) -> None:
        original = (ROOT / "profiles" / "duoplay.toml").read_text(encoding="utf-8")
        with tempfile.TemporaryDirectory() as directory:
            temporary = Path(directory)
            profiles = temporary / "profiles"
            generated = temporary / "generated"
            (profiles / "icons").mkdir(parents=True)
            (profiles / "icons" / "duoplay.png").write_bytes(
                (ROOT / "profiles" / "icons" / "duoplay.png").read_bytes()
            )
            (profiles / "duoplay.toml").write_text(
                original.replace('display_name = "DuoPlay"', 'display_name = "Café 東京"'),
                encoding="utf-8",
            )
            with (
                mock.patch.object(wrapper_cli, "PROFILES_DIR", profiles),
                mock.patch.object(wrapper_cli, "GENERATED_DIR", generated),
            ):
                profile = wrapper_cli.load_profiles()["duoplay"]
                wrapper_cli.generate_android(profile)

            properties = generated / "profiles" / "duoplay" / "android.properties"
            self.assertIn("app.name=Café 東京", properties.read_text(encoding="utf-8"))
            self.assertIn("Café 東京".encode(), properties.read_bytes())


class ReleaseSigningTest(unittest.TestCase):
    def test_android_release_signing_fails_closed(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            with self.assertRaisesRegex(wrapper_cli.ProfileError, "require.*keystore.properties"):
                wrapper_cli.require_android_release_signing(root)

            (root / "keystore.properties").write_text(
                "storeFile=release.jks\nstorePassword=secret\nkeyAlias=release\n",
                encoding="utf-8",
            )
            with self.assertRaisesRegex(wrapper_cli.ProfileError, "keyPassword"):
                wrapper_cli.require_android_release_signing(root)

            (root / "release.jks").write_bytes(b"test fixture")
            (root / "keystore.properties").write_text(
                "storeFile=release.jks\n"
                "storePassword=secret\n"
                "keyAlias=release\n"
                "keyPassword=secret\n",
                encoding="utf-8",
            )
            self.assertEqual(root / "release.jks", wrapper_cli.require_android_release_signing(root))

    def test_aab_inspection_uses_bundletool_and_verifies_signature(self) -> None:
        profile = wrapper_cli.load_profiles()["duoplay"]

        def executable(name: str, path: str | None = None) -> str | None:
            return {
                "bundletool": "/tools/bundletool",
                "jarsigner": "/tools/jarsigner",
            }.get(name)

        def command_output(command: list[str], **_: object) -> str:
            if command[0] == "/tools/bundletool":
                return '<manifest package="com.duoplay.app.debug" />'
            if command[0] == "/tools/jarsigner":
                return "jar verified."
            raise AssertionError(f"Unexpected command: {command}")

        with (
            tempfile.TemporaryDirectory() as directory,
            mock.patch.object(wrapper_cli, "environment", return_value={"PATH": "/tools"}),
            mock.patch.object(wrapper_cli.shutil, "which", side_effect=executable),
            mock.patch.object(wrapper_cli, "output", side_effect=command_output) as inspected,
        ):
            artifact = Path(directory) / "duoplay-debug.aab"
            artifact.write_bytes(b"fixture")
            wrapper_cli.inspect_android_artifact(artifact, profile, "debug", "aab")

        self.assertEqual(2, inspected.call_count)


class ArtifactSelectionTest(unittest.TestCase):
    def test_android_artifact_selection_ignores_opposite_configuration(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            outputs = Path(directory)
            for artifact, artifact_directory in (("apk", "apk"), ("aab", "bundle")):
                for configuration in ("debug", "release"):
                    path = (
                        outputs
                        / artifact_directory
                        / configuration
                        / f"androidApp-{configuration}.{artifact}"
                    )
                    path.parent.mkdir(parents=True, exist_ok=True)
                    path.write_text(configuration)

                for configuration in ("debug", "release"):
                    with self.subTest(artifact=artifact, configuration=configuration):
                        selected = wrapper_cli.resolve_android_artifact(
                            outputs,
                            configuration,
                            artifact,
                        )
                        self.assertEqual(configuration, selected.read_text())

    def test_ios_artifact_selection_ignores_opposite_configuration(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            products = Path(directory)
            for configuration in ("Debug", "Release"):
                app = products / f"{configuration}-iphonesimulator" / "DuoPlay.app"
                app.mkdir(parents=True)
                (app / "configuration.txt").write_text(configuration.lower())

            for configuration in ("debug", "release"):
                with self.subTest(configuration=configuration):
                    selected = wrapper_cli.resolve_ios_simulator_app(
                        products,
                        configuration,
                        "DuoPlay",
                    )
                    self.assertEqual(
                        configuration,
                        (selected / "configuration.txt").read_text(),
                    )


if __name__ == "__main__":
    unittest.main()
