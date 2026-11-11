package gov.cmr.minfi.db.gbe.app.depense.besoin;

import gov.cmr.minfi.db.gbe.app.common.exception.BusinessException;
import gov.cmr.minfi.db.gbe.app.common.exception.ErrorCode;
import gov.cmr.minfi.db.gbe.app.referentiel.matiere.Matiere;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static gov.cmr.minfi.db.gbe.app.depense.besoin.StatutExpression.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ExpressionBesoinTest {

    private static final LocalDateTime MAINTENANT = LocalDateTime.of(2026, 11, 11, 10, 0);
    private static final LocalDateTime DATE_LIMITE = MAINTENANT.plusDays(10);
    private static final Matiere PAPIER = Matiere.builder().id("M1").reference("PAP-A4").libelle("Papier A4").build();
    private static final Matiere ENCRE = Matiere.builder().id("M2").reference("ENC-01").libelle("Encre").build();

    private static ExpressionBesoin besoin(StatutExpression statut) {
        final ExpressionBesoin b = ExpressionBesoin.builder().reference("EB-2026-00001").statut(statut)
                .dateClotureSoumission(DATE_LIMITE).build();
        b.ajouterLigne(PAPIER, new BigDecimal("10"), "rames de 500 feuilles");
        return b;
    }

    private static void assertErreur(Runnable action, ErrorCode attendu) {
        assertThatThrownBy(action::run)
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(attendu);
    }

    @Nested
    @DisplayName("Lignes")
    class Lignes {

        @Test
        void lignesNumeroteesALaSuite() {
            final ExpressionBesoin b = besoin(BROUILLON);
            b.ajouterLigne(ENCRE, new BigDecimal("2.5"), "litres");
            assertThat(b.getLignes()).extracting(LigneBesoin::getNumeroOrdre).containsExactly(1, 2);
            assertThat(b.getLignes().get(1).getExpressionBesoin()).isSameAs(b);
        }

        @Test
        void memeMatiereDeuxFois_refusee() {
            final ExpressionBesoin b = besoin(BROUILLON);
            assertErreur(() -> b.ajouterLigne(PAPIER, BigDecimal.ONE, null), ErrorCode.BESOIN_MATIERE_EN_DOUBLE);
        }

        @Test
        void quantiteNulle_refusee() {
            final ExpressionBesoin b = besoin(BROUILLON);
            assertErreur(() -> b.ajouterLigne(ENCRE, BigDecimal.ZERO, null), ErrorCode.MONTANT_INVALIDE);
        }
    }

    @Nested
    @DisplayName("Publication")
    class Publication {

        @Test
        void publier_brouillonComplet() {
            final ExpressionBesoin b = besoin(BROUILLON);
            b.publier(MAINTENANT);
            assertThat(b.getStatut()).isEqualTo(PUBLIEE);
            assertThat(b.getDatePublication()).isEqualTo(MAINTENANT);
            assertThat(b.isOuverteAuxSoumissions(MAINTENANT)).isTrue();
        }

        @Test
        void publier_sansLigne_refuse() {
            final ExpressionBesoin b = ExpressionBesoin.builder().dateClotureSoumission(DATE_LIMITE).build();
            assertErreur(() -> b.publier(MAINTENANT), ErrorCode.BESOIN_SANS_LIGNE);
            assertThat(b.getStatut()).isEqualTo(BROUILLON);
        }

        @Test
        void publier_sansDateLimiteOuDateLimitePassee_refuse() {
            final ExpressionBesoin sansDate = besoin(BROUILLON);
            sansDate.setDateClotureSoumission(null);
            assertErreur(() -> sansDate.publier(MAINTENANT), ErrorCode.BESOIN_DATE_CLOTURE_INVALIDE);

            final ExpressionBesoin passee = besoin(BROUILLON);
            passee.setDateClotureSoumission(MAINTENANT);
            assertErreur(() -> passee.publier(MAINTENANT), ErrorCode.BESOIN_DATE_CLOTURE_INVALIDE);
        }

        @ParameterizedTest
        @EnumSource(value = StatutExpression.class, names = "BROUILLON", mode = EnumSource.Mode.EXCLUDE)
        void publier_horsBrouillon_refuse(StatutExpression statut) {
            assertErreur(() -> besoin(statut).publier(MAINTENANT), ErrorCode.TRANSITION_STATUT_INVALIDE);
        }
    }

    @Nested
    @DisplayName("Modification")
    class Modification {

        @Test
        void brouillon_toujoursModifiable() {
            besoin(BROUILLON).verifierModifiable(DATE_LIMITE.plusDays(1), false);
        }

        @Test
        void publie_modifiableAvantDateLimiteSansSoumission() {
            besoin(PUBLIEE).verifierModifiable(MAINTENANT, false);
        }

        @Test
        void publie_avecSoumission_nonModifiable() {
            assertErreur(() -> besoin(PUBLIEE).verifierModifiable(MAINTENANT, true), ErrorCode.BESOIN_NON_MODIFIABLE);
        }

        @Test
        void publie_apresDateLimite_nonModifiable() {
            assertErreur(() -> besoin(PUBLIEE).verifierModifiable(DATE_LIMITE, false), ErrorCode.BESOIN_NON_MODIFIABLE);
        }

        @ParameterizedTest
        @EnumSource(value = StatutExpression.class, names = {"EN_EVALUATION", "ATTRIBUEE", "ANNULEE", "INFRUCTUEUSE"})
        void autresStatuts_nonModifiables(StatutExpression statut) {
            assertErreur(() -> besoin(statut).verifierModifiable(MAINTENANT, false), ErrorCode.BESOIN_NON_MODIFIABLE);
        }
    }

    @Nested
    @DisplayName("Clôture, annulation, suppression")
    class FinDeVie {

        @Test
        void cloturer_avantDateLimite_refuse() {
            final ExpressionBesoin b = besoin(PUBLIEE);
            assertErreur(() -> b.cloturerSoumissions(DATE_LIMITE.minusSeconds(1)), ErrorCode.BESOIN_CLOTURE_PREMATUREE);
            assertThat(b.getStatut()).isEqualTo(PUBLIEE);
        }

        @Test
        void cloturer_aLaDateLimite_passeEnEvaluation() {
            final ExpressionBesoin b = besoin(PUBLIEE);
            b.cloturerSoumissions(DATE_LIMITE);
            assertThat(b.getStatut()).isEqualTo(EN_EVALUATION);
            assertThat(b.getDateClotureEffective()).isEqualTo(DATE_LIMITE);
            assertThat(b.isOuverteAuxSoumissions(DATE_LIMITE)).isFalse();
        }

        @Test
        void annuler_publie() {
            final ExpressionBesoin b = besoin(PUBLIEE);
            b.annuler("besoin couvert par une autre commande");
            assertThat(b.getStatut()).isEqualTo(ANNULEE);
            assertThat(b.getMotifAnnulation()).isEqualTo("besoin couvert par une autre commande");
        }

        @ParameterizedTest
        @EnumSource(value = StatutExpression.class, names = "PUBLIEE", mode = EnumSource.Mode.EXCLUDE)
        void annuler_horsPublie_refuse(StatutExpression statut) {
            assertErreur(() -> besoin(statut).annuler("x"), ErrorCode.TRANSITION_STATUT_INVALIDE);
        }

        @Test
        void supprimer_brouillon_logiquement() {
            final ExpressionBesoin b = besoin(BROUILLON);
            b.supprimer();
            assertThat(b.isSupprime()).isTrue();
        }

        @ParameterizedTest
        @EnumSource(value = StatutExpression.class, names = "BROUILLON", mode = EnumSource.Mode.EXCLUDE)
        void supprimer_horsBrouillon_refuse(StatutExpression statut) {
            final ExpressionBesoin b = besoin(statut);
            assertErreur(b::supprimer, ErrorCode.TRANSITION_STATUT_INVALIDE);
            assertThat(b.isSupprime()).isFalse();
        }
    }
}
