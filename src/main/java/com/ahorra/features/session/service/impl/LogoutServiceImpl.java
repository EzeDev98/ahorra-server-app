package com.ahorra.features.session.service.impl;

import com.ahorra.common.exception.BadRequestException;
import com.ahorra.features.session.dto.LogoutResponse;
import com.ahorra.features.session.model.UserSession;
import com.ahorra.features.session.repository.UserSessionRepository;
import com.ahorra.features.session.service.LogoutService;
import com.ahorra.features.session.service.SessionManagementSession;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class LogoutServiceImpl implements LogoutService {

    private final UserSessionRepository userSessionRepository;
    private final SessionManagementSession sessionManagementSession;

    @Override
    public LogoutResponse logout(String token) {
        if (token == null || token.trim().isEmpty()) {
            log.warn("Attempted logout with null or empty token");
            throw new BadRequestException("Invalid token");
        }

        try {
            UserSession session = userSessionRepository.findByToken(token)
                    .orElseThrow(() -> new BadRequestException("No active session found"));

            sessionManagementSession.invalidateSession(token);
            log.info("Session deleted for user: {}", session.getUserId());

            return new LogoutResponse("Logout out successfully", HttpServletResponse.SC_OK);
        } catch (BadRequestException ex) {
            log.warn("Logout failed: {}", ex.getMessage());
            return new LogoutResponse(ex.getMessage(), HttpServletResponse.SC_BAD_REQUEST);
        } catch (Exception ex) {
            log.error("Error during logout: {}", ex.getMessage());
            return new LogoutResponse("Logout failed. Please try again.", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
}
