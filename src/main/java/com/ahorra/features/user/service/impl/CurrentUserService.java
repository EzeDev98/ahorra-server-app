package com.ahorra.features.user.service.impl;

import com.ahorra.common.exception.UserNotFoundException;
import com.ahorra.features.user.model.User;
import com.ahorra.features.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CurrentUserService {

    private final UserRepository userRepository;


    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof Jwt jwt)) {
            throw new AccessDeniedException("No authenticated user found for this request");
        }
        return resolveUser(jwt);
    }

    public User resolveUser(Jwt jwt) {
        Long userId;
        try {
            userId = Long.valueOf(jwt.getSubject());
        } catch (NumberFormatException ex) {
            throw new AccessDeniedException("The token does not identify a user");
        }
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("No local account found for the authenticated user"));
    }
}