package gov.cmr.minfi.db.gbe.app.referentiel.programmatique;

import gov.cmr.minfi.db.gbe.app.common.audit.BaseEntity;
import gov.cmr.minfi.db.gbe.app.referentiel.administratif.Section;
import gov.cmr.minfi.db.gbe.app.referentiel.exercice.Exercice;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * Activité — subdivision d'une action (Programme → Action → Activité → Tâche).
 * Programme, section et exercice sont recopiés depuis l'action (jamais saisis) pour des filtres homogènes.
 */
@Entity
@Table(name = "activites")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Activite extends BaseEntity {

	@Column(name = "code", nullable = false, length = 50)
	private String code;

	@Column(name = "sigle", length = 50)
	private String sigle;

	@Column(name = "libelle", nullable = false, columnDefinition = "TEXT")
	private String libelle;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "action_id", nullable = false, foreignKey = @ForeignKey(name = "fk_activite_action"))
	private Action action;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "programme_id", nullable = false, foreignKey = @ForeignKey(name = "fk_activite_programme"))
	private Programme programme;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "section_id", foreignKey = @ForeignKey(name = "fk_activite_section"))
	private Section section;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "exercice_id", foreignKey = @ForeignKey(name = "fk_activite_exercice"))
	private Exercice exercice;

	/** Rattache l'activité à une action et recopie programme, section et exercice. */
	public void rattacherA(Action action) {
		this.action = action;
		this.programme = action.getProgramme();
		this.section = action.getSection();
		this.exercice = action.getExercice();
	}
}
