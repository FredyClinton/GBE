package gov.cmr.minfi.db.gbe.app.budget.depense.engagement;

import gov.cmr.minfi.db.gbe.app.common.exception.BusinessException;
import gov.cmr.minfi.db.gbe.app.common.exception.ErrorCode;
import gov.cmr.minfi.db.gbe.app.common.reference.ReferenceGenerator;
import gov.cmr.minfi.db.gbe.app.common.texte.MontantEnLettres;
import gov.cmr.minfi.db.gbe.app.budget.depense.besoin.ExpressionBesoin;
import gov.cmr.minfi.db.gbe.app.budget.depense.engagement.forme.DonneesForme;
import gov.cmr.minfi.db.gbe.app.budget.depense.engagement.forme.EchelleFormesActe;
import gov.cmr.minfi.db.gbe.app.budget.depense.engagement.forme.FormeActeStrategie;
import gov.cmr.minfi.db.gbe.app.budget.depense.soumission.Soumission;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * « ActeJuridiqueFactory » de l'onglet 4 : choisit la forme (determinerForme) et assemble l'acte (creerActe)
 * à partir du besoin attribué et de l'offre retenue.
 */
@Component
public class ActeJuridiqueFactory {

    private final Map<TypeActe, FormeActeStrategie> formes = new EnumMap<>(TypeActe.class);
    private final ReferenceGenerator referenceGenerator;
    private final EchelleFormesActe echelle;

    public ActeJuridiqueFactory(List<FormeActeStrategie> strategies, ReferenceGenerator referenceGenerator,
                                EchelleFormesActe echelle) {
        strategies.forEach(s -> formes.put(s.type(), s));
        this.referenceGenerator = referenceGenerator;
        this.echelle = echelle;
    }

    /**
     * Forme de l'acte (Q3) : sans choix de l'ordonnateur, la forme minimale pour le TTC (BCA < 5 M, LC < 50 M, marché
     * au-delà ; BCA si la table des seuils est vide). Une forme inférieure à la forme minimale est refusée, une forme
     * supérieure est permise. Une forme non encore mise en œuvre (Q21) est refusée.
     */
    public FormeActeStrategie determinerForme(ExpressionBesoin besoin, BigDecimal montantTTC, TypeActe demande) {
        final Optional<TypeActe> minimale = echelle.formeMinimale(montantTTC);
        if (demande != null && minimale.isPresent() && echelle.estInferieure(demande, minimale.get())) {
            throw new BusinessException(ErrorCode.FORME_ACTE_INELIGIBLE, demande, montantTTC, minimale.get());
        }
        final TypeActe type = demande != null ? demande : minimale.orElse(TypeActe.BON_DE_COMMANDE);
        final FormeActeStrategie forme = formes.get(type);
        if (forme == null) {
            throw demande == null
                    ? new BusinessException(ErrorCode.FORME_ACTE_REQUISE_NON_DISPONIBLE, montantTTC, type)
                    : new BusinessException(ErrorCode.FORME_ACTE_NON_DISPONIBLE, type);
        }
        if (!forme.verifierEligibilite(besoin, montantTTC)) {
            throw new BusinessException(ErrorCode.FORME_ACTE_INELIGIBLE, type, montantTTC, minimale.orElse(type));
        }
        return forme;
    }

    /** Stratégie de la forme d'un acte existant. */
    public FormeActeStrategie forme(TypeActe type) {
        final FormeActeStrategie forme = formes.get(type);
        if (forme == null) {
            throw new BusinessException(ErrorCode.FORME_ACTE_NON_DISPONIBLE, type);
        }
        return forme;
    }

    public ActeJuridique creerActe(ExpressionBesoin besoin, Soumission retenue, TypeActe demande,
                                   DonneesForme donnees, LocalDate dateEmission) {
        final FormeActeStrategie forme = determinerForme(besoin, retenue.getMontantProposeTTC(), demande);
        final String numero = forme.numeroter(besoin, referenceGenerator.prochainNumero(forme.cleCompteur(besoin)));
        final ActeJuridique acte = ActeJuridique.builder()
                .numero(numero)
                .type(forme.type())
                .objet(besoin.getObjet())
                .dateEmission(dateEmission)
                .montantTotalHT(retenue.getMontantProposeHT())
                .tauxTva(retenue.getTauxTva())
                .montantTva(retenue.getMontantTva())
                .tauxIr(retenue.getTauxIr())
                .montantIr(retenue.getMontantIr())
                .montantTTC(retenue.getMontantProposeTTC())
                .montantNetAPayer(retenue.getMontantNetAPayer())
                .montantEnLettres(MontantEnLettres.enLettres(retenue.getMontantProposeTTC()))
                .expressionBesoin(besoin)
                .soumission(retenue)
                .build();
        forme.appliquerDonnees(acte, retenue, donnees);
        return acte;
    }
}
