package gov.cmr.minfi.db.gbe.app.budget.depense.engagement;

import gov.cmr.minfi.db.gbe.app.budget.credit.CreditBudgetaire;
import gov.cmr.minfi.db.gbe.app.common.exception.BusinessException;
import gov.cmr.minfi.db.gbe.app.common.exception.ErrorCode;
import gov.cmr.minfi.db.gbe.app.common.reference.ReferenceGenerator;
import gov.cmr.minfi.db.gbe.app.budget.depense.besoin.ExpressionBesoin;
import gov.cmr.minfi.db.gbe.app.budget.depense.besoin.StatutExpression;
import gov.cmr.minfi.db.gbe.app.budget.depense.engagement.forme.DonneesForme;
import gov.cmr.minfi.db.gbe.app.budget.depense.engagement.forme.EchelleFormesActe;
import gov.cmr.minfi.db.gbe.app.budget.depense.engagement.forme.SeuilFormeActe;
import gov.cmr.minfi.db.gbe.app.budget.depense.engagement.forme.SeuilFormeActeRepository;
import gov.cmr.minfi.db.gbe.app.budget.depense.engagement.forme.StrategieBonDeCommande;
import gov.cmr.minfi.db.gbe.app.budget.depense.soumission.Soumission;
import gov.cmr.minfi.db.gbe.app.referentiel.administratif.Section;
import gov.cmr.minfi.db.gbe.app.referentiel.exercice.Exercice;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ActeJuridiqueFactoryTest {

    private static final LocalDate EMISSION = LocalDate.of(2026, 11, 3);
    private static final DonneesForme SANS_SAISIE = new DonneesForme(null, null);

    private final ReferenceGenerator compteur = mock(ReferenceGenerator.class);
    private final SeuilFormeActeRepository seuils = mock(SeuilFormeActeRepository.class);
    private final EchelleFormesActe echelle = new EchelleFormesActe(seuils);
    private final ActeJuridiqueFactory factory =
            new ActeJuridiqueFactory(List.of(new StrategieBonDeCommande()), compteur, echelle);

    /** Seuils de V26 (Q3) : BCA < 5 M, LC < 50 M, marché au-delà, TTC en FCFA. */
    private static final List<SeuilFormeActe> SEUILS_V26 = List.of(
            SeuilFormeActe.builder().typeActe(TypeActe.BON_DE_COMMANDE).rang(1).plafondTTC(new BigDecimal("5000000")).build(),
            SeuilFormeActe.builder().typeActe(TypeActe.LETTRE_COMMANDE).rang(2).plafondTTC(new BigDecimal("50000000")).build(),
            SeuilFormeActe.builder().typeActe(TypeActe.MARCHE_PUBLIC).rang(3).build());

    {
        when(seuils.findAllByOrderByRangAsc()).thenReturn(SEUILS_V26);
    }

    private static ErrorCode code(Runnable r) {
        try {
            r.run();
        } catch (BusinessException e) {
            return e.getErrorCode();
        }
        throw new AssertionError("BusinessException attendue");
    }

    private Soumission offreTTC(String ttc) {
        final Soumission o = offre(30);
        o.setMontantProposeTTC(new BigDecimal(ttc));
        return o;
    }

    private final ExpressionBesoin besoin = ExpressionBesoin.builder().id("B").reference("EB-2026-00001")
            .objet("Achat des préimprimés").statut(StatutExpression.ATTRIBUEE)
            .exercice(Exercice.builder().id("E").annee(2026).codeExercice(60).build())
            .section(Section.builder().id("S").codeSection("20").build())
            .build();

    /** Offre du BCA de référence (plan §4.5). */
    private Soumission offre(int delaiJours) {
        return Soumission.builder().id("SO").delaiProposeJours(delaiJours)
                .montantProposeHT(new BigDecimal("4120000.00")).tauxTva(new BigDecimal("19.25"))
                .montantTva(new BigDecimal("793100.00")).tauxIr(new BigDecimal("5.50")).montantIr(new BigDecimal("226600.00"))
                .montantProposeTTC(new BigDecimal("4913100.00")).montantNetAPayer(new BigDecimal("3893400.00"))
                .build();
    }

    @Test
    void bca_numeroteEtRepriseDesMontantsDeLOffre() {
        when(compteur.prochainNumero("BCA-A60")).thenReturn(10097L);

        final ActeJuridique acte = factory.creerActe(besoin, offre(30), null, SANS_SAISIE, EMISSION);

        assertThat(acte.getNumero()).isEqualTo("BCA-A602010097");
        assertThat(acte.getType()).isEqualTo(TypeActe.BON_DE_COMMANDE);
        assertThat(acte.getStatut()).isEqualTo(StatutActe.PROJET);
        assertThat(acte.getObjet()).isEqualTo("Achat des préimprimés");
        assertThat(acte.getDateEmission()).isEqualTo(EMISSION);
        assertThat(acte.getBonDeCommande().getDelaiLivraisonMois()).isEqualTo(1);
        assertThat(acte.getBonDeCommande().getActeJuridique()).isSameAs(acte);
        assertThat(acte.getMontantTTC()).isEqualByComparingTo("4913100");
        assertThat(acte.getMontantNetAPayer()).isEqualByComparingTo("3893400");
        assertThat(acte.getMontantEnLettres()).isEqualTo("quatre millions neuf cent treize mille cent");
    }

    @Test
    void numeroDOrdre_surCinqChiffres() {
        when(compteur.prochainNumero("BCA-A60")).thenReturn(1L);
        assertThat(factory.creerActe(besoin, offre(30), null, SANS_SAISIE, EMISSION).getNumero()).isEqualTo("BCA-A602000001");
    }

    @Test
    void donneesDeLaForme_saisies() {
        final ActeJuridique acte = factory.creerActe(besoin, offre(45), null, new DonneesForme(3, " DEV-12 "), EMISSION);
        assertThat(acte.getBonDeCommande().getDelaiLivraisonMois()).isEqualTo(3);
        assertThat(acte.getBonDeCommande().getReferenceDevis()).isEqualTo("DEV-12");
    }

    @Test
    void echelle_plafondsExclusifs() {
        assertThat(echelle.formeMinimale(new BigDecimal("4999999.99"))).contains(TypeActe.BON_DE_COMMANDE);
        assertThat(echelle.formeMinimale(new BigDecimal("5000000"))).contains(TypeActe.LETTRE_COMMANDE);
        assertThat(echelle.formeMinimale(new BigDecimal("49999999.99"))).contains(TypeActe.LETTRE_COMMANDE);
        assertThat(echelle.formeMinimale(new BigDecimal("50000000"))).contains(TypeActe.MARCHE_PUBLIC);
        assertThat(echelle.estInferieure(TypeActe.BON_DE_COMMANDE, TypeActe.LETTRE_COMMANDE)).isTrue();
        assertThat(echelle.estInferieure(TypeActe.MARCHE_PUBLIC, TypeActe.BON_DE_COMMANDE)).isFalse();
        assertThat(echelle.estInferieure(TypeActe.CONVENTION_BAIL, TypeActe.MARCHE_PUBLIC)).isFalse();
    }

    @Test
    void sansChoix_formeMinimale_lettreCommandeExigeeMaisIndisponible() {
        assertThat(code(() -> factory.creerActe(besoin, offreTTC("5000000"), null, SANS_SAISIE, EMISSION)))
                .isEqualTo(ErrorCode.FORME_ACTE_REQUISE_NON_DISPONIBLE);
        assertThat(code(() -> factory.creerActe(besoin, offreTTC("60000000"), null, SANS_SAISIE, EMISSION)))
                .isEqualTo(ErrorCode.FORME_ACTE_REQUISE_NON_DISPONIBLE);
    }

    @Test
    void bcaDemande_auDessusDuSeuil_refuseCommeFormeInferieure() {
        assertThat(code(() -> factory.creerActe(besoin, offreTTC("10000000"), TypeActe.BON_DE_COMMANDE, SANS_SAISIE,
                EMISSION))).isEqualTo(ErrorCode.FORME_ACTE_INELIGIBLE);
    }

    @Test
    void tableDesSeuilsVide_choixLibre_bcaParDefaut() {
        when(seuils.findAllByOrderByRangAsc()).thenReturn(List.of());
        when(compteur.prochainNumero("BCA-A60")).thenReturn(1L);
        assertThat(factory.creerActe(besoin, offreTTC("60000000"), null, SANS_SAISIE, EMISSION).getType())
                .isEqualTo(TypeActe.BON_DE_COMMANDE);
    }

    @Test
    void formeNonDisponible_refusee() {
        assertThatThrownBy(() -> factory.creerActe(besoin, offre(30), TypeActe.MARCHE_PUBLIC, SANS_SAISIE, EMISSION))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.FORME_ACTE_NON_DISPONIBLE);
    }

    @Test
    void projet_modifiableJusquALaSoumissionAuControle() {
        when(compteur.prochainNumero("BCA-A60")).thenReturn(1L);
        final ActeJuridique acte = factory.creerActe(besoin, offre(30), null, SANS_SAISIE, EMISSION);
        acte.modifier("Nouvel objet");
        assertThat(acte.getObjet()).isEqualTo("Nouvel objet");

        final CreditBudgetaire credit = CreditBudgetaire.builder().id("C").build();
        final LocalDateTime maintenant = LocalDateTime.of(2026, 11, 11, 10, 0);
        acte.soumettreAuControle(credit, maintenant);
        assertThat(acte.getStatut()).isEqualTo(StatutActe.SOUMIS_CONTROLE);
        assertThat(acte.getCreditBudgetaire()).isSameAs(credit);
        assertThat(acte.getDateSoumissionControle()).isEqualTo(maintenant);

        assertThatThrownBy(() -> acte.modifier("Autre"))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.ACTE_NON_MODIFIABLE);
        assertThatThrownBy(() -> acte.soumettreAuControle(credit, maintenant))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.TRANSITION_STATUT_INVALIDE);
    }
}
