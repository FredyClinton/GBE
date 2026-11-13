package gov.cmr.minfi.db.gbe.app.referentiel.matiere;

import gov.cmr.minfi.db.gbe.app.referentiel.matiere.dto.MatiereRequest;
import gov.cmr.minfi.db.gbe.app.referentiel.matiere.dto.MatiereResponse;

import java.util.List;

public interface MatiereService {

    List<MatiereResponse> getAllMatieres();

    MatiereResponse getMatiere(String matiereId);

    MatiereResponse createMatiere(MatiereRequest request);

    MatiereResponse updateMatiere(String matiereId, MatiereRequest request);

    void deleteMatiere(String matiereId);
}
