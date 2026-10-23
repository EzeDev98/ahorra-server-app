package com.ahorra.features.user.auth.service.impl;

import com.ahorra.common.exception.AuthenticationException;
import com.ahorra.common.exception.EmailNotVerifiedException;
import com.ahorra.common.exception.RefreshTokenInvalidException;
import com.ahorra.common.exception.RequiredFieldsException;
import com.ahorra.features.auth.service.impl.TokenService;
import com.ahorra.features.user.auth.dto.response.LoginResponse;
import com.ahorra.features.user.auth.service.AuthService;
import com.ahorra.features.user.enums.UserStatus;
import com.ahorra.features.user.model.User;
import com.ahorra.features.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Service
public class AuthServiceImpl implements AuthService {

    private static final String BAD_CREDENTIALS = "Invalid username or password";
    private final TokenService tokenService;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;

    @Override
    public LoginResponse login(String username, String password) {

        if (username == null || username.isBlank() || password == null || password.isBlank()) {
            throw new RequiredFieldsException("Username and password are required");
        }

        User user = findByIdentifier(username)
                .orElseThrow(() -> new AuthenticationException(BAD_CREDENTIALS));

        if (user.getPasswordHash() == null || !passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new AuthenticationException(BAD_CREDENTIALS);
        }

        if (!user.isEmailVerified()) {
            throw new EmailNotVerifiedException("Your email address is not verified. Verify it to sign in", user.getEmail());
        }

        if (user.getUserStatus() != UserStatus.ACTIVE) {
            throw new AuthenticationException("This account is not active");
        }

        return  loginResponse(user, "Login successful");
    }

    @Override
    public LoginResponse refreshAccessToken(String refreshToken) {
        Long userId = tokenService.userIdFromRefreshToken(refreshToken);
        User user = userRepository.findById(userId)
                .filter(foundUser -> foundUser.getUserStatus() == UserStatus.ACTIVE)
                .orElseThrow(() -> new RefreshTokenInvalidException("Refresh token is invalid or expired"));
        return loginResponse(user, "Token refreshed");
    }

    private LoginResponse loginResponse(User user, String message) {
        TokenService.TokenPair tokens = tokenService.issueTokens(user);
        return new LoginResponse(user.getId(), user.getFirstName(), user.getUsername(), message, tokens.accessToken(), tokens.refreshToken(), tokens.expiresIn(),tokens.refreshExpiresIn());
    }

    private Optional<User> findByIdentifier(String identifier) {
        return userRepository.findByUsername(identifier)
                .or(()-> userRepository.findByEmailIgnoreCase(identifier));
    }
}
