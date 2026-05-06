package gov.cmr.minfi.db.gbe.app.mandat;

import gov.cmr.minfi.db.gbe.app.budget.credit.CreditBudgetaire;
import gov.cmr.minfi.db.gbe.app.common.exception.BusinessException;
import gov.cmr.minfi.db.gbe.app.common.exception.ErrorCode;
import gov.cmr.minfi.db.gbe.app.iam.permission.Permission;
import gov.cmr.minfi.db.gbe.app.iam.role.RoleSysteme;
import gov.cmr.minfi.db.gbe.app.user.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class MandatScopeValidator {

    private final MandatRepository mandatRepository;

    // verifie que l'utilisateur connecté a accès au credit
    public void checkAccessCredit(CreditBudgetaire credit) {
        final User user = getCurrentUser();
        final List<Mandat> mandats = mandatRepository.findMandatsValidesParUser(user.getId());


        if (mandats.isEmpty()) {
            throw new BusinessException(ErrorCode.ACCES_REFUSE_AUCUN_MANDAT);
        }

        final String sectionId = credit.getSection().getId();
        final String programmeId = credit.getProgramme().getId();

        final boolean accesAutorise = mandats.stream().anyMatch(
                mandat -> mandatCorrespondAuCredit(mandat, sectionId, programmeId)
        );

        if (!accesAutorise) {
            log.warn("Accès refusé : user={} credit={} section={} programme={}",
                    user.getId(), credit.getId(), sectionId, programmeId);
            throw new BusinessException(ErrorCode.ACCES_REFUSE_HORS_SCOPE);
        }


    }

    // En plus du scop, verifie que le mandat possede la permission requise
    public void checkAccessCreditWithPermission(CreditBudgetaire credit, Permission permission) {
        final User user = getCurrentUser();
        final List<Mandat> mandats = mandatRepository.findMandatsValidesParUser(user.getId());

        if (mandats.isEmpty()) {
            throw new BusinessException(ErrorCode.ACCES_REFUSE_AUCUN_MANDAT);
        }

        final String sectionId = credit.getSection().getId();
        final String programmeId = credit.getProgramme().getId();

        final boolean accesAutorise = mandats.stream().anyMatch(mandat ->
                mandatCorrespondAuCredit(mandat, sectionId, programmeId)
                        && mandat.hasPermission(permission)
        );

        if (!accesAutorise) {
            log.warn("Accès refusé : user={} credit={} permission={}",
                    user.getId(), credit.getId(), permission);
            throw new BusinessException(ErrorCode.ACCES_REFUSE_HORS_SCOPE);
        }
    }

    // Retourne les Ids de programme accessible par l'utilisateur connect pour une section donnee
    public Set<String> getProgrammeIdsAutorises(String sectionId) {
        final User user = getCurrentUser();
        final List<Mandat> mandats = mandatRepository
                .findMandatsValidesParUser(user.getId());

        return mandats.stream()
                .filter(mandat -> mandat.getSection().getId().equals(sectionId))
                .filter(mandat -> {
                    // Le ministre a tous les programme
                    if (mandat.getRoleSysteme() == RoleSysteme.MINISTRE) {
                        return true;
                    }

                    // Autres roles
                    return mandat.getProgramme() != null;
                })
                .map(
                        mandat -> {
                            // MINISTRE - retourn null comme signa "tous les programmes"
                            if (mandat.getProgramme() == null) return null;
                            return mandat.getProgramme().getId();
                        }
                ).collect(Collectors.toSet());

    }

    public void checkSectionAccess(String sectionId) {
        final User user = getCurrentUser();
        final List<Mandat> mandats = mandatRepository
                .findMandatsValidesParUser(user.getId());

        final boolean accessAutorise = mandats.stream().anyMatch(
                mandat -> mandat.getSection().getId().equals(sectionId)
        );
        if (!accessAutorise) {
            log.warn("Accès refusé : user={} section={}", user.getId(), sectionId);
            throw new BusinessException(ErrorCode.ACCES_REFUSE_HORS_SCOPE);
        }
    }

    private boolean mandatCorrespondAuCredit(Mandat mandat, String sectionId, String programmeId) {
        if (!mandat.getSection().getId().equals(sectionId)) {
            return false;
        }

        // LE MINISTRE - acces a toute la section
        if (mandat.getRoleSysteme() == RoleSysteme.MINISTRE && mandat.getProgramme() == null) {
            return true;
        }

        // Autre roles - le programme doit correspondre

        return mandat.getProgramme() != null && mandat.getProgramme().getId().equals(programmeId);
    }

    private User getCurrentUser() {
        return (User) Objects.requireNonNull(SecurityContextHolder.getContext() // TODO: Enlever le required si necessaire
                        .getAuthentication())
                .getPrincipal();
    }
}
