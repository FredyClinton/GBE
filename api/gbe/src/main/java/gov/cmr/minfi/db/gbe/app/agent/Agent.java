package gov.cmr.minfi.db.gbe.app.agent;

import gov.cmr.minfi.db.gbe.app.common.audit.BaseEntity;
import gov.cmr.minfi.db.gbe.app.user.User;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Table(
        name = "AGENT",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_agent_matricule", columnNames = "MATRICULE"),
                @UniqueConstraint(name = "uk_agent_nui", columnNames = "NUI"),
                @UniqueConstraint(name = "uk_agent_cni", columnNames = "NUMERO_CNI"),
                @UniqueConstraint(name = "uk_agent_phone", columnNames = "PHONE_NUMBER")
        }
)
public class Agent extends BaseEntity {
    @Column(name = "FIRST_NAME", length = 50)
    private String firstName;
    @Column(name = "LAST_NAME", length = 50)
    private String lastName;

    @Column(name = "DATE_OF_BIRTH")
    private LocalDate dateOfBirth;

    @Column(name = "MATRICULE", nullable = false)
    private String matricule;

    @Column(name = "NUI", nullable = false)
    private String nui;

    @Column(name = "CNI_NUMBER", nullable = false)
    private String numeroCni;

    @Column(name = "CNI_ISSUE_DATE", nullable = false)
    private LocalDate cniIssueDate;
    @Column(name = "CNI_EXPIRY_DATE", nullable = false)
    private LocalDate cniExpiryDate;

    @Column(name = "PHONE_NUMBER", nullable = false)
    private String phoneNumber;

    @OneToOne(mappedBy = "agent", fetch = FetchType.LAZY)
    private User user;

    @Column(name = "ACTIF", nullable = false)
    @Builder.Default
    private boolean actif = true;
}
