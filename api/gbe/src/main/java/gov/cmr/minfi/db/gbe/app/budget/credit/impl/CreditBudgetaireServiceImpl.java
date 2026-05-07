package gov.cmr.minfi.db.gbe.app.budget.credit.impl;

import gov.cmr.minfi.db.gbe.app.budget.credit.CreditBudgetaire;
import gov.cmr.minfi.db.gbe.app.budget.credit.CreditBudgetaireMapper;
import gov.cmr.minfi.db.gbe.app.budget.credit.CreditBudgetaireRepository;
import gov.cmr.minfi.db.gbe.app.budget.credit.CreditBudgetaireService;
import gov.cmr.minfi.db.gbe.app.budget.credit.dto.CreateCreditRequest;
import gov.cmr.minfi.db.gbe.app.budget.credit.dto.CreditBudgetaireResponse;
import gov.cmr.minfi.db.gbe.app.budget.credit.dto.UpdateCreditBudgetaireRequest;
import gov.cmr.minfi.db.gbe.app.common.exception.BusinessException;
import gov.cmr.minfi.db.gbe.app.common.exception.ErrorCode;
import gov.cmr.minfi.db.gbe.app.exercice.Exercice;
import gov.cmr.minfi.db.gbe.app.exercice.ExerciceRepository;
import gov.cmr.minfi.db.gbe.app.iam.permission.Permission;
import gov.cmr.minfi.db.gbe.app.mandat.MandatScopeValidator;
import gov.cmr.minfi.db.gbe.app.referentiel.administratif.Chapitre;
import gov.cmr.minfi.db.gbe.app.referentiel.administratif.ChapitreRepository;
import gov.cmr.minfi.db.gbe.app.referentiel.administratif.Section;
import gov.cmr.minfi.db.gbe.app.referentiel.administratif.SectionRepository;
import gov.cmr.minfi.db.gbe.app.referentiel.programmatique.Action;
import gov.cmr.minfi.db.gbe.app.referentiel.programmatique.ActionRepository;
import gov.cmr.minfi.db.gbe.app.referentiel.programmatique.Programme;
import gov.cmr.minfi.db.gbe.app.referentiel.programmatique.ProgrammeRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

@Slf4j
@RequiredArgsConstructor
@Service
public class CreditBudgetaireServiceImpl implements CreditBudgetaireService {

	private final ChapitreRepository chapitreRepository;
	private final ActionRepository actionRepository;
	private final ProgrammeRepository programmeRepository;
	private final SectionRepository sectionRepository;
	private final ExerciceRepository exerciceRepository;
	private final CreditBudgetaireRepository creditBudgetaireRepository;
	private final MandatScopeValidator mandatScopeValidator;
	private final CreditBudgetaireMapper creditBudgetaireMapper;

	@Override
	@Transactional
	public CreditBudgetaireResponse createCredit(CreateCreditRequest request) {
		final Exercice exercice = findExercice(request.exerciceId());
		final Section section = findSection(request.sectionId());
		final Programme programme = findProgramme(request.programmeId());
		final Action action = findAction(request.actionId());
		final Chapitre chapitre = findChapitre(request.chapitreId());

		validateProgrammeBelongsToSection(programme, section);
		validateActionBelongsToProgramme(action, programme);
		validateImputationUniqueness(request);

		CreditBudgetaire creditParent = null;
		if (request.creditParentId() != null) {
			creditParent =
				creditBudgetaireRepository
					.findById(request.creditParentId())
					.orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND, request.creditParentId()));
			validateNoCreditCycle(request.creditParentId());
		}

		final String codeImputation = buildImputationCode(exercice, section, programme, action, chapitre);

		final CreditBudgetaire credit = CreditBudgetaire
			.builder()
			.exercice(exercice)
			.section(section)
			.programme(programme)
			.action(action)
			.chapitre(chapitre)
			.codeImputation(codeImputation)
			.montantAE(request.montantAE())
			.montantCP(request.montantCP())
			.creditParent(creditParent)
			.build();

		creditBudgetaireRepository.save(credit);
		log.info("Crédit budgétaire créé : {}", codeImputation);
		return creditBudgetaireMapper.toResponse(credit);
	}

	@Override
	public CreditBudgetaireResponse getCredit(String creditId) {
		final CreditBudgetaire credit = findCredit(creditId);
		mandatScopeValidator.checkAccessCredit(credit);
		return creditBudgetaireMapper.toResponse(credit);
	}

	@Override
	public List<CreditBudgetaireResponse> getCreditsByExercice(String exerciceId) {
		return creditBudgetaireRepository
			.findByExerciceId(exerciceId)
			.stream()
			.filter(this::isInScope)
			.map(creditBudgetaireMapper::toResponse)
			.toList();
	}

	@Override
	public List<CreditBudgetaireResponse> getCreditsBySection(String sectionId, String exerciceId) {
		mandatScopeValidator.checkSectionAccess(sectionId);
		final Set<String> programmesAutorises = mandatScopeValidator.getProgrammeIdsAutorises(sectionId);
		return creditBudgetaireRepository
			.findBySectionIdAndExerciceId(sectionId, exerciceId)
			.stream()
			.filter(c -> isAutoriseInProgramme(c, programmesAutorises))
			.map(creditBudgetaireMapper::toResponse)
			.toList();
	}

	@Override
	public List<CreditBudgetaireResponse> getCreditsByProgramme(String programmeId) {
		return creditBudgetaireRepository
			.findByProgrammeId(programmeId)
			.stream()
			.filter(this::isInScope)
			.map(creditBudgetaireMapper::toResponse)
			.toList();
	}

	@Override
	@Transactional
	public void updateAE(String creditId, UpdateCreditBudgetaireRequest request) {
		final CreditBudgetaire credit = findCredit(creditId);
		mandatScopeValidator.checkAccessCreditWithPermission(credit, Permission.REVISER_AE);
		validateRevisionAE(credit, request.montant());
		credit.setMontantAE(request.montant());
		credit.setMontantAEDisponible(request.montant().subtract(credit.getMontantAEConsomme()));
		creditBudgetaireRepository.save(credit);
	}

	@Override
	@Transactional
	public void updateCP(String creditId, UpdateCreditBudgetaireRequest request) {
		final CreditBudgetaire credit = findCredit(creditId);
		mandatScopeValidator.checkAccessCreditWithPermission(credit, Permission.REVISER_CP);
		validateRevisionCP(credit, request.montant());
		credit.setMontantCP(request.montant());
		credit.setMontantCPDisponible(request.montant().subtract(credit.getMontantCPConsomme()));
		creditBudgetaireRepository.save(credit);
	}

	@Override
	@Transactional
	public void engagerCredit(String creditId) {
		final CreditBudgetaire credit = findCredit(creditId);
		mandatScopeValidator.checkAccessCreditWithPermission(credit, Permission.ENGAGE_DEPENSE);
		credit.engager();
		creditBudgetaireRepository.save(credit);
	}

	@Override
	@Transactional
	public void suspendreCredit(String creditId) {
		final CreditBudgetaire credit = findCredit(creditId);
		mandatScopeValidator.checkAccessCreditWithPermission(credit, Permission.REVISER_AE);
		credit.suspendre();
		creditBudgetaireRepository.save(credit);
	}

	@Override
	@Transactional
	public void debloquerCredit(String creditId) {
		final CreditBudgetaire credit = findCredit(creditId);
		mandatScopeValidator.checkAccessCreditWithPermission(credit, Permission.REVISER_AE);
		credit.debloquer();
		creditBudgetaireRepository.save(credit);
	}

	@Override
	@Transactional
	public void cantionnerCredit(String creditId) {
		final CreditBudgetaire credit = findCredit(creditId);
		mandatScopeValidator.checkAccessCreditWithPermission(credit, Permission.REVISER_AE);
		credit.cantonner();
		creditBudgetaireRepository.save(credit);
	}

	@Override
	@Transactional
	public void decantionnerCredit(String creditId) {
		final CreditBudgetaire credit = findCredit(creditId);
		mandatScopeValidator.checkAccessCreditWithPermission(credit, Permission.REVISER_AE);
		credit.decantionner();
		creditBudgetaireRepository.save(credit);
	}

	@Override
	@Transactional
	public void solderCredit(String creditId) {
		final CreditBudgetaire credit = findCredit(creditId);
		mandatScopeValidator.checkAccessCreditWithPermission(credit, Permission.ENGAGE_DEPENSE);
		credit.solder();
		creditBudgetaireRepository.save(credit);
	}

	@Override
	@Transactional
	public void annulerCredit(String creditId) {
		final CreditBudgetaire credit = findCredit(creditId);
		mandatScopeValidator.checkAccessCreditWithPermission(credit, Permission.REVISER_AE);
		credit.annuler();
		creditBudgetaireRepository.save(credit);
	}

	// ── Validations ───────────────────────────────────

	private void validateProgrammeBelongsToSection(Programme programme, Section section) {
		if (!programme.getSection().getId().equals(section.getId())) {
			throw new BusinessException(ErrorCode.PROGRAMME_NOT_IN_SECTION);
		}
	}

	private void validateActionBelongsToProgramme(Action action, Programme programme) {
		if (!action.getProgramme().getId().equals(programme.getId())) {
			throw new BusinessException(ErrorCode.ACTION_NOT_IN_PROGRAMME);
		}
	}

	private void validateImputationUniqueness(CreateCreditRequest request) {
		if (
			creditBudgetaireRepository.existsByExerciceIdAndSectionIdAndProgrammeIdAndActionIdAndChapitreId(
				request.exerciceId(),
				request.sectionId(),
				request.programmeId(),
				request.actionId(),
				request.chapitreId()
			)
		) {
			throw new BusinessException(ErrorCode.CREDIT_ALREADY_EXISTS);
		}
	}

	private void validateNoCreditCycle(String creditParentId) {
		creditBudgetaireRepository.findAllAncestorCreditIds(creditParentId);
	}

	private void validateRevisionAE(CreditBudgetaire credit, BigDecimal nouveauMontant) {
		if (nouveauMontant.compareTo(credit.getMontantAEConsomme()) < 0) {
			throw new BusinessException(ErrorCode.NEW_AE_AMOUNT_LESS_THAN_CONSOMME);
		}
	}

	private void validateRevisionCP(CreditBudgetaire credit, BigDecimal nouveauMontant) {
		if (nouveauMontant.compareTo(credit.getMontantCPConsomme()) < 0) {
			throw new BusinessException(ErrorCode.NEW_CP_AMOUNT_LESS_THAN_CONSOMME);
		}
	}

	// ── Finders ───────────────────────────────────────

	private CreditBudgetaire findCredit(String id) {
		return creditBudgetaireRepository
			.findById(id)
			.orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND, id));
	}

	private Exercice findExercice(String id) {
		return exerciceRepository.findById(id).orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND, id));
	}

	private Section findSection(String id) {
		return sectionRepository.findById(id).orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND, id));
	}

	private Programme findProgramme(String id) {
		return programmeRepository
			.findById(id)
			.orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND, id));
	}

	private Action findAction(String id) {
		return actionRepository.findById(id).orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND, id));
	}

	private Chapitre findChapitre(String id) {
		return chapitreRepository.findById(id).orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND, id));
	}

	// ── Helpers scope ─────────────────────────────────

	private boolean isInScope(CreditBudgetaire credit) {
		try {
			mandatScopeValidator.checkAccessCredit(credit);
			return true;
		} catch (BusinessException e) {
			return false;
		}
	}

	private boolean isAutoriseInProgramme(CreditBudgetaire credit, Set<String> programmesAutorises) {
		if (programmesAutorises.contains(null)) return true; // MINISTRE — accès total section
		return programmesAutorises.contains(credit.getProgramme().getId());
	}

	// ── Code imputation ───────────────────────────────

	/**
	 * Construit le code d'imputation budgétaire (Art. 22 NBE) :
	 * codeExercice(2) + codeSection(2) + codeProgramme(3) + codeAction(1) + codeCompletChapitre(8)
	 */
	private String buildImputationCode(
		Exercice exercice,
		Section section,
		Programme programme,
		Action action,
		Chapitre chapitre
	) {
		return (
			exercice.getCodeExercice() +
			section.getCodeSection() +
			programme.getCode() +
			action.getCodeAction() +
			chapitre.getCodeComplet()
		);
	}
}
