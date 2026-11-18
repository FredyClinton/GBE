package gov.cmr.minfi.db.gbe.app.notification;

import gov.cmr.minfi.db.gbe.app.budget.depense.controle.DecisionCfEvent;
import gov.cmr.minfi.db.gbe.app.budget.depense.engagement.ActeAnnuleEvent;
import gov.cmr.minfi.db.gbe.app.budget.depense.engagement.ActeNotifieEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Transforme les événements métier en notifications. Écoute synchrone : la notification est écrite dans la
 * transaction de l'opération (signature, annulation, visa) et disparaît avec elle en cas d'échec. Un canal temps
 * réel (SSE) ou un envoi d'e-mail se brancheront ici plus tard, après validation de la transaction.
 */
@Component
@RequiredArgsConstructor
public class NotificationListener {

    static final String OBJET_ACTE = "ACTE_JURIDIQUE";

    private final NotificationService notificationService;

    @EventListener
    public void acteNotifie(ActeNotifieEvent e) {
        notificationService.notifier(e.fournisseurUserId(), TypeNotification.ACTE_NOTIFIE,
                "Bon de commande " + e.numero() + " notifié",
                "L'acte " + e.numero() + " (" + e.objet() + ") est signé : vous pouvez commencer l'exécution.",
                OBJET_ACTE, e.acteId());
    }

    @EventListener
    public void acteAnnule(ActeAnnuleEvent e) {
        notificationService.notifier(e.fournisseurUserId(), TypeNotification.ACTE_ANNULE,
                "Bon de commande " + e.numero() + " annulé",
                "L'acte " + e.numero() + " est annulé. Motif : " + e.motif(),
                OBJET_ACTE, e.acteId());
    }

    @EventListener
    public void decisionCf(DecisionCfEvent e) {
        final String titre = switch (e.decision()) {
            case FAVORABLE -> "Acte " + e.numero() + " visé par le CF";
            case DEFAVORABLE_REJET -> "Acte " + e.numero() + " rejeté par le CF";
            case SUSPENDU_RESERVES -> "Réserves du CF sur l'acte " + e.numero();
        };
        final String message = switch (e.decision()) {
            case FAVORABLE -> "Visa favorable : les AE sont réservées, l'acte peut être signé.";
            case DEFAVORABLE_REJET -> "L'acte revient en projet pour correction. Motif : " + e.observations();
            case SUSPENDU_RESERVES -> "L'acte reste au contrôle, en attente de compléments : " + e.observations();
        };
        notificationService.notifier(e.ordonnateurUserId(), TypeNotification.DECISION_CF, titre, message,
                OBJET_ACTE, e.acteId());
    }
}
