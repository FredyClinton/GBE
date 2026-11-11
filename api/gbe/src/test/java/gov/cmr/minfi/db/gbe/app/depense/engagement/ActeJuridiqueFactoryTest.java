package gov.cmr.minfi.db.gbe.app.depense.engagement;

import gov.cmr.minfi.db.gbe.app.budget.credit.CreditBudgetaire;
import gov.cmr.minfi.db.gbe.app.common.exception.BusinessException;
import gov.cmr.minfi.db.gbe.app.common.exception.ErrorCode;
import gov.cmr.minfi.db.gbe.app.common.reference.ReferenceGenerator;
import gov.cmr.minfi.db.gbe.app.depense.besoin.ExpressionBesoin;
import gov.cmr.minfi.db.gbe.app.depense.besoin.StatutExpression;
import gov.cmr.minfi.db.gbe.app.depense.engagement.forme.StrategieBonDeCommande;
import gov.cmr.minfi.db.gbe.app.depense.soumission.Soumission;
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

    private final ReferenceGenerator compteur = mock(ReferenceGenerator.class);
    private final ActeJuridiqueFactory factory = new ActeJuridiqueFactory(List.of(new StrategieBonDeCommande()), compteur);

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

        final ActeJuridique acte = factory.creerActe(besoin, offre(30), null, null, EMISSION);

        assertThat(acte.getNumero()).isEqualTo("BCA-A602010097");
        assertThat(acte.getType()).isEqualTo(TypeActe.BON_DE_COMMANDE);
        assertThat(acte.getStatut()).isEqualTo(StatutActe.PROJET);
        assertThat(acte.getObjet()).isEqualTo("Achat des préimprimés");
        assertThat(acte.getDateEmission()).isEqualTo(EMISSION);
        assertThat(acte.getDelaiLivraisonMois()).isEqualTo(1);
        assertThat(acte.getMontantTTC()).isEqualByComparingTo("4913100");
        assertThat(acte.getMontantNetAPayer()).isEqualByComparingTo("3893400");
        assertThat(acte.getMontantEnLettres()).isEqualTo("quatre millions neuf cent treize mille cent");
    }

    @Test
    void numeroDOrdre_surCinqChiffres() {
        when(compteur.prochainNumero("BCA-A60")).thenReturn(1L);
        assertThat(factory.creerActe(besoin, offre(30), null, null, EMISSION).getNumero()).isEqualTo("BCA-A602000001");
    }

    @Test
    void delai_proposeEnMoisArrondiAuSuperieur_ouSaisi() {
        assertThat(ActeJuridiqueFactory.moisPourJours(1)).isEqualTo(1);
        assertThat(ActeJuridiqueFactory.moisPourJours(30)).isEqualTo(1);
        assertThat(ActeJuridiqueFactory.moisPourJours(31)).isEqualTo(2);
        assertThat(factory.creerActe(besoin, offre(45), null, 3, EMISSION).getDelaiLivraisonMois()).isEqualTo(3);
    }

    @Test
    void formeNonDisponible_refusee() {
        assertThatThrownBy(() -> factory.creerActe(besoin, offre(30), TypeActe.MARCHE_PUBLIC, null, EMISSION))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.FORME_ACTE_NON_DISPONIBLE);
    }

    @Test
    void projet_modifiableJusquALaSoumissionAuControle() {
        when(compteur.prochainNumero("BCA-A60")).thenReturn(1L);
        final ActeJuridique acte = factory.creerActe(besoin, offre(30), null, null, EMISSION);
        acte.modifier("Nouvel objet", 2);
        assertThat(acte.getDelaiLivraisonMois()).isEqualTo(2);

        final CreditBudgetaire credit = CreditBudgetaire.builder().id("C").build();
        final LocalDateTime maintenant = LocalDateTime.of(2026, 11, 11, 10, 0);
        acte.soumettreAuControle(credit, maintenant);
        assertThat(acte.getStatut()).isEqualTo(StatutActe.SOUMIS_CONTROLE);
        assertThat(acte.getCreditBudgetaire()).isSameAs(credit);
        assertThat(acte.getDateSoumissionControle()).isEqualTo(maintenant);

        assertThatThrownBy(() -> acte.modifier("Autre", 3))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.ACTE_NON_MODIFIABLE);
        assertThatThrownBy(() -> acte.soumettreAuControle(credit, maintenant))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.TRANSITION_STATUT_INVALIDE);
    }
}
