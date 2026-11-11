package gov.cmr.minfi.db.gbe.app.depense.soumission.dto;

import java.math.BigDecimal;

public record LigneSoumissionResponse(
        Integer numeroOrdre,
        String ligneBesoinId,
        String matiereReference,
        String matiereLibelle,
        BigDecimal quantiteDemandee,
        BigDecimal prixUnitairePropose,
        BigDecimal montantTotalLigneHT,
        String remarquesTechniques
) {}
