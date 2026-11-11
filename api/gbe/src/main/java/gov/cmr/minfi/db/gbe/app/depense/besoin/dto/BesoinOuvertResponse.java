package gov.cmr.minfi.db.gbe.app.depense.besoin.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Besoin ouvert aux soumissions, tel que vu par le public et les fournisseurs (UC02) :
 * pas d'identifiant interne de tâche, de programme ni d'exercice. {@code lignes} est absent dans les listes.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record BesoinOuvertResponse(
        String id,
        String reference,
        String objet,
        LocalDateTime datePublication,
        LocalDateTime dateClotureSoumission,
        String sectionLibelle,
        String structureEmettrice,
        String natureEconomiqueCode,
        String natureEconomiqueLibelle,
        List<LigneBesoinResponse> lignes
) {}
