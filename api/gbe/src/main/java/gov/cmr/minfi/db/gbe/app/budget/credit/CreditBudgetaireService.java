package gov.cmr.minfi.db.gbe.app.budget.credit;

import gov.cmr.minfi.db.gbe.app.budget.credit.dto.CreateCreditRequest;
import gov.cmr.minfi.db.gbe.app.budget.credit.dto.CreditBudgetaireResponse;
import gov.cmr.minfi.db.gbe.app.budget.credit.dto.UpdateCreditBudgetaireRequest;

import java.util.List;

public interface CreditBudgetaireService {

    CreditBudgetaireResponse createCredit(CreateCreditRequest request);

    CreditBudgetaireResponse getCredit(String creditId);

    List<CreditBudgetaireResponse> getCreditsByExercice(String exerciceId);

    List<CreditBudgetaireResponse> getCreditsBySection(String sectionId, String exerciceId);

    List<CreditBudgetaireResponse> getCreditsByProgramme(String programmeId);

    void updateAE(String creditId, UpdateCreditBudgetaireRequest request);

    void updateCP(String creditId, UpdateCreditBudgetaireRequest request);


    void engagerCredit(String creditId);

    void suspendreCredit(String creditId);

    void debloquerCredit(String creditId);

    void cantionnerCredit(String creditId);

    void decantionnerCredit(String creditId);

    void solderCredit(String creditId);

    void annulerCredit(String creditId);
}