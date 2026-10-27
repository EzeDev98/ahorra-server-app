package com.ahorra.features.reports.service;


import com.ahorra.features.reports.dto.AmbientResponse;

public interface AmbientService {
   AmbientResponse today(Long userId);
}
