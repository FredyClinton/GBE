package gov.cmr.minfi.db.gbe.app.referentiel.administratif;

import gov.cmr.minfi.db.gbe.app.common.audit.BaseEntity;
import gov.cmr.minfi.db.gbe.app.exercice.Exercice;
import gov.cmr.minfi.db.gbe.app.referentiel.programmatique.Programme;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * Section budgétaire — ministère ou institution constitutionnelle.
 * Classification administrative 1er niveau (Article 9 NBE).
 * Code sur 2 caractères numériques (ex: "20" = Finances).
 */
@Entity
@Table(name = "sections")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Section extends BaseEntity {

	@Column(name = "code")
	private String codeSection;

	@Column(name = "code_exercice")
	private String codeExercice;

	@Column(name = "sigle")
	private String sigle;

	@Column(name = "libelle", columnDefinition = "TEXT")
	private String libelleFr;

	@Column(name = "libelle_en", columnDefinition = "TEXT")
	private String libelleEn;

	// Relations
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "exercice_id", foreignKey = @ForeignKey(name = "fk_section_exercice"))
	private Exercice exercice;

	@OneToMany(mappedBy = "section", cascade = CascadeType.ALL, orphanRemoval = true)
	@Builder.Default
	private List<Chapitre> chapitres = new ArrayList<>();

	@OneToMany(mappedBy = "section", cascade = CascadeType.ALL, orphanRemoval = true)
	@Builder.Default
	private List<Programme> programmes = new ArrayList<>();
}
