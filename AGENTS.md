# Agent Instructions

## Purpose
Build a reusable Java/JCA hardware signing bridge. First hardware target is YubiKey PIV through PKCS#11; core APIs must remain hardware-agnostic.

## Hard boundaries
- Do not add CA creation or certificate issuance.
- Do not add RustDesk package IDs, APK release logic, production fingerprints, production object IDs, runner paths, or workflow policy.
- Do not read, request, or use production PINs, private keys, or physical YubiKeys unless the user explicitly authorizes a later hardware-validation task.
- Never silently fall back to software signing or another hardware object.
- Never log PINs or persist them to files.
- Do not install or reorder global JCA providers implicitly.
- Do not merge or publish releases without explicit user approval.

## Engineering requirements
- JDK 21, Maven, deterministic unit tests.
- Separate API/JCA integration from hardware adapters.
- Fail closed on identity mismatch and backend errors.
- Close signing sessions on every exit path.
- Test malformed inputs, identity mismatch, cleanup, and failure behavior.
- Label mock-only validation clearly; never claim it validates physical hardware.
- Run mvn -B verify where an execution environment is available and report actual results only.
