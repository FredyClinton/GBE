package gov.cmr.minfi.db.gbe.app.depense.besoin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Création ou modification d'une expression de besoin. Les lignes envoyées remplacent les lignes existantes.
 * Aucun prix (règle actée). Exercice, section et programme sont déduits de la tâche.
 */
public record ExpressionBesoinRequest(
        @NotBlank String objet,
        @Schema(description = "Date et heure limites de soumission ; obligatoires et futures pour publier",
                example = "2026-11-30T12:00:00")
        LocalDateTime dateClotureSoumission,
        @NotBlank String tacheId,
        @NotBlank @Schema(description = "Structure émettrice, dans la section de la tâche") String chapitreId,
        @NotBlank String natureEconomiqueId,
        @Valid List<LigneBesoinRequest> lignes
) {}
