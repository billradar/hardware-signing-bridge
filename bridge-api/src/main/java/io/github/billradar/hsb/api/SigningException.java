package io.github.billradar.hsb.api;
/** Checked failure raised by a hardware signing backend. Never includes secret material. */
public class SigningException extends Exception {
 public SigningException(String message) { super(message); }
 public SigningException(String message, Throwable cause) { super(message, cause); }
}
