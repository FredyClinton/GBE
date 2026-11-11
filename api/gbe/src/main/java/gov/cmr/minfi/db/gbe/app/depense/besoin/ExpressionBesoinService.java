package gov.cmr.minfi.db.gbe.app.depense.besoin;

import gov.cmr.minfi.db.gbe.app.depense.besoin.dto.AnnulationBesoinRequest;
import gov.cmr.minfi.db.gbe.app.depense.besoin.dto.ExpressionBesoinRequest;
import gov.cmr.minfi.db.gbe.app.depense.besoin.dto.ExpressionBesoinResponse;

import gov.cmr.minfi.db.gbe.app.common.pagination.PageResponse;
import org.springframework.data.domain.Pageable;

public interface ExpressionBesoinService {

    ExpressionBesoinResponse creer(ExpressionBesoinRequest request);

    ExpressionBesoinResponse modifier(String besoinId, ExpressionBesoinRequest request);

    ExpressionBesoinResponse publier(String besoinId);

    ExpressionBesoinResponse cloturerSoumissions(String besoinId);

    ExpressionBesoinResponse annuler(String besoinId, AnnulationBesoinRequest request);

    void supprimer(String besoinId);

    ExpressionBesoinResponse getBesoin(String besoinId);

    /** Besoins du périmètre de l'utilisateur connecté, paginés ; filtres facultatifs. */
    PageResponse<ExpressionBesoinResponse> rechercher(String exerciceId, String sectionId, String programmeId,
                                                      StatutExpression statut, Pageable pageable);
}
