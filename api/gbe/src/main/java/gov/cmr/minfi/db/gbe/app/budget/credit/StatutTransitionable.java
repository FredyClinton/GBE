package gov.cmr.minfi.db.gbe.app.budget.credit;

public interface StatutTransitionable {
    void engager();

    void suspendre();

    void debloquer();

    void cantonner();

    void decantionner();

    void solder();

    void annuler();

}