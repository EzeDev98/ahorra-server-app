package com.ahorra.features.pin.web;

import com.ahorra.features.pin.dto.request.ChangePinRequest;
import com.ahorra.features.pin.dto.request.PinRequest;
import com.ahorra.features.pin.dto.request.VerifyPinRequest;
import com.ahorra.features.pin.dto.response.PinStatusResponse;
import com.ahorra.features.pin.service.PinService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("${app.title}/pin")
public class PinController {

    private final PinService pinService;

    @PostMapping
    public ResponseEntity<PinStatusResponse> set(@RequestBody PinRequest request) {
        PinStatusResponse response = pinService.setPin(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/status")
    public ResponseEntity<PinStatusResponse> status() {
        return ResponseEntity.ok(pinService.status());
    }

    @PutMapping
    public ResponseEntity<PinStatusResponse> changePin(@RequestBody ChangePinRequest request) {
        PinStatusResponse response = pinService.changePin(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/verify")
    public ResponseEntity<Map<String, Boolean>> verify(@RequestBody VerifyPinRequest request) {
        pinService.verifyPin(request);
        return ResponseEntity.ok(Map.of("valid", true));
    }

    @PostMapping("/remove")
    public ResponseEntity<PinStatusResponse> remove(@RequestBody VerifyPinRequest request) {
        PinStatusResponse response = pinService.removePin(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/reset")
    public ResponseEntity<PinStatusResponse> reset(@RequestBody PinRequest request) {
        PinStatusResponse response = pinService.resetPin(request);
        return ResponseEntity.ok(response);
    }
}
