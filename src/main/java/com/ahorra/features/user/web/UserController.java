package com.ahorra.features.user.web;

import com.ahorra.features.auth.dto.request.RegistrationRequest;
import com.ahorra.features.auth.dto.response.UserRegistrationResponse;
import com.ahorra.features.user.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@AllArgsConstructor
@RestController
@RequestMapping("${app.title}")
public class UserController {

    private final UserService userService;

    @PostMapping("/register-user")
    public ResponseEntity<UserRegistrationResponse> registerUser(@RequestBody RegistrationRequest request) {
        UserRegistrationResponse response = userService.registerUser(request);
        return ResponseEntity.ok(response);
    }
}
