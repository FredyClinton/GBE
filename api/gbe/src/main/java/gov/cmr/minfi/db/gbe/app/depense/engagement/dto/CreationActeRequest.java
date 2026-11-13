package gov.cmr.minfi.db.gbe.app.depense.engagement.dto;

import gov.cmr.minfi.db.gbe.app.depense.engagement.TypeActe;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record CreationActeRequest(
        @NotBlank @Schema(description = "Besoin attribué dont l'acte est issu") String besoinId,
        @Schema(description = "Forme de l'acte ; BON_DE_COMMANDE par défaut, seule forme disponible pour l'instant")
        TypeActe type,
        @Positive @Schema(description = "Délai de livraison en mois ; par défaut, délai de l'offre en jours ÷ 30, arrondi au supérieur")
        Integer delaiLivraisonMois,
        @Size(max = 100) @Schema(description = "BCA : référence du devis du fournisseur, facultative") String referenceDevis
) {
}
