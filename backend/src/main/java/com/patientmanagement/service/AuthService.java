package com.patientmanagement.service;

import com.patientmanagement.dto.JwtAuthResponse;
import com.patientmanagement.dto.LoginRequest;
import com.patientmanagement.dto.RegisterRequest;

public interface AuthService {
    JwtAuthResponse login(LoginRequest loginRequest);
    JwtAuthResponse registerPatient(RegisterRequest registerRequest);
    Object getCurrentUserProfile(String username);
}

