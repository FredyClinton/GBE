package gov.cmr.minfi.db.gbe.app.depense.engagement.forme;

import gov.cmr.minfi.db.gbe.app.common.exception.BusinessException;
import gov.cmr.minfi.db.gbe.app.common.exception.ErrorCode;
import gov.cmr.minfi.db.gbe.app.depense.engagement.ActeJuridique;
import gov.cmr.minfi.db.gbe.app.depense.soumission.Soumission;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StrategieBonDeCommandeTest {

    private final StrategieBonDeCommande bca = new StrategieBonDeCommande();
    private final Soumission offre45Jours = Soumission.builder().delaiProposeJours(45).build();

    @Test
    void delaiPropose_enMoisArrondiAuSuperieur() {
        assertThat(StrategieBonDeCommande.moisPourJours(1)).isEqualTo(1);
        assertThat(StrategieBonDeCommande.moisPourJours(30)).isEqualTo(1);
        assertThat(StrategieBonDeCommande.moisPourJours(31)).isEqualTo(2);

        final ActeJuridique acte = ActeJuridique.builder().build();
        bca.appliquerDonnees(acte, offre45Jours, new DonneesForme(null, null));
        assertThat(acte.getBonDeCommande().getDelaiLivraisonMois()).isEqualTo(2);
    }

    @Test
    void modification_conserveLeDelaiNonSaisi_etRemplaceLeDevis() {
        final ActeJuridique acte = ActeJuridique.builder().build();
        bca.appliquerDonnees(acte, offre45Jours, new DonneesForme(4, "DEV-1"));
        final BonDeCommande donnees = acte.getBonDeCommande();

        bca.appliquerDonnees(acte, offre45Jours, new DonneesForme(null, " "));

        assertThat(acte.getBonDeCommande()).isSameAs(donnees);
        assertThat(donnees.getDelaiLivraisonMois()).isEqualTo(4);
        assertThat(donnees.getReferenceDevis()).isNull();
    }

    @Test
    void delaiNegatif_refuse() {
        assertThatThrownBy(() -> bca.appliquerDonnees(ActeJuridique.builder().build(), offre45Jours,
                new DonneesForme(-1, null)))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.MONTANT_INVALIDE);
    }
}
