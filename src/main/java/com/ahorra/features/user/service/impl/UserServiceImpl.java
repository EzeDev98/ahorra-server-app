package com.ahorra.features.user.service.impl;

import com.ahorra.common.exception.UserAlreadyExistException;
import com.ahorra.features.emailVerification.service.EmailVerificationService;
import com.ahorra.features.user.repository.UserRepository;
import com.ahorra.features.user.auth.dto.request.RegistrationRequest;
import com.ahorra.features.user.auth.dto.response.UserRegistrationResponse;
import com.ahorra.features.user.enums.UserStatus;
import com.ahorra.features.user.model.User;
import com.ahorra.features.user.service.UserService;
import com.ahorra.features.user.utility.RegistrationValidator;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.format.DateTimeFormatter;

import static com.ahorra.common.constants.GeneralConstants.DATE_FORMAT;

@Slf4j
@RequiredArgsConstructor
@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RegistrationValidator registrationValidator;
    private final EmailVerificationService emailVerificationService;

    @Override
    public UserRegistrationResponse registerUser(RegistrationRequest request) {

        registrationValidator.validateRegistrationRequest(request);

        if (userRepository.existsByEmailIgnoreCase(request.getEmail())) {
            throw new UserAlreadyExistException("A user with this email is already registered");
        }

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new UserAlreadyExistException("This username is already taken");
        }

        User user = buildUser(request);

        try {
            userRepository.save(user);;
        } catch (DataIntegrityViolationException ex) {
            throw new UserAlreadyExistException("That email or username is already registered");
        }

        emailVerificationService.sendCode(user);
        log.info("Registered user {}", user.getUsername());
        return new UserRegistrationResponse(user.getUsername(), HttpServletResponse.SC_CREATED, "User registered successfully. Enter the code sent to your email to verify it.");
    }


    private User buildUser(RegistrationRequest request) {

        LocalDate dateOfBirth = LocalDate.parse(request.getDateOfBirth(), DateTimeFormatter.ofPattern(DATE_FORMAT));

        User user = new User();
        user.setFirstName(request.getFirstName());
        user.setMiddleName(request.getMiddleName());
        user.setLastName(request.getLastName());
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setDateOfBirth(request.getDateOfBirth());
        user.setAge(Period.between(dateOfBirth, LocalDate.now()).getYears());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setUserStatus(UserStatus.INACTIVE);
        user.setEmailVerified(false);
        user.setCreatedAt(LocalDateTime.now());
        return user;
    }

}
