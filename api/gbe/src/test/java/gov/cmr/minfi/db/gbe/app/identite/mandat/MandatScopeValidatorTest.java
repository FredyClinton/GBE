package gov.cmr.minfi.db.gbe.app.identite.mandat;

import gov.cmr.minfi.db.gbe.app.budget.credit.CreditBudgetaire;
import gov.cmr.minfi.db.gbe.app.common.exception.BusinessException;
import gov.cmr.minfi.db.gbe.app.common.exception.ErrorCode;
import gov.cmr.minfi.db.gbe.app.identite.iam.permission.Permission;
import gov.cmr.minfi.db.gbe.app.identite.iam.role.RoleSysteme;
import gov.cmr.minfi.db.gbe.app.identite.user.User;
import gov.cmr.minfi.db.gbe.app.referentiel.administratif.Section;
import gov.cmr.minfi.db.gbe.app.referentiel.programmatique.Programme;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MandatScopeValidatorTest {

    private static final String USER_ID = "user-1";

    private final Section section = Section.builder().id("S1").build();
    private final Section autreSection = Section.builder().id("S2").build();
    private final Programme programme = Programme.builder().id("P1").build();
    private final Programme autreProgramme = Programme.builder().id("P2").build();

    @Mock
    private MandatRepository mandatRepository;

    @InjectMocks
    private MandatScopeValidator validator;

    @BeforeEach
    void connecterUtilisateur() {
        final User user = User.builder().id(USER_ID).build();
        SecurityContextHolder.getContext()
                .setAuthentication(new UsernamePasswordAuthenticationToken(user, null, List.of()));
    }

    @AfterEach
    void deconnecter() {
        SecurityContextHolder.clearContext();
    }

    private void mandats(Mandat... mandats) {
        when(mandatRepository.findMandatsValidesParUser(USER_ID)).thenReturn(List.of(mandats));
    }

    private static Mandat mandat(RoleSysteme role, Section section, Programme programme, Permission... permissions) {
        return Mandat.builder()
                .roleSysteme(role)
                .section(section)
                .programme(programme)
                .actif(true)
                .permissions(new HashSet<>(Arrays.asList(permissions)))
                .build();
    }

    private CreditBudgetaire credit(Programme programme) {
        return CreditBudgetaire.builder().id("C1").section(section).programme(programme).build();
    }

    private static void assertRefus(Runnable action, ErrorCode attendu) {
        assertThatThrownBy(action::run)
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(attendu);
    }

    // ── checkAccessCredit ─────────────────────────────

    @Test
    void sansMandat_accesRefuse() {
        mandats();
        assertRefus(() -> validator.checkAccessCredit(credit(programme)), ErrorCode.ACCES_REFUSE_AUCUN_MANDAT);
    }

    @Test
    void mandatSurLeProgrammeDuCredit_accesAutorise() {
        mandats(mandat(RoleSysteme.ORDONNATEUR, section, programme));
        assertThatCode(() -> validator.checkAccessCredit(credit(programme))).doesNotThrowAnyException();
    }

    @Test
    void mandatSurUnAutreProgramme_accesRefuse() {
        mandats(mandat(RoleSysteme.ORDONNATEUR, section, autreProgramme));
        assertRefus(() -> validator.checkAccessCredit(credit(programme)), ErrorCode.ACCES_REFUSE_HORS_SCOPE);
    }

    @Test
    void mandatSurUneAutreSection_accesRefuse() {
        mandats(mandat(RoleSysteme.MINISTRE, autreSection, null));
        assertRefus(() -> validator.checkAccessCredit(credit(programme)), ErrorCode.ACCES_REFUSE_HORS_SCOPE);
    }

    @Test
    void ministreSansProgramme_couvreToutLaSection() {
        mandats(mandat(RoleSysteme.MINISTRE, section, null));
        assertThatCode(() -> validator.checkAccessCredit(credit(programme))).doesNotThrowAnyException();
        assertThatCode(() -> validator.checkAccessCredit(credit(autreProgramme))).doesNotThrowAnyException();
    }

    @Test
    void autreRoleSansProgramme_neCouvrePasLaSection() {
        mandats(mandat(RoleSysteme.ORDONNATEUR, section, null));
        assertRefus(() -> validator.checkAccessCredit(credit(programme)), ErrorCode.ACCES_REFUSE_HORS_SCOPE);
    }

    // ── checkAccessCreditWithPermission ───────────────

    @Test
    void permissionPresenteSurLeMandatDuScope_accesAutorise() {
        mandats(mandat(RoleSysteme.ORDONNATEUR, section, programme, Permission.REVISER_AE));
        assertThatCode(() -> validator.checkAccessCreditWithPermission(credit(programme), Permission.REVISER_AE))
                .doesNotThrowAnyException();
    }

    @Test
    void permissionAbsente_accesRefuse() {
        mandats(mandat(RoleSysteme.ORDONNATEUR, section, programme, Permission.REVISER_CP));
        assertRefus(() -> validator.checkAccessCreditWithPermission(credit(programme), Permission.REVISER_AE),
                ErrorCode.ACCES_REFUSE_HORS_SCOPE);
    }

    @Test
    void permissionPorteeParUnMandatHorsScope_neComptePas() {
        mandats(
                mandat(RoleSysteme.ORDONNATEUR, section, programme),
                mandat(RoleSysteme.ORDONNATEUR, section, autreProgramme, Permission.REVISER_AE)
        );
        assertRefus(() -> validator.checkAccessCreditWithPermission(credit(programme), Permission.REVISER_AE),
                ErrorCode.ACCES_REFUSE_HORS_SCOPE);
    }

    // ── checkSectionAccess / getProgrammeIdsAutorises ─

    @Test
    void checkSectionAccess() {
        mandats(mandat(RoleSysteme.ORDONNATEUR, section, programme));
        assertThatCode(() -> validator.checkSectionAccess("S1")).doesNotThrowAnyException();
        assertRefus(() -> validator.checkSectionAccess("S2"), ErrorCode.ACCES_REFUSE_HORS_SCOPE);
    }

    @Test
    void programmesAutorises_neRetientQueLaSectionDemandee() {
        mandats(
                mandat(RoleSysteme.ORDONNATEUR, section, programme),
                mandat(RoleSysteme.ORDONNATEUR, autreSection, autreProgramme),
                mandat(RoleSysteme.GESTIONNAIRE, section, null)
        );
        assertThat(validator.getProgrammeIdsAutorises("S1")).containsExactly("P1");
    }

    @Test
    void programmesAutorises_ministreSansProgramme_contientNull() {
        mandats(mandat(RoleSysteme.MINISTRE, section, null));
        final Set<String> ids = validator.getProgrammeIdsAutorises("S1");
        assertThat(ids).containsExactly((String) null);
    }
}
