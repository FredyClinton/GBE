package gov.cmr.minfi.db.gbe.app.referentiel.fonctionnel;

import gov.cmr.minfi.db.gbe.app.common.audit.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Table(name = "CLASSE_FONCTIONNELLE")
public class ClasseFonctionnelle extends BaseEntity {



    // 1 caractère — ex: "1", "2"
    @Column(name = "CODE_CLASSE", nullable = false, length = 1)
    private String codeClasse;

    // Calculé : codeDivision(2) + codeGroupe(1) + codeClasse(1) = 4 caractères
    @Column(name = "CODE_COMPLET", nullable = false, unique = true, length = 4)
    private String codeComplet;

    @Column(name = "LIBELLE_FR", nullable = false)
    private String libelleFr;

    @Column(name = "LIBELLE_EN", nullable = false)
    private String libelleEn;


}