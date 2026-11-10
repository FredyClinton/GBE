package gov.cmr.minfi.db.gbe.app.referentiel.economique;

import gov.cmr.minfi.db.gbe.app.referentiel.economique.dto.NatureEconomiqueResponse;

import java.util.List;

public interface NatureEconomiqueService {

    List<NatureEconomiqueResponse> getAllNaturesEconomiques();

    List<NatureEconomiqueResponse> getNaturesEconomiquesByExercice(String exerciceId);
}
