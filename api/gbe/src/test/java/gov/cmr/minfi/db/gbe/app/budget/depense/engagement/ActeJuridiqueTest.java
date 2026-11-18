package gov.cmr.minfi.db.gbe.app.budget.depense.engagement;

import gov.cmr.minfi.db.gbe.app.budget.credit.CreditBudgetaire;
import gov.cmr.minfi.db.gbe.app.common.exception.BusinessException;
import gov.cmr.minfi.db.gbe.app.common.exception.ErrorCode;
import gov.cmr.minfi.db.gbe.app.identite.user.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Signature, notification et annulation de l'acte (lot 2d) ; annulation de la réservation (Q2). */
class ActeJuridiqueTest {

    private static final LocalDateTime MAINTENANT = LocalDateTime.of(2026, 11, 18, 10, 0);
    private final User ordonnateur = User.builder().id("U").firstName("Awa").lastName("Mbarga").build();

    private static ActeJuridique acte(StatutActe statut) {
        return ActeJuridique.builder().id("A").numero("BCA-A602000001").statut(statut)
                .montantTTC(new BigDecimal("42334.94")).build();
    }

    private static ErrorCode code(Runnable r) {
        try {
            r.run();
        } catch (BusinessException e) {
            return e.getErrorCode();
        }
        throw new AssertionError("BusinessException attendue");
    }

    @Test
    @DisplayName("Un acte visé se signe puis se notifie : signataire et dates tracés")
    void signatureEtNotification() {
        final ActeJuridique a = acte(StatutActe.VISE_CF);
        a.signer(ordonnateur, MAINTENANT);
        assertThat(a.getStatut()).isEqualTo(StatutActe.SIGNE_ORDONNATEUR);
        assertThat(a.getSignataire()).isSameAs(ordonnateur);
        assertThat(a.getDateSignature()).isEqualTo(MAINTENANT);
        assertThat(a.isNotifie()).isFalse();

        a.notifierFournisseur(MAINTENANT);
        assertThat(a.getStatut()).isEqualTo(StatutActe.NOTIFIE_PRESTATAIRE);
        assertThat(a.isNotifie()).isTrue();
    }

    @ParameterizedTest
    @EnumSource(value = StatutActe.class, names = "VISE_CF", mode = EnumSource.Mode.EXCLUDE)
    void signature_refuseeHorsVisa(StatutActe statut) {
        assertThat(code(() -> acte(statut).signer(ordonnateur, MAINTENANT))).isEqualTo(ErrorCode.TRANSITION_STATUT_INVALIDE);
    }

    @Test
    void notification_refuseeAvantSignature() {
        assertThat(code(() -> acte(StatutActe.VISE_CF).notifierFournisseur(MAINTENANT)))
                .isEqualTo(ErrorCode.TRANSITION_STATUT_INVALIDE);
    }

    @ParameterizedTest
    @EnumSource(value = StatutActe.class, names = {"VISE_CF", "SIGNE_ORDONNATEUR", "NOTIFIE_PRESTATAIRE"})
    void annulation_dUnActeViseSigneOuNotifie(StatutActe statut) {
        final ActeJuridique a = acte(statut);
        a.annuler("Besoin couvert", MAINTENANT);
        assertThat(a.getStatut()).isEqualTo(StatutActe.ANNULE);
        assertThat(a.getMotifAnnulation()).isEqualTo("Besoin couvert");
        assertThat(a.getDateAnnulation()).isEqualTo(MAINTENANT);
    }

    @ParameterizedTest
    @EnumSource(value = StatutActe.class, names = {"PROJET", "SOUMIS_CONTROLE", "REJETE", "ANNULE"})
    void annulation_refuseeSansVisa(StatutActe statut) {
        assertThat(code(() -> acte(statut).annuler("x", MAINTENANT))).isEqualTo(ErrorCode.TRANSITION_STATUT_INVALIDE);
    }

    @Test
    @DisplayName("Annuler la réservation libère les AE sur la ligne, une seule fois")
    void annulationReservation_libereLesAE() {
        final CreditBudgetaire credit = CreditBudgetaire.builder().id("C").montantAE(new BigDecimal("1000000"))
                .montantAEDisponible(new BigDecimal("1000000")).montantAEConsomme(BigDecimal.ZERO).build();
        final ReservationCredit reservation = ReservationCredit.bloquer("RES-2026-00001", acte(StatutActe.SOUMIS_CONTROLE),
                credit, MAINTENANT);
        assertThat(credit.getMontantAEConsomme()).isEqualByComparingTo("42334.94");

        reservation.annuler(MAINTENANT);
        assertThat(reservation.getStatut()).isEqualTo(StatutReservation.ANNULEE);
        assertThat(reservation.getDateAnnulation()).isEqualTo(MAINTENANT);
        assertThat(credit.getMontantAEConsomme()).isEqualByComparingTo("0");
        assertThat(credit.getMontantAEDisponible()).isEqualByComparingTo("1000000");

        assertThatThrownBy(() -> reservation.annuler(MAINTENANT)).isInstanceOf(BusinessException.class);
        assertThat(credit.getMontantAEDisponible()).isEqualByComparingTo("1000000");
    }
}
