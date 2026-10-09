# Architecture

Phase 1 separates responsibilities:

- bridge-api: backend contract, signing-session lifecycle, opaque private-key reference, explicit certificate fingerprint binding, and PIN buffer helper.
- bridge-jca: a JCA Provider exposing a deliberately small set of ECDSA algorithms and delegating operations to an injected SigningBackend.
- Future hardware adapter: YubiKey PIV through a configurable PKCS#11 module. Discovery, token selection, PIN authentication, and native resource handling belong in the adapter, not the API.

The application constructs a SigningKeyReference with an explicit object identifier, X.509 certificate, and expected SHA-256 certificate fingerprint. The constructor rejects a fingerprint mismatch. The backend must independently ensure that the selected private-key object corresponds to that certificate and must not fall back to another object.

The provider is instantiated with an explicit backend. It does not register itself in JVM-global provider order.

## Operation lifecycle

1. Caller constructs a certificate-bound HardwarePrivateKey.
2. JCA initializes signing; the SPI asks the backend to begin a session for the exact key reference and algorithm.
3. Updates are streamed to the session; the bridge does not accumulate the full message.
4. sign() returns the backend signature. The adapter contract must document encoding; ECDSA PKCS#11 raw-to-DER conversion belongs at the adapter boundary.
5. Session closes on success, failure, reinitialization, or update error.

## Phase 1 limits

The API and JCA adapter precede the PKCS#11/YubiKey adapter. Until that adapter and its tests are added, this project cannot sign with real hardware. Fingerprint binding alone does not prove that a hardware private key matches the certificate public key; the adapter must validate object/certificate correspondence before signing.
