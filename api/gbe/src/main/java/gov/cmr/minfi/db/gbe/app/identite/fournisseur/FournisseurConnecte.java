package gov.cmr.minfi.db.gbe.app.identite.fournisseur;

import gov.cmr.minfi.db.gbe.app.common.exception.BusinessException;
import gov.cmr.minfi.db.gbe.app.common.exception.ErrorCode;
import gov.cmr.minfi.db.gbe.app.identite.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/** Fiche fournisseur de l'utilisateur connecté (le fournisseur ne voit que ses propres données). */
@Component
@RequiredArgsConstructor
public class FournisseurConnecte {

    private final FournisseurRepository fournisseurRepository;

    public Fournisseur exiger() {
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof User user)) {
            throw new BusinessException(ErrorCode.FOURNISSEUR_NOT_FOUND, "utilisateur courant");
        }
        return fournisseurRepository.findByUserId(user.getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.FOURNISSEUR_NOT_FOUND, user.getEmail()));
    }
}
