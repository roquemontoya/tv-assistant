#!/usr/bin/env python3
"""Audit checksum-pinned Maven dependencies against the OSV vulnerability database.

Both APK runtime and host/test manifests are checked. This deliberately fails closed
when a dependency cannot be mapped to a Maven coordinate: add explicit ecosystem/name
metadata rather than silently skipping a dependency.
"""
from __future__ import annotations

import argparse
import json
import re
import sys
from pathlib import Path
from urllib.parse import urlparse
from urllib.request import Request, urlopen

ROOT = Path(__file__).resolve().parents[1]
OSV_BATCH_URL = "https://api.osv.dev/v1/querybatch"
SHA256_RE = re.compile(r"^[0-9a-fA-F]{64}$")


def maven_coordinates(url: str) -> tuple[str, str] | None:
    parsed = urlparse(url)
    parts = [part for part in parsed.path.split("/") if part]
    try:
        marker = parts.index("maven2")
    except ValueError:
        return None
    tail = parts[marker + 1:]
    if len(tail) < 4:
        return None
    group_parts, artifact, version = tail[:-3], tail[-3], tail[-2]
    if not group_parts or not artifact or not version:
        return None
    return ".".join(group_parts) + ":" + artifact, version


def load_dependencies(manifest_paths: list[str]) -> list[dict]:
    dependencies = []
    errors = []
    for manifest in manifest_paths:
        path = (ROOT / manifest).resolve()
        if ROOT not in path.parents:
            errors.append(f"Manifest path escapes repository: {manifest}")
            continue
        try:
            items = json.loads(path.read_text(encoding="utf-8"))
        except Exception as exc:
            errors.append(f"{manifest}: cannot parse JSON: {exc}")
            continue
        if not isinstance(items, list):
            errors.append(f"{manifest}: expected a JSON array")
            continue
        for index, item in enumerate(items):
            label = f"{manifest}[{index}]"
            if not isinstance(item, dict):
                errors.append(f"{label}: expected an object")
                continue
            filename, url, checksum = item.get("file"), item.get("url"), item.get("sha256")
            if not isinstance(filename, str) or not filename or Path(filename).name != filename:
                errors.append(f"{label}: invalid file name")
                continue
            if not isinstance(url, str) or urlparse(url).scheme != "https":
                errors.append(f"{label}: dependency URL must use HTTPS")
                continue
            if not isinstance(checksum, str) or not SHA256_RE.fullmatch(checksum):
                errors.append(f"{label}: missing or invalid SHA-256 pin")
                continue
            ecosystem = item.get("ecosystem")
            name = item.get("name")
            version = item.get("version")
            if ecosystem == "Maven" and isinstance(name, str) and isinstance(version, str):
                coordinate = (name, version)
            else:
                coordinate = maven_coordinates(url)
            if coordinate is None:
                errors.append(
                    f"{label}: cannot derive Maven coordinates; add ecosystem='Maven', "
                    "name='group:artifact', and version metadata"
                )
                continue
            dependencies.append({
                "manifest": manifest,
                "file": filename,
                "name": coordinate[0],
                "version": coordinate[1],
            })
    if errors:
        for error in errors:
            print(f"ERROR: {error}", file=sys.stderr)
        raise SystemExit(2)
    return dependencies


def query_osv(dependencies: list[dict]) -> list[dict]:
    if not dependencies:
        return []
    payload = {
        "queries": [
            {"package": {"ecosystem": "Maven", "name": dep["name"]}, "version": dep["version"]}
            for dep in dependencies
        ]
    }
    request = Request(
        OSV_BATCH_URL,
        data=json.dumps(payload).encode("utf-8"),
        headers={"Content-Type": "application/json", "Accept": "application/json"},
        method="POST",
    )
    with urlopen(request, timeout=30) as response:
        result = json.loads(response.read().decode("utf-8"))
    return result.get("results", [])


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument(
        "--manifest", action="append",
        default=None,
        help="Dependency manifest to audit (may be repeated)",
    )
    args = parser.parse_args()
    manifests = args.manifest or ["dependencies.json", "host-dependencies.json"]
    dependencies = load_dependencies(manifests)
    print(f"Validated {len(dependencies)} pinned dependency entries.")
    if not dependencies:
        print("No third-party Maven dependencies are currently declared.")
        return 0

    results = query_osv(dependencies)
    if len(results) != len(dependencies):
        print("ERROR: OSV returned an unexpected result count.", file=sys.stderr)
        return 2

    findings = 0
    for dep, result in zip(dependencies, results):
        vulnerabilities = result.get("vulns") or []
        if not vulnerabilities:
            print(f"OK: {dep['name']}:{dep['version']} ({dep['manifest']})")
            continue
        for vulnerability in vulnerabilities:
            findings += 1
            print(
                f"VULNERABLE: {dep['name']}:{dep['version']} "
                f"({dep['manifest']}) — {vulnerability.get('id', 'unknown ID')}"
            )
            summary = vulnerability.get("summary")
            if summary:
                print(f"  {summary}")
            for reference in vulnerability.get("references", [])[:5]:
                if reference.get("url"):
                    print(f"  {reference['url']}")
    if findings:
        print(f"FAIL: found {findings} vulnerability record(s).")
        return 1
    print("PASS: no known OSV vulnerabilities in the declared dependency versions.")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
