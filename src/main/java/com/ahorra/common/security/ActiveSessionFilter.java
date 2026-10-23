package com.ahorra.common.security;
import com.ahorra.features.session.repository.UserSessionRepository;
import com.ahorra.features.user.model.User;
import com.ahorra.features.user.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

@Slf4j
public class ActiveSessionFilter extends OncePerRequestFilter {

    public static final String SESSION_REVOKED_CODE = "SESSION_REVOKED";

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final UserRepository userRepository;
    private final UserSessionRepository userSessionRepository;

    public ActiveSessionFilter(UserRepository userRepository, UserSessionRepository userSessionRepository) {
        this.userRepository = userRepository;
        this.userSessionRepository = userSessionRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.getPrincipal() instanceof Jwt jwt && jwt.getSubject() != null) {
            Optional<User> localUser = userRepository.findById(Long.valueOf(jwt.getSubject()));

            if (localUser.isPresent() && !userSessionRepository.existsByUserIdAndIsActiveTrue(localUser.get().getId())) {
                log.warn("Rejected {} {} - no active session for user id {}", request.getMethod(), request.getRequestURI(), localUser.get().getId());
                reject(response);
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private void reject(HttpServletResponse response) throws IOException {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        MAPPER.writeValue(response.getWriter(), Map.of(
                "status", HttpStatus.UNAUTHORIZED.value(),
                "title", "Unauthorized",
                "detail", "Your session has ended. Please sign in again.",
                "code", SESSION_REVOKED_CODE
        ));
    }
}
