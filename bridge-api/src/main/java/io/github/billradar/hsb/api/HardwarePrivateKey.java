package io.github.billradar.hsb.api;
import java.security.PrivateKey;
import java.util.Objects;
/** Opaque reference to a hardware-held private key. Private key bytes are never exposed. */
public final class HardwarePrivateKey implements PrivateKey {
 private static final long serialVersionUID = 1L;
 private final SigningKeyReference reference;
 public HardwarePrivateKey(SigningKeyReference reference) {
  this.reference = Objects.requireNonNull(reference, "reference");
  if (!"EC".equalsIgnoreCase(reference.certificate().getPublicKey().getAlgorithm()))
   throw new IllegalArgumentException("Only EC certificates are supported in Phase 1");
 }
 public SigningKeyReference reference() { return reference; }
 @Override public String getAlgorithm() { return "EC"; }
 @Override public String getFormat() { return null; }
 @Override public byte[] getEncoded() { return null; }
}
