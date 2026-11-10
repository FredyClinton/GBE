package gov.cmr.minfi.db.gbe.app.security;

import org.springframework.core.io.Resource;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

/**
 * Chargement des clés RSA au format PEM (PKCS#8 pour la clé privée, X.509 pour la clé publique).
 * Les emplacements sont des {@link Resource} Spring : {@code classpath:...} ou {@code file:...}.
 */
public final class KeyUtils {

    private KeyUtils() {
    }

    public static PrivateKey loadPrivateKey(final Resource pem) throws IOException, GeneralSecurityException {
        final byte[] decoded = decodePem(pem, "PRIVATE KEY");
        return KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(decoded));
    }

    public static PublicKey loadPublicKey(final Resource pem) throws IOException, GeneralSecurityException {
        final byte[] decoded = decodePem(pem, "PUBLIC KEY");
        return KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(decoded));
    }

    private static byte[] decodePem(final Resource pem, final String type) throws IOException {
        if (!pem.exists()) {
            throw new IllegalArgumentException("Fichier de clé introuvable : " + pem.getDescription());
        }
        try (final InputStream is = pem.getInputStream()) {
            final String key = new String(is.readAllBytes(), StandardCharsets.US_ASCII)
                    .replace("-----BEGIN " + type + "-----", "")
                    .replace("-----END " + type + "-----", "")
                    .replaceAll("\\s", "");
            return Base64.getDecoder().decode(key);
        }
    }
}
