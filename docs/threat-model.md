# Threat Model

## Assets
- Integrity and authorization of signing operations.
- Hardware-resident private keys.
- PIN material and token authentication state.
- Correct binding between a certificate identity and a hardware private-key object.

## Trust boundaries
- Application configuration to library API.
- JCA SPI to injected backend.
- Backend to OS PKCS#11 module and hardware token.
- PIN source to native token authentication.

## Threats and controls
- Wrong-key selection: require explicit object ID and expected certificate fingerprint; adapter must verify the actual selected object and fail closed.
- Private-key extraction: expose only an opaque PrivateKey reference; getEncoded() and getFormat() return null.
- PIN leakage: no logging or persistence in API; use owned char[] buffers and clear them; avoid command-line arguments and Java String conversions.
- Silent software fallback: prohibited; backend errors surface as signing errors.
- Provider side effects: provider construction does not mutate global provider order.
- Resource leakage: signing sessions close on completion and failure.
- Overclaiming tests: mock tests are not hardware validation.

## Out of scope
CA issuance, certificate enrollment, release orchestration, APK verification, and production runner deployment.
