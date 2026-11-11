package gov.cmr.minfi.db.gbe.app.identite.fournisseur;

import gov.cmr.minfi.db.gbe.app.identite.fournisseur.dto.CreateFournisseurRequest;
import gov.cmr.minfi.db.gbe.app.identite.fournisseur.dto.FournisseurResponse;
import gov.cmr.minfi.db.gbe.app.identite.fournisseur.dto.RibRequest;
import gov.cmr.minfi.db.gbe.app.identite.fournisseur.dto.UpdateFournisseurRequest;

import java.util.List;

public interface FournisseurService {

    FournisseurResponse createFournisseur(CreateFournisseurRequest request);

    List<FournisseurResponse> getAllFournisseurs();

    FournisseurResponse getFournisseur(String fournisseurId);

    FournisseurResponse updateFournisseur(String fournisseurId, UpdateFournisseurRequest request);

    FournisseurResponse ajouterRib(String fournisseurId, RibRequest request);

    FournisseurResponse retirerRib(String fournisseurId, String coordonneeBancaireId);

    /** Fiche du fournisseur connecté. */
    FournisseurResponse getMonProfil();
}
