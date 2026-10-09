package io.github.billradar.hsb.jca;
import io.github.billradar.hsb.api.*;
import java.security.*;
import java.security.spec.AlgorithmParameterSpec;
import java.util.Objects;
/** Streaming JCA adapter over a backend-owned signing session. */
public final class HardwareEcdsaSignatureSpi extends SignatureSpi {
 private final SigningBackend backend;
 private final String algorithm;
 private SigningSession session;
 private boolean initialized;
 public HardwareEcdsaSignatureSpi(SigningBackend backend, String algorithm) {
  this.backend = Objects.requireNonNull(backend, "backend"); this.algorithm = Objects.requireNonNull(algorithm, "algorithm");
 }
 @Override protected void engineInitVerify(PublicKey key) throws InvalidKeyException {
  closeSession(); initialized = false; throw new InvalidKeyException("This provider supports hardware signing only");
 }
 @Override protected void engineInitSign(PrivateKey key) throws InvalidKeyException { engineInitSign(key, null); }
 @Override protected void engineInitSign(PrivateKey key, SecureRandom random) throws InvalidKeyException {
  closeSession(); initialized = false;
  if (!(key instanceof HardwarePrivateKey hardwareKey)) throw new InvalidKeyException("Expected a HardwarePrivateKey reference");
  try {
   session = backend.begin(hardwareKey.reference(), algorithm);
   if (session == null) throw new SigningException("Backend returned no signing session");
   initialized = true;
  } catch (SigningException e) { closeSession(); throw new InvalidKeyException("Unable to initialize hardware signing session", e); }
 }
 @Override protected void engineUpdate(byte b) throws SignatureException {
  byte[] one = {b};
  try { engineUpdate(one, 0, 1); } finally { one[0] = 0; }
 }
 @Override protected void engineUpdate(byte[] input, int off, int len) throws SignatureException {
  if (!initialized || session == null) throw new SignatureException("Signature is not initialized for signing");
  if (input == null) throw new SignatureException("Input must not be null");
  if (off < 0 || len < 0 || off > input.length - len) throw new SignatureException("Invalid input range");
  try { session.update(input, off, len); }
  catch (SigningException e) { closeSession(); initialized = false; throw new SignatureException("Hardware signing update failed", e); }
 }
 @Override protected byte[] engineSign() throws SignatureException {
  if (!initialized || session == null) throw new SignatureException("Signature is not initialized for signing");
  try {
   byte[] result = session.sign();
   if (result == null || result.length == 0) throw new SignatureException("Hardware backend returned an empty signature");
   return result;
  } catch (SigningException e) { throw new SignatureException("Hardware signing failed", e); }
  finally { closeSession(); initialized = false; }
 }
 @Override protected boolean engineVerify(byte[] signature) throws SignatureException {
  throw new SignatureException("This provider supports hardware signing only");
 }
 @Override protected void engineSetParameter(AlgorithmParameterSpec params) throws InvalidAlgorithmParameterException { throw new InvalidAlgorithmParameterException("Signature parameters are not supported"); }
 @Override @Deprecated protected void engineSetParameter(String name, Object value) { throw new UnsupportedOperationException("Signature parameters are not supported"); }
 @Override protected Object engineGetParameter(String name) { throw new UnsupportedOperationException("Signature parameters are not supported"); }
 private void closeSession() {
  if (session != null) { try { session.close(); } catch (RuntimeException ignored) { /* preserve the primary failure */ } session = null; }
 }
}
