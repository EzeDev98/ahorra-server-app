package com.ahorra.features.user.auth.web;

import com.ahorra.features.session.service.SessionManagementSession;
import com.ahorra.features.user.auth.dto.request.LoginRequest;
import com.ahorra.features.user.auth.dto.response.LoginResponse;
import com.ahorra.features.user.auth.service.AuthService;
import com.ahorra.features.user.model.User;
import com.ahorra.features.user.service.impl.CurrentUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("${app.title}")
public class AuthController {

    private final AuthService authService;
    private final JwtDecoder jwtDecoder;
    private final CurrentUserService currentUserService;
    private final SessionManagementSession sessionManagementSession;

    @PostMapping("/auth/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        LoginResponse loginResponse = authService.login(request.getUsername(), request.getPassword());
        try {
            Jwt jwt = jwtDecoder.decode(loginResponse.getAccessToken());
            User user = currentUserService.resolveUser(jwt);
            loginResponse.setFirstName(user.getFirstName());
            sessionManagementSession.createSession(user.getId(), loginResponse.getAccessToken());
        } catch (Exception ex) {
            log.warn("Could not resolve local user for new login; Local session not opened", ex);
        }

        return ResponseEntity.ok(loginResponse);
    }

    @PostMapping("/auth/refresh-token")
    public ResponseEntity<?> refreshToken(@RequestBody Map<String, String> body) {
        String refreshToken = body.get("refreshToken");

        if (refreshToken == null || refreshToken.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Refresh token is required"));
        }

        LoginResponse tokenResponse = authService.refreshAccessToken(refreshToken);

        try {
            Jwt jwt = jwtDecoder.decode(tokenResponse.getAccessToken());
            User user = currentUserService.resolveUser(jwt);
            sessionManagementSession.updateSessionOnRefresh(user.getId(), tokenResponse.getAccessToken());
        } catch (Exception ex) {
            log.warn("Could not resolve local user for refreshed token; session TTL not reset", ex);
        }

        return ResponseEntity.ok(tokenResponse);
    }
}
