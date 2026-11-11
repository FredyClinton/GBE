package gov.cmr.minfi.db.gbe.app.identite.iam.role;

import gov.cmr.minfi.db.gbe.app.common.exception.BusinessException;
import gov.cmr.minfi.db.gbe.app.common.exception.ErrorCode;
import gov.cmr.minfi.db.gbe.app.identite.mandat.Mandat;
import org.junit.jupiter.api.Test;

import static gov.cmr.minfi.db.gbe.app.identite.iam.permission.Permission.LIQUIDER_DEPENSE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RoleSystemeTest {

    @Test
    void liquidation_releveDeLOrdonnateur() {
        assertThat(RoleSysteme.ORDONNATEUR.getDefaultPermissions()).contains(LIQUIDER_DEPENSE);
        assertThat(RoleSysteme.MINISTRE.getDefaultPermissions()).contains(LIQUIDER_DEPENSE);
        assertThat(RoleSysteme.COMPTABLE.getDefaultPermissions()).doesNotContain(LIQUIDER_DEPENSE);
    }

    @Test
    void seulLeFournisseurEstExterne() {
        for (RoleSysteme role : RoleSysteme.values()) {
            assertThat(role.isExterne()).as(role.name()).isEqualTo(role == RoleSysteme.FOURNISSEUR);
        }
    }

    @Test
    void unRoleExterneNePeutPasPorterDeMandat() {
        final Mandat mandat = Mandat.builder().roleSysteme(RoleSysteme.FOURNISSEUR).build();
        assertThatThrownBy(mandat::initialiserPermissionsDepuisRole)
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.ROLE_EXTERNE_SANS_MANDAT);
    }
}
