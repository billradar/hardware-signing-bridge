package io.github.billradar.hsb.api;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.HexFormat;
import java.util.Objects;
/** Explicit binding between a hardware object identifier and its expected X.509 certificate. */
public final class SigningKeyReference {
 private final String objectId;
 private final X509Certificate certificate;
 private final String expectedCertificateSha256;
 public SigningKeyReference(String objectId, X509Certificate certificate, String expectedCertificateSha256) {
  this.objectId = requireText(objectId, "objectId");
  this.certificate = Objects.requireNonNull(certificate, "certificate");
  this.expectedCertificateSha256 = normalizeFingerprint(requireText(expectedCertificateSha256, "expectedCertificateSha256"));
  String actual = fingerprint(certificate);
  if (!MessageDigest.isEqual(this.expectedCertificateSha256.getBytes(java.nio.charset.StandardCharsets.US_ASCII), actual.getBytes(java.nio.charset.StandardCharsets.US_ASCII)))
   throw new IllegalArgumentException("Certificate SHA-256 fingerprint does not match the expected identity");
 }
 public String objectId() { return objectId; }
 public X509Certificate certificate() { return certificate; }
 public String expectedCertificateSha256() { return expectedCertificateSha256; }
 public static String fingerprint(X509Certificate certificate) {
  try { return HexFormat.of().withUpperCase().formatHex(MessageDigest.getInstance("SHA-256").digest(certificate.getEncoded())); }
  catch (NoSuchAlgorithmException | CertificateEncodingException e) { throw new IllegalArgumentException("Unable to calculate certificate fingerprint", e); }
 }
 private static String normalizeFingerprint(String value) {
  String normalized = value.replace(":", "").replace(" ", "").toUpperCase(java.util.Locale.ROOT);
  if (!normalized.matches("[0-9A-F]{64}")) throw new IllegalArgumentException("Expected a 32-byte SHA-256 certificate fingerprint");
  return normalized;
 }
 private static String requireText(String value, String name) {
  if (value == null || value.isBlank()) throw new IllegalArgumentException(name + " must not be blank");
  return value.trim();
 }
}
