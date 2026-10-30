package com.ahorra.features.appearance.dto;

import com.ahorra.features.user.enums.ThemeMode;
import jakarta.validation.constraints.NotNull;

public record AppearanceSetting(@NotNull ThemeMode theme) {
}
