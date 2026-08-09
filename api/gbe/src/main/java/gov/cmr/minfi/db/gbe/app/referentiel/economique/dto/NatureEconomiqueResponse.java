package gov.cmr.minfi.db.gbe.app.referentiel.economique.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record NatureEconomiqueResponse(
        String id,
        String code,
        String codeTitre,
        String codeArticle,
        String codeParagraphe,
        String codeRubrique,
        String libelleFr,
        String libelleEn
) {}
