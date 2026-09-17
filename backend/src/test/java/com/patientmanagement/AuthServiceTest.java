package com.patientmanagement;

import com.patientmanagement.dto.JwtAuthResponse;
import com.patientmanagement.dto.LoginRequest;
import com.patientmanagement.dto.RegisterRequest;
import com.patientmanagement.entity.User;
import com.patientmanagement.repository.UserRepository;
import com.patientmanagement.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class AuthServiceTest {

    @Autowired
    private AuthService authService;

    @Autowired
    private UserRepository userRepository;

    @Test
    public void testPatientRegistrationAndLogin() {
        String username = "testuser_" + System.currentTimeMillis();
        String email = username + "@test.com";

        RegisterRequest regReq = new RegisterRequest();
        regReq.setUsername(username);
        regReq.setEmail(email);
        regReq.setPassword("Password123!");
        regReq.setFullName("Test User");
        regReq.setPhone("+1-555-9999");
        regReq.setDateOfBirth(LocalDate.of(1995, 1, 1));
        regReq.setGender("Other");

        JwtAuthResponse regResponse = authService.registerPatient(regReq);
        assertNotNull(regResponse);
        assertNotNull(regResponse.getAccessToken());
        assertEquals(username, regResponse.getUsername());
        assertEquals("PATIENT", regResponse.getRole());

        // Verify password hashing in DB
        User user = userRepository.findByUsername(username).orElse(null);
        assertNotNull(user);
        assertNotEquals("Password123!", user.getPassword());
        assertTrue(user.getPassword().startsWith("$2a$"));

        // Login Test
        LoginRequest loginReq = new LoginRequest();
        loginReq.setUsername(username);
        loginReq.setPassword("Password123!");

        JwtAuthResponse loginResponse = authService.login(loginReq);
        assertNotNull(loginResponse);
        assertNotNull(loginResponse.getAccessToken());
    }
}

