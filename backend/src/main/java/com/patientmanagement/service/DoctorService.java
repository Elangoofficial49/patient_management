package com.patientmanagement.service;

import com.patientmanagement.dto.*;

import java.util.List;

public interface DoctorService {
    DoctorDto getDoctorProfile(String userId);
    DoctorDto updateDoctorProfile(String userId, DoctorDto doctorDto);
    List<AppointmentResponseDto> getDoctorAppointments(String userId);
    List<AppointmentResponseDto> getTodayAppointments(String userId);
    PatientProfileDto getPatientDetails(String userId, String patientId);
    List<MedicalRecordDto> getPatientMedicalRecords(String userId, String patientId);
    MedicalRecordDto createMedicalRecord(String userId, MedicalRecordDto dto);
    MedicalRecordDto updateMedicalRecord(String userId, String recordId, MedicalRecordDto dto);
    PrescriptionDto createPrescription(String userId, PrescriptionDto dto);
    PrescriptionDto updatePrescription(String userId, String prescriptionId, PrescriptionDto dto);
    AppointmentResponseDto updateAppointmentStatus(String userId, String appointmentId, String status);
    DoctorDashboardStatsDto getDashboardStats(String userId);
}

