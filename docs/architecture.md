# Architecture

Phase 1 separates responsibilities:

- bridge-api: backend contract, signing-session lifecycle, opaque private-key reference, explicit certificate fingerprint binding, and PIN buffer helper.
- bridge-jca: a JCA Provider exposing a deliberately small set of ECDSA algorithms and delegating operations to an injected SigningBackend.
- adapter-yubikey-piv: an experimental JDK SunPKCS11 adapter for a caller-configured PKCS#11 module. It is not yet validated against physical hardware.

The application constructs a SigningKeyReference with an explicit PKCS#11 KeyStore alias, X.509 certificate, and expected SHA-256 certificate fingerprint. The constructor rejects a fingerprint mismatch. The adapter checks the token certificate for the exact alias and verifies its fingerprint before obtaining the associated private key. There is no alias search or fallback.

The provider is instantiated with an explicit backend. It does not register itself in JVM-global provider order. The PKCS#11 adapter selects its provider explicitly for signature operations, preventing software-provider fallback.

## Operation lifecycle

1. Caller constructs a certificate-bound HardwarePrivateKey.
2. JCA initializes signing; the SPI asks the backend to begin a session for the exact key reference and algorithm.
3. The PKCS#11 backend acquires an owned PIN buffer, loads the token KeyStore, verifies the certificate fingerprint, obtains the exact alias key, initializes a signature using the configured PKCS#11 provider, then clears the PIN buffer.
4. Updates are streamed to the session; the bridge does not accumulate the full message.
5. sign() returns the backend signature. The adapter contract must document encoding; verify the target provider's ECDSA output format before production use.
6. Session references are cleared on success, failure, reinitialization, or update error.

## Phase 1 limits

The adapter is experimental. JDK SunPKCS11 manages token sessions and login state internally, and this adapter does not guarantee deterministic token logout or native session destruction. Do not use it for production signing until session lifecycle, token identity, signature encoding, and real-hardware behavior have been reviewed and explicitly validated. Certificate fingerprint binding alone does not prove a hardware private key mathematically matches the certificate public key; the PKCS#11 alias association must be trusted and validated.
