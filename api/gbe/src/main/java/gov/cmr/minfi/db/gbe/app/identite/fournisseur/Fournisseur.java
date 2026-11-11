package gov.cmr.minfi.db.gbe.app.identite.fournisseur;

import gov.cmr.minfi.db.gbe.app.common.audit.BaseEntity;
import gov.cmr.minfi.db.gbe.app.common.exception.BusinessException;
import gov.cmr.minfi.db.gbe.app.common.exception.ErrorCode;
import gov.cmr.minfi.db.gbe.app.identite.user.User;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * Fournisseur (prestataire) : utilisateur externe, sans fiche Agent ni mandat.
 * Email, téléphone et mot de passe sont portés par son compte {@link User} (rôle FOURNISSEUR).
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Table(name = "fournisseurs")
public class Fournisseur extends BaseEntity {

    @Column(name = "raison_sociale", nullable = false)
    private String raisonSociale;

    /** Numéro d'identifiant unique (fiscal). */
    @Column(name = "niu", nullable = false, unique = true, length = 50)
    private String niu;

    @Column(name = "rccm", length = 100)
    private String rccm;

    @Column(name = "adresse", columnDefinition = "TEXT")
    private String adresse;

    /** Téléphone de l'entreprise (celui du représentant est porté par son compte User). */
    @Column(name = "telephone", length = 30)
    private String telephone;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true, foreignKey = @ForeignKey(name = "fk_fournisseur_user"))
    private User user;

    @OneToMany(mappedBy = "fournisseur", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("createdDate ASC, rib ASC")
    @Builder.Default
    private List<CoordonneeBancaire> coordonneesBancaires = new ArrayList<>();

    /** Ajoute un RIB (déjà normalisé) ; refuse un doublon pour ce fournisseur. */
    public CoordonneeBancaire ajouterRib(String rib) {
        if (coordonneesBancaires.stream().anyMatch(c -> c.getRib().equals(rib))) {
            throw new BusinessException(ErrorCode.RIB_ALREADY_EXISTS, rib);
        }
        final CoordonneeBancaire coordonnee = CoordonneeBancaire.builder().rib(rib).fournisseur(this).build();
        coordonneesBancaires.add(coordonnee);
        return coordonnee;
    }

    public void retirerRib(String coordonneeBancaireId) {
        final boolean retire = coordonneesBancaires.removeIf(c -> coordonneeBancaireId.equals(c.getId()));
        if (!retire) {
            throw new BusinessException(ErrorCode.RIB_NOT_FOUND, coordonneeBancaireId);
        }
    }
}
