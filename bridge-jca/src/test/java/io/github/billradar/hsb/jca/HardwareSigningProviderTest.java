package io.github.billradar.hsb.jca;

import static org.junit.jupiter.api.Assertions.*;
import io.github.billradar.hsb.api.PinMaterial;
import io.github.billradar.hsb.api.SigningBackend;
import io.github.billradar.hsb.api.SigningException;
import java.security.InvalidKeyException;
import java.security.KeyPairGenerator;
import java.security.Provider;
import java.security.Signature;
import org.junit.jupiter.api.Test;

class HardwareSigningProviderTest {
 @Test void providerRegistersOnlySupportedAlgorithms() {
  SigningBackend backend = (key, algorithm) -> { throw new SigningException("not used"); };
  Provider provider = new HardwareSigningProvider(backend);
  assertNotNull(provider.getService("Signature", "SHA256withECDSA"));
  assertNotNull(provider.getService("Signature", "SHA384withECDSA"));
  assertNull(provider.getService("Signature", "SHA512withECDSA"));
  assertEquals("HardwareSigningBridge", provider.getName());
 }

 @Test void providerRejectsOrdinaryExportablePrivateKeys() throws Exception {
  SigningBackend backend = (key, algorithm) -> { throw new AssertionError("backend must not be called"); };
  Provider provider = new HardwareSigningProvider(backend);
  Signature signature = Signature.getInstance("SHA256withECDSA", provider);
  var generator = KeyPairGenerator.getInstance("EC");
  generator.initialize(256);
  var pair = generator.generateKeyPair();
  assertThrows(InvalidKeyException.class, () -> signature.initSign(pair.getPrivate()));
 }

 @Test void pinMaterialClearsOwnedBuffer() {
  char[] pin = {'1','2','3','4'};
  try (PinMaterial material = new PinMaterial(pin)) {
   assertArrayEquals(new char[] {'1','2','3','4'}, material.value());
  }
  assertArrayEquals(new char[] {'\0','\0','\0','\0'}, pin);
  PinMaterial material = new PinMaterial(new char[] {'x'});
  material.close();
  assertThrows(IllegalStateException.class, material::value);
 }
}
