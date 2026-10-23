package com.ahorra.features.session.service.impl;

import com.ahorra.features.session.dto.InvalidateSessionResponse;
import com.ahorra.features.session.model.UserSession;
import com.ahorra.features.session.repository.UserSessionRepository;
import com.ahorra.features.session.service.SessionManagementSession;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

import static com.ahorra.common.constants.GeneralConstants.*;

@Slf4j
@RequiredArgsConstructor
@Service
public class SessionManagementSessionImpl implements SessionManagementSession {

    private final UserSessionRepository userSessionRepository;


    @Override
    public void createSession(Long userId, String token) {
        userSessionRepository.findByUserIdAndIsActiveTrue(userId)
                .ifPresent(existing -> closeSession(existing, REPLACED_BY_NEW_LOGIN));

        UserSession session = new UserSession();
        session.setUserId(userId);
        session.setToken(token);
        session.setActive(true);
        session.setCreatedAt(LocalDateTime.now());

        userSessionRepository.save(session);
        log.info("New session created for user: {}", userId);
    }

    @Override
    public void invalidateSession(String token) {
        userSessionRepository.findByToken(token)
                .ifPresentOrElse( session -> {
                    closeSession(session, LOGOUT);
                    log.info("Session invalidate for user: {}", session.getUserId());
                }, ()-> log.warn("No session found for token during logout"));
    }


    @Override
    @Transactional
    public InvalidateSessionResponse invalidateUserSessions(Long userId) {
        return userSessionRepository.findByUserIdAndIsActiveTrue(userId)
                .map(session -> {
                    closeSession(session, ADMIN_ACTION);
                    log.info("Session invalidated by admin for user: {}", session.getUserId());
                    return new InvalidateSessionResponse(HttpServletResponse.SC_OK, "Sessions successfully invalidated for user " + userId);
                })
                .orElseGet(() -> {
                    log.warn("No active session found for {} during admin invalidation — nothing to update", userId);
                    return new InvalidateSessionResponse(HttpServletResponse.SC_NOT_FOUND, "No active session found for user " + userId);
                });
    }

    @Override
    @Transactional
    public void updateSessionOnRefresh(Long userId, String newToken) {
        userSessionRepository.findByUserIdAndIsActiveTrue(userId)
                .ifPresentOrElse(
                        session -> {
                            session.setToken(newToken);
                            session.setCreatedAt(LocalDateTime.now());
                            userSessionRepository.save(session);
                            log.info("Session refreshed and TTL reset for user: {}", userId);
                        },
                        () -> log.warn("No active session found for {} during refresh — nothing to update", userId)
                );
    }

    private void closeSession(UserSession session, String reason) {
        session.setActive(false);
        session.setEndedAt(LocalDateTime.now());
        session.setEndReason(reason);
        userSessionRepository.save(session);
    }
}
