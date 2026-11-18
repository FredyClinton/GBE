package gov.cmr.minfi.db.gbe.app.notification;

import gov.cmr.minfi.db.gbe.app.common.config.OpenApiTags;
import gov.cmr.minfi.db.gbe.app.common.pagination.PageResponse;
import gov.cmr.minfi.db.gbe.app.notification.dto.CompteurNotificationsResponse;
import gov.cmr.minfi.db.gbe.app.notification.dto.NotificationResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/** Boîte de notifications de l'utilisateur connecté (agent ou fournisseur). */
@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
@Tag(name = OpenApiTags.NOTIFICATIONS)
public class NotificationController {

    private final NotificationService notificationService;

    @Operation(
        summary = "Lister ses notifications",
        description = "Retourne les notifications de l'utilisateur connecté, les plus récentes d'abord. " +
            "Filtre facultatif : seulement les non lues. Tri possible sur dateCreation (par défaut, décroissant) et type."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Page de notifications"),
        @ApiResponse(responseCode = "400", description = "Propriété de tri non autorisée (TRI_INVALIDE)"),
        @ApiResponse(responseCode = "403", description = "Utilisateur non connecté")
    })
    @GetMapping
    public ResponseEntity<PageResponse<NotificationResponse>> mesNotifications(
            @Parameter(description = "Filtre facultatif : true pour ne garder que les non lues")
            @RequestParam(defaultValue = "false") boolean nonLues,
            @ParameterObject Pageable pageable) {
        return ResponseEntity.ok(notificationService.mesNotifications(nonLues, pageable));
    }

    @Operation(
        summary = "Compter ses notifications non lues",
        description = "Retourne le nombre de notifications non lues, pour le badge du frontend. " +
            "Pas de temps réel pour l'instant : le frontend interroge cette route à intervalles réguliers."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Nombre de notifications non lues"),
        @ApiResponse(responseCode = "403", description = "Utilisateur non connecté")
    })
    @GetMapping("/non-lues/nombre")
    public ResponseEntity<CompteurNotificationsResponse> compterNonLues() {
        return ResponseEntity.ok(notificationService.compterNonLues());
    }

    @Operation(
        summary = "Marquer une notification comme lue",
        description = "Enregistre la date de lecture ; sans effet si elle est déjà lue. " +
            "La notification d'un autre utilisateur est introuvable."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Notification lue"),
        @ApiResponse(responseCode = "403", description = "Utilisateur non connecté"),
        @ApiResponse(responseCode = "404", description = "Notification introuvable (NOTIFICATION_NOT_FOUND)")
    })
    @PatchMapping("/{notificationId}/lue")
    public ResponseEntity<NotificationResponse> marquerLue(
            @Parameter(description = "Identifiant de la notification", required = true) @PathVariable String notificationId) {
        return ResponseEntity.ok(notificationService.marquerLue(notificationId));
    }

    @Operation(
        summary = "Marquer toutes ses notifications comme lues",
        description = "Enregistre la date de lecture de toutes les notifications non lues de l'utilisateur connecté."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Notifications marquées comme lues"),
        @ApiResponse(responseCode = "403", description = "Utilisateur non connecté")
    })
    @PatchMapping("/lues")
    public ResponseEntity<Void> marquerToutesLues() {
        notificationService.marquerToutesLues();
        return ResponseEntity.noContent().build();
    }
}
