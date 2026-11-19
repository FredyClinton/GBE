package gov.cmr.minfi.db.gbe.app.common.config;

import io.swagger.v3.oas.models.media.Schema;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.type.filter.RegexPatternTypeFilter;

import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Ordre des schémas dans Swagger : par domaine, dans l'ordre des tags ({@link OpenApiConfig}), puis alphabétique.
 * Seule la liste {@code components.schemas} est réordonnée : les propriétés de chaque schéma et les routes gardent
 * leur ordre. Le domaine d'un schéma est le package de la classe du même nom ; un schéma générique
 * (ex. {@code PageResponseActeJuridiqueResponse}) suit la classe par laquelle son nom se termine.
 */
@Configuration
public class OpenApiSchemasConfig {

    private static final String RACINE = "gov.cmr.minfi.db.gbe.app";

    /** Préfixes de package, dans l'ordre d'affichage ; le premier qui correspond donne le rang du domaine. */
    private static final List<String> ORDRE_DOMAINES = List.of(
            "identite.auth",
            "identite.user",
            "admin",
            "identite.agent",
            "identite.mandat",
            "identite.iam",
            "identite.fournisseur",
            "referentiel.exercice",
            "referentiel.administratif",
            "referentiel.programmatique",
            "referentiel.economique",
            "referentiel.matiere",
            "referentiel.taxe",
            "referentiel",
            "budget.credit",
            "budget.depense.besoin",
            "budget.depense.soumission",
            "budget.depense.engagement",
            "budget.depense.controle",
            "budget.depense",
            "notification",
            "common"
    );

    @Bean
    public OpenApiCustomizer trierSchemasParDomaine() {
        final Map<String, Integer> rangParClasse = rangsDesClassesDuProjet();
        return openApi -> {
            if (openApi.getComponents() == null || openApi.getComponents().getSchemas() == null) {
                return;
            }
            final var schemas = openApi.getComponents().getSchemas();
            // swagger-models expose Map<String, Schema> avec Schema en type brut : impossible de le paramétrer ici
            @SuppressWarnings("rawtypes")
            final Map<String, Schema> tries = new LinkedHashMap<>();
            schemas.keySet().stream()
                    .sorted(Comparator.comparingInt((String nom) -> rang(nom, rangParClasse))
                            .thenComparing(String.CASE_INSENSITIVE_ORDER))
                    .forEach(nom -> tries.put(nom, schemas.get(nom)));
            openApi.getComponents().setSchemas(tries);
        };
    }

    /** Rang du domaine ; les schémas hors projet (Spring, etc.) viennent en dernier. */
    private static int rang(String schema, Map<String, Integer> rangParClasse) {
        final Integer direct = rangParClasse.get(schema);
        if (direct != null) {
            return direct;
        }
        return rangParClasse.entrySet().stream()
                .filter(e -> schema.endsWith(e.getKey()))
                .max(Comparator.comparingInt(e -> e.getKey().length()))
                .map(Map.Entry::getValue)
                .orElse(ORDRE_DOMAINES.size());
    }

    /** Nom simple de chaque classe du projet (DTO, énumération…) → rang de son domaine. */
    private static Map<String, Integer> rangsDesClassesDuProjet() {
        final ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false) {
            @Override
            protected boolean isCandidateComponent(AnnotatedBeanDefinition beanDefinition) {
                return true;
            }
        };
        scanner.addIncludeFilter(new RegexPatternTypeFilter(Pattern.compile(".*")));
        final Map<String, Integer> rangs = new HashMap<>();
        for (var candidat : scanner.findCandidateComponents(RACINE)) {
            final String nomComplet = candidat.getBeanClassName();
            if (nomComplet == null) {
                continue;
            }
            final String nomSimple = nomComplet.substring(Math.max(nomComplet.lastIndexOf('.'), nomComplet.lastIndexOf('$')) + 1);
            rangs.putIfAbsent(nomSimple, rangDuPackage(nomComplet.substring(RACINE.length() + 1)));
        }
        return rangs;
    }

    private static int rangDuPackage(String cheminRelatif) {
        for (int i = 0; i < ORDRE_DOMAINES.size(); i++) {
            final String prefixe = ORDRE_DOMAINES.get(i);
            if (cheminRelatif.equals(prefixe) || cheminRelatif.startsWith(prefixe + ".")) {
                return i;
            }
        }
        return ORDRE_DOMAINES.size();
    }
}
