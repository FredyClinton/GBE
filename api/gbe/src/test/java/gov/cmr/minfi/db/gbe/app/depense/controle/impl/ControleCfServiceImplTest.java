package gov.cmr.minfi.db.gbe.app.depense.controle.impl;

import gov.cmr.minfi.db.gbe.app.budget.credit.CreditBudgetaire;
import gov.cmr.minfi.db.gbe.app.common.exception.BusinessException;
import gov.cmr.minfi.db.gbe.app.common.exception.ErrorCode;
import gov.cmr.minfi.db.gbe.app.common.reference.ReferenceGenerator;
import gov.cmr.minfi.db.gbe.app.depense.besoin.ExpressionBesoin;
import gov.cmr.minfi.db.gbe.app.depense.controle.StatutVisa;
import gov.cmr.minfi.db.gbe.app.depense.controle.Visa;
import gov.cmr.minfi.db.gbe.app.depense.controle.VisaMapper;
import gov.cmr.minfi.db.gbe.app.depense.controle.VisaRepository;
import gov.cmr.minfi.db.gbe.app.depense.controle.dto.DecisionVisaRequest;
import gov.cmr.minfi.db.gbe.app.depense.engagement.*;
import gov.cmr.minfi.db.gbe.app.identite.iam.permission.Permission;
import gov.cmr.minfi.db.gbe.app.identite.mandat.MandatScopeValidator;
import gov.cmr.minfi.db.gbe.app.referentiel.administratif.Section;
import gov.cmr.minfi.db.gbe.app.referentiel.exercice.Exercice;
import gov.cmr.minfi.db.gbe.app.referentiel.programmatique.Programme;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class ControleCfServiceImplTest {

    private static final Clock HORLOGE = Clock.fixed(Instant.parse("2026-11-11T09:00:00Z"), ZoneId.of("UTC"));

    private final ActeJuridiqueRepository acteRepository = mock(ActeJuridiqueRepository.class);
    private final VisaRepository visaRepository = mock(VisaRepository.class);
    private final ReservationCreditRepository reservationRepository = mock(ReservationCreditRepository.class);
    private final MandatScopeValidator scope = mock(MandatScopeValidator.class);
    private final ReferenceGenerator references = mock(ReferenceGenerator.class);
    private final ControleCfServiceImpl service = new ControleCfServiceImpl(acteRepository, visaRepository,
            reservationRepository, scope, references, mock(VisaMapper.class), HORLOGE);

    private CreditBudgetaire credit;
    private ActeJuridique acte;

    @BeforeEach
    void init() {
        final ExpressionBesoin besoin = ExpressionBesoin.builder().id("B").reference("EB-2026-00001")
                .exercice(Exercice.builder().id("E").annee(2026).build())
                .section(Section.builder().id("S").build())
                .programme(Programme.builder().id("P").build())
                .build();
        credit = CreditBudgetaire.builder().id("C").montantAE(new BigDecimal("1000000"))
                .montantAEDisponible(new BigDecimal("1000000")).build();
        acte = ActeJuridique.builder().id("A").numero("BCA-A602000001").statut(StatutActe.SOUMIS_CONTROLE)
                .montantTTC(new BigDecimal("4913100")).expressionBesoin(besoin).creditBudgetaire(credit)
                .dateSoumissionControle(LocalDateTime.of(2026, 11, 10, 8, 0)).build();
        when(acteRepository.findById("A")).thenReturn(Optional.of(acte));
        when(references.prochaineReference(anyString(), anyInt())).thenAnswer(i -> i.getArgument(0) + "-2026-00001");
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
    void favorable_reserveLesAEPortesLeQuitusEtViseLActe() {
        credit.setMontantAEDisponible(new BigDecimal("5000000"));
        credit.setMontantAE(new BigDecimal("5000000"));

        service.decider("A", new DecisionVisaRequest(StatutVisa.FAVORABLE, null));

        assertThat(acte.getStatut()).isEqualTo(StatutActe.VISE_CF);
        assertThat(credit.getMontantAEConsomme()).isEqualByComparingTo("4913100");
        assertThat(credit.getMontantAEDisponible()).isEqualByComparingTo("86900");
        final ArgumentCaptor<ReservationCredit> reservation = ArgumentCaptor.forClass(ReservationCredit.class);
        verify(reservationRepository).save(reservation.capture());
        assertThat(reservation.getValue().getMontant()).isEqualByComparingTo("4913100");
        assertThat(reservation.getValue().getCreditBudgetaire()).isSameAs(credit);
        final ArgumentCaptor<Visa> visa = ArgumentCaptor.forClass(Visa.class);
        verify(visaRepository).save(visa.capture());
        assertThat(visa.getValue().getReferenceQuitus()).isEqualTo("QUI-2026-00001");
        assertThat(visa.getValue().getDateReception()).isEqualTo(LocalDateTime.of(2026, 11, 10, 8, 0));
        verify(scope).checkAccessPerimetre("S", "P", Permission.VISA_CFI);
        verify(scope).checkAccessPerimetre("S", "P", Permission.RESERVER_CREDIT);
        verify(scope).checkAccessPerimetre("S", "P", Permission.QUITUS_NATURE);
    }

    @Test
    void favorable_avecAEInsuffisantes_refuse_rienNEstReserve() {
        assertThat(code(() -> service.decider("A", new DecisionVisaRequest(StatutVisa.FAVORABLE, null))))
                .isEqualTo(ErrorCode.MONTANT_AE_INSUFFISANT);
        assertThat(acte.getStatut()).isEqualTo(StatutActe.SOUMIS_CONTROLE);
        verify(reservationRepository, never()).save(any());
        verify(visaRepository, never()).save(any());
    }

    @Test
    void rejet_renvoieEnProjetSansReserver() {
        service.decider("A", new DecisionVisaRequest(StatutVisa.DEFAVORABLE_REJET, "Imputation erronée"));

        assertThat(acte.getStatut()).isEqualTo(StatutActe.PROJET);
        assertThat(acte.getCreditBudgetaire()).isNull();
        assertThat(credit.getMontantAEConsomme()).isEqualByComparingTo("0");
        verify(reservationRepository, never()).save(any());
        verify(visaRepository).save(any());
    }

    @Test
    void reserves_laissentLActeSoumis() {
        service.decider("A", new DecisionVisaRequest(StatutVisa.SUSPENDU_RESERVES, "Joindre le devis"));
        assertThat(acte.getStatut()).isEqualTo(StatutActe.SOUMIS_CONTROLE);
        verify(reservationRepository, never()).save(any());
    }

    @Test
    void rejetOuReserves_sansMotif_refuses() {
        assertThat(code(() -> service.decider("A", new DecisionVisaRequest(StatutVisa.DEFAVORABLE_REJET, "  "))))
                .isEqualTo(ErrorCode.VISA_MOTIF_OBLIGATOIRE);
        assertThat(code(() -> service.decider("A", new DecisionVisaRequest(StatutVisa.SUSPENDU_RESERVES, null))))
                .isEqualTo(ErrorCode.VISA_MOTIF_OBLIGATOIRE);
    }

    @Test
    void acteNonSoumis_refuse() {
        acte.setStatut(StatutActe.PROJET);
        assertThat(code(() -> service.decider("A", new DecisionVisaRequest(StatutVisa.FAVORABLE, null))))
                .isEqualTo(ErrorCode.TRANSITION_STATUT_INVALIDE);
    }
}
