package com.ahorra.features.pin.service;

import com.ahorra.features.pin.dto.request.ChangePinRequest;
import com.ahorra.features.pin.dto.request.PinRequest;
import com.ahorra.features.pin.dto.request.VerifyPinRequest;
import com.ahorra.features.pin.dto.response.PinStatusResponse;

public interface PinService {
    PinStatusResponse status();
    PinStatusResponse setPin(PinRequest request);
    PinStatusResponse changePin(ChangePinRequest request);
    void verifyPin(VerifyPinRequest request);
    PinStatusResponse removePin(VerifyPinRequest request);
    PinStatusResponse resetPin(PinRequest request);
    void markFreshLogin(Long userId);
}
