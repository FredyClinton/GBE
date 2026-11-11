package gov.cmr.minfi.db.gbe.app.depense.engagement;

import gov.cmr.minfi.db.gbe.app.common.pagination.PageResponse;
import gov.cmr.minfi.db.gbe.app.depense.engagement.dto.ActeJuridiqueResponse;
import gov.cmr.minfi.db.gbe.app.depense.engagement.dto.CreationActeRequest;
import gov.cmr.minfi.db.gbe.app.depense.engagement.dto.ModificationActeRequest;
import org.springframework.data.domain.Pageable;

public interface ActeJuridiqueService {

    /** UC04 : projet d'acte à partir d'un besoin ATTRIBUEE et de son offre retenue. */
    ActeJuridiqueResponse creer(CreationActeRequest request);

    ActeJuridiqueResponse modifier(String acteId, ModificationActeRequest request);

    /** UC04 : PROJET → SOUMIS_CONTROLE, ligne de crédit déduite du besoin (Q22). */
    ActeJuridiqueResponse soumettreAuControle(String acteId);

    ActeJuridiqueResponse getActe(String acteId);

    PageResponse<ActeJuridiqueResponse> rechercher(String exerciceId, StatutActe statut, Pageable pageable);
}
