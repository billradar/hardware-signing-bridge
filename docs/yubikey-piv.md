# YubiKey PIV adapter (Phase 1)

The initial adapter uses the JDK SunPKCS11 provider and a caller-supplied PKCS#11 module path, such as an OpenSC PKCS#11 library. No vendor-specific dependency or hard-coded machine path is included.

## Configuration

Construct SunPkcs11PivBackend with:

- absolute path to the PKCS#11 shared library;
- PKCS#11 slot-list index chosen by the operator;
- a PinSource that returns a fresh caller-owned char array.

The SigningKeyReference objectId is interpreted as the exact PKCS#11 KeyStore alias exposed by the configured provider. It is not assumed to be universally equal to a raw PIV object ID. There is no alias search or fallback. The alias certificate must match the expected SHA-256 fingerprint and the supplied X.509 certificate.

The adapter writes a temporary PKCS#11 configuration containing only the module path and slot-list index, attempts owner-only POSIX permissions, and deletes the file after provider configuration. It never writes the PIN to the configuration file.

## Safety limits

- This adapter has not been tested with a physical YubiKey in Phase 1.
- Do not use production PINs, keys, or production signing identities for initial validation.
- The JDK PKCS#11 provider may manage token sessions and login state internally; this adapter does not claim deterministic token logout or session destruction. Review this limitation before any production use.
- Confirm the PKCS#11 provider's ECDSA signature output matches the JCA DER signature contract on the target stack.
- Backend errors fail closed; the adapter never falls back to a software provider.
- Hardware integration tests must be separately gated and must not run in ordinary CI.
