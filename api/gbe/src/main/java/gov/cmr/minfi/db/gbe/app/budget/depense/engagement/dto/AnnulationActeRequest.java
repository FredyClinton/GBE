package gov.cmr.minfi.db.gbe.app.budget.depense.engagement.dto;

import jakarta.validation.constraints.NotBlank;

/** Annulation d'un acte visé, signé ou notifié (Q2) : le motif est obligatoire. */
public record AnnulationActeRequest(@NotBlank String motif) {}
