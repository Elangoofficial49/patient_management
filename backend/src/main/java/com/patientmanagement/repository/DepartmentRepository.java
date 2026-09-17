package com.patientmanagement.repository;

import com.patientmanagement.entity.Department;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DepartmentRepository extends MongoRepository<Department, String> {
    Optional<Department> findByDepartmentName(String departmentName);
    Boolean existsByDepartmentName(String departmentName);
}

