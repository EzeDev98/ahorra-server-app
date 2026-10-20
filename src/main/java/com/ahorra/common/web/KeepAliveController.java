package com.ahorra.common.web;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("${app.title}")
public class KeepAliveController {

    @GetMapping("/keep-alive")
    public ResponseEntity<String> keepAlive() {
        return ResponseEntity.ok("Ok, I am live.");
    }
}
