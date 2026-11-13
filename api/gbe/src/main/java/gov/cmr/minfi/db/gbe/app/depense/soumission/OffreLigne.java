package gov.cmr.minfi.db.gbe.app.depense.soumission;

import java.math.BigDecimal;

/** Prix proposé par le fournisseur pour une ligne du besoin. */
public record OffreLigne(String ligneBesoinId, BigDecimal prixUnitairePropose, String remarquesTechniques) {
}
