package gov.cmr.minfi.db.gbe.app.referentiel.taxe;

/** Taxes paramétrées (V18). La contrainte CHECK de taxes.code liste ces valeurs. */
public enum CodeTaxe {
    /** Taxe sur la valeur ajoutée : TTC = HT + TVA. */
    TVA,
    /** Impôt sur le revenu retenu à la source : déduit du net à payer. */
    IR
}
