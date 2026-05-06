package gov.cmr.minfi.db.gbe.app.iam.role;

import gov.cmr.minfi.db.gbe.app.iam.permission.Permission;

import java.util.Set;

public enum RoleSysteme {

    ADMIN("Administrateur") {
        @Override
        public Set<Permission> getDefaultPermissions() {
            return Set.of(
                    Permission.MANAGE_USERS,
                    Permission.MANAGE_AFFECTATIONS,
                    Permission.INSCRIRE_CREDIT
            );
        }
    },

    // ── Fusion des 3 anciens ordonnateurs ─────────────
    // Le scope (section/programme/chapitre) est porté par le Mandat
    ORDONNATEUR("Ordonnateur") {
        @Override
        public Set<Permission> getDefaultPermissions() {
            return Set.of(
                    Permission.ENGAGE_DEPENSE,
                    Permission.REVISER_AE,
                    Permission.REVISER_CP
            );
        }
    },

    // ── Nouveau : vue section entière ─────────────────
    // Art. 66 al.2 — chef de département ministériel
    MINISTRE("Ministre") {
        @Override
        public Set<Permission> getDefaultPermissions() {
            return Set.of(
                    Permission.ENGAGE_DEPENSE,
                    Permission.REVISER_AE,
                    Permission.REVISER_CP,
                    Permission.REJETER_DEPENSE,
                    Permission.MANAGE_USERS,
                    Permission.MANAGE_AFFECTATIONS
            );
        }
    },

    // ── Contrôleur financier ──────────────────────────
    // Art. 70 — visa obligatoire avant engagement
    CONTROLEUR_FINANCIER("Contrôleur financier") {
        @Override
        public Set<Permission> getDefaultPermissions() {
            return Set.of(
                    Permission.VISA_CFI,
                    Permission.REJETER_CFI
            );
        }
    },

    // ── Comptable ─────────────────────────────────────
    COMPTABLE("Comptable") {
        @Override
        public Set<Permission> getDefaultPermissions() {
            return Set.of(
                    Permission.LIQUIDER_DEPENSE,
                    Permission.PAYER_DEPENSE
            );
        }
    },

    // ── Nouveau : lecture seule ───────────────────────
    // Lié à Agent — consulte les crédits et dépenses qui le concernent
    GESTIONNAIRE("Gestionnaire") {
        @Override
        public Set<Permission> getDefaultPermissions() {
            return Set.of(
                    Permission.CONSULTER,
                    Permission.ENGAGE_DEPENSE,
                    Permission.REVISER_AE,
                    Permission.REVISER_CP,
                    Permission.VISA_CFI
            );
        }
    },

    // ── Nouveau : rôle global de base ─────────────────
    // Tout agent référencé dans le système
    AGENT("Agent") {
        @Override
        public Set<Permission> getDefaultPermissions() {
            return Set.of(
                    Permission.CONSULTER
            );
        }
    };

    private final String libelle;

    RoleSysteme(String libelle) {
        this.libelle = libelle;
    }

    public String getLibelle() {
        return libelle;
    }

    public abstract Set<Permission> getDefaultPermissions();
}