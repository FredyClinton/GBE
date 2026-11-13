package gov.cmr.minfi.db.gbe.app.common.texte;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Montant en toutes lettres, en français, pour la mention « Arrêté à la somme de » du BCA
 * (ex. 4 913 100 → « quatre millions neuf cent treize mille cent »).
 * Orthographe traditionnelle : « vingt et un », « soixante et onze », « quatre-vingts », « deux cents »,
 * « deux cent mille » (cent et vingt restent invariables devant mille), « deux cents millions ».
 * Les centimes éventuels sont ajoutés (« … et quarante centimes ») : le franc CFA n'en a pas, l'arrondi
 * des montants à l'unité reste à décider (plan §7).
 */
public final class MontantEnLettres {

    private static final String[] UNITES = {"zéro", "un", "deux", "trois", "quatre", "cinq", "six", "sept", "huit",
            "neuf", "dix", "onze", "douze", "treize", "quatorze", "quinze", "seize"};
    private static final String[] DIZAINES = {"", "dix", "vingt", "trente", "quarante", "cinquante", "soixante"};

    private MontantEnLettres() {
    }

    public static String enLettres(BigDecimal montant) {
        final BigDecimal arrondi = montant.setScale(2, RoundingMode.HALF_UP);
        final long entier = arrondi.longValue();
        final int centimes = arrondi.remainder(BigDecimal.ONE).movePointRight(2).abs().intValue();
        final String texte = entier(Math.abs(entier));
        final String signe = arrondi.signum() < 0 ? "moins " : "";
        return centimes == 0 ? signe + texte
                : signe + texte + " et " + entier(centimes) + (centimes > 1 ? " centimes" : " centime");
    }

    static String entier(long n) {
        if (n == 0) {
            return UNITES[0];
        }
        final StringBuilder sb = new StringBuilder();
        final long milliards = n / 1_000_000_000L;
        final long millions = (n / 1_000_000L) % 1000;
        final long milliers = (n / 1000) % 1000;
        final long reste = n % 1000;
        if (milliards > 0) {
            ajouter(sb, centaines((int) milliards, true) + (milliards > 1 ? " milliards" : " milliard"));
        }
        if (millions > 0) {
            ajouter(sb, centaines((int) millions, true) + (millions > 1 ? " millions" : " million"));
        }
        if (milliers > 0) {
            // « mille » est invariable et n'est pas précédé de « un » ; « cent » et « vingt » y restent au singulier
            ajouter(sb, milliers == 1 ? "mille" : centaines((int) milliers, false) + " mille");
        }
        if (reste > 0) {
            ajouter(sb, centaines((int) reste, true));
        }
        return sb.toString();
    }

    /** 1 à 999 ; {@code pluriel} : « cents » / « quatre-vingts » prennent un s s'ils terminent le nombre. */
    private static String centaines(int n, boolean pluriel) {
        final int c = n / 100;
        final int r = n % 100;
        final StringBuilder sb = new StringBuilder();
        if (c > 0) {
            sb.append(c == 1 ? "cent" : UNITES[c] + " cent");
            if (c > 1 && r == 0 && pluriel) {
                sb.append('s');
            }
        }
        if (r > 0) {
            ajouter(sb, dizaines(r, pluriel));
        }
        return sb.toString();
    }

    /** 1 à 99. */
    private static String dizaines(int n, boolean pluriel) {
        if (n <= 16) {
            return UNITES[n];
        }
        if (n < 20) {
            return "dix-" + UNITES[n - 10];
        }
        final int d = n / 10;
        final int u = n % 10;
        if (d == 7 || d == 9) {
            // 70-79 : soixante-dix… ; 90-99 : quatre-vingt-dix…
            final String base = d == 7 ? "soixante" : "quatre-vingt";
            final int suite = 10 + u;
            if (d == 7 && u == 1) {
                return "soixante et onze";
            }
            return base + "-" + dizaines(suite, false);
        }
        if (d == 8) {
            return u == 0 ? (pluriel ? "quatre-vingts" : "quatre-vingt") : "quatre-vingt-" + UNITES[u];
        }
        if (u == 0) {
            return DIZAINES[d];
        }
        return u == 1 ? DIZAINES[d] + " et un" : DIZAINES[d] + "-" + UNITES[u];
    }

    private static void ajouter(StringBuilder sb, String mot) {
        if (!sb.isEmpty()) {
            sb.append(' ');
        }
        sb.append(mot);
    }
}
