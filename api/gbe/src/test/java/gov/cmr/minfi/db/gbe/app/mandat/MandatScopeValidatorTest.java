package gov.cmr.minfi.db.gbe.app.mandat;

import gov.cmr.minfi.db.gbe.app.budget.credit.CreditBudgetaire;
import gov.cmr.minfi.db.gbe.app.common.exception.BusinessException;
import gov.cmr.minfi.db.gbe.app.common.exception.ErrorCode;
import gov.cmr.minfi.db.gbe.app.iam.permission.Permission;
import gov.cmr.minfi.db.gbe.app.iam.role.RoleSysteme;
import gov.cmr.minfi.db.gbe.app.referentiel.administratif.Section;
import gov.cmr.minfi.db.gbe.app.referentiel.programmatique.Programme;
import gov.cmr.minfi.db.gbe.app.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("MandatScopeValidator — Tests unitaires")
class MandatScopeValidatorTest {

    @Mock
    private MandatRepository mandatRepository;

    @InjectMocks
    private MandatScopeValidator scopeValidator;

    private User user;
    private Section sectionMinfi;
    private Section sectionMinesec;
    private Programme progPilotage;
    private Programme progMobilisation;
    private CreditBudgetaire creditPilotage;
    private CreditBudgetaire creditMobilisation;
    private CreditBudgetaire creditMinesec;

    @BeforeEach
    void setUp() {
        // ── User avec ID fixe ─────────────────────────
        user = User.builder()
                .email("test@minfi.cm")
                .password("pwd")
                .build();
        user.setId("user-test-id");  // ← id fixe, sinon user.getId() = null

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(user, null, List.of())
        );

        // ── Sections avec IDs fixes ───────────────────
        sectionMinfi = Section.builder()
                .codeSection("20").sigle("MINFI")
                .libelleFr("Ministère des Finances")
                .libelleEn("Ministry of Finance")
                .build();
        sectionMinfi.setId("section-minfi-id");  // ← id fixe

        sectionMinesec = Section.builder()
                .codeSection("53").sigle("MINESEC")
                .libelleFr("Ministère des Enseignements Secondaires")
                .libelleEn("Ministry of Secondary Education")
                .build();
        sectionMinesec.setId("section-minesec-id");  // ← id fixe

        // ── Programmes avec IDs fixes ─────────────────
        progPilotage = Programme.builder()
                .code("231").libelleFr("Pilotage")
                .section(sectionMinfi).build();
        progPilotage.setId("prog-pilotage-id");  // ← id fixe

        progMobilisation = Programme.builder()
                .code("232").libelleFr("Mobilisation")
                .section(sectionMinfi).build();
        progMobilisation.setId("prog-mobilisation-id");  // ← id fixe

        // ── Programme MINESEC avec ID fixe ────────────
        final Programme progMinesec = Programme.builder()
                .code("150").section(sectionMinesec).build();
        progMinesec.setId("prog-minesec-id");  // ← id fixe — extrait de l'inline

        // ── Crédits ───────────────────────────────────
        creditPilotage = CreditBudgetaire.builder()
                .section(sectionMinfi)
                .programme(progPilotage)
                .build();

        creditMobilisation = CreditBudgetaire.builder()
                .section(sectionMinfi)
                .programme(progMobilisation)
                .build();

        creditMinesec = CreditBudgetaire.builder()
                .section(sectionMinesec)
                .programme(progMinesec)  // ← variable extraite avec id fixe
                .build();
    }

    // ── Helpers fixtures ──────────────────────────────

    private Mandat mandatOrdonnateur(Section section, Programme programme) {
        final Mandat mandat = Mandat.builder()
                .user(user)
                .section(section)
                .programme(programme)
                .roleSysteme(RoleSysteme.ORDONNATEUR)
                .actif(true)
                .build();
        mandat.initialiserPermissionsDepuisRole();
        return mandat;
    }

    private Mandat mandatMinistre(Section section) {
        final Mandat mandat = Mandat.builder()
                .user(user)
                .section(section)
                .programme(null)
                .roleSysteme(RoleSysteme.MINISTRE)
                .actif(true)
                .build();
        mandat.initialiserPermissionsDepuisRole();
        return mandat;
    }

    // ── Tests ─────────────────────────────────────────

    @Nested
    @DisplayName("verifierAccesCredit()")
    class VerifierAccesCreditTests {

        @Test
        @DisplayName("ORDONNATEUR avec mandat sur le programme → accès autorisé")
        void ordonnateur_mandatSurLeProgramme_accesAutorise() {
            final Mandat mandat = mandatOrdonnateur(sectionMinfi, progPilotage);
            when(mandatRepository.findMandatsValidesParUser(any()))  // ← any()
                    .thenReturn(List.of(mandat));

            assertThatCode(() -> scopeValidator.checkAccessCredit(creditPilotage))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("ORDONNATEUR sans mandat sur le programme → accès refusé")
        void ordonnateur_sansMandatSurLeProgramme_accesRefuse() {
            final Mandat mandat = mandatOrdonnateur(sectionMinfi, progPilotage);
            when(mandatRepository.findMandatsValidesParUser(any()))  // ← any()
                    .thenReturn(List.of(mandat));

            assertThatThrownBy(
                    () -> scopeValidator.checkAccessCredit(creditMobilisation))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(ex -> assertThat(
                            ((BusinessException) ex).getErrorCode())
                            .isEqualTo(ErrorCode.ACCES_REFUSE_HORS_SCOPE));
        }

        @Test
        @DisplayName("ORDONNATEUR avec mandat MINFI → ne voit pas les crédits MINESEC")
        void ordonnateur_mandatMinfi_nepasVoirMinesec() {
            final Mandat mandat = mandatOrdonnateur(sectionMinfi, progPilotage);
            when(mandatRepository.findMandatsValidesParUser(any()))  // ← any()
                    .thenReturn(List.of(mandat));

            assertThatThrownBy(
                    () -> scopeValidator.checkAccessCredit(creditMinesec))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(ex -> assertThat(
                            ((BusinessException) ex).getErrorCode())
                            .isEqualTo(ErrorCode.ACCES_REFUSE_HORS_SCOPE));
        }

        @Test
        @DisplayName("MINISTRE avec mandat section MINFI → accès à tous les programmes MINFI")
        void ministre_mandatSection_accesTousProgrammes() {
            final Mandat mandatMinistre = mandatMinistre(sectionMinfi);
            when(mandatRepository.findMandatsValidesParUser(any()))  // ← any()
                    .thenReturn(List.of(mandatMinistre));

            assertThatCode(() -> scopeValidator.checkAccessCredit(creditPilotage))
                    .doesNotThrowAnyException();

            assertThatCode(() -> scopeValidator.checkAccessCredit(creditMobilisation))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("MINISTRE MINFI → ne voit pas les crédits MINESEC")
        void ministre_mandatMinfi_nepasVoirMinesec() {
            final Mandat mandatMinistre = mandatMinistre(sectionMinfi);
            when(mandatRepository.findMandatsValidesParUser(any()))  // ← any()
                    .thenReturn(List.of(mandatMinistre));

            assertThatThrownBy(
                    () -> scopeValidator.checkAccessCredit(creditMinesec))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(ex -> assertThat(
                            ((BusinessException) ex).getErrorCode())
                            .isEqualTo(ErrorCode.ACCES_REFUSE_HORS_SCOPE));
        }

        @Test
        @DisplayName("Aucun mandat actif → accès refusé")
        void aucunMandat_accesRefuse() {
            when(mandatRepository.findMandatsValidesParUser(any()))  // ← any()
                    .thenReturn(List.of());

            assertThatThrownBy(
                    () -> scopeValidator.checkAccessCredit(creditPilotage))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(ex -> assertThat(
                            ((BusinessException) ex).getErrorCode())
                            .isEqualTo(ErrorCode.ACCES_REFUSE_AUCUN_MANDAT));
        }
    }

    @Nested
    @DisplayName("verifierAccesCreditAvecPermission()")
    class VerifierAccesCreditAvecPermissionTests {

        @Test
        @DisplayName("Mandat avec permission REVISER_AE → accès autorisé")
        void mandatAvecPermission_accesAutorise() {
            final Mandat mandat = mandatOrdonnateur(sectionMinfi, progPilotage);
            when(mandatRepository.findMandatsValidesParUser(any()))  // ← any()
                    .thenReturn(List.of(mandat));

            assertThatCode(() -> scopeValidator.checkAccessCreditWithPermission(
                    creditPilotage, Permission.REVISER_AE))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Mandat sans permission VISA_CFI → accès refusé")
        void mandatSansPermission_accesRefuse() {
            final Mandat mandat = mandatOrdonnateur(sectionMinfi, progPilotage);
            when(mandatRepository.findMandatsValidesParUser(any()))  // ← any()
                    .thenReturn(List.of(mandat));

            assertThatThrownBy(() -> scopeValidator.checkAccessCreditWithPermission(
                    creditPilotage, Permission.VISA_CFI))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(ex -> assertThat(
                            ((BusinessException) ex).getErrorCode())
                            .isEqualTo(ErrorCode.ACCES_REFUSE_HORS_SCOPE));
        }
    }
}