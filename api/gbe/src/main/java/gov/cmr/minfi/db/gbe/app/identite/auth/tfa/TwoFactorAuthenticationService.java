package gov.cmr.minfi.db.gbe.app.identite.auth.tfa;

import dev.samstevens.totp.code.*;
import dev.samstevens.totp.exceptions.QrGenerationException;
import dev.samstevens.totp.qr.QrData;
import dev.samstevens.totp.qr.QrGenerator;
import dev.samstevens.totp.qr.ZxingPngQrGenerator;
import dev.samstevens.totp.secret.DefaultSecretGenerator;
import dev.samstevens.totp.time.SystemTimeProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import static dev.samstevens.totp.util.Utils.getDataUriForImage;

@Service
@Slf4j
public class TwoFactorAuthenticationService {

    /**
     * Paramètres TOTP communs au QR code et à la vérification : ils doivent être identiques, sinon une application
     * qui respecte le QR code produit des codes toujours refusés. SHA1 car Google Authenticator ignore le paramètre
     * d'algorithme et calcule toujours en SHA1 (les comptes déjà configurés restent valides).
     */
    static final HashingAlgorithm ALGORITHME = HashingAlgorithm.SHA1;
    static final int CHIFFRES = 6;
    static final int PERIODE_SECONDES = 30;

    private static final String EMETTEUR = "GBE-MINFI";

    public String generateNewSecret() {
        return new DefaultSecretGenerator().generate();
    }

    public String generateQrCodeImageUri(String secret) {
        QrGenerator generator = new ZxingPngQrGenerator();
        byte[] imageData = new byte[0];
        try {
            imageData = generator.generate(donneesQrCode(secret));
        } catch (QrGenerationException e) {
            log.error("Error generating QR code image", e);
        }

        return getDataUriForImage(imageData, generator.getImageMimeType());
    }

    /** Contenu du QR code (URI otpauth) ; mêmes paramètres que {@link #isOtpValid}. */
    QrData donneesQrCode(String secret) {
        return new QrData.Builder()
                .label(EMETTEUR)
                .secret(secret)
                .issuer(EMETTEUR)
                .algorithm(ALGORITHME)
                .digits(CHIFFRES)
                .period(PERIODE_SECONDES)
                .build();
    }

    public boolean isOtpValid(String secret, String code) {
        final DefaultCodeVerifier verifier = new DefaultCodeVerifier(
                new DefaultCodeGenerator(ALGORITHME, CHIFFRES), new SystemTimeProvider());
        verifier.setTimePeriod(PERIODE_SECONDES);
        return verifier.isValidCode(secret, code);
    }

    public boolean isNonOtpValid(String secret, String code) {
        return !isOtpValid(secret, code);
    }
}
