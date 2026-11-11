package gov.cmr.minfi.db.gbe.app.depense.soumission;

import gov.cmr.minfi.db.gbe.app.common.exception.BusinessException;
import gov.cmr.minfi.db.gbe.app.common.exception.ErrorCode;
import gov.cmr.minfi.db.gbe.app.depense.besoin.ExpressionBesoin;
import gov.cmr.minfi.db.gbe.app.depense.besoin.LigneBesoin;
import gov.cmr.minfi.db.gbe.app.depense.besoin.StatutExpression;
import gov.cmr.minfi.db.gbe.app.identite.fournisseur.CoordonneeBancaire;
import gov.cmr.minfi.db.gbe.app.identite.fournisseur.Fournisseur;
import gov.cmr.minfi.db.gbe.app.referentiel.matiere.Matiere;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SoumissionTest {

    private static final LocalDateTime MAINTENANT = LocalDateTime.of(2026, 11, 11, 10, 0);
    private static final LocalDateTime DATE_LIMITE = MAINTENANT.plusDays(10);

    private ExpressionBesoin besoin;
    private Fournisseur fournisseur;
    private CoordonneeBancaire rib;
    private String l1;
    private String l2;

    @BeforeEach
    void init() {
        besoin = ExpressionBesoin.builder().id("B").reference("EB-2026-00001")
                .statut(StatutExpression.PUBLIEE).dateClotureSoumission(DATE_LIMITE).build();
        besoin.ajouterLigne(Matiere.builder().id("M1").reference("PAP").build(), new BigDecimal("10"), null).setId("L1");
        besoin.ajouterLigne(Matiere.builder().id("M2").reference("ENC").build(), new BigDecimal("2.5"), null).setId("L2");
        l1 = "L1";
        l2 = "L2";
        fournisseur = Fournisseur.builder().id("F").niu("M0001").build();
        rib = fournisseur.ajouterRib("10005000010123456789012");
        rib.setId("R");
    }

    private Soumission soumission() {
        final Soumission s = Soumission.builder().numero("SO-2026-00001").expressionBesoin(besoin)
                .fournisseur(fournisseur).dateSoumission(MAINTENANT).delaiProposeJours(30).build();
        s.choisirRib(rib);
        return s;
    }

    private static void assertErreur(Runnable action, ErrorCode attendu) {
        assertThatThrownBy(action::run)
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(attendu);
    }

    @Test
    void offreTotale_calculeLesSousTotauxEtLeTotalHT() {
        final Soumission s = soumission();
        s.definirOffre(List.of(new OffreLigne(l2, new BigDecimal("1000.40"), "encre noire"),
                new OffreLigne(l1, new BigDecimal("3500"), null)));

        assertThat(s.getLignes()).extracting(LigneSoumission::getNumeroOrdre).containsExactly(1, 2);
        assertThat(s.getLignes().get(0).getMontantTotalLigneHT()).isEqualByComparingTo("35000.00");
        assertThat(s.getLignes().get(1).getMontantTotalLigneHT()).isEqualByComparingTo("2501.00");
        assertThat(s.getMontantProposeHT()).isEqualByComparingTo("37501.00");
    }

    @Test
    void offrePartielle_refusee() {
        assertErreur(() -> soumission().definirOffre(List.of(new OffreLigne(l1, BigDecimal.TEN, null))),
                ErrorCode.SOUMISSION_INCOMPLETE);
    }

    @Test
    void ligneInconnueOuEnDouble_refusee() {
        assertErreur(() -> soumission().definirOffre(List.of(new OffreLigne(l1, BigDecimal.TEN, null),
                new OffreLigne(l2, BigDecimal.TEN, null), new OffreLigne("X", BigDecimal.TEN, null))),
                ErrorCode.SOUMISSION_LIGNE_INCONNUE);
        assertErreur(() -> soumission().definirOffre(List.of(new OffreLigne(l1, BigDecimal.TEN, null),
                new OffreLigne(l1, BigDecimal.ONE, null), new OffreLigne(l2, BigDecimal.TEN, null))),
                ErrorCode.SOUMISSION_LIGNE_EN_DOUBLE);
    }

    @Test
    void prixNul_refuse() {
        assertErreur(() -> soumission().definirOffre(List.of(new OffreLigne(l1, BigDecimal.ZERO, null),
                new OffreLigne(l2, BigDecimal.TEN, null))), ErrorCode.MONTANT_INVALIDE);
    }

    @Test
    void ribDUnAutreFournisseur_refuse() {
        final Fournisseur autre = Fournisseur.builder().id("F2").build();
        final CoordonneeBancaire ribAutre = autre.ajouterRib("20005000010123456789012");
        assertErreur(() -> soumission().choisirRib(ribAutre), ErrorCode.RIB_NOT_FOUND);
    }

    @Test
    void modifiableJusquALaDateLimite() {
        final Soumission s = soumission();
        s.verifierModifiable(DATE_LIMITE.minusSeconds(1));
        assertErreur(() -> s.verifierModifiable(DATE_LIMITE), ErrorCode.SOUMISSION_NON_MODIFIABLE);
        s.setStatut(StatutSoumission.EN_EXAMEN);
        assertErreur(() -> s.verifierModifiable(MAINTENANT), ErrorCode.SOUMISSION_NON_MODIFIABLE);
    }

    @Test
    void sceau_stablePourUnMemeContenu_changeSiLePrixChange() {
        final Soumission s = soumission();
        s.definirOffre(List.of(new OffreLigne(l1, BigDecimal.TEN, null), new OffreLigne(l2, BigDecimal.TEN, null)));
        s.setMontantProposeTTC(new BigDecimal("200"));
        s.sceller(MAINTENANT);
        final String premier = s.getEmpreinte();
        s.sceller(MAINTENANT);
        assertThat(s.getEmpreinte()).hasSize(64).isEqualTo(premier);

        s.definirOffre(List.of(new OffreLigne(l1, new BigDecimal("11"), null), new OffreLigne(l2, BigDecimal.TEN, null)));
        s.sceller(MAINTENANT);
        assertThat(s.getEmpreinte()).isNotEqualTo(premier);
    }
}
