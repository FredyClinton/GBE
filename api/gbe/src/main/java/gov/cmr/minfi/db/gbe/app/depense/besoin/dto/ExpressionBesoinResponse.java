package gov.cmr.minfi.db.gbe.app.depense.besoin.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import gov.cmr.minfi.db.gbe.app.depense.besoin.StatutExpression;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/** Détail d'une expression de besoin ; {@code lignes} est absent dans les listes. */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ExpressionBesoinResponse(
        String id,
        String reference,
        String objet,
        StatutExpression statut,
        LocalDate dateExpression,
        LocalDateTime dateClotureSoumission,
        LocalDateTime datePublication,
        LocalDateTime dateClotureEffective,
        LocalDateTime dateDecision,
        String motifAnnulation,
        String tacheId,
        String tacheCode,
        String tacheLibelle,
        String programmeId,
        String sectionId,
        String exerciceId,
        String chapitreId,
        String chapitreCode,
        String natureEconomiqueId,
        String natureEconomiqueCode,
        String natureEconomiqueLibelle,
        List<LigneBesoinResponse> lignes
) {}
