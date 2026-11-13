package gov.cmr.minfi.db.gbe.app.budget.credit;

import gov.cmr.minfi.db.gbe.app.common.exception.BusinessException;
import gov.cmr.minfi.db.gbe.app.common.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.util.function.Consumer;

import static gov.cmr.minfi.db.gbe.app.budget.credit.StatutCredit.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CreditBudgetaireTest {

    private static CreditBudgetaire credit(StatutCredit statut) {
        return CreditBudgetaire.builder()
                .statut(statut)
                .montantAE(new BigDecimal("1000"))
                .montantAEDisponible(new BigDecimal("1000"))
                .montantCP(new BigDecimal("500"))
                .montantCPDisponible(new BigDecimal("500"))
                .build();
    }

    private static void assertTransitionRefusee(StatutCredit depart, Consumer<CreditBudgetaire> transition) {
        final CreditBudgetaire credit = credit(depart);
        assertThatThrownBy(() -> transition.accept(credit))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.TRANSITION_STATUT_INVALIDE);
        assertThat(credit.getStatut()).isEqualTo(depart);
    }

    private static void assertErreur(Runnable action, ErrorCode attendu) {
        assertThatThrownBy(action::run)
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(attendu);
    }

    @Nested
    @DisplayName("Transitions autorisées")
    class TransitionsAutorisees {

        @Test
        void suspendrePuisDebloquer_revientADisponible() {
            final CreditBudgetaire c = credit(DISPONIBLE);
            c.suspendre();
            assertThat(c.getStatut()).isEqualTo(SUSPENDU);
            c.debloquer();
            assertThat(c.getStatut()).isEqualTo(DISPONIBLE);
        }

        @Test
        void cantonnerPuisDecantonner_revientADisponible() {
            final CreditBudgetaire c = credit(DISPONIBLE);
            c.cantonner();
            assertThat(c.getStatut()).isEqualTo(CANTONNE);
            c.decantionner();
            assertThat(c.getStatut()).isEqualTo(DISPONIBLE);
        }

        @ParameterizedTest
        @EnumSource(value = StatutCredit.class, names = {"DISPONIBLE", "SUSPENDU", "CANTONNE"})
        void annuler_depuisStatutNonTerminal(StatutCredit depart) {
            final CreditBudgetaire c = credit(depart);
            c.annuler();
            assertThat(c.getStatut()).isEqualTo(ANNULE);
            assertThat(c.isTerminal()).isTrue();
        }
    }

    @Nested
    @DisplayName("Transitions refusées")
    class TransitionsRefusees {

        @ParameterizedTest
        @EnumSource(value = StatutCredit.class, names = "DISPONIBLE", mode = EnumSource.Mode.EXCLUDE)
        void suspendre_horsDisponible(StatutCredit depart) {
            assertTransitionRefusee(depart, CreditBudgetaire::suspendre);
        }

        @ParameterizedTest
        @EnumSource(value = StatutCredit.class, names = "SUSPENDU", mode = EnumSource.Mode.EXCLUDE)
        void debloquer_horsSuspendu(StatutCredit depart) {
            assertTransitionRefusee(depart, CreditBudgetaire::debloquer);
        }

        @ParameterizedTest
        @EnumSource(value = StatutCredit.class, names = "CANTONNE", mode = EnumSource.Mode.EXCLUDE)
        void decantonner_horsCantonne(StatutCredit depart) {
            assertTransitionRefusee(depart, CreditBudgetaire::decantionner);
        }

        @Test
        void annuler_depuisAnnule() {
            assertTransitionRefusee(ANNULE, CreditBudgetaire::annuler);
        }

        @Test
        void annuler_avecAEConsommees_estRefuse() {
            final CreditBudgetaire c = credit(DISPONIBLE);
            c.consommerAE(new BigDecimal("1"));
            assertErreur(c::annuler, ErrorCode.CREDIT_ANNULATION_CREDITS_CONSOMMES);
            assertThat(c.getStatut()).isEqualTo(DISPONIBLE);
        }

        @Test
        void annuler_avecCPConsommes_estRefuse() {
            final CreditBudgetaire c = credit(DISPONIBLE);
            c.consommerCP(new BigDecimal("1"));
            c.suspendre();
            assertErreur(c::annuler, ErrorCode.CREDIT_ANNULATION_CREDITS_CONSOMMES);
            assertThat(c.getStatut()).isEqualTo(SUSPENDU);
        }

        @Test
        void annuler_apresLiberationTotale_estAccepte() {
            final CreditBudgetaire c = credit(DISPONIBLE);
            c.consommerAE(new BigDecimal("300"));
            c.libererAE(new BigDecimal("300"));
            c.annuler();
            assertThat(c.getStatut()).isEqualTo(ANNULE);
        }
    }

    @Nested
    @DisplayName("Consommation AE / CP")
    class Consommation {

        @Test
        void consommerPuisLibererAE_metAJourLesMontants() {
            final CreditBudgetaire c = credit(DISPONIBLE);

            c.consommerAE(new BigDecimal("300"));
            assertThat(c.getMontantAEConsomme()).isEqualByComparingTo("300");
            assertThat(c.getMontantAEDisponible()).isEqualByComparingTo("700");

            c.libererAE(new BigDecimal("100"));
            assertThat(c.getMontantAEConsomme()).isEqualByComparingTo("200");
            assertThat(c.getMontantAEDisponible()).isEqualByComparingTo("800");
        }

        @Test
        void consommerAE_auDelaDuDisponible_estRefuse() {
            final CreditBudgetaire c = credit(DISPONIBLE);
            assertThatThrownBy(() -> c.consommerAE(new BigDecimal("1000.01")))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(ErrorCode.MONTANT_AE_INSUFFISANT);
            assertThat(c.getMontantAEDisponible()).isEqualByComparingTo("1000");
        }

        @Test
        void consommerAE_exactementLeDisponible_estAccepte() {
            final CreditBudgetaire c = credit(DISPONIBLE);
            c.consommerAE(new BigDecimal("1000"));
            assertThat(c.getMontantAEDisponible()).isEqualByComparingTo("0");
        }

        @Test
        void consommerPuisLibererCP_metAJourLesMontants() {
            final CreditBudgetaire c = credit(DISPONIBLE);

            c.consommerCP(new BigDecimal("200"));
            assertThat(c.getMontantCPConsomme()).isEqualByComparingTo("200");
            assertThat(c.getMontantCPDisponible()).isEqualByComparingTo("300");

            c.libererCP(new BigDecimal("50"));
            assertThat(c.getMontantCPConsomme()).isEqualByComparingTo("150");
            assertThat(c.getMontantCPDisponible()).isEqualByComparingTo("350");
        }

        @Test
        void consommerCP_auDelaDuDisponible_estRefuse() {
            final CreditBudgetaire c = credit(DISPONIBLE);
            assertThatThrownBy(() -> c.consommerCP(new BigDecimal("501")))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(ErrorCode.MONTANT_CP_INSUFFISANT);
        }
            @Test
        void consommer_creditDisponible_estAccepte() {
            final CreditBudgetaire c = credit(DISPONIBLE);
            c.consommerAE(new BigDecimal("100"));
            c.consommerCP(new BigDecimal("100"));
            assertThat(c.getMontantAEDisponible()).isEqualByComparingTo("900");
            assertThat(c.getMontantCPDisponible()).isEqualByComparingTo("400");
        }

        @ParameterizedTest
        @EnumSource(value = StatutCredit.class, names = "DISPONIBLE", mode = EnumSource.Mode.EXCLUDE)
        void consommer_creditBloque_estRefuse(StatutCredit depart) {
            final CreditBudgetaire c = credit(depart);
            assertErreur(() -> c.consommerAE(new BigDecimal("100")), ErrorCode.CREDIT_NON_CONSOMMABLE);
            assertErreur(() -> c.consommerCP(new BigDecimal("100")), ErrorCode.CREDIT_NON_CONSOMMABLE);
            assertThat(c.getMontantAEDisponible()).isEqualByComparingTo("1000");
            assertThat(c.getMontantCPDisponible()).isEqualByComparingTo("500");
        }

        @Test
        void liberer_creditBloque_resteAutorise() {
            final CreditBudgetaire c = credit(DISPONIBLE);
            c.consommerAE(new BigDecimal("300"));
            c.suspendre();
            c.libererAE(new BigDecimal("300"));
            assertThat(c.getMontantAEDisponible()).isEqualByComparingTo("1000");
        }

        @Test
        void liberer_auDelaDuConsomme_estRefuse() {
            final CreditBudgetaire c = credit(DISPONIBLE);
            c.consommerAE(new BigDecimal("300"));
            c.consommerCP(new BigDecimal("100"));
            assertErreur(() -> c.libererAE(new BigDecimal("300.01")), ErrorCode.MONTANT_LIBERE_SUPERIEUR_CONSOMME);
            assertErreur(() -> c.libererCP(new BigDecimal("101")), ErrorCode.MONTANT_LIBERE_SUPERIEUR_CONSOMME);
            assertThat(c.getMontantAEConsomme()).isEqualByComparingTo("300");
            assertThat(c.getMontantCPConsomme()).isEqualByComparingTo("100");
        }

        @ParameterizedTest
        @ValueSource(strings = {"0", "-1"})
        void montantNulOuNegatif_estRefuse(String montant) {
            final CreditBudgetaire c = credit(DISPONIBLE);
            final BigDecimal m = new BigDecimal(montant);
            assertErreur(() -> c.consommerAE(m), ErrorCode.MONTANT_INVALIDE);
            assertErreur(() -> c.consommerCP(m), ErrorCode.MONTANT_INVALIDE);
            assertErreur(() -> c.libererAE(m), ErrorCode.MONTANT_INVALIDE);
            assertErreur(() -> c.libererCP(m), ErrorCode.MONTANT_INVALIDE);
        }

        @Test
        void montantNull_estRefuse() {
            final CreditBudgetaire c = credit(DISPONIBLE);
            assertErreur(() -> c.consommerAE(null), ErrorCode.MONTANT_INVALIDE);
        }
    }
}
