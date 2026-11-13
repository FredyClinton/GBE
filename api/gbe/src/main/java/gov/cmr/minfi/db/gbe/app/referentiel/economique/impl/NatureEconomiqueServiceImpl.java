package gov.cmr.minfi.db.gbe.app.referentiel.economique.impl;

import gov.cmr.minfi.db.gbe.app.referentiel.ReferentielMapper;
import gov.cmr.minfi.db.gbe.app.referentiel.economique.NatureEconomiqueRepository;
import gov.cmr.minfi.db.gbe.app.referentiel.economique.NatureEconomiqueService;
import gov.cmr.minfi.db.gbe.app.referentiel.economique.dto.NatureEconomiqueResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NatureEconomiqueServiceImpl implements NatureEconomiqueService {

    private final NatureEconomiqueRepository natureEconomiqueRepository;
    private final ReferentielMapper referentielMapper;

    @Override
    public List<NatureEconomiqueResponse> getAllNaturesEconomiques() {
        return natureEconomiqueRepository.findAll().stream()
                .map(referentielMapper::toNatureEconomiqueResponse).toList();
    }

    @Override
    public List<NatureEconomiqueResponse> getNaturesEconomiquesByExercice(String exerciceId) {
        return natureEconomiqueRepository.findByExerciceId(exerciceId).stream()
                .map(referentielMapper::toNatureEconomiqueResponse).toList();
    }
}
