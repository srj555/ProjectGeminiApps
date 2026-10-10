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


if __name__ == "__main__":
    unittest.main()
