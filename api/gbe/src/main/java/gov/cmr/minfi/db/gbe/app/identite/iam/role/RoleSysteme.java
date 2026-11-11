package gov.cmr.minfi.db.gbe.app.identite.iam.role;

import gov.cmr.minfi.db.gbe.app.identite.iam.permission.Permission;

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
                    Permission.REVISER_CP,
                    Permission.EXPRIMER_BESOIN,
                    Permission.LIQUIDER_DEPENSE,
                    Permission.ORDONNANCER
            );
        }
    },

    // ── Nouveau : vue section entière
    MINISTRE("Ministre") {
        @Override
        public Set<Permission> getDefaultPermissions() {
            return Set.of(
                    Permission.ENGAGE_DEPENSE,
                    Permission.REVISER_AE,
                    Permission.REVISER_CP,
                    Permission.REJETER_DEPENSE,
                    Permission.MANAGE_USERS,
                    Permission.MANAGE_AFFECTATIONS,
                    Permission.EXPRIMER_BESOIN,
                    Permission.LIQUIDER_DEPENSE,
                    Permission.ORDONNANCER
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
                    Permission.REJETER_CFI,
                    Permission.RESERVER_CREDIT,
                    Permission.QUITUS_NATURE,
                    Permission.VISER_ORDONNANCEMENT
            );
        }
    },

    // ── Comptable public ──────────────────────────────
    // Prise en charge du mandat et paiement. La liquidation relève de l'ordonnateur
    // (diagramme d'activité, décision 2026-11-11).
    COMPTABLE("Comptable") {
        @Override
        public Set<Permission> getDefaultPermissions() {
            return Set.of(
                    Permission.PAYER_DEPENSE
            );
        }
    },

    // ── Comptable matière ─────────────────────────────
    // Réceptionne les livraisons et établit le PV de réception
    COMPTABLE_MATIERE("Comptable matière") {
        @Override
        public Set<Permission> getDefaultPermissions() {
            return Set.of(
                    Permission.RECEPTIONNER_LIVRAISON,
                    Permission.CONSULTER
            );
        }
    },

    // ── Fournisseur (externe) ─────────────────────────
    // Pas de fiche Agent ni de mandat de section : ne voit que ses propres
    // soumissions et demandes de paiement (périmètre géré dans le lot 1).
    FOURNISSEUR("Fournisseur") {
        @Override
        public Set<Permission> getDefaultPermissions() {
            return Set.of(
                    Permission.SOUMISSIONNER
            );
        }

        @Override
        public boolean isExterne() {
            return true;
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

    /**
     * Rôle externe à l'administration : jamais de mandat ; l'utilisateur reçoit directement
     * les permissions par défaut du rôle (voir UserServiceImpl.loadUserByUsername).
     */
    public boolean isExterne() {
        return false;
    }
}
