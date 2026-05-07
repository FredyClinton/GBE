package gov.cmr.minfi.db.gbe.app.referentiel.economique;

import gov.cmr.minfi.db.gbe.app.common.audit.BaseEntity;
import gov.cmr.minfi.db.gbe.app.exercice.Exercice;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * Nature économique de la dépense — classification par nature (Articles 17-19 NBE).
 * Cohérente avec le Plan Comptable de l'État (Art. 18 NBE).
 *
 * 4 niveaux obligatoires :
 *   code_titre      : 1 car.  → Titre (1=Dette, 2=Personnel, 3=B&S, 4=Transfert, 5=Invest., 6=Autres)
 *   code_article    : 2 car.  → compte principal PCE
 *   code_paragraphe : 1 car.  → 3 premiers caractères des comptes PCE  (VARCHAR(3) en base)
 *   code_rubrique   : 2 car.  → subdivision du paragraphe              (VARCHAR(10) en base)
 */
@Entity
@Table(name = "natures_economiques")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class NatureEconomique extends BaseEntity {

	@Column(name = "code")
	private String code;

	@Column(name = "code_titre", length = 1)
	private String codeTitre;

	@Column(name = "code_article", length = 2)
	private String codeArticle;

	@Column(name = "code_paragraphe", length = 3)
	private String codeParagraphe;

	@Column(name = "code_rubrique", length = 10)
	private String codeRubrique;

	@Column(name = "libelle", nullable = false, columnDefinition = "TEXT")
	private String libelleFr;

	@Column(name = "libelle_en", columnDefinition = "TEXT")
	private String libelleEn;

	@Column(name = "code_exercice")
	private String codeExercice;

	// Relation

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "exercice_id",
			foreignKey = @ForeignKey(name = "fk_nature_eco_exercice"))
	private Exercice exercice;
}
