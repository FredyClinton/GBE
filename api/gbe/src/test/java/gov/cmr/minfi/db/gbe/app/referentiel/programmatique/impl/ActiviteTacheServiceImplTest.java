package gov.cmr.minfi.db.gbe.app.referentiel.programmatique.impl;

import gov.cmr.minfi.db.gbe.app.common.exception.BusinessException;
import gov.cmr.minfi.db.gbe.app.common.exception.ErrorCode;
import gov.cmr.minfi.db.gbe.app.referentiel.ReferentielMapper;
import gov.cmr.minfi.db.gbe.app.referentiel.administratif.Section;
import gov.cmr.minfi.db.gbe.app.referentiel.exercice.Exercice;
import gov.cmr.minfi.db.gbe.app.referentiel.programmatique.*;
import gov.cmr.minfi.db.gbe.app.referentiel.programmatique.dto.ActiviteRequest;
import gov.cmr.minfi.db.gbe.app.referentiel.programmatique.dto.TacheRequest;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ActiviteTacheServiceImplTest {

    private static final Exercice EXERCICE = Exercice.builder().id("E2026").build();
    private static final Section SECTION = Section.builder().id("S").build();
    private static final Programme PROGRAMME = Programme.builder().id("P").section(SECTION).exercice(EXERCICE).build();
    private static final Programme AUTRE_PROGRAMME = Programme.builder().id("P2").section(SECTION).exercice(EXERCICE).build();
    private static final Action ACTION = Action.builder().id("A").codeAction("01")
            .programme(PROGRAMME).section(SECTION).exercice(EXERCICE).build();
    private static final Action AUTRE_ACTION = Action.builder().id("A2").codeAction("02")
            .programme(AUTRE_PROGRAMME).section(SECTION).exercice(EXERCICE).build();

    private static BusinessException erreur(Runnable r) {
        try {
            r.run();
        } catch (BusinessException e) {
            return e;
        }
        throw new AssertionError("BusinessException attendue");
    }

    @Nested
    @ExtendWith(MockitoExtension.class)
    @MockitoSettings(strictness = Strictness.LENIENT)
    class Activites {

        @Mock private ActiviteRepository activiteRepository;
        @Mock private TacheRepository tacheRepository;
        @Mock private ActionRepository actionRepository;
        @Mock private ReferentielMapper referentielMapper;
        @InjectMocks private ActiviteServiceImpl service;

        @Test
        void creation_recopieLaHierarchieDeLAction() {
            when(actionRepository.findById("A")).thenReturn(Optional.of(ACTION));

            service.createActivite(new ActiviteRequest(" 01 ", "ACT", "Activité 1", "A"));

            final ArgumentCaptor<Activite> captor = ArgumentCaptor.forClass(Activite.class);
            verify(activiteRepository).save(captor.capture());
            final Activite a = captor.getValue();
            assertThat(a.getCode()).isEqualTo("01");
            assertThat(a.getAction()).isSameAs(ACTION);
            assertThat(a.getProgramme()).isSameAs(PROGRAMME);
            assertThat(a.getSection()).isSameAs(SECTION);
            assertThat(a.getExercice()).isSameAs(EXERCICE);
        }

        @Test
        void creation_codeDejaPrisDansLAction_refusee() {
            when(actionRepository.findById("A")).thenReturn(Optional.of(ACTION));
            when(activiteRepository.existsByActionIdAndCode("A", "01")).thenReturn(true);

            assertThat(erreur(() -> service.createActivite(new ActiviteRequest("01", null, "x", "A"))).getErrorCode())
                    .isEqualTo(ErrorCode.ACTIVITE_ALREADY_EXISTS);
            verify(activiteRepository, never()).save(any());
        }

        @Test
        void creation_actionInconnue_refusee() {
            when(actionRepository.findById("X")).thenReturn(Optional.empty());
            assertThat(erreur(() -> service.createActivite(new ActiviteRequest("01", null, "x", "X"))).getErrorCode())
                    .isEqualTo(ErrorCode.ACTION_NOT_FOUND);
        }

        @Test
        void changementDAction_propageLaHierarchieAuxTaches() {
            final Activite activite = Activite.builder().id("AC").code("01").libelle("x").build();
            activite.rattacherA(ACTION);
            final Tache tache = Tache.builder().id("T").code("01").libelle("t").build();
            tache.rattacherA(activite);
            when(activiteRepository.findById("AC")).thenReturn(Optional.of(activite));
            when(actionRepository.findById("A2")).thenReturn(Optional.of(AUTRE_ACTION));
            when(tacheRepository.findByActiviteId("AC")).thenReturn(List.of(tache));

            service.updateActivite("AC", new ActiviteRequest("01", null, "x", "A2"));

            assertThat(activite.getProgramme()).isSameAs(AUTRE_PROGRAMME);
            assertThat(tache.getAction()).isSameAs(AUTRE_ACTION);
            assertThat(tache.getProgramme()).isSameAs(AUTRE_PROGRAMME);
        }

        @Test
        void suppression_activiteAvecTaches_refusee() {
            final Activite activite = Activite.builder().id("AC").code("01").build();
            when(activiteRepository.findById("AC")).thenReturn(Optional.of(activite));
            when(tacheRepository.existsByActiviteId("AC")).thenReturn(true);

            assertThat(erreur(() -> service.deleteActivite("AC")).getErrorCode())
                    .isEqualTo(ErrorCode.ACTIVITE_A_DES_TACHES);
            verify(activiteRepository, never()).delete(any());
        }
    }

    @Nested
    @ExtendWith(MockitoExtension.class)
    @MockitoSettings(strictness = Strictness.LENIENT)
    class Taches {

        @Mock private TacheRepository tacheRepository;
        @Mock private ActiviteRepository activiteRepository;
        @Mock private ReferentielMapper referentielMapper;
        @InjectMocks private TacheServiceImpl service;

        private Activite activite() {
            final Activite a = Activite.builder().id("AC").code("01").libelle("x").build();
            a.rattacherA(ACTION);
            return a;
        }

        @Test
        void creation_recopieLaHierarchieDeLActivite() {
            final Activite activite = activite();
            when(activiteRepository.findById("AC")).thenReturn(Optional.of(activite));

            service.createTache(new TacheRequest("01", null, "Tâche 1", "AC"));

            final ArgumentCaptor<Tache> captor = ArgumentCaptor.forClass(Tache.class);
            verify(tacheRepository).save(captor.capture());
            final Tache t = captor.getValue();
            assertThat(t.getActivite()).isSameAs(activite);
            assertThat(t.getAction()).isSameAs(ACTION);
            assertThat(t.getProgramme()).isSameAs(PROGRAMME);
            assertThat(t.getSection()).isSameAs(SECTION);
            assertThat(t.getExercice()).isSameAs(EXERCICE);
        }

        @Test
        void creation_codeDejaPrisDansLActivite_refusee() {
            when(activiteRepository.findById("AC")).thenReturn(Optional.of(activite()));
            when(tacheRepository.existsByActiviteIdAndCode("AC", "01")).thenReturn(true);

            assertThat(erreur(() -> service.createTache(new TacheRequest("01", null, "x", "AC"))).getErrorCode())
                    .isEqualTo(ErrorCode.TACHE_ALREADY_EXISTS);
            verify(tacheRepository, never()).save(any());
        }

        @Test
        void modification_tacheInconnue_refusee() {
            when(tacheRepository.findById("X")).thenReturn(Optional.empty());
            assertThatThrownBy(() -> service.updateTache("X", new TacheRequest("01", null, "x", "AC")))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(ErrorCode.TACHE_NOT_FOUND);
        }
    }
}
