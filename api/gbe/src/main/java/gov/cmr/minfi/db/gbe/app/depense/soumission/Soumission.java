package gov.cmr.minfi.db.gbe.app.depense.soumission;

import gov.cmr.minfi.db.gbe.app.common.audit.BaseEntity;
import gov.cmr.minfi.db.gbe.app.common.exception.BusinessException;
import gov.cmr.minfi.db.gbe.app.common.exception.ErrorCode;
import gov.cmr.minfi.db.gbe.app.depense.besoin.ExpressionBesoin;
import gov.cmr.minfi.db.gbe.app.depense.besoin.LigneBesoin;
import gov.cmr.minfi.db.gbe.app.depense.taxe.MontantsTaxes;
import gov.cmr.minfi.db.gbe.app.identite.fournisseur.CoordonneeBancaire;
import gov.cmr.minfi.db.gbe.app.identite.fournisseur.Fournisseur;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Soumission (UC03, classe Soumission de l'onglet 4) : offre d'un fournisseur sur une expression de besoin.
 * <ul>
 *   <li>offre <b>totale</b> : un prix pour chaque ligne du besoin, ni plus ni moins (règle actée, section 9) ;</li>
 *   <li>une seule soumission par fournisseur et par besoin ;</li>
 *   <li>modifiable tant que le besoin est ouvert (date limite non atteinte, décision Q11) ; pas de retrait ;</li>
 *   <li>RIB choisi parmi ceux du fournisseur (onglet 3) ; récépissé horodaté et scellé ({@link #empreinte}, UC03).</li>
 * </ul>
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Table(name = "soumissions")
public class Soumission extends BaseEntity {

    @Column(name = "numero", nullable = false, unique = true, length = 30)
    private String numero;

    /** Horodatage du dépôt initial. */
    @Column(name = "date_soumission", nullable = false)
    private LocalDateTime dateSoumission;

    @Column(name = "date_derniere_modification")
    private LocalDateTime dateDerniereModification;

    /** Somme des montants HT des lignes (getSousTotal du modèle). */
    @Column(name = "montant_propose_ht", nullable = false, precision = 20, scale = 2)
    private BigDecimal montantProposeHT;

    /** TTC = HT + TVA, calculé par le serveur (Q23). */
    @Column(name = "montant_propose_ttc", nullable = false, precision = 20, scale = 2)
    private BigDecimal montantProposeTTC;

    @Column(name = "taux_tva", nullable = false, precision = 5, scale = 2)
    private BigDecimal tauxTva;

    @Column(name = "montant_tva", nullable = false, precision = 20, scale = 2)
    private BigDecimal montantTva;

    @Column(name = "taux_ir", nullable = false, precision = 5, scale = 2)
    private BigDecimal tauxIr;

    @Column(name = "montant_ir", nullable = false, precision = 20, scale = 2)
    private BigDecimal montantIr;

    /** Net à payer = TTC − TVA − IR. */
    @Column(name = "montant_net_a_payer", nullable = false, precision = 20, scale = 2)
    private BigDecimal montantNetAPayer;

    @Column(name = "delai_propose_jours", nullable = false)
    private Integer delaiProposeJours;

    @Enumerated(EnumType.STRING)
    @Column(name = "statut", nullable = false, length = 30)
    @Builder.Default
    private StatutSoumission statut = StatutSoumission.DEPOSEE;

    /** Date à laquelle l'offre a été retenue ou écartée (lot 2a). */
    @Column(name = "date_decision")
    private LocalDateTime dateDecision;

    /** Sceau SHA-256 (hexadécimal) du contenu de l'offre, recalculé à chaque modification. */
    @Column(name = "empreinte", nullable = false, length = 64)
    private String empreinte;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "expression_besoin_id", nullable = false, foreignKey = @ForeignKey(name = "fk_soumission_besoin"))
    private ExpressionBesoin expressionBesoin;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "fournisseur_id", nullable = false, foreignKey = @ForeignKey(name = "fk_soumission_fournisseur"))
    private Fournisseur fournisseur;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "coordonnee_bancaire_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_soumission_coordonnee_bancaire"))
    private CoordonneeBancaire coordonneeBancaire;

    @OneToMany(mappedBy = "soumission", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("numeroOrdre ASC")
    @Builder.Default
    private List<LigneSoumission> lignes = new ArrayList<>();

    // ── Offre ─────────────────────────────────────────

    /**
     * Remplace les lignes par l'offre fournie et recalcule le total HT (validerCompletude du modèle).
     * Refuse une offre partielle, une ligne inconnue du besoin ou une ligne proposée deux fois.
     */
    public void definirOffre(List<OffreLigne> offre) {
        final Map<String, LigneBesoin> lignesBesoin = expressionBesoin.getLignes().stream()
                .collect(Collectors.toMap(LigneBesoin::getId, l -> l));
        final Map<String, OffreLigne> parLigne = new HashMap<>();
        for (OffreLigne o : offre) {
            if (!lignesBesoin.containsKey(o.ligneBesoinId())) {
                throw new BusinessException(ErrorCode.SOUMISSION_LIGNE_INCONNUE, o.ligneBesoinId());
            }
            if (parLigne.put(o.ligneBesoinId(), o) != null) {
                throw new BusinessException(ErrorCode.SOUMISSION_LIGNE_EN_DOUBLE, lignesBesoin.get(o.ligneBesoinId()).getNumeroOrdre());
            }
            if (o.prixUnitairePropose() == null || o.prixUnitairePropose().signum() <= 0) {
                throw new BusinessException(ErrorCode.MONTANT_INVALIDE);
            }
        }
        for (LigneBesoin lb : expressionBesoin.getLignes()) {
            if (!parLigne.containsKey(lb.getId())) {
                throw new BusinessException(ErrorCode.SOUMISSION_INCOMPLETE, lb.getNumeroOrdre());
            }
        }

        this.lignes.clear();
        BigDecimal total = BigDecimal.ZERO;
        for (LigneBesoin lb : expressionBesoin.getLignes()) {
            final OffreLigne o = parLigne.get(lb.getId());
            final BigDecimal sousTotal = o.prixUnitairePropose().multiply(lb.getQuantiteDemandee())
                    .setScale(2, RoundingMode.HALF_UP);
            this.lignes.add(LigneSoumission.builder()
                    .numeroOrdre(lb.getNumeroOrdre())
                    .prixUnitairePropose(o.prixUnitairePropose())
                    .montantTotalLigneHT(sousTotal)
                    .remarquesTechniques(o.remarquesTechniques())
                    .ligneBesoin(lb)
                    .soumission(this)
                    .build());
            total = total.add(sousTotal);
        }
        this.montantProposeHT = total;
    }

    /** Reprend les montants calculés par le serveur à partir du total HT de l'offre. */
    public void appliquerTaxes(MontantsTaxes montants) {
        this.montantProposeTTC = montants.montantTTC();
        this.tauxTva = montants.tauxTva();
        this.montantTva = montants.montantTva();
        this.tauxIr = montants.tauxIr();
        this.montantIr = montants.montantIr();
        this.montantNetAPayer = montants.montantNetAPayer();
    }

    /** RIB du fournisseur sur lequel il sera payé ; doit lui appartenir. */
    public void choisirRib(CoordonneeBancaire rib) {
        if (rib.getFournisseur() == null || !rib.getFournisseur().getId().equals(fournisseur.getId())) {
            throw new BusinessException(ErrorCode.RIB_NOT_FOUND, rib.getId());
        }
        this.coordonneeBancaire = rib;
    }

    /** Modifiable tant que DEPOSEE et que le besoin est ouvert (date limite non atteinte). */
    public void verifierModifiable(LocalDateTime maintenant) {
        if (statut != StatutSoumission.DEPOSEE || !expressionBesoin.isOuverteAuxSoumissions(maintenant)) {
            throw new BusinessException(ErrorCode.SOUMISSION_NON_MODIFIABLE, numero);
        }
    }

    // ── Attribution (lot 2a) ──────────────────────────

    /** DEPOSEE → RETENUE (retenirCommeAdjudicataire de l'onglet 4). */
    public void retenir(LocalDateTime maintenant) {
        decider(StatutSoumission.RETENUE, maintenant);
    }

    /** DEPOSEE → NON_RETENUE : une autre offre a été retenue sur le même besoin. */
    public void ecarter(LocalDateTime maintenant) {
        decider(StatutSoumission.NON_RETENUE, maintenant);
    }

    private void decider(StatutSoumission cible, LocalDateTime maintenant) {
        if (statut != StatutSoumission.DEPOSEE) {
            throw new BusinessException(ErrorCode.TRANSITION_STATUT_INVALIDE, statut, cible,
                    "Seule une offre DEPOSEE peut être retenue ou écartée");
        }
        this.statut = cible;
        this.dateDecision = maintenant;
    }

    // ── Sceau ─────────────────────────────────────────

    /** Recalcule l'empreinte à partir du contenu de l'offre et de son horodatage. */
    public void sceller(LocalDateTime horodatage) {
        final StringBuilder contenu = new StringBuilder()
                .append(numero).append('|')
                .append(expressionBesoin.getReference()).append('|')
                .append(fournisseur.getNiu()).append('|')
                .append(coordonneeBancaire.getRib()).append('|')
                .append(delaiProposeJours).append('|')
                .append(montantProposeHT.toPlainString()).append('|')
                .append(montantProposeTTC.toPlainString()).append('|')
                .append(horodatage);
        for (LigneSoumission l : lignes) {
            contenu.append('|').append(l.getNumeroOrdre()).append(':').append(l.getPrixUnitairePropose().toPlainString());
        }
        this.empreinte = sha256(contenu.toString());
    }

    private static String sha256(String texte) {
        try {
            return HexFormat.of().formatHex(
                    MessageDigest.getInstance("SHA-256").digest(texte.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 indisponible", e);
        }
    }
}
