# Hardware Signing Bridge

A hardware-agnostic Java/JCA signing bridge with an initial YubiKey PIV / PKCS#11 integration target.

> **Status:** Phase 1 implementation in draft review. The JDK-based PKCS#11 adapter is experimental and has not been validated against physical hardware.

## Modules

- **bridge-api** — backend contract, signing-session lifecycle, opaque hardware private-key reference, certificate fingerprint binding, and PIN-buffer helper.
- **bridge-jca** — JCA Provider exposing SHA256withECDSA and SHA384withECDSA through an explicitly injected backend.
- **adapter-yubikey-piv** — experimental adapter using JDK SunPKCS11 with a caller-configured PKCS#11 module.

## Build

Requires JDK 21 and Maven 3.9+.

```sh
mvn --batch-mode --no-transfer-progress verify
```

## Scope boundaries

- No CA creation, certificate issuance, or certificate lifecycle management.
- No RustDesk release workflows, APK policy, production certificate fingerprints, or production signing secrets.
- No automatic PIN persistence, silent fallback, or private-key export.
- No CLI or native-image packaging in Phase 1.

See SECURITY.md, docs/architecture.md, docs/threat-model.md, and docs/yubikey-piv.md.
