package gov.cmr.minfi.db.gbe.app.budget.depense.engagement.forme;

import gov.cmr.minfi.db.gbe.app.budget.depense.engagement.TypeActe;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Échelle des formes d'acte selon le montant TTC (Q3, table {@code seuils_forme_acte}) : BCA < 5 M, LC < 50 M,
 * marché au-delà. Une forme absente de la table (convention de bail) est hors échelle. Table vide : choix libre.
 */
@Component
@RequiredArgsConstructor
public class EchelleFormesActe {

    private final SeuilFormeActeRepository seuilRepository;

    /** Forme la plus légère permise pour ce montant ; vide si la table des seuils est vide. */
    public Optional<TypeActe> formeMinimale(BigDecimal montantTTC) {
        final List<SeuilFormeActe> seuils = seuilRepository.findAllByOrderByRangAsc();
        if (seuils.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(seuils.stream()
                .filter(s -> s.couvre(montantTTC))
                .findFirst()
                .orElse(seuils.get(seuils.size() - 1))
                .getTypeActe());
    }

    /** {@code demande} est plus légère que {@code minimale} sur l'échelle ; faux si l'une est hors échelle. */
    public boolean estInferieure(TypeActe demande, TypeActe minimale) {
        final List<SeuilFormeActe> seuils = seuilRepository.findAllByOrderByRangAsc();
        final Optional<Integer> rangDemande = rang(seuils, demande);
        final Optional<Integer> rangMinimal = rang(seuils, minimale);
        return rangDemande.isPresent() && rangMinimal.isPresent() && rangDemande.get() < rangMinimal.get();
    }

    private static Optional<Integer> rang(List<SeuilFormeActe> seuils, TypeActe type) {
        return seuils.stream().filter(s -> s.getTypeActe() == type).map(SeuilFormeActe::getRang).findFirst();
    }
}
