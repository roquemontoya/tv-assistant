import json
import tempfile
import unittest
from pathlib import Path
from unittest.mock import patch

from scripts import audit_dependencies


class DependencyAuditTests(unittest.TestCase):
    def test_parses_maven_central_coordinate(self):
        self.assertEqual(
            audit_dependencies.maven_coordinates(
                "https://repo.maven.apache.org/maven2/org/json/json/20240303/json-20240303.jar"
            ),
            ("org.json:json", "20240303"),
        )

    def test_rejects_non_maven_url_without_explicit_metadata(self):
        self.assertIsNone(audit_dependencies.maven_coordinates(
            "https://example.org/releases/library-1.2.jar"
        ))

    def test_loads_both_repository_manifests(self):
        dependencies = audit_dependencies.load_dependencies([
            "dependencies.json", "host-dependencies.json"
        ])
        self.assertEqual(
            [(item["name"], item["version"]) for item in dependencies],
            [("org.json:json", "20240303")],
        )

    def test_rejects_missing_checksum_pin(self):
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            (root / "manifest.json").write_text(json.dumps([{
                "file": "library.jar",
                "url": "https://repo.maven.apache.org/maven2/example/library/1.0/library-1.0.jar",
                "sha256": "not-a-checksum",
            }]), encoding="utf-8")
            with patch.object(audit_dependencies, "ROOT", root):
                with self.assertRaises(SystemExit) as error:
                    audit_dependencies.load_dependencies(["manifest.json"])
                self.assertEqual(error.exception.code, 2)


if __name__ == "__main__":
    unittest.main()
