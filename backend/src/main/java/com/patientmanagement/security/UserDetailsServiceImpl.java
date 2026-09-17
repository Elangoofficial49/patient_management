package com.patientmanagement.security;

import com.patientmanagement.entity.Doctor;
import com.patientmanagement.entity.Patient;
import com.patientmanagement.entity.User;
import com.patientmanagement.repository.DoctorRepository;
import com.patientmanagement.repository.PatientRepository;
import com.patientmanagement.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;

    public UserDetailsServiceImpl(UserRepository userRepository, PatientRepository patientRepository, DoctorRepository doctorRepository) {
        this.userRepository = userRepository;
        this.patientRepository = patientRepository;
        this.doctorRepository = doctorRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String usernameOrEmail) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(usernameOrEmail)
                .orElseGet(() -> userRepository.findByEmail(usernameOrEmail)
                        .orElseThrow(() -> new UsernameNotFoundException("User not found with username or email: " + usernameOrEmail)));

        String profileId = null;
        if ("PATIENT".equalsIgnoreCase(user.getRole())) {
            Optional<Patient> patient = patientRepository.findByUserId(user.getId());
            if (patient.isPresent()) {
                profileId = patient.get().getId();
            }
        } else if ("DOCTOR".equalsIgnoreCase(user.getRole())) {
            Optional<Doctor> doctor = doctorRepository.findByUserId(user.getId());
            if (doctor.isPresent()) {
                profileId = doctor.get().getId();
            }
        }

        return CustomUserDetails.create(user, profileId);
    }
}

