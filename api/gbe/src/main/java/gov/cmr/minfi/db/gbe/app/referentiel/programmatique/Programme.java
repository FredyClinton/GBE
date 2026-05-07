package gov.cmr.minfi.db.gbe.app.referentiel.programmatique;

import gov.cmr.minfi.db.gbe.app.common.audit.BaseEntity;
import gov.cmr.minfi.db.gbe.app.exercice.Exercice;
import gov.cmr.minfi.db.gbe.app.referentiel.administratif.Section;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * Programme budgétaire — regroupe les crédits d'un même ministère
 * pour un ensemble cohérent d'actions (Articles 13-14 NBE, Art. 30 RF).
 *
 * - Numéro séquentiel sur 3 caractères à partir de 001 (Art. 14 NBE)
 * - Indépendant du ministère gestionnaire
 * - Rattaché à une seule section (Art. 30 al.1 RF)
 */
@Entity
@Table(name = "programmes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Programme extends BaseEntity {

	@Column(name = "code")
	private String code;

	@Column(name = "code_exercice")
	private String codeExercice;

	@Column(name = "code_section")
	private String codeSection;

	@Column(name = "libelle", columnDefinition = "TEXT")
	private String libelleFr;

	@Column(name = "libelle_en", columnDefinition = "TEXT")
	private String libelleEn;

	@Column(name = "numero")
	private String numero;

	// Relations
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "section_id", foreignKey = @ForeignKey(name = "fk_programme_section"))
	private Section section;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "exercice_id", foreignKey = @ForeignKey(name = "fk_programme_exercice"))
	private Exercice exercice;

	@OneToMany(mappedBy = "programme", cascade = CascadeType.ALL, orphanRemoval = true)
	@Builder.Default
	private List<Action> actions = new ArrayList<>();
}
