package gov.cmr.minfi.db.gbe.app.depense.engagement;

import gov.cmr.minfi.db.gbe.app.depense.engagement.dto.ActeJuridiqueResponse;
import gov.cmr.minfi.db.gbe.app.depense.soumission.SoumissionMapper;
import org.mapstruct.InheritConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = SoumissionMapper.class)
public interface ActeJuridiqueMapper {

    @Mapping(target = "delaiLivraisonMois", source = "bonDeCommande.delaiLivraisonMois")
    @Mapping(target = "referenceDevis", source = "bonDeCommande.referenceDevis")
    @Mapping(target = "besoinId", source = "expressionBesoin.id")
    @Mapping(target = "besoinReference", source = "expressionBesoin.reference")
    @Mapping(target = "soumissionId", source = "soumission.id")
    @Mapping(target = "soumissionNumero", source = "soumission.numero")
    @Mapping(target = "structureEmettriceCode", source = "expressionBesoin.chapitre.codeComplet")
    @Mapping(target = "structureEmettriceLibelle", source = "expressionBesoin.chapitre.libelleFr")
    @Mapping(target = "exerciceCode", source = "expressionBesoin.exercice.codeExercice")
    @Mapping(target = "sectionCode", source = "expressionBesoin.section.codeSection")
    @Mapping(target = "programmeNumero", source = "expressionBesoin.programme.numero")
    @Mapping(target = "programmeLibelle", source = "expressionBesoin.programme.libelleFr")
    @Mapping(target = "actionNumero", source = "expressionBesoin.tache.action.numero")
    @Mapping(target = "actionLibelle", source = "expressionBesoin.tache.action.libelleFr")
    @Mapping(target = "activiteCode", source = "expressionBesoin.tache.activite.code")
    @Mapping(target = "activiteLibelle", source = "expressionBesoin.tache.activite.libelle")
    @Mapping(target = "tacheCode", source = "expressionBesoin.tache.code")
    @Mapping(target = "tacheLibelle", source = "expressionBesoin.tache.libelle")
    @Mapping(target = "natureEconomiqueCode", source = "expressionBesoin.natureEconomique.code")
    @Mapping(target = "natureEconomiqueLibelle", source = "expressionBesoin.natureEconomique.libelleFr")
    @Mapping(target = "creditBudgetaireId", source = "creditBudgetaire.id")
    @Mapping(target = "reservationNumero", source = "reservation.numero")
    @Mapping(target = "montantReserve", source = "reservation.montant")
    @Mapping(target = "fournisseurId", source = "soumission.fournisseur.id")
    @Mapping(target = "fournisseurNiu", source = "soumission.fournisseur.niu")
    @Mapping(target = "fournisseurRaisonSociale", source = "soumission.fournisseur.raisonSociale")
    @Mapping(target = "fournisseurAdresse", source = "soumission.fournisseur.adresse")
    @Mapping(target = "fournisseurTelephone", source = "soumission.fournisseur.telephone")
    @Mapping(target = "rib", source = "soumission.coordonneeBancaire.rib")
    @Mapping(target = "lignes", source = "soumission.lignes")
    ActeJuridiqueResponse toResponse(ActeJuridique acte);

    /** Pour les listes : sans les lignes (pas de chargement supplémentaire). */
    @InheritConfiguration(name = "toResponse")
    @Mapping(target = "lignes", ignore = true)
    ActeJuridiqueResponse toResume(ActeJuridique acte);
}
