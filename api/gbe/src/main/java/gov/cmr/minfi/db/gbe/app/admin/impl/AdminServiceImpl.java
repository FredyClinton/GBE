package gov.cmr.minfi.db.gbe.app.admin.impl;

import gov.cmr.minfi.db.gbe.app.admin.AdminService;
import gov.cmr.minfi.db.gbe.app.admin.dto.CreateUserRequest;
import gov.cmr.minfi.db.gbe.app.admin.dto.UserSummaryResponse;
import gov.cmr.minfi.db.gbe.app.agent.Agent;
import gov.cmr.minfi.db.gbe.app.agent.AgentRepository;
import gov.cmr.minfi.db.gbe.app.auth.tfa.TwoFactorAuthenticationService;
import gov.cmr.minfi.db.gbe.app.common.exception.BusinessException;
import gov.cmr.minfi.db.gbe.app.common.exception.ErrorCode;
import gov.cmr.minfi.db.gbe.app.iam.role.Role;
import gov.cmr.minfi.db.gbe.app.iam.role.RoleRepository;
import gov.cmr.minfi.db.gbe.app.iam.role.RoleSysteme;
import gov.cmr.minfi.db.gbe.app.mandat.Mandat;
import gov.cmr.minfi.db.gbe.app.mandat.MandatMapper;
import gov.cmr.minfi.db.gbe.app.mandat.MandatRepository;
import gov.cmr.minfi.db.gbe.app.mandat.dto.MandatSummary;
import gov.cmr.minfi.db.gbe.app.referentiel.administratif.Section;
import gov.cmr.minfi.db.gbe.app.referentiel.administratif.SectionRepository;
import gov.cmr.minfi.db.gbe.app.referentiel.programmatique.Programme;
import gov.cmr.minfi.db.gbe.app.referentiel.programmatique.ProgrammeRepository;
import gov.cmr.minfi.db.gbe.app.user.User;
import gov.cmr.minfi.db.gbe.app.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.function.Function;

@Slf4j
@RequiredArgsConstructor
@Service
public class AdminServiceImpl implements AdminService {

	private final UserRepository userRepository;
	private final AgentRepository agentRepository;
	private final SectionRepository sectionRepository;
	private final ProgrammeRepository programmeRepository;
	private final MandatRepository mandatRepository;
	private final RoleRepository roleRepository;
	private final TwoFactorAuthenticationService tfaService;
	private final PasswordEncoder passwordEncoder;
	private final MandatMapper mandatMapper;

	@Override
	@Transactional
	public UserSummaryResponse createUser(CreateUserRequest request) {
		final Agent agent = agentRepository
			.findById(request.agentId())
			.orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND, request.agentId()));

		if (userRepository.existsByAgentId(request.agentId())) {
			throw new BusinessException(ErrorCode.USER_ALREADY_EXISTS_FOR_AGENT);
		}

		if (userRepository.existsByEmailIgnoreCase(request.email())) {
			throw new BusinessException(ErrorCode.EMAIL_ALREADY_EXISTS);
		}

		final Section section = sectionRepository
			.findById(request.sectionId())
			.orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND, request.sectionId()));

		final Role role = roleRepository
			.findByName("ROLE_" + request.roleSysteme().name())
			.orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND, request.roleSysteme().name()));

		final User user = User
			.builder()
			.email(request.email())
			.password(passwordEncoder.encode(request.password()))
			.agent(agent)
			.role(role)
			.firstName(agent.getFirstName())
			.lastName(agent.getLastName())
			.matricule(agent.getMatricule())
			.nui(agent.getNui())
			.numeroCni(agent.getNumeroCni())
			.phoneNumber(agent.getPhoneNumber())
			.cniIssueDate(agent.getCniIssueDate())
			.cniExpiryDate(agent.getCniExpiryDate())
			.enabled(true)
			.locked(false)
			.credentialsExpired(false)
			.emailVerified(false)
			.phoneVerified(false)
			.firstLogin(true)
			.secret(tfaService.generateNewSecret())
			.build();

		userRepository.save(user);
		log.info("User créé : {} → agent {}", user.getEmail(), agent.getMatricule());

		final List<Mandat> mandats = request
			.programmeIds()
			.stream()
			.map(programmeId -> {
				final Programme programme = programmeRepository
					.findById(programmeId)
					.orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND, programmeId));

				if (!programme.getSection().getId().equals(section.getId())) {
					throw new BusinessException(ErrorCode.PROGRAMME_NOT_IN_SECTION);
				}

				final Mandat mandat = Mandat
					.builder()
					.user(user)
					.section(section)
					.programme(programme)
					.roleSysteme(request.roleSysteme())
					.dateDebut(LocalDate.now())
					.actif(true)
					.build();

				mandat.initialiserPermissionsDepuisRole();
				return mandat;
			})
			.toList();

		mandatRepository.saveAll(mandats);
		log.info("{} mandat(s) créé(s) pour {}", mandats.size(), user.getEmail());

		return toResponse(user, mandats);
	}

	@Override
	public UserSummaryResponse getUser(String userId) {
		final User user = userRepository
			.findById(userId)
			.orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND, "user:" + userId));
		return toResponse(user, mandatRepository.findByUserId(userId));
	}

	@Override
	public List<UserSummaryResponse> getAllUsers() {
		return userRepository
			.findAll()
			.stream()
			.map(user -> toResponse(user, mandatRepository.findByUserId(user.getId())))
			.toList();
	}

	@Override
	@Transactional
	public void activeUserAccount(String userId) {
		final User user = userRepository
			.findById(userId)
			.orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND, userId));
		if (user.isEnabled()) throw new BusinessException(ErrorCode.ACCOUNT_ALREADY_ACTIVATED);
		user.setEnabled(true);
		userRepository.save(user);
	}

	@Override
	@Transactional
	public void deactivateUserAccount(String userId) {
		final User user = userRepository
			.findById(userId)
			.orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND, userId));
		if (!user.isEnabled()) throw new BusinessException(ErrorCode.ACCOUNT_ALREADY_DEACTIVATED);
		user.setEnabled(false);
		userRepository.save(user);
	}

	@Override
	@Transactional
	public void deleteUserAccount(String userId) {
		final User user = userRepository
			.findById(userId)
			.orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND, userId));
		mandatRepository.deleteAll(mandatRepository.findByUserId(userId));
		userRepository.delete(user);
		log.debug("Utilisateur supprimé : {}", user.getEmail());
	}

	// ── Helpers privés ────────────────────────────────

	private UserSummaryResponse toResponse(User user, List<Mandat> mandats) {
		final List<MandatSummary> mandatSummaries = mandatMapper.toSummaryList(mandats);
		final Agent agent = user.getAgent();

		return UserSummaryResponse
			.builder()
			.id(user.getId())
			.email(user.getEmail())
			.enabled(user.isEnabled())
			.firstLogin(user.isFirstLogin())
			.mfaEnabled(user.isMfaEnabled())
			.createdDate(user.getCreatedDate())
			.role(user.getRole() != null ? RoleSysteme.valueOf(user.getRole().getName().replace("ROLE_", "")) : null)
			.agentId(agent != null ? agent.getId() : null)
			.firstName(fromAgent(user, Agent::getFirstName, User::getFirstName))
			.lastName(fromAgent(user, Agent::getLastName, User::getLastName))
			.dateOfBirth(fromAgentDate(user, Agent::getDateOfBirth, User::getDateOfBirth))
			.matricule(fromAgent(user, Agent::getMatricule, User::getMatricule))
			.nui(fromAgent(user, Agent::getNui, User::getNui))
			.cniNumber(fromAgent(user, Agent::getNumeroCni, User::getNumeroCni))
			.phoneNumber(fromAgent(user, Agent::getPhoneNumber, User::getPhoneNumber))
			.mandats(mandatSummaries)
			.build();
	}

	private String fromAgent(User u, Function<Agent, String> agentGetter, Function<User, String> userFallback) {
		return u.getAgent() != null ? agentGetter.apply(u.getAgent()) : userFallback.apply(u);
	}

	private LocalDate fromAgentDate(
		User u,
		Function<Agent, LocalDate> agentGetter,
		Function<User, LocalDate> userFallback
	) {
		return u.getAgent() != null ? agentGetter.apply(u.getAgent()) : userFallback.apply(u);
	}
}
