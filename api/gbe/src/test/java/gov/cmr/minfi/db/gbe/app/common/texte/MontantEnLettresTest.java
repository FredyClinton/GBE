package gov.cmr.minfi.db.gbe.app.common.texte;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class MontantEnLettresTest {

    @ParameterizedTest
    @CsvSource(delimiter = ';', value = {
            "0;zéro",
            "1;un",
            "16;seize",
            "17;dix-sept",
            "21;vingt et un",
            "22;vingt-deux",
            "70;soixante-dix",
            "71;soixante et onze",
            "72;soixante-douze",
            "80;quatre-vingts",
            "81;quatre-vingt-un",
            "91;quatre-vingt-onze",
            "99;quatre-vingt-dix-neuf",
            "100;cent",
            "101;cent un",
            "200;deux cents",
            "201;deux cent un",
            "280;deux cent quatre-vingts",
            "1000;mille",
            "1001;mille un",
            "2000;deux mille",
            "80000;quatre-vingt mille",
            "200000;deux cent mille",
            "1000000;un million",
            "2000000;deux millions",
            "200000000;deux cents millions",
            "80000000;quatre-vingts millions",
            "1000000000;un milliard",
            // BCA de référence (plan §4.5)
            "4913100;quatre millions neuf cent treize mille cent",
            "3893400;trois millions huit cent quatre-vingt-treize mille quatre cents",
            "4120000;quatre millions cent vingt mille",
            "44719.94;quarante-quatre mille sept cent dix-neuf et quatre-vingt-quatorze centimes",
    })
    void enLettres(String montant, String attendu) {
        assertThat(MontantEnLettres.enLettres(new BigDecimal(montant))).isEqualTo(attendu);
    }
}
