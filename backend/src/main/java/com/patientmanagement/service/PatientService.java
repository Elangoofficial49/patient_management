package com.patientmanagement.service;

import com.patientmanagement.dto.*;
import com.patientmanagement.entity.Notification;

import java.util.List;

public interface PatientService {
    PatientProfileDto getPatientProfile(String userId);
    PatientProfileDto updatePatientProfile(String userId, PatientProfileDto profileDto);
    List<DoctorDto> getDoctors(String departmentId, String search);
    DoctorDto getDoctorById(String doctorId);
    AppointmentResponseDto bookAppointment(String userId, AppointmentBookingRequest request);
    List<AppointmentResponseDto> getPatientAppointments(String userId);
    AppointmentResponseDto rescheduleAppointment(String userId, String appointmentId, String newDate, String newTime);
    AppointmentResponseDto cancelAppointment(String userId, String appointmentId);
    List<MedicalRecordDto> getMedicalRecords(String userId);
    List<PrescriptionDto> getPrescriptions(String userId);
    List<Notification> getNotifications(String userId);
    PatientDashboardStatsDto getDashboardStats(String userId);
}

