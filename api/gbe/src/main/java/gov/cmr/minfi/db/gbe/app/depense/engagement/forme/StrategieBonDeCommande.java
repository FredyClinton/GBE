package gov.cmr.minfi.db.gbe.app.depense.engagement.forme;

import gov.cmr.minfi.db.gbe.app.depense.besoin.ExpressionBesoin;
import gov.cmr.minfi.db.gbe.app.depense.engagement.TypeActe;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Bon de commande administratif (BCA). Numéro (décision Q24) : {@code BCA-A<code exercice><code section><ordre>},
 * par exemple BCA-A602010097 = exercice 60 (2026), section 20 (MINFI), ordre 10097.
 * Numéro d'ordre sur 5 chiffres, qui repart à chaque exercice, toutes sections confondues
 * (<b>à vérifier</b>, plan §7 ; premier numéro supposé 00001).
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
}
