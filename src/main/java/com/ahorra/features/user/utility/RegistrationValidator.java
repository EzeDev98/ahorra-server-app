package com.ahorra.features.user.utility;

import com.ahorra.common.exception.RegistrationException;
import com.ahorra.features.user.auth.dto.request.RegistrationRequest;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import static com.ahorra.common.constants.GeneralConstants.DATE_FORMAT;

@Component
public class RegistrationValidator {

    public void validateRegistrationRequest(RegistrationRequest request) {
        if (request.getFirstName() == null || request.getFirstName().isBlank()) {
            throw new RegistrationException("First name is required");
        }

        if (request.getLastName() == null || request.getLastName().isBlank()) {
            throw new RegistrationException("Last name is required");
        }

        if (request.getUsername() == null || request.getUsername().isBlank()) {
            throw new RegistrationException("Username is required");
        }

        if (request.getEmail() == null || request.getEmail().isBlank()) {
            throw new RegistrationException("Email is required");
        }

        if (!request.getEmail().trim().matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
            throw new RegistrationException("Enter a valid email address");
        }

        validateNewPassword(request.getPassword(), request.getConfirmPassword());

        if (request.getDateOfBirth() == null || request.getDateOfBirth().isBlank()) {
            throw new RegistrationException("Date of birth is required");
        }

        LocalDate dateOfBirth;
        try {
            dateOfBirth = LocalDate.parse(request.getDateOfBirth(), DateTimeFormatter.ofPattern(DATE_FORMAT));
        } catch (DateTimeParseException ex) {
            throw new RegistrationException("Date of birth must be in yyyy-MM-dd format, e.g. 2000-01-01");
        }

        int age = Period.between(dateOfBirth, LocalDate.now()).getYears();
        if (age < 18) {
            throw new RegistrationException("User must be at least 18 years old to register.");
        }
    }

    public void validateNewPassword(String password, String confirmPassword) {
        if (password == null || password.isBlank()) {
            throw new RegistrationException("Password is required");
        }

        if (password.length() < 8) {
            throw new RegistrationException("Password must be at least 8 characters");
        }

        if (confirmPassword == null || confirmPassword.isBlank()) {
            throw new RegistrationException("Confirm password is required");
        }

        if (!confirmPassword.equals(password)) {
            throw new RegistrationException("Passwords do not match");
        }
    }
}
