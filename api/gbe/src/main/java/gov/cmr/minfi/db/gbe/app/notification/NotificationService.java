package gov.cmr.minfi.db.gbe.app.notification;

import gov.cmr.minfi.db.gbe.app.common.pagination.PageResponse;
import gov.cmr.minfi.db.gbe.app.notification.dto.CompteurNotificationsResponse;
import gov.cmr.minfi.db.gbe.app.notification.dto.NotificationResponse;
import org.springframework.data.domain.Pageable;

/** Boîte de notifications de l'utilisateur connecté ; l'écriture passe par {@link #notifier}. */
public interface NotificationService {

    /**
     * Dépose une notification dans la boîte du destinataire, dans la transaction en cours. Ignorée si le destinataire
     * n'est pas un compte existant (ex. objet créé par « SYSTEM »).
     */
    void notifier(String destinataireUserId, TypeNotification type, String titre, String message,
                  String objetType, String objetId);

    PageResponse<NotificationResponse> mesNotifications(boolean nonLuesSeulement, Pageable pageable);

    CompteurNotificationsResponse compterNonLues();

    NotificationResponse marquerLue(String notificationId);

    /** @return le nombre de notifications passées à lues */
    int marquerToutesLues();
}
