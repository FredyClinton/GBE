package gov.cmr.minfi.db.gbe.app.depense.taxe;

import gov.cmr.minfi.db.gbe.app.common.exception.BusinessException;
import gov.cmr.minfi.db.gbe.app.common.exception.ErrorCode;
import gov.cmr.minfi.db.gbe.app.referentiel.taxe.CodeTaxe;
import gov.cmr.minfi.db.gbe.app.referentiel.taxe.Taxe;
import gov.cmr.minfi.db.gbe.app.referentiel.taxe.TaxeRepository;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TvaEtIrParametresTest {

    private final TaxeRepository taxes = mock(TaxeRepository.class);
    private final CalculTaxe calcul = new TvaEtIrParametres(taxes);

    private void taux(String tva, String ir) {
        when(taxes.findByCode(CodeTaxe.TVA)).thenReturn(Optional.of(
                Taxe.builder().code(CodeTaxe.TVA).taux(new BigDecimal(tva)).build()));
        when(taxes.findByCode(CodeTaxe.IR)).thenReturn(Optional.of(
                Taxe.builder().code(CodeTaxe.IR).taux(new BigDecimal(ir)).build()));
    }

    /** Montants du BCA réel BCA-A602010097 (plan §4.5). */
    @Test
    void montantsDuBcaDeReference() {
        taux("19.25", "5.50");
        final MontantsTaxes m = calcul.calculer(null, new BigDecimal("4120000.00"));
        assertThat(m.montantTva()).isEqualByComparingTo("793100");
        assertThat(m.montantIr()).isEqualByComparingTo("226600");
        assertThat(m.montantTTC()).isEqualByComparingTo("4913100");
        assertThat(m.montantNetAPayer()).isEqualByComparingTo("3893400");
        assertThat(m.tauxTva()).isEqualByComparingTo("19.25");
    }

    @Test
    void arrondiAuCentime() {
        taux("19.25", "5.50");
        final MontantsTaxes m = calcul.calculer(null, new BigDecimal("37501.00"));
        assertThat(m.montantTva()).isEqualByComparingTo("7218.94");   // 7218,9425
        assertThat(m.montantIr()).isEqualByComparingTo("2062.56");    // 2062,555
        assertThat(m.montantNetAPayer()).isEqualByComparingTo("35438.44");
    }

    @Test
    void taxeNonParametree_refusee() {
        when(taxes.findByCode(CodeTaxe.TVA)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> calcul.calculer(null, BigDecimal.TEN))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.TAXE_NON_PARAMETREE);
    }

    @Test
    void tauxInvalide_refuse() {
        final Taxe tva = Taxe.builder().code(CodeTaxe.TVA).taux(new BigDecimal("19.25")).build();
        for (String invalide : new String[]{"-1", "100"}) {
            assertThatThrownBy(() -> tva.changerTaux(new BigDecimal(invalide)))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(ErrorCode.TAUX_TAXE_INVALIDE);
        }
        tva.changerTaux(new BigDecimal("18"));
        assertThat(tva.getTaux()).isEqualByComparingTo("18");
    }
}
