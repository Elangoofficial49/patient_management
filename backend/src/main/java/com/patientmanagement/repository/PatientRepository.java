package com.patientmanagement.repository;

import com.patientmanagement.entity.Patient;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PatientRepository extends MongoRepository<Patient, String> {
    Optional<Patient> findByUserId(String userId);
    Optional<Patient> findByPatientCode(String patientCode);
    List<Patient> findByFullNameContainingIgnoreCase(String name);
}

