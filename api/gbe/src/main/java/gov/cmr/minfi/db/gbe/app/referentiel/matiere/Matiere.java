package gov.cmr.minfi.db.gbe.app.referentiel.matiere;

import gov.cmr.minfi.db.gbe.app.common.audit.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * Matière : entrée du catalogue des biens et services sur lesquels porte une expression de besoin.
 * Volontairement non liée à une nature économique : une même matière peut relever de
 * plusieurs natures selon le contexte.
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Table(name = "matieres")
public class Matiere extends BaseEntity {

    @Column(name = "reference", nullable = false, unique = true, length = 100)
    private String reference;

    @Column(name = "libelle", nullable = false, columnDefinition = "TEXT")
    private String libelle;
}
