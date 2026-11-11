package gov.cmr.minfi.db.gbe.app.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

import static org.springframework.http.HttpStatus.*;

@Getter
public enum ErrorCode {
    USER_NOT_FOUND("USER_NOT_FOUND", "User not found with id %s", NOT_FOUND),
    CHANGE_PASSWORD_MISMATCH("CHANGE_PASSWORD_MISMATCH", "The new password and his confirmed are differents", BAD_REQUEST),
    INVALID_CURRENT_PASSWORD("INVALID_CURRENT_PASSWORD", "The current password is invalid", BAD_REQUEST),
    ACCOUNT_ALREADY_DEACTIVATED("ACCOUNT_ALREADY_DEACTIVATED", "The account is already deactivated", BAD_REQUEST),
    EMAIL_ALREADY_EXISTS("EMAIL_ALREADY_EXISTS", "Email already exists", BAD_REQUEST),
    PHONE_NUMBER_ALREADY_EXISTS("PHONE_NUMBER_ALREADY_EXISTS", "Phone number already exists", BAD_REQUEST),
    PASSWORD_MISMATCH("PASSWORD_MISMATCH", "passwords does not match", BAD_REQUEST),
    ERR_USER_DISABLED("ERR_USER_DISABLED", "user is disabled", UNAUTHORIZED),
    BAD_CREDENTIALS("BAD_CREDENTIALS", "Bad credentials", UNAUTHORIZED),
    USERNAME_NOT_FOUND("USERNAME_NOT_FOUND", "User not found with username %s", NOT_FOUND),
    INTERNAL_EXCEPTION("INTERNAL_EXCEPTION", "Internal exception", INTERNAL_SERVER_ERROR),
    ENTITY_NOT_FOUND("ENTITY_NOT_FOUND", "Entity not found with  %s", NOT_FOUND),
    METHODE_ARGUMENT_NOT_VALID("METHODE_ARGUMENT_NOT_VALID", "Method argument not valid", BAD_REQUEST),
    NUI_ALREADY_EXISTS("NUI_ALREADY_EXISTS", "this nui is already use", BAD_REQUEST),
    CNI_ALREADY_EXISTS("CNI_ALREADY_EXISTS", "this cni is already use", BAD_REQUEST),
    ACCOUNT_ALREADY_ACTIVATED("ACCOUNT_ALREADY_ACTIVATED", "this account is already activated", BAD_REQUEST),
    MATRICULE_ALREADY_EXISTS("MATRICULE_ALREADY_EXISTS", "this matricule is already use", BAD_REQUEST),
    MFA_ALREADY_CONFIRMED("MFA_ALREADY_CONFIRMED", "mfa is already confirmed for this user", BAD_REQUEST),
    AFFECTATION_ALREADY_EXISTS("AFFECTATION_ALREADY_EXISTS", "Affectation already exists for this user and programme", BAD_REQUEST),
    AFFECTATION_ALREADY_ACTIVE("AFFECTATION_ALREADY_ACTIVE", "Affectation is already active", BAD_REQUEST),
    AFFECTATION_ALREADY_INACTIVE("AFFECTATION_ALREADY_INACTIVE", "Affectation is already inactive", BAD_REQUEST),
    AFFECTATION_NOT_FOUND("AFFECTATION_NOT_FOUND", "Affectation not found for this user", NOT_FOUND),
    ACTION_NOT_IN_PROGRAMME("ACTION_NOT_IN_PROGRAMME", "This action does not belong to the specified programme", BAD_REQUEST),
    ACTION_NOT_FOUND("ACTION_NOT_FOUND", "Action not found with id %s", NOT_FOUND),
    ACTION_ALREADY_EXISTS("ACTION_ALREADY_EXISTS", "An action with code %s already exists in this programme", BAD_REQUEST),
    PROGRAMME_NOT_IN_SECTION("PROGRAMME_NOT_IN_SECTION", "This programme does not belong to the specified section", BAD_REQUEST),
    NATURE_ECONOMIQUE_NOT_IN_EXERCICE("NATURE_ECONOMIQUE_NOT_IN_EXERCICE",
            "La nature économique %s n'appartient pas à l'exercice du crédit", BAD_REQUEST),
    CREDIT_ALREADY_EXISTS("CREDIT_ALREADY_EXISTS", " this credit was already created", BAD_REQUEST),
    NEW_AE_AMOUNT_LESS_THAN_CONSOMME("NEW_AE_AMOUNT_LESS_THAN_CONSOMME",
            "New AE amount cannot be less than already consumed amount", BAD_REQUEST),
    NEW_CP_AMOUNT_LESS_THAN_CONSOMME("NEW_CP_AMOUNT_LESS_THAN_CONSOMME",
            "New CP amount cannot be less than already consumed amount", BAD_REQUEST),
    INVALID_MFA_TOKEN("INVALID_MFA_TOKEN", "Invalid or expired MFA token", UNAUTHORIZED),
    MFA_TOKEN_EXPIRED("MFA_TOKEN_EXPIRED", "MFA token has expired, please login again", UNAUTHORIZED),
    CHAPITRE_CYCLE_DETECTED("CHAPITRE_CYCLE_DETECTED", "This requete may create a chapter cycle", BAD_REQUEST),
    TRANSITION_STATUT_INVALIDE("TRANSITION_STATUT_INVALIDE",
            "Transition invalide depuis le statut %s vers %s : %s",
            BAD_REQUEST
    ),
    MONTANT_AE_INSUFFISANT(
            "MONTANT_AE_INSUFFISANT",
            "Le montant AE disponible est insuffisant pour cette opération",
            BAD_REQUEST
    ),
    MONTANT_CP_INSUFFISANT(
            "MONTANT_CP_INSUFFISANT",
            "Le montant CP disponible est insuffisant pour cette opération",
            BAD_REQUEST
    ),
    MONTANT_INVALIDE(
            "MONTANT_INVALIDE",
            "Le montant doit être strictement positif",
            BAD_REQUEST
    ),
    MONTANT_LIBERE_SUPERIEUR_CONSOMME(
            "MONTANT_LIBERE_SUPERIEUR_CONSOMME",
            "Le montant %s à libérer dépasse le montant consommé",
            BAD_REQUEST
    ),
    CREDIT_NON_CONSOMMABLE(
            "CREDIT_NON_CONSOMMABLE",
            "Un crédit au statut %s ne peut pas être consommé",
            BAD_REQUEST
    ),
    CREDIT_ANNULATION_CREDITS_CONSOMMES(
            "CREDIT_ANNULATION_CREDITS_CONSOMMES",
            "Annulation impossible : %s d'AE et %s de CP sont consommés sur ce crédit",
            BAD_REQUEST
    ),
    FOURNISSEUR_NOT_FOUND("FOURNISSEUR_NOT_FOUND", "Fournisseur introuvable : %s", NOT_FOUND),
    FOURNISSEUR_LIE_AU_COMPTE("FOURNISSEUR_LIE_AU_COMPTE",
            "Ce compte est celui d'un fournisseur : désactivez-le au lieu de le supprimer", BAD_REQUEST),
    RIB_INVALIDE("RIB_INVALIDE",
            "RIB invalide : %s (23 caractères attendus : banque 5 chiffres, guichet 5 chiffres, compte 11, clé 2 chiffres)",
            BAD_REQUEST),
    RIB_ALREADY_EXISTS("RIB_ALREADY_EXISTS", "Le RIB %s est déjà enregistré pour ce fournisseur", BAD_REQUEST),
    RIB_NOT_FOUND("RIB_NOT_FOUND", "RIB introuvable pour ce fournisseur : %s", NOT_FOUND),
    ROLE_EXTERNE_SANS_MANDAT("ROLE_EXTERNE_SANS_MANDAT",
            "Le rôle %s est externe : il ne peut pas porter de mandat", BAD_REQUEST),
    ACTION_A_DES_ACTIVITES("ACTION_A_DES_ACTIVITES",
            "L'action %s contient des activités : supprimez-les ou déplacez-les d'abord", BAD_REQUEST),
    ACTIVITE_NOT_FOUND("ACTIVITE_NOT_FOUND", "Activité introuvable : %s", NOT_FOUND),
    ACTIVITE_ALREADY_EXISTS("ACTIVITE_ALREADY_EXISTS", "Une activité de code %s existe déjà dans cette action", BAD_REQUEST),
    ACTIVITE_A_DES_TACHES("ACTIVITE_A_DES_TACHES",
            "L'activité %s contient des tâches : supprimez-les ou déplacez-les d'abord", BAD_REQUEST),
    TACHE_NOT_FOUND("TACHE_NOT_FOUND", "Tâche introuvable : %s", NOT_FOUND),
    TACHE_ALREADY_EXISTS("TACHE_ALREADY_EXISTS", "Une tâche de code %s existe déjà dans cette activité", BAD_REQUEST),
    BESOIN_NOT_FOUND("BESOIN_NOT_FOUND", "Expression de besoin introuvable : %s", NOT_FOUND),
    BESOIN_NON_MODIFIABLE("BESOIN_NON_MODIFIABLE",
            "L'expression de besoin %s n'est plus modifiable (statut %s, date limite passée ou soumission déjà déposée)",
            BAD_REQUEST),
    BESOIN_SANS_LIGNE("BESOIN_SANS_LIGNE", "L'expression de besoin doit contenir au moins une ligne", BAD_REQUEST),
    BESOIN_MATIERE_EN_DOUBLE("BESOIN_MATIERE_EN_DOUBLE",
            "La matière %s figure déjà sur une autre ligne de ce besoin", BAD_REQUEST),
    BESOIN_DATE_CLOTURE_INVALIDE("BESOIN_DATE_CLOTURE_INVALIDE",
            "La date limite de soumission doit être renseignée et située dans le futur", BAD_REQUEST),
    BESOIN_CLOTURE_PREMATUREE("BESOIN_CLOTURE_PREMATUREE",
            "Les soumissions ne peuvent être clôturées qu'après la date limite (%s)", BAD_REQUEST),
    BESOIN_CHAPITRE_HORS_SECTION("BESOIN_CHAPITRE_HORS_SECTION",
            "Le chapitre %s n'appartient pas à la section de la tâche", BAD_REQUEST),
    MATIERE_NOT_FOUND("MATIERE_NOT_FOUND", "Matière introuvable : %s", NOT_FOUND),
    MATIERE_ALREADY_EXISTS("MATIERE_ALREADY_EXISTS", "Une matière de référence %s existe déjà", BAD_REQUEST),
    MANDAT_ALREADY_EXISTS(
            "MANDAT_ALREADY_EXISTS",
            "Un mandat existe déjà pour cet utilisateur sur ce périmètre",
            BAD_REQUEST
    ),
    MANDAT_NOT_FOUND(
            "MANDAT_NOT_FOUND",
            "Mandat non trouvé pour cet utilisateur : %s",
            NOT_FOUND
    ),
    MANDAT_ALREADY_ACTIVE(
            "MANDAT_ALREADY_ACTIVE",
            "Ce mandat est déjà actif",
            BAD_REQUEST
    ),
    MANDAT_ALREADY_INACTIVE(
            "MANDAT_ALREADY_INACTIVE",
            "Ce mandat est déjà inactif",
            BAD_REQUEST
    ),
    USER_ALREADY_EXISTS_FOR_AGENT("USER_ALREADY_EXISTS_FOR_AGENT", "USER_ALREADY_EXISTS_FOR_AGENT", BAD_REQUEST),
    ACCES_REFUSE_HORS_SCOPE(
            "ACCES_REFUSE_HORS_SCOPE",
            "Accès refusé : ce crédit n'est pas dans votre périmètre de mandat",
            FORBIDDEN
    ),
    ACCES_REFUSE_PERMISSION(
            "ACCES_REFUSE_PERMISSION",
            "Accès refusé : vous n'avez pas la permission requise pour cette opération",
            FORBIDDEN
    ),
    ACCES_REFUSE_HORS_PERIMETRE(
            "ACCES_REFUSE_HORS_PERIMETRE",
            "Accès refusé : cet élément n'est pas dans le périmètre de vos mandats (ou la permission manque)",
            FORBIDDEN
    ),
    ACCES_REFUSE_AUCUN_MANDAT(
            "ACCES_REFUSE_AUCUN_MANDAT",
            "Accès refusé : aucun mandat actif trouvé pour cet utilisateur",
            FORBIDDEN
    );

    private final String code;
    private final String defaultMessage;
    private final HttpStatus httpStatus;

    ErrorCode(final String code, final String defaultMessage, final HttpStatus httpStatus) {
        this.code = code;
        this.defaultMessage = defaultMessage;
        this.httpStatus = httpStatus;
    }


}
