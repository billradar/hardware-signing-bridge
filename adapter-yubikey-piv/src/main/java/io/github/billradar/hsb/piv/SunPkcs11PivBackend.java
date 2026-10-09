package io.github.billradar.hsb.piv;

import io.github.billradar.hsb.api.PinSource;
import io.github.billradar.hsb.api.SigningBackend;
import io.github.billradar.hsb.api.SigningException;
import io.github.billradar.hsb.api.SigningKeyReference;
import io.github.billradar.hsb.api.SigningSession;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.security.Key;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.Security;
import java.security.Signature;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;

/**
 * Initial YubiKey PIV adapter using the JDK SunPKCS11 provider and a configurable PKCS#11 module
 * (commonly OpenSC's opensc-pkcs11 module). No PIN is written to the provider configuration.
 *
 * This adapter is opt-in and has not been validated against physical hardware in Phase 1.
 */
public final class SunPkcs11PivBackend implements SigningBackend {
    private static final Set<String> ALGORITHMS = Set.of("SHA256withECDSA", "SHA384withECDSA");
    private final Path modulePath;
    private final int slotListIndex;
    private final PinSource pinSource;
    private volatile Provider configuredProvider;

    public SunPkcs11PivBackend(Path modulePath, int slotListIndex, PinSource pinSource) {
        this.modulePath = Objects.requireNonNull(modulePath, "modulePath").toAbsolutePath().normalize();
        if (slotListIndex < 0) throw new IllegalArgumentException("slotListIndex must be non-negative");
        if (this.modulePath.toString().contains("\n") || this.modulePath.toString().contains("\r"))
            throw new IllegalArgumentException("PKCS#11 module path must not contain line breaks");
        this.slotListIndex = slotListIndex;
        this.pinSource = Objects.requireNonNull(pinSource, "pinSource");
    }

    @Override public SigningSession begin(SigningKeyReference key, String algorithm) throws SigningException {
        Objects.requireNonNull(key, "key");
        if (!ALGORITHMS.contains(algorithm)) throw new SigningException("Unsupported signature algorithm");
        char[] pin = null;
        try {
            pin = pinSource.acquirePin();
            if (pin == null) throw new SigningException("PIN source returned null");
            KeyStore tokenStore = KeyStore.getInstance("PKCS11", provider());
            tokenStore.load(null, pin);

            String alias = key.objectId();
            Certificate tokenCertificate = tokenStore.getCertificate(alias);
            if (!(tokenCertificate instanceof X509Certificate x509))
                throw new SigningException("Configured PKCS#11 alias has no X.509 certificate");
            String tokenFingerprint = SigningKeyReference.fingerprint(x509);
            if (!tokenFingerprint.equals(key.expectedCertificateSha256()))
                throw new SigningException("Token certificate fingerprint does not match the expected identity");
            if (!tokenFingerprint.equals(SigningKeyReference.fingerprint(key.certificate())))
                throw new SigningException("Supplied certificate and token certificate do not match");

            Key tokenKey = tokenStore.getKey(alias, pin);
            if (!(tokenKey instanceof PrivateKey privateKey) || !"EC".equalsIgnoreCase(privateKey.getAlgorithm()))
                throw new SigningException("Configured PKCS#11 alias does not resolve to an EC private key");

            Signature signature = Signature.getInstance(algorithm, provider());
            signature.initSign(privateKey);
            return new PivSigningSession(signature);
        } catch (SigningException e) {
            throw e;
        } catch (Exception e) {
            // Avoid including provider diagnostics that may disclose token details in the public message.
            throw new SigningException("Unable to initialize the requested PKCS#11 signing operation", e);
        } finally {
            if (pin != null) Arrays.fill(pin, '\0');
        }
    }

    private Provider provider() throws SigningException {
        Provider cached = configuredProvider;
        if (cached != null) return cached;
        synchronized (this) {
            if (configuredProvider != null) return configuredProvider;
            Path config = null;
            try {
                Provider base = Security.getProvider("SunPKCS11");
                if (base == null) throw new SigningException("JDK SunPKCS11 provider is unavailable");
                config = Files.createTempFile("hsb-pkcs11-", ".cfg");
                try {
                    Set<PosixFilePermission> permissions = EnumSet.of(
                        PosixFilePermission.OWNER_READ, PosixFilePermission.OWNER_WRITE);
                    Files.setPosixFilePermissions(config, permissions);
                } catch (UnsupportedOperationException ignored) {
                    // On non-POSIX systems, rely on the OS temporary-directory ACL.
                }
                String configText = "name = HsbPiv\nlibrary = " + modulePath
                    + "\nslotListIndex = " + slotListIndex + "\n";
                Files.writeString(config, configText, java.nio.charset.StandardCharsets.UTF_8);
                configuredProvider = base.configure(config.toAbsolutePath().toString());
                return configuredProvider;
            } catch (SigningException e) {
                throw e;
            } catch (Exception e) {
                throw new SigningException("Unable to configure the PKCS#11 provider", e);
            } finally {
                if (config != null) {
                    try { Files.deleteIfExists(config); } catch (IOException ignored) { /* best-effort cleanup */ }
                }
            }
        }
    }

    private static final class PivSigningSession implements SigningSession {
        private Signature signature;
        private boolean completed;

        PivSigningSession(Signature signature) { this.signature = signature; }

        @Override public void update(byte[] input, int offset, int length) throws SigningException {
            ensureOpen();
            try { signature.update(input, offset, length); }
            catch (Exception e) { close(); throw new SigningException("PKCS#11 signing update failed", e); }
        }

        @Override public byte[] sign() throws SigningException {
            ensureOpen();
            try {
                byte[] result = signature.sign();
                completed = true;
                return result;
            } catch (Exception e) {
                throw new SigningException("PKCS#11 signing operation failed", e);
            } finally {
                close();
            }
        }

        @Override public void close() {
            signature = null;
            completed = true;
        }

        private void ensureOpen() throws SigningException {
            if (completed || signature == null) throw new SigningException("Signing session is closed");
        }
    }
}
