package com.ahorra.features.session.web;

import com.ahorra.features.session.dto.LogoutResponse;
import com.ahorra.features.session.service.LogoutService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@AllArgsConstructor
@RestController
@RequestMapping("${app.title}/logout")
public class LogoutController {

    private final LogoutService logoutService;

    @PostMapping("/user")
    public ResponseEntity<LogoutResponse> logout(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "").trim();
        LogoutResponse response = logoutService.logout(token);
        return ResponseEntity.ok(response);
    }
}
