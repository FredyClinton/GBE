package gov.cmr.minfi.db.gbe.app.notification.dto;

import gov.cmr.minfi.db.gbe.app.notification.TypeNotification;

import java.time.LocalDateTime;

public record NotificationResponse(
        String id,
        TypeNotification type,
        String titre,
        String message,
        String objetType,
        String objetId,
        LocalDateTime dateCreation,
        LocalDateTime dateLecture,
        boolean lue
) {}
