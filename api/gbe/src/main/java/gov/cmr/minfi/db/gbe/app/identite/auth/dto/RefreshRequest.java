package gov.cmr.minfi.db.gbe.app.identite.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record RefreshRequest(
        @NotBlank @Schema(description = "Jeton de rafraîchissement reçu à la connexion") String refreshToken) {
}
