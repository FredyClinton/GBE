package gov.cmr.minfi.db.gbe.app.notification;

import gov.cmr.minfi.db.gbe.app.common.audit.BaseEntity;
import gov.cmr.minfi.db.gbe.app.identite.user.User;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

/**
 * Message de la boîte de notifications d'un utilisateur (Q20). Écrite dans la transaction de l'événement métier
 * ({@link NotificationListener}). Absente des diagrammes : écart volontaire (plan §4.5, 2d).
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Table(name = "notifications")
public class Notification extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "destinataire_id", nullable = false, foreignKey = @ForeignKey(name = "fk_notification_destinataire"))
    private User destinataire;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 40)
    private TypeNotification type;

    @Column(name = "titre", nullable = false, length = 200)
    private String titre;

    @Column(name = "message", nullable = false, columnDefinition = "TEXT")
    private String message;

    /** Objet concerné, pour que le frontend ouvre la bonne page (ex. {@code ACTE_JURIDIQUE}). */
    @Column(name = "objet_type", length = 40)
    private String objetType;

    @Column(name = "objet_id")
    private String objetId;

    @Column(name = "date_creation", nullable = false)
    private LocalDateTime dateCreation;

    @Column(name = "date_lecture")
    private LocalDateTime dateLecture;

    public boolean isLue() {
        return dateLecture != null;
    }

    /** Sans effet si elle est déjà lue (la première date de lecture est conservée). */
    public void marquerLue(LocalDateTime maintenant) {
        if (dateLecture == null) {
            this.dateLecture = maintenant;
        }
    }
}
