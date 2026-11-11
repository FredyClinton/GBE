package gov.cmr.minfi.db.gbe.app.referentiel.taxe;

import gov.cmr.minfi.db.gbe.app.common.audit.BaseEntity;
import gov.cmr.minfi.db.gbe.app.common.exception.BusinessException;
import gov.cmr.minfi.db.gbe.app.common.exception.ErrorCode;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** Taux d'une taxe appliqué au montant HT (décision Q23 : le serveur calcule les taxes). */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Table(name = "taxes")
public class Taxe extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(name = "code", nullable = false, unique = true, length = 20)
    private CodeTaxe code;

    @Column(name = "libelle", nullable = false)
    private String libelle;

    /** Pourcentage, par exemple 19.25 pour 19,25 %. */
    @Column(name = "taux", nullable = false, precision = 5, scale = 2)
    private BigDecimal taux;

    public void changerTaux(BigDecimal nouveauTaux) {
        if (nouveauTaux == null || nouveauTaux.signum() < 0 || nouveauTaux.compareTo(BigDecimal.valueOf(100)) >= 0) {
            throw new BusinessException(ErrorCode.TAUX_TAXE_INVALIDE, nouveauTaux);
        }
        this.taux = nouveauTaux;
    }

    /** Montant de la taxe sur une base HT, arrondi au centime (demi supérieur). */
    public BigDecimal appliquer(BigDecimal baseHT) {
        return baseHT.multiply(taux).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }
}
