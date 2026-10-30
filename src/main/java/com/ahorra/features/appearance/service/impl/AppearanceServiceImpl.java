package com.ahorra.features.appearance.service.impl;

import com.ahorra.features.appearance.service.AppearanceService;
import com.ahorra.features.user.enums.ThemeMode;
import com.ahorra.features.user.model.User;
import com.ahorra.features.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AppearanceServiceImpl implements AppearanceService  {

    private final UserRepository userRepository;

    @Override
    public ThemeMode getTheme(Long userId) {
        return userRepository.findById(userId).map(User::getTheme).orElse(null);
    }

    @Override
    @Transactional
    public void setTheme(Long userId, ThemeMode theme) {
        userRepository.findById(userId).ifPresent(user -> user.setTheme(theme));
    }
}
