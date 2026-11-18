package gov.cmr.minfi.db.gbe.app.budget.depense.engagement;

import gov.cmr.minfi.db.gbe.app.common.pagination.PageResponse;
import gov.cmr.minfi.db.gbe.app.budget.depense.engagement.dto.ActeJuridiqueResponse;
import gov.cmr.minfi.db.gbe.app.budget.depense.engagement.dto.CreationActeRequest;
import gov.cmr.minfi.db.gbe.app.budget.depense.engagement.dto.ModificationActeRequest;
import org.springframework.data.domain.Pageable;

public interface ActeJuridiqueService {

    /** UC04 : projet d'acte à partir d'un besoin ATTRIBUEE et de son offre retenue. */
    ActeJuridiqueResponse creer(CreationActeRequest request);

    ActeJuridiqueResponse modifier(String acteId, ModificationActeRequest request);

    /** UC04 : PROJET → SOUMIS_CONTROLE, ligne de crédit déduite du besoin (Q22). */
    ActeJuridiqueResponse soumettreAuControle(String acteId);

    /** UC05 : VISE_CF → SIGNE_ORDONNATEUR → NOTIFIE_PRESTATAIRE dans la même opération (Q6, Q20). */
    ActeJuridiqueResponse signer(String acteId);

    /** Acte visé, signé ou notifié → ANNULE ; réservation annulée et AE libérées (Q2). */
    ActeJuridiqueResponse annuler(String acteId, String motif);

    ActeJuridiqueResponse getActe(String acteId);

    /** Espace fournisseur : actes notifiés au fournisseur connecté. */
    PageResponse<ActeJuridiqueResponse> mesActesFournisseur(Pageable pageable);

    ActeJuridiqueResponse getActeFournisseur(String acteId);

    PageResponse<ActeJuridiqueResponse> rechercher(String exerciceId, StatutActe statut, Pageable pageable);
}
