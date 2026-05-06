package gov.cmr.minfi.db.gbe.app.mandat.impl;

import gov.cmr.minfi.db.gbe.app.common.exception.BusinessException;
import gov.cmr.minfi.db.gbe.app.common.exception.ErrorCode;
import gov.cmr.minfi.db.gbe.app.iam.role.RoleSysteme;
import gov.cmr.minfi.db.gbe.app.mandat.Mandat;
import gov.cmr.minfi.db.gbe.app.mandat.MandatRepository;
import gov.cmr.minfi.db.gbe.app.mandat.MandatService;
import gov.cmr.minfi.db.gbe.app.mandat.dto.CreateMandatRequest;
import gov.cmr.minfi.db.gbe.app.mandat.dto.MandatSummary;
import gov.cmr.minfi.db.gbe.app.mandat.dto.UpdateMandatRequest;
import gov.cmr.minfi.db.gbe.app.referentiel.administratif.Section;
import gov.cmr.minfi.db.gbe.app.referentiel.administratif.SectionRepository;
import gov.cmr.minfi.db.gbe.app.referentiel.programmatique.Programme;
import gov.cmr.minfi.db.gbe.app.referentiel.programmatique.ProgrammeRepository;
import gov.cmr.minfi.db.gbe.app.user.User;
import gov.cmr.minfi.db.gbe.app.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class MandatServiceImpl implements MandatService {

	private final MandatRepository mandatRepository;
	private final UserRepository userRepository;
	private final SectionRepository sectionRepository;
	private final ProgrammeRepository programmeRepository;

	@Override
	@Transactional
	public MandatSummary createMandat(String userId, CreateMandatRequest request) {
		final User user = findUser(userId);
		final Section section = findSection(request.sectionId());

		// Gestion du programme // null pour le ministre
		Programme programme = null;
		if (request.programmeId() != null) {
			programme = findProgramme(request.programmeId());

			// Verifier que le programme appartient a la section
			if (!programme.getSection().getId().equals(section.getId())) {
				throw new BusinessException(ErrorCode.PROGRAMME_NOT_IN_SECTION);
			}

			// Verifier l'unicite user + Programme
			if (mandatRepository.existsByUserIdAndProgrammeId(userId, request.programmeId())) {
				throw new BusinessException(ErrorCode.MANDAT_ALREADY_EXISTS);
			}
		} else {
			// Scope sur la section entiere -
			if (mandatRepository.existsByUserIdAndSectionIdAndProgrammeIsNull(userId, request.sectionId())) {
				throw new BusinessException(ErrorCode.MANDAT_ALREADY_EXISTS);
			}
		}

		final Mandat mandat = Mandat
			.builder()
			.user(user)
			.section(section)
			.programme(programme)
			.roleSysteme(request.roleSysteme())
			.dateDebut(request.dateDebut())
			.dateFin(request.dateFin())
			.numeroDecision(request.numeroDecision())
			.actif(true)
			.build();

		mandat.initialiserPermissionsDepuisRole();
		mandatRepository.save(mandat);
		log.info(
			"Mandat créé : user={} section={} programme={} role={}",
			userId,
			section.getSigle(),
			programme != null ? programme.getCode() : "SECTION",
			request.roleSysteme()
		);

		return toSummary(mandat);
	}

	@Override
	public List<MandatSummary> getMandats(String userId) {
		findUser(userId);
		return mandatRepository.findByUserId(userId).stream().map(this::toSummary).toList();
	}

	@Override
	@Transactional
	public MandatSummary updateRole(String userId, String mandatId, RoleSysteme roleSysteme) {
		return null;
	}

	@Override
	public MandatSummary updateMandat(String userId, String mandatId, UpdateMandatRequest request) {
		final Mandat mandat = findMandat(userId, mandatId);

		if (request.roleSysteme() != null) {
			mandat.setRoleSysteme(request.roleSysteme());
			mandat.initialiserPermissionsDepuisRole();
		}

		if (request.dateDebut() != null) {
			mandat.setDateDebut(request.dateDebut());
		}
		if (request.dateFin() != null) {
			mandat.setDateFin(request.dateFin());
		}

		if (request.numeroDecision() != null) {
			mandat.setNumeroDecision(request.numeroDecision());
		}

		mandatRepository.save(mandat);
		log.info("Mandat mis a jour {}", mandatId);

		return toSummary(mandat);
	}

	@Override
	@Transactional
	public void activateMandat(String userId, String mandatId) {
		final Mandat mandat = findMandat(userId, mandatId);
		if (mandat.isActif()) {
			throw new BusinessException(ErrorCode.MANDAT_ALREADY_ACTIVE);
		}
		mandat.setActif(true);
		mandatRepository.save(mandat);
		log.info("Mandat activé : {}", mandatId);
	}

	@Override
	@Transactional
	public void deactivateMandat(String userId, String mandatId) {
		final Mandat mandat = findMandat(userId, mandatId);
		if (!mandat.isActif()) {
			throw new BusinessException(ErrorCode.MANDAT_ALREADY_INACTIVE);
		}
		mandat.setActif(false);
		mandatRepository.save(mandat);
		log.info("Mandat désactivé : {}", mandatId);
	}

	@Override
	@Transactional
	public void removeMandat(String userId, String mandatId) {
		final Mandat mandat = findMandat(userId, mandatId);
		mandatRepository.delete(mandat);
		log.info("Mandat supprimé : {}", mandatId);
	}

	// Methodes prives
	private User findUser(String userId) {
		return userRepository.findById(userId).orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
	}

	private Section findSection(String sectionId) {
		return sectionRepository
			.findById(sectionId)
			.orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND, sectionId));
	}

	private Programme findProgramme(String programmeId) {
		return programmeRepository
			.findById(programmeId)
			.orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND, programmeId));
	}

	private Mandat findMandat(String userId, String mandatId) {
		final Mandat mandat = mandatRepository
			.findById(mandatId)
			.orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND, mandatId));

		if (!mandat.getUser().getId().equals(userId)) {
			throw new BusinessException(ErrorCode.MANDAT_NOT_FOUND, mandatId);
		}

		return mandat;
	}

	private MandatSummary toSummary(Mandat mandat) {
		return MandatSummary
			.builder()
			.mandatId(mandat.getId())
			.roleSysteme(mandat.getRoleSysteme())
			.sectionId(mandat.getSection().getId())
			.sectionLibelle(mandat.getSection().getLibelleFr())
			.programmeId(mandat.getProgramme() != null ? mandat.getProgramme().getId() : null)
			.programmeLibelle(mandat.getProgramme() != null ? mandat.getProgramme().getLibelleFr() : null)
			.dateDebut(mandat.getDateDebut())
			.dateFin(mandat.getDateFin())
			.numeroDecision(mandat.getNumeroDecision())
			.actif(mandat.isActif())
			.valide(mandat.isValide())
			.build();
	}
}
