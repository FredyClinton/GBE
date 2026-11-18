package gov.cmr.minfi.db.gbe.app.budget.depense.besoin.dto;

import jakarta.validation.constraints.NotBlank;

public record AnnulationBesoinRequest(@NotBlank String motif) {}
