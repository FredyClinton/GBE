package gov.cmr.minfi.db.gbe.app.budget.credit.impl;

import gov.cmr.minfi.db.gbe.app.budget.credit.CreditBudgetaire;
import gov.cmr.minfi.db.gbe.app.budget.credit.CreditBudgetaireMapper;
import gov.cmr.minfi.db.gbe.app.budget.credit.CreditBudgetaireRepository;
import gov.cmr.minfi.db.gbe.app.budget.credit.dto.CreateCreditRequest;
import gov.cmr.minfi.db.gbe.app.common.exception.BusinessException;
import gov.cmr.minfi.db.gbe.app.common.exception.ErrorCode;
import gov.cmr.minfi.db.gbe.app.referentiel.administratif.Chapitre;
import gov.cmr.minfi.db.gbe.app.referentiel.administratif.ChapitreRepository;
import gov.cmr.minfi.db.gbe.app.referentiel.administratif.Section;
import gov.cmr.minfi.db.gbe.app.referentiel.administratif.SectionRepository;
import gov.cmr.minfi.db.gbe.app.referentiel.economique.NatureEconomique;
import gov.cmr.minfi.db.gbe.app.referentiel.economique.NatureEconomiqueRepository;
import gov.cmr.minfi.db.gbe.app.referentiel.exercice.Exercice;
import gov.cmr.minfi.db.gbe.app.referentiel.exercice.ExerciceRepository;
import gov.cmr.minfi.db.gbe.app.referentiel.programmatique.Action;
import gov.cmr.minfi.db.gbe.app.referentiel.programmatique.ActionRepository;
import gov.cmr.minfi.db.gbe.app.referentiel.programmatique.Programme;
import gov.cmr.minfi.db.gbe.app.referentiel.programmatique.ProgrammeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CreditBudgetaireServiceImplTest {

    @Mock private ChapitreRepository chapitreRepository;
    @Mock private ActionRepository actionRepository;
    @Mock private ProgrammeRepository programmeRepository;
    @Mock private SectionRepository sectionRepository;
    @Mock private ExerciceRepository exerciceRepository;
    @Mock private NatureEconomiqueRepository natureEconomiqueRepository;
    @Mock private CreditBudgetaireRepository creditBudgetaireRepository;
    @Mock private CreditBudgetaireMapper creditBudgetaireMapper;

    @InjectMocks
    private CreditBudgetaireServiceImpl service;

    private final Exercice exercice = Exercice.builder().id("E2026").codeExercice(60).build();
    private final Exercice autreExercice = Exercice.builder().id("E2025").codeExercice(59).build();
    private final Section section = Section.builder().id("S").codeSection("20").build();
    private final Programme programme = Programme.builder().id("P").code("200001").section(section).build();
    private final Action action = Action.builder().id("A").codeAction("01").programme(programme).build();
    private final Chapitre chapitre = Chapitre.builder().id("C").codeComplet("20012345").build();
    private final NatureEconomique nature = NatureEconomique.builder().id("N").code("612100").exercice(exercice).build();

    private static CreateCreditRequest requete() {
        return CreateCreditRequest.builder()
                .exerciceId("E2026").sectionId("S").programmeId("P").actionId("A").chapitreId("C")
                .natureEconomiqueId("N")
                .montantAE(new BigDecimal("1000")).montantCP(new BigDecimal("500"))
                .build();
    }

    @BeforeEach
    void referentiel() {
        when(exerciceRepository.findById("E2026")).thenReturn(Optional.of(exercice));
        when(sectionRepository.findById("S")).thenReturn(Optional.of(section));
        when(programmeRepository.findById("P")).thenReturn(Optional.of(programme));
        when(actionRepository.findById("A")).thenReturn(Optional.of(action));
        when(chapitreRepository.findById("C")).thenReturn(Optional.of(chapitre));
        when(natureEconomiqueRepository.findById("N")).thenReturn(Optional.of(nature));
    }

    @Test
    void creation_rattacheLaNatureEtLIntegreAuCodeImputation() {
        service.createCredit(requete());

        final ArgumentCaptor<CreditBudgetaire> captor = ArgumentCaptor.forClass(CreditBudgetaire.class);
        verify(creditBudgetaireRepository).save(captor.capture());
        final CreditBudgetaire credit = captor.getValue();
        assertThat(credit.getNatureEconomique()).isSameAs(nature);
        assertThat(credit.getCodeImputation()).isEqualTo("60" + "20" + "200001" + "01" + "20012345" + "612100");
    }

    @Test
    void unicite_verifieeAvecLaNatureEconomique() {
        when(creditBudgetaireRepository
                .existsByExerciceIdAndSectionIdAndProgrammeIdAndActionIdAndChapitreIdAndNatureEconomiqueId(
                        "E2026", "S", "P", "A", "C", "N"))
                .thenReturn(true);

        assertThatThrownBy(() -> service.createCredit(requete()))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.CREDIT_ALREADY_EXISTS);
        verify(creditBudgetaireRepository, never()).save(any());
    }

    @Test
    void natureDUnAutreExercice_refusee() {
        nature.setExercice(autreExercice);

        assertThatThrownBy(() -> service.createCredit(requete()))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.NATURE_ECONOMIQUE_NOT_IN_EXERCICE);
        verify(creditBudgetaireRepository, never()).save(any());
    }

    @Test
    void natureInconnue_refusee() {
        when(natureEconomiqueRepository.findById("N")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.createCredit(requete()))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.ENTITY_NOT_FOUND);
    }
}
