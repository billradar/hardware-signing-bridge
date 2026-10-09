# Hardware Signing Bridge

A hardware-agnostic Java/JCA signing bridge with an initial YubiKey PIV / PKCS#11 integration target.

> **Status:** Phase 1 — core library foundation. Not production-ready; no real hardware signing has been validated by this repository yet.

## Scope

- Reusable Java API and JCA integration, independent of RustDesk and APK packaging.
- Hardware-backed, non-exportable private-key references.
- Explicit signing identity and algorithm selection; fail closed on ambiguity or errors.
- Pure-software tests with mock signers.
- YubiKey PIV support through a configurable PKCS#11 module is the first hardware adapter target.

## Out of scope

- CA creation, certificate issuance, or certificate lifecycle management.
- RustDesk release workflows, APK policy, production certificate fingerprints, and production signing secrets.
- Automatic PIN persistence, silent fallback, or exporting private key material.
- Native-image packaging and production CLI in Phase 1.

## Build

Requires JDK 21 and Maven 3.9+.

```sh
mvn -B verify
```

Phase 1 currently establishes the API/JCA library and mock-backed test baseline. Hardware tests are opt-in and must never be required by ordinary CI.

## Security posture

Treat this project as a security-sensitive library. Do not commit PINs, private keys, token dumps, production identity values, or local PKCS#11 configuration. Real-hardware validation is separate from mock tests and requires an explicitly authorized test environment.

See [SECURITY.md](SECURITY.md), [docs/architecture.md](docs/architecture.md), and [docs/threat-model.md](docs/threat-model.md).
