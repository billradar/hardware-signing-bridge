package io.github.billradar.hsb.api;
/** Supplies a short-lived PIN copy. Implementations must not persist or log the PIN. */
@FunctionalInterface
public interface PinSource {
 /** Caller owns the returned array and must clear it as soon as authentication completes. */
 char[] acquirePin() throws SigningException;
}
