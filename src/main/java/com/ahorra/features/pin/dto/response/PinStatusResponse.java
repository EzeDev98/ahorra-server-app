package com.ahorra.features.pin.dto.response;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PinStatusResponse {
    private final boolean pinSet;
    private final boolean locked;
    private final long retryAfterSeconds;
}
