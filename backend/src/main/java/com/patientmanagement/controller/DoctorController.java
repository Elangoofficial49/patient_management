package com.patientmanagement.controller;

import com.patientmanagement.dto.*;
import com.patientmanagement.security.CustomUserDetails;
import com.patientmanagement.service.DoctorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/doctors")
@Tag(name = "Doctor API", description = "Endpoints for doctor portal actions")
public class DoctorController {

    private final DoctorService doctorService;

    public DoctorController(DoctorService doctorService) {
        this.doctorService = doctorService;
    }

    @GetMapping("/dashboard")
    @Operation(summary = "Get doctor dashboard statistics")
    public ResponseEntity<ApiResponse<DoctorDashboardStatsDto>> getDashboardStats(@AuthenticationPrincipal CustomUserDetails user) {
        DoctorDashboardStatsDto stats = doctorService.getDashboardStats(user.getId());
        return ResponseEntity.ok(ApiResponse.success("Doctor dashboard stats retrieved", stats));
    }

    @GetMapping("/profile")
    @Operation(summary = "Get doctor profile")
    public ResponseEntity<ApiResponse<DoctorDto>> getProfile(@AuthenticationPrincipal CustomUserDetails user) {
        DoctorDto profile = doctorService.getDoctorProfile(user.getId());
        return ResponseEntity.ok(ApiResponse.success("Doctor profile retrieved", profile));
    }

    @PutMapping("/profile")
    @Operation(summary = "Update doctor profile")
    public ResponseEntity<ApiResponse<DoctorDto>> updateProfile(@AuthenticationPrincipal CustomUserDetails user,
                                                                 @Valid @RequestBody DoctorDto doctorDto) {
        DoctorDto updated = doctorService.updateDoctorProfile(user.getId(), doctorDto);
        return ResponseEntity.ok(ApiResponse.success("Doctor profile updated successfully", updated));
    }

    @GetMapping("/appointments")
    @Operation(summary = "Get all appointments assigned to logged-in doctor")
    public ResponseEntity<ApiResponse<List<AppointmentResponseDto>>> getAppointments(@AuthenticationPrincipal CustomUserDetails user) {
        List<AppointmentResponseDto> appointments = doctorService.getDoctorAppointments(user.getId());
        return ResponseEntity.ok(ApiResponse.success("Doctor appointments retrieved", appointments));
    }

    @GetMapping("/appointments/today")
    @Operation(summary = "Get today's appointments for logged-in doctor")
    public ResponseEntity<ApiResponse<List<AppointmentResponseDto>>> getTodayAppointments(@AuthenticationPrincipal CustomUserDetails user) {
        List<AppointmentResponseDto> appointments = doctorService.getTodayAppointments(user.getId());
        return ResponseEntity.ok(ApiResponse.success("Today's appointments retrieved", appointments));
    }

    @GetMapping("/patients/{patientId}")
    @Operation(summary = "Get patient details for assigned appointment")
    public ResponseEntity<ApiResponse<PatientProfileDto>> getPatientDetails(@AuthenticationPrincipal CustomUserDetails user,
                                                                           @PathVariable String patientId) {
        PatientProfileDto patient = doctorService.getPatientDetails(user.getId(), patientId);
        return ResponseEntity.ok(ApiResponse.success("Patient details retrieved", patient));
    }

    @GetMapping("/patients/{patientId}/medical-records")
    @Operation(summary = "Get medical records of a patient")
    public ResponseEntity<ApiResponse<List<MedicalRecordDto>>> getPatientMedicalRecords(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable String patientId) {
        List<MedicalRecordDto> records = doctorService.getPatientMedicalRecords(user.getId(), patientId);
        return ResponseEntity.ok(ApiResponse.success("Patient medical records retrieved", records));
    }

    @PostMapping("/medical-records")
    @Operation(summary = "Add a new medical record for a patient")
    public ResponseEntity<ApiResponse<MedicalRecordDto>> createMedicalRecord(
            @AuthenticationPrincipal CustomUserDetails user,
            @Valid @RequestBody MedicalRecordDto dto) {
        MedicalRecordDto record = doctorService.createMedicalRecord(user.getId(), dto);
        return ResponseEntity.ok(ApiResponse.success("Medical record created successfully", record));
    }

    @PutMapping("/medical-records/{id}")
    @Operation(summary = "Update an existing medical record")
    public ResponseEntity<ApiResponse<MedicalRecordDto>> updateMedicalRecord(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable String id,
            @Valid @RequestBody MedicalRecordDto dto) {
        MedicalRecordDto record = doctorService.updateMedicalRecord(user.getId(), id, dto);
        return ResponseEntity.ok(ApiResponse.success("Medical record updated successfully", record));
    }

    @PostMapping("/prescriptions")
    @Operation(summary = "Create a new prescription for a patient")
    public ResponseEntity<ApiResponse<PrescriptionDto>> createPrescription(
            @AuthenticationPrincipal CustomUserDetails user,
            @Valid @RequestBody PrescriptionDto dto) {
        PrescriptionDto prescription = doctorService.createPrescription(user.getId(), dto);
        return ResponseEntity.ok(ApiResponse.success("Prescription created successfully", prescription));
    }

    @PutMapping("/prescriptions/{id}")
    @Operation(summary = "Update an existing prescription")
    public ResponseEntity<ApiResponse<PrescriptionDto>> updatePrescription(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable String id,
            @Valid @RequestBody PrescriptionDto dto) {
        PrescriptionDto prescription = doctorService.updatePrescription(user.getId(), id, dto);
        return ResponseEntity.ok(ApiResponse.success("Prescription updated successfully", prescription));
    }

    @PutMapping("/appointments/{id}/status")
    @Operation(summary = "Update status of an appointment (e.g., COMPLETED, CANCELLED)")
    public ResponseEntity<ApiResponse<AppointmentResponseDto>> updateAppointmentStatus(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable String id,
            @RequestBody Map<String, String> body) {
        String status = body.get("status");
        AppointmentResponseDto updated = doctorService.updateAppointmentStatus(user.getId(), id, status);
        return ResponseEntity.ok(ApiResponse.success("Appointment status updated to " + status, updated));
    }
}

