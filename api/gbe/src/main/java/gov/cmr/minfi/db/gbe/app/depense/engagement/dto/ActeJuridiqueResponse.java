package gov.cmr.minfi.db.gbe.app.depense.engagement.dto;

import gov.cmr.minfi.db.gbe.app.depense.engagement.StatutActe;
import gov.cmr.minfi.db.gbe.app.depense.engagement.TypeActe;
import gov.cmr.minfi.db.gbe.app.depense.soumission.dto.LigneSoumissionResponse;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/** Contenu du BCA (plan §4.5) ; {@code lignes} est absent dans les listes. */
public record ActeJuridiqueResponse(
        String id,
        String numero,
        TypeActe type,
        StatutActe statut,
        String objet,
        LocalDate dateEmission,
        Integer delaiLivraisonMois,
        LocalDateTime dateSoumissionControle,
        // Montants
        BigDecimal montantTotalHT,
        BigDecimal tauxTva,
        BigDecimal montantTva,
        BigDecimal tauxIr,
        BigDecimal montantIr,
        BigDecimal montantTTC,
        BigDecimal montantNetAPayer,
        String montantEnLettres,
        // Besoin et offre
        String besoinId,
        String besoinReference,
        String soumissionId,
        String soumissionNumero,
        // Structure émettrice et imputation
        String structureEmettriceCode,
        String structureEmettriceLibelle,
        String exerciceCode,
        String sectionCode,
        String programmeNumero,
        String programmeLibelle,
        String actionNumero,
        String actionLibelle,
        String activiteCode,
        String activiteLibelle,
        String tacheCode,
        String tacheLibelle,
        String natureEconomiqueCode,
        String natureEconomiqueLibelle,
        String creditBudgetaireId,
        // Fournisseur
        String fournisseurId,
        String fournisseurNiu,
        String fournisseurRaisonSociale,
        String fournisseurAdresse,
        String fournisseurTelephone,
        String rib,
        List<LigneSoumissionResponse> lignes
) {
}
