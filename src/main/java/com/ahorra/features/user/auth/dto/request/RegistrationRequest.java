package com.ahorra.features.user.auth.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RegistrationRequest {
    private String firstName;
    private String middleName;
    private String lastName;
    private String username;
    private String email;
    private String password;
    private String confirmPassword;
    private String dateOfBirth;
}
