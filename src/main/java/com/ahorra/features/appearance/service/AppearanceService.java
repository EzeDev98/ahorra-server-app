package com.ahorra.features.appearance.service;

import com.ahorra.features.user.enums.ThemeMode;

public interface AppearanceService {
    ThemeMode getTheme(Long userId);
    void setTheme(Long userId, ThemeMode theme);
}
