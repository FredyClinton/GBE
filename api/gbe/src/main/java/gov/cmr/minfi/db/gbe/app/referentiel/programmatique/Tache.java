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
 * Tâche — subdivision d'une activité ; l'expression de besoin s'inscrit dans une tâche.
 * Action, programme, section et exercice sont recopiés depuis l'activité (jamais saisis).
 */
@Entity
@Table(name = "taches")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Tache extends BaseEntity {

	@Column(name = "code", nullable = false, length = 50)
	private String code;

	@Column(name = "sigle", length = 50)
	private String sigle;

	@Column(name = "libelle", nullable = false, columnDefinition = "TEXT")
	private String libelle;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "activite_id", nullable = false, foreignKey = @ForeignKey(name = "fk_tache_activite"))
	private Activite activite;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "action_id", nullable = false, foreignKey = @ForeignKey(name = "fk_tache_action"))
	private Action action;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "programme_id", nullable = false, foreignKey = @ForeignKey(name = "fk_tache_programme"))
	private Programme programme;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "section_id", foreignKey = @ForeignKey(name = "fk_tache_section"))
	private Section section;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "exercice_id", foreignKey = @ForeignKey(name = "fk_tache_exercice"))
	private Exercice exercice;

	/** Rattache la tâche à une activité et recopie action, programme, section et exercice. */
	public void rattacherA(Activite activite) {
		this.activite = activite;
		this.action = activite.getAction();
		this.programme = activite.getProgramme();
		this.section = activite.getSection();
		this.exercice = activite.getExercice();
	}
}
