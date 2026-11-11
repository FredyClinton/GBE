package gov.cmr.minfi.db.gbe.app.depense.taxe;

import gov.cmr.minfi.db.gbe.app.common.exception.BusinessException;
import gov.cmr.minfi.db.gbe.app.common.exception.ErrorCode;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TtcSaisiParFournisseurTest {

    private final CalculTaxe calcul = new TtcSaisiParFournisseur();
    private final BigDecimal ht = new BigDecimal("1000.00");

    @Test
    void ttcAuMoinsEgalAuHT_retenuTelQuel() {
        assertThat(calcul.calculerTTC(null, ht, new BigDecimal("1192.50"))).isEqualByComparingTo("1192.50");
        assertThat(calcul.calculerTTC(null, ht, ht)).isEqualByComparingTo(ht); // exonéré
    }

    @Test
    void ttcAbsentOuInferieurAuHT_refuse() {
        for (BigDecimal ttc : new BigDecimal[]{null, new BigDecimal("999.99")}) {
            assertThatThrownBy(() -> calcul.calculerTTC(null, ht, ttc))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(ErrorCode.MONTANT_TTC_INVALIDE);
        }
    }
}
