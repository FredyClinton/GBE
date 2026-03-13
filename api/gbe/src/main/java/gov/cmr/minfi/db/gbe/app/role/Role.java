package gov.cmr.minfi.db.gbe.app.role;


import gov.cmr.minfi.db.gbe.app.common.BaseEntity;
import gov.cmr.minfi.db.gbe.app.user.User;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Table(name = "ROLES")
public class Role extends BaseEntity {

    private String name;

    @ManyToMany(mappedBy = "roles")
    private List<User> users;
}
