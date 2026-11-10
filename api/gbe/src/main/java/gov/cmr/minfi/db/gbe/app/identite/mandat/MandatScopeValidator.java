package gov.cmr.minfi.db.gbe.app.identite.mandat;

import gov.cmr.minfi.db.gbe.app.budget.credit.CreditBudgetaire;
import gov.cmr.minfi.db.gbe.app.common.exception.BusinessException;
import gov.cmr.minfi.db.gbe.app.common.exception.ErrorCode;
import gov.cmr.minfi.db.gbe.app.identite.iam.permission.Permission;
import gov.cmr.minfi.db.gbe.app.identite.iam.role.RoleSysteme;
import gov.cmr.minfi.db.gbe.app.identite.user.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Vérifie que l'utilisateur connecté agit dans le périmètre (section / programme) de ses mandats valides.
 * <p>
 * Règles :
 * <ul>
 *     <li>un mandat ne donne accès qu'à sa section ;</li>
 *     <li>un mandat MINISTRE sans programme couvre toute la section ;</li>
 *     <li>tout autre mandat ne couvre que son programme.</li>
 * </ul>
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class MandatScopeValidator {

    private final MandatRepository mandatRepository;

    /** Vérifie que l'utilisateur connecté a accès au crédit. */
    public void checkAccessCredit(CreditBudgetaire credit) {
        checkAccessCredit(credit, null);
    }

    /** Vérifie, en plus du périmètre, que le mandat concerné possède la permission requise. */
    public void checkAccessCreditWithPermission(CreditBudgetaire credit, Permission permission) {
        checkAccessCredit(credit, Objects.requireNonNull(permission));
    }

    /**
     * Ids des programmes accessibles par l'utilisateur connecté dans une section.
     * L'ensemble contient {@code null} lorsqu'un mandat MINISTRE couvre toute la section.
     */
    public Set<String> getProgrammeIdsAutorises(String sectionId) {
        return mandatsValidesDeUtilisateurCourant().stream()
                .filter(mandat -> mandat.getSection().getId().equals(sectionId))
                .filter(mandat -> mandat.getRoleSysteme() == RoleSysteme.MINISTRE || mandat.getProgramme() != null)
                .map(mandat -> mandat.getProgramme() == null ? null : mandat.getProgramme().getId())
                .collect(Collectors.toSet());
    }

    /** Vérifie que l'utilisateur connecté possède au moins un mandat valide sur la section. */
    public void checkSectionAccess(String sectionId) {
        final User user = getCurrentUser();
        final boolean accesAutorise = mandatRepository.findMandatsValidesParUser(user.getId()).stream()
                .anyMatch(mandat -> mandat.getSection().getId().equals(sectionId));

        if (!accesAutorise) {
            log.warn("Accès refusé : user={} section={}", user.getId(), sectionId);
            throw new BusinessException(ErrorCode.ACCES_REFUSE_HORS_SCOPE);
        }
    }

    // ── Interne ───────────────────────────────────────

    /** @param permission permission requise, ou {@code null} pour un simple contrôle de périmètre */
    private void checkAccessCredit(CreditBudgetaire credit, Permission permission) {
        final User user = getCurrentUser();
        final List<Mandat> mandats = mandatRepository.findMandatsValidesParUser(user.getId());

        if (mandats.isEmpty()) {
            throw new BusinessException(ErrorCode.ACCES_REFUSE_AUCUN_MANDAT);
        }

        final String sectionId = credit.getSection().getId();
        final String programmeId = credit.getProgramme().getId();

        final boolean accesAutorise = mandats.stream().anyMatch(mandat ->
                mandatCorrespondAuCredit(mandat, sectionId, programmeId)
                        && (permission == null || mandat.hasPermission(permission))
        );

        if (!accesAutorise) {
            log.warn("Accès refusé : user={} credit={} section={} programme={} permission={}",
                    user.getId(), credit.getId(), sectionId, programmeId, permission);
            throw new BusinessException(ErrorCode.ACCES_REFUSE_HORS_SCOPE);
        }
    }

    private List<Mandat> mandatsValidesDeUtilisateurCourant() {
        return mandatRepository.findMandatsValidesParUser(getCurrentUser().getId());
    }

    private static boolean mandatCorrespondAuCredit(Mandat mandat, String sectionId, String programmeId) {
        if (!mandat.getSection().getId().equals(sectionId)) {
            return false;
        }
        if (mandat.getRoleSysteme() == RoleSysteme.MINISTRE && mandat.getProgramme() == null) {
            return true;
        }
        return mandat.getProgramme() != null && mandat.getProgramme().getId().equals(programmeId);
    }

    private static User getCurrentUser() {
        return (User) Objects.requireNonNull(SecurityContextHolder.getContext().getAuthentication())
                .getPrincipal();
    }
}
