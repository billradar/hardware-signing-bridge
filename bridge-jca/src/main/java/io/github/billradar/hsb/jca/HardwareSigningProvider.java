package io.github.billradar.hsb.jca;
import io.github.billradar.hsb.api.SigningBackend;
import java.security.Provider;
import java.util.Objects;
/** Explicitly-instantiated JCA provider; it does not mutate JVM-global provider order. */
public final class HardwareSigningProvider extends Provider {
 private static final long serialVersionUID = 1L;
 public HardwareSigningProvider(SigningBackend backend) {
  super("HardwareSigningBridge", "0.1", "Hardware-backed JCA signing bridge");
  Objects.requireNonNull(backend, "backend");
  putService(new BackendService(this, backend, "SHA256withECDSA"));
  putService(new BackendService(this, backend, "SHA384withECDSA"));
 }
 private static final class BackendService extends Provider.Service {
  private final SigningBackend backend;
  private final String algorithm;
  BackendService(Provider provider, SigningBackend backend, String algorithm) {
   super(provider, "Signature", algorithm, HardwareEcdsaSignatureSpi.class.getName(), null, null);
   this.backend = backend; this.algorithm = algorithm;
  }
  @Override public Object newInstance(Object parameter) {
   if (parameter != null) throw new IllegalArgumentException("No constructor parameter is supported");
   return new HardwareEcdsaSignatureSpi(backend, algorithm);
  }
 }
}
