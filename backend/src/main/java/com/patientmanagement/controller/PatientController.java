package com.patientmanagement.controller;

import com.patientmanagement.dto.*;
import com.patientmanagement.entity.Notification;
import com.patientmanagement.security.CustomUserDetails;
import com.patientmanagement.service.PatientService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/patients")
@Tag(name = "Patient API", description = "Endpoints for patient portal actions")
public class PatientController {

    private final PatientService patientService;

    public PatientController(PatientService patientService) {
        this.patientService = patientService;
    }

    @GetMapping("/dashboard")
    @Operation(summary = "Get patient dashboard statistics")
    public ResponseEntity<ApiResponse<PatientDashboardStatsDto>> getDashboardStats(@AuthenticationPrincipal CustomUserDetails user) {
        PatientDashboardStatsDto stats = patientService.getDashboardStats(user.getId());
        return ResponseEntity.ok(ApiResponse.success("Dashboard statistics fetched", stats));
    }

    @GetMapping("/profile")
    @Operation(summary = "Get patient profile")
    public ResponseEntity<ApiResponse<PatientProfileDto>> getProfile(@AuthenticationPrincipal CustomUserDetails user) {
        PatientProfileDto profile = patientService.getPatientProfile(user.getId());
        return ResponseEntity.ok(ApiResponse.success("Patient profile fetched", profile));
    }

    @PutMapping("/profile")
    @Operation(summary = "Update patient profile")
    public ResponseEntity<ApiResponse<PatientProfileDto>> updateProfile(@AuthenticationPrincipal CustomUserDetails user,
                                                                        @Valid @RequestBody PatientProfileDto profileDto) {
        PatientProfileDto updated = patientService.updatePatientProfile(user.getId(), profileDto);
        return ResponseEntity.ok(ApiResponse.success("Profile updated successfully", updated));
    }

    @GetMapping("/doctors")
    @Operation(summary = "View and search doctors with optional department filter")
    public ResponseEntity<ApiResponse<List<DoctorDto>>> getDoctors(
            @RequestParam(required = false) String departmentId,
            @RequestParam(required = false) String search) {
        List<DoctorDto> doctors = patientService.getDoctors(departmentId, search);
        return ResponseEntity.ok(ApiResponse.success("Doctors retrieved", doctors));
    }

    @GetMapping("/doctors/{id}")
    @Operation(summary = "Get doctor details by ID")
    public ResponseEntity<ApiResponse<DoctorDto>> getDoctorById(@PathVariable String id) {
        DoctorDto doctor = patientService.getDoctorById(id);
        return ResponseEntity.ok(ApiResponse.success("Doctor details retrieved", doctor));
    }

    @GetMapping("/appointments")
    @Operation(summary = "Get all appointments for logged-in patient")
    public ResponseEntity<ApiResponse<List<AppointmentResponseDto>>> getAppointments(@AuthenticationPrincipal CustomUserDetails user) {
        List<AppointmentResponseDto> appointments = patientService.getPatientAppointments(user.getId());
        return ResponseEntity.ok(ApiResponse.success("Appointments retrieved", appointments));
    }

    @PostMapping("/appointments")
    @Operation(summary = "Book a new appointment")
    public ResponseEntity<ApiResponse<AppointmentResponseDto>> bookAppointment(@AuthenticationPrincipal CustomUserDetails user,
                                                                                 @Valid @RequestBody AppointmentBookingRequest request) {
        AppointmentResponseDto appointment = patientService.bookAppointment(user.getId(), request);
        return ResponseEntity.ok(ApiResponse.success("Appointment booked successfully", appointment));
    }

    @PutMapping("/appointments/{id}")
    @Operation(summary = "Reschedule an existing appointment")
    public ResponseEntity<ApiResponse<AppointmentResponseDto>> rescheduleAppointment(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable String id,
            @RequestBody Map<String, String> body) {
        String newDate = body.get("appointmentDate");
        String newTime = body.get("appointmentTime");
        AppointmentResponseDto appointment = patientService.rescheduleAppointment(user.getId(), id, newDate, newTime);
        return ResponseEntity.ok(ApiResponse.success("Appointment rescheduled successfully", appointment));
    }

    @DeleteMapping("/appointments/{id}")
    @Operation(summary = "Cancel an appointment")
    public ResponseEntity<ApiResponse<AppointmentResponseDto>> cancelAppointment(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable String id) {
        AppointmentResponseDto appointment = patientService.cancelAppointment(user.getId(), id);
        return ResponseEntity.ok(ApiResponse.success("Appointment cancelled successfully", appointment));
    }

    @GetMapping("/medical-records")
    @Operation(summary = "Get medical records for logged-in patient")
    public ResponseEntity<ApiResponse<List<MedicalRecordDto>>> getMedicalRecords(@AuthenticationPrincipal CustomUserDetails user) {
        List<MedicalRecordDto> records = patientService.getMedicalRecords(user.getId());
        return ResponseEntity.ok(ApiResponse.success("Medical records retrieved", records));
    }

    @GetMapping("/prescriptions")
    @Operation(summary = "Get prescriptions for logged-in patient")
    public ResponseEntity<ApiResponse<List<PrescriptionDto>>> getPrescriptions(@AuthenticationPrincipal CustomUserDetails user) {
        List<PrescriptionDto> prescriptions = patientService.getPrescriptions(user.getId());
        return ResponseEntity.ok(ApiResponse.success("Prescriptions retrieved", prescriptions));
    }

    @GetMapping("/notifications")
    @Operation(summary = "Get notifications for logged-in patient")
    public ResponseEntity<ApiResponse<List<Notification>>> getNotifications(@AuthenticationPrincipal CustomUserDetails user) {
        List<Notification> notifications = patientService.getNotifications(user.getId());
        return ResponseEntity.ok(ApiResponse.success("Notifications retrieved", notifications));
    }
}

