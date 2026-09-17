package com.patientmanagement.service;

import com.patientmanagement.dto.*;
import com.patientmanagement.entity.Department;
import com.patientmanagement.entity.User;

import java.util.List;

public interface AdminService {
    AdminDashboardStatsDto getDashboardStats();
    List<PatientProfileDto> getPatients();
    List<DoctorDto> getDoctors();
    DoctorDto addDoctor(DoctorCreateRequest request);
    DoctorDto updateDoctor(String doctorId, DoctorDto dto);
    void deleteDoctor(String doctorId);
    List<Department> getDepartments();
    Department addDepartment(Department department);
    Department updateDepartment(String id, Department department);
    void deleteDepartment(String id);
    List<AppointmentResponseDto> getAllAppointments();
    AppointmentResponseDto updateAppointmentStatus(String id, String status);
    List<User> getAllUsers();
}

