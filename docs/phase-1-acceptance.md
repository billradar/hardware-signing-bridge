# Phase 1 Acceptance Checklist

- [ ] Multi-module Maven build succeeds on JDK 21.
- [ ] API module contains no RustDesk, APK, CA issuance, or production identity policy.
- [ ] JCA provider only advertises explicitly supported ECDSA algorithms.
- [ ] Signing session closes after success and every failure path.
- [ ] Certificate fingerprint mismatch is rejected before backend use.
- [ ] Hardware private key exposes no encoded private key bytes.
- [ ] PIN buffers are cleared deterministically when owned by the helper.
- [ ] Mock tests cover initialization, updates, signing, and backend failures.
- [ ] Dependency and secret scans pass.
- [ ] Hardware adapter tests are separately gated and not required by ordinary CI.
- [ ] No claim of real-hardware validation is made until an explicitly authorized hardware test succeeds.
