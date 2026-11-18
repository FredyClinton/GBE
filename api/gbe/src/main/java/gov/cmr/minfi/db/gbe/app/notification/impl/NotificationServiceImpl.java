package gov.cmr.minfi.db.gbe.app.notification.impl;

import gov.cmr.minfi.db.gbe.app.common.exception.BusinessException;
import gov.cmr.minfi.db.gbe.app.common.exception.ErrorCode;
import gov.cmr.minfi.db.gbe.app.common.pagination.PageResponse;
import gov.cmr.minfi.db.gbe.app.common.pagination.Tris;
import gov.cmr.minfi.db.gbe.app.identite.user.User;
import gov.cmr.minfi.db.gbe.app.identite.user.UserRepository;
import gov.cmr.minfi.db.gbe.app.notification.*;
import gov.cmr.minfi.db.gbe.app.notification.dto.CompteurNotificationsResponse;
import gov.cmr.minfi.db.gbe.app.notification.dto.NotificationResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class NotificationServiceImpl implements NotificationService {

    static final Set<String> TRIS_AUTORISES = Set.of("dateCreation", "type");

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final NotificationMapper notificationMapper;
    private final Clock clock;

    @Override
    @Transactional
    public void notifier(String destinataireUserId, TypeNotification type, String titre, String message,
                         String objetType, String objetId) {
        if (destinataireUserId == null || !userRepository.existsById(destinataireUserId)) {
            log.warn("Notification {} ignorée : destinataire inconnu ({})", type, destinataireUserId);
            return;
        }
        notificationRepository.save(Notification.builder()
                .destinataire(userRepository.getReferenceById(destinataireUserId))
                .type(type)
                .titre(titre)
                .message(message)
                .objetType(objetType)
                .objetId(objetId)
                .dateCreation(LocalDateTime.now(clock))
                .build());
    }

    @Override
    public PageResponse<NotificationResponse> mesNotifications(boolean nonLuesSeulement, Pageable pageable) {
        final String moi = utilisateurCourant().getId();
        final Pageable page = Tris.verifier(pageable, TRIS_AUTORISES, Sort.by(Sort.Direction.DESC, "dateCreation"));
        return PageResponse.of(nonLuesSeulement
                ? notificationRepository.findByDestinataireIdAndDateLectureIsNull(moi, page)
                : notificationRepository.findByDestinataireId(moi, page), notificationMapper::toResponse);
    }

    @Override
    public CompteurNotificationsResponse compterNonLues() {
        return new CompteurNotificationsResponse(
                notificationRepository.countByDestinataireIdAndDateLectureIsNull(utilisateurCourant().getId()));
    }

    @Override
    @Transactional
    public NotificationResponse marquerLue(String notificationId) {
        // La notification d'un autre utilisateur est introuvable (pas de 403 qui révélerait son existence)
        final Notification notification = notificationRepository
                .findByIdAndDestinataireId(notificationId, utilisateurCourant().getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOTIFICATION_NOT_FOUND, notificationId));
        notification.marquerLue(LocalDateTime.now(clock));
        return notificationMapper.toResponse(notification);
    }

    @Override
    @Transactional
    public int marquerToutesLues() {
        return notificationRepository.marquerToutesLues(utilisateurCourant().getId(), LocalDateTime.now(clock));
    }

    private static User utilisateurCourant() {
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof User user)) {
            throw new BusinessException(ErrorCode.ACCES_REFUSE_PERMISSION);
        }
        return user;
    }
}
