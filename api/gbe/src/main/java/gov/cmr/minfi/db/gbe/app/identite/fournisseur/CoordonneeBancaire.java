package gov.cmr.minfi.db.gbe.app.identite.fournisseur;

import gov.cmr.minfi.db.gbe.app.common.audit.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Coordonnée bancaire d'un fournisseur. Pour le moment, seul le RIB est stocké :
 * 23 caractères, banque (5 chiffres) + guichet (5 chiffres) + compte (11 caractères) + clé (2 chiffres).
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Table(name = "coordonnees_bancaires")
public class CoordonneeBancaire extends BaseEntity {

    public static final Pattern FORMAT_RIB = Pattern.compile("^\\d{5}\\d{5}[0-9A-Z]{11}\\d{2}$");

    @Column(name = "rib", nullable = false, length = 23)
    private String rib;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "fournisseur_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_coordonnee_bancaire_fournisseur"))
    private Fournisseur fournisseur;

    /** Retire espaces et tirets, met en majuscules. */
    public static String normaliserRib(String rib) {
        return rib == null ? null : rib.replaceAll("[\\s-]", "").toUpperCase(Locale.ROOT);
    }

    public static boolean isRibValide(String ribNormalise) {
        return ribNormalise != null && FORMAT_RIB.matcher(ribNormalise).matches();
    }
}
