package gov.cmr.minfi.db.gbe.app.mandat;

import gov.cmr.minfi.db.gbe.app.common.audit.BaseEntity;
import gov.cmr.minfi.db.gbe.app.iam.permission.Permission;
import gov.cmr.minfi.db.gbe.app.iam.role.RoleSysteme;
import gov.cmr.minfi.db.gbe.app.referentiel.administratif.Section;
import gov.cmr.minfi.db.gbe.app.referentiel.programmatique.Programme;
import gov.cmr.minfi.db.gbe.app.user.User;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Table(
        name = "MANDAT",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_mandat_user_programme",
                columnNames = {"USER_ID", "PROGRAMME_ID"}
        )
)
public class Mandat extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_ID", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "SECTION_ID", nullable = false)
    private Section section;

    // Nullable — un admin peut être affecté à toute une section
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PROGRAMME_ID")
    private Programme programme;

    @Enumerated(EnumType.STRING)
    @Column(name = "ROLE_SYSTEME", nullable = false)
    private RoleSysteme roleSysteme;

    // Permissions effectives sur ce programme
    // Peuvent être surchargées par l'admin
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "MANDAT_PERMISSIONS",
            joinColumns = @JoinColumn(name = "MANDAT_ID")
    )
    @Enumerated(EnumType.STRING)
    @Column(name = "PERMISSION")
    @Builder.Default
    private Set<Permission> permissions = new HashSet<>();

    @Column(name = "START_MANDAT")
    private LocalDate dateDebut;

    @Column(name = "END_MANDAT")
    private LocalDate dateFin;

    @Column(name = "NUMERO_DECISION", length = 100)
    private String numeroDecision;

    @Column(name = "ACTIF", nullable = false)
    @Builder.Default
    private boolean actif = true;

    // Appelée par AdminService à la création
    public void initialiserPermissionsDepuisRole() {
        this.permissions = new HashSet<>(this.roleSysteme.getDefaultPermissions());
    }

    // Vérifie si cette mandat donne accès à une permission donnée
    public boolean hasPermission(Permission permission) {
        return this.actif && this.permissions.contains(permission);
    }

    public boolean isValide() {
        if (!this.actif) {
            return false;
        }
        final LocalDate today = LocalDate.now();
        if (this.dateDebut != null && today.isBefore(this.dateDebut)) {
            return false;
        }
        if (this.dateFin != null && today.isAfter(this.dateFin)) {
            return false;
        }
        return true;
    }

    public boolean isSectionOnly() {
        return this.section == null;
    }
}