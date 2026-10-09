# Security Policy

Phase 1 is a library foundation, not a production-ready signing service. Mock tests do not establish real-hardware correctness.

- Private key material must never be exported from hardware.
- Key selection must be explicit and bound to an expected X.509 certificate SHA-256 fingerprint.
- Ambiguous identity, missing key, token mismatch, PIN failure, or backend errors must fail closed; no fallback to another key or software signer.
- PINs must not be logged, persisted, placed in command-line arguments, or committed.
- Clear owned PIN buffers promptly. JVM zeroization cannot guarantee erasure of every copy.
- Do not install or reorder global JCA providers implicitly.
- Do not include production certificates, object IDs, token labels, host paths, runner usernames, or production workflow policy as library defaults.
- Real hardware tests must be opt-in and run only in an explicitly authorized isolated environment.

Do not publish exploit details or sensitive token output in public issues. Report security issues privately to the repository maintainer.
