package gov.cmr.minfi.db.gbe.app.depense.besoin.dto;

import java.math.BigDecimal;

public record LigneBesoinResponse(
        String id,
        Integer numeroOrdre,
        String matiereId,
        String matiereReference,
        String matiereLibelle,
        BigDecimal quantiteDemandee,
        String specifications
) {}
