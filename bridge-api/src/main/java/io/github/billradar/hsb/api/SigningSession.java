package io.github.billradar.hsb.api;
/** One signing operation. Implementations must release native/session resources on close. */
public interface SigningSession extends AutoCloseable {
 void update(byte[] input, int offset, int length) throws SigningException;
 /** Returns a signature in the encoding documented by the selected backend. */
 byte[] sign() throws SigningException;
 @Override void close();
}
