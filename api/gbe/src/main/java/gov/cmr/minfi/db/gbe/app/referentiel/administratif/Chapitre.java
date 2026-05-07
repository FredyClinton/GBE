package gov.cmr.minfi.db.gbe.app.referentiel.administratif;

import gov.cmr.minfi.db.gbe.app.common.audit.BaseEntity;
import gov.cmr.minfi.db.gbe.app.exercice.Exercice;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * Chapitre budgétaire — unité administrative destinataire de la dépense.
 */
@Entity
@Table(name = "chapitres")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Chapitre extends BaseEntity {

	/**
	 * Code complet 8 caractères du chapitre
	 */
	@Column(name = "code", nullable = false)
	private String codeComplet;

	@Column(name = "code_chap", length = 3)
	private String codeChap;

	@Column(name = "code_exercice")
	private String codeExercice;

	@Column(name = "code_section")
	private String codeSection;

	@Column(name = "libelle", nullable = false, columnDefinition = "TEXT")
	private String libelleFr;

	@Column(name = "libelle_en", columnDefinition = "TEXT")
	private String libelleEn;

	// -------------------------------------------------------
	// Relations
	// -------------------------------------------------------

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "section_id", foreignKey = @ForeignKey(name = "fk_chapitre_section"))
	private Section section;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "exercice_id", foreignKey = @ForeignKey(name = "fk_chapitre_exercice"))
	private Exercice exercice;
	// -------------------------------------------------------
	// TODO : Tutelle administrative (à ajouter quand chapitre_parent_id
	//        sera ajouté dans referentiel-db.sql)
	// -------------------------------------------------------
	// @ManyToOne(fetch = FetchType.LAZY)
	// @JoinColumn(name = "chapitre_parent_id",
	//             foreignKey = @ForeignKey(name = "fk_chapitre_parent"))
	// private Chapitre chapitreParent;
}
