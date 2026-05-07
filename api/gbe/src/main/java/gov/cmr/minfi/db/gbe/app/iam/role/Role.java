package gov.cmr.minfi.db.gbe.app.iam.role;

import gov.cmr.minfi.db.gbe.app.common.audit.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * Entité IAM utilisée par Spring Security.
 * Distinct de RoleSysteme (enum métier).
 * Un User a désormais un seul Role (ManyToOne), donc
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Table(name = "ROLES")
public class Role extends BaseEntity {

    @Column(name = "NAME", nullable = false, unique = true)
    private String name;

}