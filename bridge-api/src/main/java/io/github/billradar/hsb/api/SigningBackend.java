package io.github.billradar.hsb.api;
/** Hardware-specific implementation. Implementations must fail closed and never export private keys. */
public interface SigningBackend {
 SigningSession begin(SigningKeyReference key, String jcaSignatureAlgorithm) throws SigningException;
}
