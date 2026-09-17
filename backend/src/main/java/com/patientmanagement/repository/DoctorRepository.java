package com.patientmanagement.repository;

import com.patientmanagement.entity.Doctor;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DoctorRepository extends MongoRepository<Doctor, String> {
    Optional<Doctor> findByUserId(String userId);
    Optional<Doctor> findByDoctorCode(String doctorCode);
    List<Doctor> findByDepartmentId(String departmentId);
    List<Doctor> findByFullNameContainingIgnoreCase(String name);
    List<Doctor> findBySpecializationContainingIgnoreCase(String specialization);
    List<Doctor> findByStatus(String status);
}

