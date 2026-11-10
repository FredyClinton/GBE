package gov.cmr.minfi.db.gbe.app.budget.credit;

import gov.cmr.minfi.db.gbe.app.common.exception.BusinessException;
import gov.cmr.minfi.db.gbe.app.common.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

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

    @Nested
    @DisplayName("Transitions autorisées")
    class TransitionsAutorisees {

        @Test
        void engager_depuisDisponible() {
            final CreditBudgetaire c = credit(DISPONIBLE);
            c.engager();
            assertThat(c.getStatut()).isEqualTo(ENGAGE);
        }

        @Test
        void suspendrePuisDebloquer_revientADisponible() {
            final CreditBudgetaire c = credit(ENGAGE);
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

        @Test
        void solder_depuisEngage() {
            final CreditBudgetaire c = credit(ENGAGE);
            c.solder();
            assertThat(c.getStatut()).isEqualTo(SOLDE);
            assertThat(c.isTerminal()).isTrue();
        }

        @ParameterizedTest
        @EnumSource(value = StatutCredit.class, names = {"DISPONIBLE", "ENGAGE", "SUSPENDU", "CANTONNE"})
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

        @Test
        void unCreditCantonneNePeutPasEtreEngage() {
            assertTransitionRefusee(CANTONNE, CreditBudgetaire::engager);
        }

        @ParameterizedTest
        @EnumSource(value = StatutCredit.class, names = "DISPONIBLE", mode = EnumSource.Mode.EXCLUDE)
        void engager_horsDisponible(StatutCredit depart) {
            assertTransitionRefusee(depart, CreditBudgetaire::engager);
        }

        @ParameterizedTest
        @EnumSource(value = StatutCredit.class, names = "ENGAGE", mode = EnumSource.Mode.EXCLUDE)
        void suspendreEtSolder_horsEngage(StatutCredit depart) {
            assertTransitionRefusee(depart, CreditBudgetaire::suspendre);
            assertTransitionRefusee(depart, CreditBudgetaire::solder);
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

        @ParameterizedTest
        @EnumSource(value = StatutCredit.class, names = {"SOLDE", "ANNULE"})
        void annuler_depuisStatutTerminal(StatutCredit depart) {
            assertTransitionRefusee(depart, CreditBudgetaire::annuler);
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
    }
}
