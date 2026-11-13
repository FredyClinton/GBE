package gov.cmr.minfi.db.gbe.app.common.reference;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Numérotation des références métier (EB-2026-00001, …) : un compteur par clé,
 * incrémenté de façon atomique en base (table compteurs_reference, V15).
 * L'incrément se fait dans la transaction appelante : si elle échoue, le numéro est rendu ; la ligne du
 * compteur reste verrouillée jusqu'au commit, donc deux créations simultanées sur la même clé s'enchaînent
 * (pas de doublon). Un brouillon supprimé logiquement garde sa référence.
 */
@Component
@RequiredArgsConstructor
public class ReferenceGenerator {

    private final JdbcTemplate jdbcTemplate;

    /** Prochain numéro pour la clé (1 au premier appel). */
    public long prochainNumero(String cle) {
        final Long valeur = jdbcTemplate.queryForObject("""
                INSERT INTO compteurs_reference (cle, valeur) VALUES (?, 1)
                ON CONFLICT (cle) DO UPDATE SET valeur = compteurs_reference.valeur + 1
                RETURNING valeur
                """, Long.class, cle);
        return valeur == null ? 1 : valeur;
    }

    /** Référence de la forme PREFIXE-ANNEE-00001, numérotée par préfixe et par année. */
    public String prochaineReference(String prefixe, int annee) {
        return "%s-%d-%05d".formatted(prefixe, annee, prochainNumero(prefixe + "-" + annee));
    }
}
