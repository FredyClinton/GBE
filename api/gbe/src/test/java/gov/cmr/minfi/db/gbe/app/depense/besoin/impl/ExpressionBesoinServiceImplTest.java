package gov.cmr.minfi.db.gbe.app.depense.besoin.impl;

import gov.cmr.minfi.db.gbe.app.common.exception.BusinessException;
import gov.cmr.minfi.db.gbe.app.common.exception.ErrorCode;
import gov.cmr.minfi.db.gbe.app.common.reference.ReferenceGenerator;
import gov.cmr.minfi.db.gbe.app.depense.besoin.ExpressionBesoin;
import gov.cmr.minfi.db.gbe.app.depense.besoin.ExpressionBesoinMapper;
import gov.cmr.minfi.db.gbe.app.depense.besoin.ExpressionBesoinRepository;
import gov.cmr.minfi.db.gbe.app.depense.besoin.StatutExpression;
import gov.cmr.minfi.db.gbe.app.depense.besoin.dto.ExpressionBesoinRequest;
import gov.cmr.minfi.db.gbe.app.depense.besoin.dto.LigneBesoinRequest;
import gov.cmr.minfi.db.gbe.app.depense.soumission.SoumissionRepository;
import gov.cmr.minfi.db.gbe.app.identite.iam.permission.Permission;
import gov.cmr.minfi.db.gbe.app.identite.mandat.MandatScopeValidator;
import gov.cmr.minfi.db.gbe.app.referentiel.administratif.Chapitre;
import gov.cmr.minfi.db.gbe.app.referentiel.administratif.ChapitreRepository;
import gov.cmr.minfi.db.gbe.app.referentiel.administratif.Section;
import gov.cmr.minfi.db.gbe.app.referentiel.economique.NatureEconomique;
import gov.cmr.minfi.db.gbe.app.referentiel.economique.NatureEconomiqueRepository;
import gov.cmr.minfi.db.gbe.app.referentiel.exercice.Exercice;
import gov.cmr.minfi.db.gbe.app.referentiel.matiere.Matiere;
import gov.cmr.minfi.db.gbe.app.referentiel.matiere.MatiereRepository;
import gov.cmr.minfi.db.gbe.app.referentiel.programmatique.Programme;
import gov.cmr.minfi.db.gbe.app.referentiel.programmatique.Tache;
import gov.cmr.minfi.db.gbe.app.referentiel.programmatique.TacheRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ExpressionBesoinServiceImplTest {

    private static final ZoneId DOUALA = ZoneId.of("Africa/Douala");
    private static final Clock HORLOGE = Clock.fixed(Instant.parse("2026-11-11T09:00:00Z"), DOUALA);
    private static final LocalDateTime DATE_LIMITE = LocalDateTime.of(2026, 11, 30, 12, 0);

    @Mock private ExpressionBesoinRepository besoinRepository;
    @Mock private TacheRepository tacheRepository;
    @Mock private ChapitreRepository chapitreRepository;
    @Mock private NatureEconomiqueRepository natureEconomiqueRepository;
    @Mock private MatiereRepository matiereRepository;
    @Mock private SoumissionRepository soumissionRepository;
    @Mock private MandatScopeValidator mandatScopeValidator;
    @Mock private ReferenceGenerator referenceGenerator;
    @Mock private ExpressionBesoinMapper besoinMapper;

    private ExpressionBesoinServiceImpl service;

    private final Exercice exercice = Exercice.builder().id("E2026").annee(2026).build();
    private final Exercice autreExercice = Exercice.builder().id("E2025").annee(2025).build();
    private final Section section = Section.builder().id("S").build();
    private final Section autreSection = Section.builder().id("S2").build();
    private final Programme programme = Programme.builder().id("P").section(section).build();
    private final Tache tache = Tache.builder().id("T").code("01").libelle("Tâche").programme(programme)
            .section(section).exercice(exercice).build();
    private final Chapitre chapitre = Chapitre.builder().id("C").codeComplet("20012345").section(section).build();
    private final NatureEconomique nature = NatureEconomique.builder().id("N").code("612100").exercice(exercice).build();
    private final Matiere papier = Matiere.builder().id("M").reference("PAP-A4").libelle("Papier").build();

    private static ExpressionBesoinRequest requete(String chapitreId, String natureId) {
        return new ExpressionBesoinRequest("Fournitures de bureau", DATE_LIMITE, "T", chapitreId, natureId,
                List.of(new LigneBesoinRequest("M", new BigDecimal("10"), "rames")));
    }

    private static ErrorCode code(Runnable r) {
        try {
            r.run();
        } catch (BusinessException e) {
            return e.getErrorCode();
        }
        throw new AssertionError("BusinessException attendue");
    }

    @BeforeEach
    void init() {
        service = new ExpressionBesoinServiceImpl(besoinRepository, tacheRepository, chapitreRepository,
                natureEconomiqueRepository, matiereRepository, soumissionRepository, mandatScopeValidator, referenceGenerator,
                besoinMapper, HORLOGE);
        when(tacheRepository.findById("T")).thenReturn(Optional.of(tache));
        when(chapitreRepository.findById("C")).thenReturn(Optional.of(chapitre));
        when(natureEconomiqueRepository.findById("N")).thenReturn(Optional.of(nature));
        when(matiereRepository.findById("M")).thenReturn(Optional.of(papier));
        when(referenceGenerator.prochaineReference("EB", 2026)).thenReturn("EB-2026-00001");
    }

    @Test
    void creation_brouillonRattacheALaTache_avecReferenceDeLExercice() {
        service.creer(requete("C", "N"));

        final ArgumentCaptor<ExpressionBesoin> captor = ArgumentCaptor.forClass(ExpressionBesoin.class);
        verify(besoinRepository).save(captor.capture());
        final ExpressionBesoin b = captor.getValue();
        assertThat(b.getReference()).isEqualTo("EB-2026-00001");
        assertThat(b.getStatut()).isEqualTo(StatutExpression.BROUILLON);
        assertThat(b.getDateExpression()).isEqualTo(LocalDateTime.now(HORLOGE).toLocalDate());
        assertThat(b.getProgramme()).isSameAs(programme);
        assertThat(b.getSection()).isSameAs(section);
        assertThat(b.getExercice()).isSameAs(exercice);
        assertThat(b.getLignes()).hasSize(1);
        verify(mandatScopeValidator).checkAccessPerimetre("S", "P", Permission.EXPRIMER_BESOIN);
    }

    @Test
    void creation_horsPerimetre_refusee() {
        doThrow(new BusinessException(ErrorCode.ACCES_REFUSE_HORS_PERIMETRE))
                .when(mandatScopeValidator).checkAccessPerimetre("S", "P", Permission.EXPRIMER_BESOIN);
        assertThat(code(() -> service.creer(requete("C", "N")))).isEqualTo(ErrorCode.ACCES_REFUSE_HORS_PERIMETRE);
        verify(besoinRepository, never()).save(any());
        verify(referenceGenerator, never()).prochaineReference(any(), org.mockito.ArgumentMatchers.anyInt());
    }

    @Test
    void creation_chapitreDUneAutreSection_refusee() {
        when(chapitreRepository.findById("C2")).thenReturn(Optional.of(
                Chapitre.builder().id("C2").codeComplet("99000000").section(autreSection).build()));
        assertThat(code(() -> service.creer(requete("C2", "N")))).isEqualTo(ErrorCode.BESOIN_CHAPITRE_HORS_SECTION);
    }

    @Test
    void creation_natureDUnAutreExercice_refusee() {
        when(natureEconomiqueRepository.findById("N2")).thenReturn(Optional.of(
                NatureEconomique.builder().id("N2").code("612100").exercice(autreExercice).build()));
        assertThat(code(() -> service.creer(requete("C", "N2")))).isEqualTo(ErrorCode.NATURE_ECONOMIQUE_NOT_IN_EXERCICE);
    }

    @Test
    void modification_dUnBesoinPublie_quiLeRendraitIncomplet_refusee() {
        final ExpressionBesoin publie = ExpressionBesoin.builder().id("B").reference("EB-2026-00001")
                .statut(StatutExpression.PUBLIEE).dateClotureSoumission(DATE_LIMITE).build();
        publie.rattacherA(tache);
        publie.ajouterLigne(papier, BigDecimal.ONE, null);
        when(besoinRepository.findById("B")).thenReturn(Optional.of(publie));

        final ExpressionBesoinRequest sansLigne =
                new ExpressionBesoinRequest("Fournitures", DATE_LIMITE, "T", "C", "N", List.of());
        assertThat(code(() -> service.modifier("B", sansLigne))).isEqualTo(ErrorCode.BESOIN_SANS_LIGNE);
    }

    @Test
    void modification_dUnBesoinPublieAvecSoumission_refusee() {
        final ExpressionBesoin publie = ExpressionBesoin.builder().id("B").reference("EB-2026-00001")
                .statut(StatutExpression.PUBLIEE).dateClotureSoumission(DATE_LIMITE).build();
        publie.rattacherA(tache);
        publie.ajouterLigne(papier, BigDecimal.ONE, null);
        when(besoinRepository.findById("B")).thenReturn(Optional.of(publie));
        when(soumissionRepository.existsByExpressionBesoinId("B")).thenReturn(true);

        assertThat(code(() -> service.modifier("B", requete("C", "N")))).isEqualTo(ErrorCode.BESOIN_NON_MODIFIABLE);
    }

    @Test
    void publication_utiliseLHorlogeMetier() {
        final ExpressionBesoin brouillon = ExpressionBesoin.builder().id("B").reference("EB-2026-00001")
                .dateClotureSoumission(DATE_LIMITE).build();
        brouillon.rattacherA(tache);
        brouillon.ajouterLigne(papier, BigDecimal.ONE, null);
        when(besoinRepository.findById("B")).thenReturn(Optional.of(brouillon));

        service.publier("B");

        assertThat(brouillon.getStatut()).isEqualTo(StatutExpression.PUBLIEE);
        assertThat(brouillon.getDatePublication()).isEqualTo(LocalDateTime.now(HORLOGE));
        verify(mandatScopeValidator).checkAccessPerimetre("S", "P", Permission.EXPRIMER_BESOIN);
    }
}
