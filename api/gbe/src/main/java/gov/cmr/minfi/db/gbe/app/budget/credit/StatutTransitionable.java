package gov.cmr.minfi.db.gbe.app.budget.credit;

public interface StatutTransitionable {
    void suspendre();

    void debloquer();

    void cantonner();

    void decantionner();

    void annuler();

}
