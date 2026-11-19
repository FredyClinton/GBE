package gov.cmr.minfi.db.gbe.app.identite.auth.tfa;

import dev.samstevens.totp.code.DefaultCodeGenerator;
import dev.samstevens.totp.code.HashingAlgorithm;
import dev.samstevens.totp.qr.QrData;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TwoFactorAuthenticationServiceTest {

    private final TwoFactorAuthenticationService service = new TwoFactorAuthenticationService();

    /** Code que calcule une application d'authentification à partir du seul contenu du QR code. */
    private static String codeCalculeDepuis(QrData qr) throws Exception {
        final long intervalle = Math.floorDiv(System.currentTimeMillis() / 1000, qr.getPeriod());
        return new DefaultCodeGenerator(HashingAlgorithm.valueOf(qr.getAlgorithm()), qr.getDigits())
                .generate(qr.getSecret(), intervalle);
    }

    @Test
    @DisplayName("un code calculé avec les paramètres du QR code est accepté")
    void codeSelonQrCode_accepte() throws Exception {
        final String secret = service.generateNewSecret();

        assertThat(service.isOtpValid(secret, codeCalculeDepuis(service.donneesQrCode(secret)))).isTrue();
    }

    @Test
    @DisplayName("le QR code annonce SHA1, 6 chiffres, 30 s (compatible Google Authenticator)")
    void qrCode_parametresCompatibles() {
        final QrData qr = service.donneesQrCode(service.generateNewSecret());

        assertThat(qr.getAlgorithm()).isEqualTo("SHA1");
        assertThat(qr.getDigits()).isEqualTo(6);
        assertThat(qr.getPeriod()).isEqualTo(30);
    }

    @Test
    @DisplayName("un code d'un autre algorithme, un code faux ou un secret absent sont refusés")
    void codeInvalide_refuse() throws Exception {
        final String secret = service.generateNewSecret();
        final long intervalle = Math.floorDiv(System.currentTimeMillis() / 1000, 30);
        final String codeSha256 = new DefaultCodeGenerator(HashingAlgorithm.SHA256, 6).generate(secret, intervalle);
        final String codeSha1 = new DefaultCodeGenerator(HashingAlgorithm.SHA1, 6).generate(secret, intervalle);

        if (!codeSha256.equals(codeSha1)) {
            assertThat(service.isOtpValid(secret, codeSha256)).isFalse();
        }
        assertThat(service.isOtpValid(secret, "abcdef")).isFalse();
        assertThat(service.isOtpValid(null, codeSha1)).isFalse();
    }
}
