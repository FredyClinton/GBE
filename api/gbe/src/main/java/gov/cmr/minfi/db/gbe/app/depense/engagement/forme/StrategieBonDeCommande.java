package gov.cmr.minfi.db.gbe.app.depense.engagement.forme;

import gov.cmr.minfi.db.gbe.app.common.exception.BusinessException;
import gov.cmr.minfi.db.gbe.app.common.exception.ErrorCode;
import gov.cmr.minfi.db.gbe.app.depense.besoin.ExpressionBesoin;
import gov.cmr.minfi.db.gbe.app.depense.engagement.ActeJuridique;
import gov.cmr.minfi.db.gbe.app.depense.engagement.TypeActe;
import gov.cmr.minfi.db.gbe.app.depense.soumission.Soumission;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Bon de commande administratif (BCA), seule forme traitée (Q21). Données : {@link BonDeCommande}.
 * Numéro (décision Q24) : {@code BCA-A<code exercice><code section><ordre>}, par exemple BCA-A602010097 =
 * exercice 60 (2026), section 20 (MINFI), ordre 10097. Numéro d'ordre sur 5 chiffres, qui repart à chaque exercice,
 * toutes sections confondues (<b>à vérifier</b>, plan §7 ; premier numéro supposé 00001).
 */
@Component
public class StrategieBonDeCommande implements FormeActeStrategie {

    @Override
    public TypeActe type() {
        return TypeActe.BON_DE_COMMANDE;
    }

    @Override
    public boolean verifierEligibilite(ExpressionBesoin besoin, BigDecimal montantTTC) {
        return true; // table des seuils (Q3) pas encore renseignée
    }

    @Override
    public String cleCompteur(ExpressionBesoin besoin) {
        return "BCA-A" + besoin.getExercice().getCodeExercice();
    }

    @Override
    public String numeroter(ExpressionBesoin besoin, long numeroOrdre) {
        return "BCA-A%d%s%05d".formatted(besoin.getExercice().getCodeExercice(),
                besoin.getSection().getCodeSection(), numeroOrdre);
    }

    /** Délai : saisi, sinon conservé, sinon proposé depuis l'offre (Q25). La référence du devis est remplacée. */
    @Override
    public void appliquerDonnees(ActeJuridique acte, Soumission retenue, DonneesForme donnees) {
        BonDeCommande bca = acte.getBonDeCommande();
        if (bca == null) {
            bca = BonDeCommande.builder().build();
            acte.rattacherBonDeCommande(bca);
        }
        final Integer delai = donnees.delaiLivraisonMois() != null ? donnees.delaiLivraisonMois()
                : bca.getDelaiLivraisonMois() != null ? bca.getDelaiLivraisonMois()
                : moisPourJours(retenue.getDelaiProposeJours());
        if (delai <= 0) {
            throw new BusinessException(ErrorCode.MONTANT_INVALIDE);
        }
        bca.setDelaiLivraisonMois(delai);
        bca.setReferenceDevis(donnees.referenceDevis() == null || donnees.referenceDevis().isBlank()
                ? null : donnees.referenceDevis().trim());
    }

    /** Q25 : délai proposé = délai de l'offre en jours ÷ 30, arrondi au mois supérieur. */
    static int moisPourJours(Integer jours) {
        return jours == null || jours <= 0 ? 1 : (jours + 29) / 30;
    }
}
