package com.ahorra.features.pin.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ChangePinRequest {
    private String oldPin;
    private String pin;
    private String confirmPin;
}
