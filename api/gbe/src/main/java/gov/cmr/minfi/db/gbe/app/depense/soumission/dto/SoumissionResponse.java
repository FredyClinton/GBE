package gov.cmr.minfi.db.gbe.app.depense.soumission.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import gov.cmr.minfi.db.gbe.app.depense.soumission.StatutSoumission;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/** Soumission et récépissé (numéro, horodatage, empreinte) ; {@code lignes} est absent dans les listes. */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record SoumissionResponse(
        String id,
        String numero,
        StatutSoumission statut,
        LocalDateTime dateDecision,
        LocalDateTime dateSoumission,
        LocalDateTime dateDerniereModification,
        String empreinte,
        String fournisseurId,
        String fournisseurRaisonSociale,
        String fournisseurNiu,
        String besoinId,
        String besoinReference,
        String besoinObjet,
        LocalDateTime dateClotureSoumission,
        String coordonneeBancaireId,
        String rib,
        Integer delaiProposeJours,
        BigDecimal montantProposeHT,
        BigDecimal tauxTva,
        BigDecimal montantTva,
        BigDecimal montantProposeTTC,
        BigDecimal tauxIr,
        BigDecimal montantIr,
        BigDecimal montantNetAPayer,
        List<LigneSoumissionResponse> lignes
) {}
