package com.patientmanagement.service.impl;

import com.patientmanagement.dto.*;
import com.patientmanagement.entity.Doctor;
import com.patientmanagement.entity.Patient;
import com.patientmanagement.entity.User;
import com.patientmanagement.exception.BadRequestException;
import com.patientmanagement.exception.ResourceNotFoundException;
import com.patientmanagement.repository.DoctorRepository;
import com.patientmanagement.repository.PatientRepository;
import com.patientmanagement.repository.UserRepository;
import com.patientmanagement.security.CustomUserDetails;
import com.patientmanagement.security.JwtTokenProvider;
import com.patientmanagement.service.AuthService;
import com.patientmanagement.util.CodeGenerator;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;

    public AuthServiceImpl(AuthenticationManager authenticationManager, UserRepository userRepository,
                           PatientRepository patientRepository, DoctorRepository doctorRepository,
                           PasswordEncoder passwordEncoder, JwtTokenProvider tokenProvider) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.patientRepository = patientRepository;
        this.doctorRepository = doctorRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
    }

    @Override
    public JwtAuthResponse login(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsername(),
                        loginRequest.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String token = tokenProvider.generateToken(authentication);
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        String fullName = userDetails.getUsername();
        if ("PATIENT".equals(userDetails.getRole())) {
            Optional<Patient> p = patientRepository.findByUserId(userDetails.getId());
            if (p.isPresent()) fullName = p.get().getFullName();
        } else if ("DOCTOR".equals(userDetails.getRole())) {
            Optional<Doctor> d = doctorRepository.findByUserId(userDetails.getId());
            if (d.isPresent()) fullName = d.get().getFullName();
        } else {
            fullName = "System Administrator";
        }

        return new JwtAuthResponse(
                token,
                userDetails.getId(),
                userDetails.getUsername(),
                userDetails.getEmail(),
                userDetails.getRole(),
                userDetails.getProfileId(),
                fullName
        );
    }

    @Override
    public JwtAuthResponse registerPatient(RegisterRequest registerRequest) {
        if (userRepository.existsByUsername(registerRequest.getUsername())) {
            throw new BadRequestException("Username is already taken!");
        }

        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new BadRequestException("Email is already registered!");
        }

        User user = new User(
                registerRequest.getUsername(),
                registerRequest.getEmail(),
                passwordEncoder.encode(registerRequest.getPassword()),
                "PATIENT"
        );
        User savedUser = userRepository.save(user);

        Patient patient = new Patient();
        patient.setUserId(savedUser.getId());
        patient.setPatientCode(CodeGenerator.generatePatientCode());
        patient.setFullName(registerRequest.getFullName());
        patient.setEmail(registerRequest.getEmail());
        patient.setPhone(registerRequest.getPhone());
        patient.setDateOfBirth(registerRequest.getDateOfBirth());
        patient.setGender(registerRequest.getGender());
        patient.setAddress(registerRequest.getAddress());
        patient.setBloodGroup(registerRequest.getBloodGroup());
        patient.setEmergencyContact(registerRequest.getEmergencyContact());

        Patient savedPatient = patientRepository.save(patient);

        LoginRequest loginReq = new LoginRequest();
        loginReq.setUsername(registerRequest.getUsername());
        loginReq.setPassword(registerRequest.getPassword());
        return login(loginReq);
    }

    @Override
    public Object getCurrentUserProfile(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if ("PATIENT".equals(user.getRole())) {
            return patientRepository.findByUserId(user.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Patient profile not found"));
        } else if ("DOCTOR".equals(user.getRole())) {
            return doctorRepository.findByUserId(user.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Doctor profile not found"));
        }
        return user;
    }
}

