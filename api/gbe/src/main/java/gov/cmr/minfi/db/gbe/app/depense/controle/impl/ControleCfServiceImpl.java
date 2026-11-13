package gov.cmr.minfi.db.gbe.app.depense.controle.impl;

import gov.cmr.minfi.db.gbe.app.common.exception.BusinessException;
import gov.cmr.minfi.db.gbe.app.common.exception.ErrorCode;
import gov.cmr.minfi.db.gbe.app.common.reference.ReferenceGenerator;
import gov.cmr.minfi.db.gbe.app.depense.besoin.ExpressionBesoin;
import gov.cmr.minfi.db.gbe.app.depense.controle.*;
import gov.cmr.minfi.db.gbe.app.depense.controle.dto.DecisionVisaRequest;
import gov.cmr.minfi.db.gbe.app.depense.controle.dto.VisaResponse;
import gov.cmr.minfi.db.gbe.app.depense.engagement.ActeJuridique;
import gov.cmr.minfi.db.gbe.app.depense.engagement.ActeJuridiqueRepository;
import gov.cmr.minfi.db.gbe.app.depense.engagement.ReservationCredit;
import gov.cmr.minfi.db.gbe.app.depense.engagement.ReservationCreditRepository;
import gov.cmr.minfi.db.gbe.app.identite.iam.permission.Permission;
import gov.cmr.minfi.db.gbe.app.identite.mandat.MandatScopeValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Contrôle du CF (UC06, UC07, UC08) en une seule validation transactionnelle (Q1), isolée ici pour pouvoir passer
 * plus tard à trois étapes. Rien n'est réservé avant le visa favorable (Q2).
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ControleCfServiceImpl implements ControleCfService {

    private final ActeJuridiqueRepository acteRepository;
    private final VisaRepository visaRepository;
    private final ReservationCreditRepository reservationRepository;
    private final MandatScopeValidator mandatScopeValidator;
    private final ReferenceGenerator referenceGenerator;
    private final VisaMapper visaMapper;
    private final Clock clock;

    @Override
    @Transactional
    public VisaResponse decider(String acteId, DecisionVisaRequest request) {
        final ActeJuridique acte = findActe(acteId);
        final ExpressionBesoin besoin = acte.getExpressionBesoin();
        verifierPermission(besoin, Permission.VISA_CFI);
        acte.verifierSoumisAuControle();
        final boolean favorable = request.decision() == StatutVisa.FAVORABLE;
        final String observations = request.observations() == null || request.observations().isBlank()
                ? null : request.observations().trim();
        if (!favorable && observations == null) {
            throw new BusinessException(ErrorCode.VISA_MOTIF_OBLIGATOIRE, request.decision());
        }

        final LocalDateTime maintenant = LocalDateTime.now(clock);
        final int annee = besoin.getExercice().getAnnee();
        final Visa visa = Visa.builder()
                .numero(referenceGenerator.prochaineReference("VISA", annee))
                .typeVisa(TypeVisa.ENGAGEMENT_JURIDIQUE)
                .dateReception(acte.getDateSoumissionControle())
                .dateVisa(maintenant)
                .decision(request.decision())
                .observations(observations)
                .acteJuridique(acte)
                .build();

        switch (request.decision()) {
            case FAVORABLE -> {
                // UC07 et UC08 sont inclus dans le visa : le CF doit aussi pouvoir réserver et donner le quitus
                verifierPermission(besoin, Permission.RESERVER_CREDIT);
                verifierPermission(besoin, Permission.QUITUS_NATURE);
                if (acte.getCreditBudgetaire() == null) {
                    throw new BusinessException(ErrorCode.CREDIT_IMPUTATION_INTROUVABLE, besoin.getReference());
                }
                // UC07 : AE consommées pour le TTC ; refus si la ligne est bloquée ou insuffisante (le CF rejette alors)
                reservationRepository.save(ReservationCredit.bloquer(referenceGenerator.prochaineReference("RES", annee),
                        acte, acte.getCreditBudgetaire(), maintenant));
                // UC08 : quitus sur le classement en nature économique, porté par le visa (Q19)
                visa.setReferenceQuitus(referenceGenerator.prochaineReference("QUI", annee));
                acte.viser();
            }
            case DEFAVORABLE_REJET -> acte.renvoyerEnProjet();
            case SUSPENDU_RESERVES -> { /* l'acte reste SOUMIS_CONTROLE, en attente de compléments (Q18) */ }
        }
        visaRepository.save(visa);
        log.info("Visa {} ({}) sur l'acte {}", visa.getNumero(), visa.getDecision(), acte.getNumero());
        return visaMapper.toResponse(visa);
    }

    @Override
    public List<VisaResponse> getVisas(String acteId) {
        final ActeJuridique acte = findActe(acteId);
        verifierPermission(acte.getExpressionBesoin(), null);
        return visaRepository.findByActeJuridiqueIdOrderByDateVisaAscNumeroAsc(acteId).stream().map(visaMapper::toResponse).toList();
    }

    private ActeJuridique findActe(String acteId) {
        return acteRepository.findById(acteId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ACTE_NOT_FOUND, acteId));
    }

    private void verifierPermission(ExpressionBesoin besoin, Permission permission) {
        mandatScopeValidator.checkAccessPerimetre(besoin.getSection().getId(), besoin.getProgramme().getId(), permission);
    }
}
