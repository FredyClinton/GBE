package gov.cmr.minfi.db.gbe.app.identite.fournisseur;

import gov.cmr.minfi.db.gbe.app.common.exception.BusinessException;
import gov.cmr.minfi.db.gbe.app.common.exception.ErrorCode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FournisseurTest {

    private static final String RIB = "10005000010123456789012";

    @Test
    void normaliserRib_retireEspacesEtTirets_metEnMajuscules() {
        assertThat(CoordonneeBancaire.normaliserRib("10005 00001-0123456789a 12")).isEqualTo("10005000010123456789A12");
    }

    @Test
    void ribValide_23CaracteresAuBonFormat() {
        assertThat(CoordonneeBancaire.isRibValide(RIB)).isTrue();
        assertThat(CoordonneeBancaire.isRibValide("1000500001ABCDEFGHIJK12")).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "1000500001012345678901",    // 22 caractères
            "100050000101234567890123",  // 24 caractères
            "A0005000010123456789012",   // lettre dans le code banque
            "100050000101234567890AB",   // lettres dans la clé
            "10005000010123456789o12"    // minuscule non normalisée
    })
    void ribInvalide(String rib) {
        assertThat(CoordonneeBancaire.isRibValide(rib)).isFalse();
    }

    @Test
    void ajouterRib_doublonRefuse() {
        final Fournisseur f = Fournisseur.builder().build();
        f.ajouterRib(RIB);
        assertThatThrownBy(() -> f.ajouterRib(RIB))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.RIB_ALREADY_EXISTS);
        assertThat(f.getCoordonneesBancaires()).hasSize(1);
        assertThat(f.getCoordonneesBancaires().get(0).getFournisseur()).isSameAs(f);
    }

    @Test
    void retirerRib_inconnuRefuse() {
        final Fournisseur f = Fournisseur.builder().build();
        assertThatThrownBy(() -> f.retirerRib("X"))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.RIB_NOT_FOUND);
    }
}
