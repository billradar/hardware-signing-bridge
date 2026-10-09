package io.github.billradar.hsb.api;
import java.util.Arrays;
/** Helper for deterministic clearing of an owned PIN buffer. */
public final class PinMaterial implements AutoCloseable {
 private char[] value;
 public PinMaterial(char[] ownedValue) {
  if (ownedValue == null) throw new IllegalArgumentException("PIN must not be null");
  this.value = ownedValue;
 }
 public char[] value() {
  if (value == null) throw new IllegalStateException("PIN material has been cleared");
  return value;
 }
 @Override public void close() {
  if (value != null) { Arrays.fill(value, '\0'); value = null; }
 }
}
