package gov.cmr.minfi.db.gbe.app.referentiel.programmatique;

import gov.cmr.minfi.db.gbe.app.common.audit.BaseEntity;
import gov.cmr.minfi.db.gbe.app.exercice.Exercice;
import gov.cmr.minfi.db.gbe.app.referentiel.administratif.Section;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * Action budgétaire — subdivision d'un programme (Art. 13 al.3 NBE).
 */
@Entity
@Table(name = "actions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Action extends BaseEntity {

	@Column(name = "code")
	private String codeAction;

	@Column(name = "code_chap")
	private String codeChap;

	@Column(name = "code_exercice")
	private String codeExercice;

	@Column(name = "code_programme")
	private String codeProgramme;

	@Column(name = "code_section")
	private String codeSection;

	@Column(name = "libelle", columnDefinition = "TEXT")
	private String libelleFr;

	@Column(name = "libelle_en", columnDefinition = "TEXT")
	private String libelleEn;

	@Column(name = "numero")
	private String numero;

	@Column(name = "numero_programme")
	private String numeroProgramme;

	// Relations

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "programme_id", nullable = false, foreignKey = @ForeignKey(name = "fk_action_programme"))
	private Programme programme;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "section_id", foreignKey = @ForeignKey(name = "fk_action_section"))
	private Section section;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "exercice_id", foreignKey = @ForeignKey(name = "fk_action_exercice"))
	private Exercice exercice;
}
