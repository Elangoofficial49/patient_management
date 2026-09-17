package com.patientmanagement.controller;

import com.patientmanagement.dto.*;
import com.patientmanagement.entity.Department;
import com.patientmanagement.entity.User;
import com.patientmanagement.service.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@Tag(name = "Admin API", description = "Endpoints for hospital system administration")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/dashboard")
    @Operation(summary = "Get system-wide hospital dashboard statistics")
    public ResponseEntity<ApiResponse<AdminDashboardStatsDto>> getDashboardStats() {
        AdminDashboardStatsDto stats = adminService.getDashboardStats();
        return ResponseEntity.ok(ApiResponse.success("Admin dashboard stats retrieved", stats));
    }

    @GetMapping("/patients")
    @Operation(summary = "Get all registered patients")
    public ResponseEntity<ApiResponse<List<PatientProfileDto>>> getPatients() {
        List<PatientProfileDto> patients = adminService.getPatients();
        return ResponseEntity.ok(ApiResponse.success("Patients retrieved", patients));
    }

    @GetMapping("/doctors")
    @Operation(summary = "Get all doctors")
    public ResponseEntity<ApiResponse<List<DoctorDto>>> getDoctors() {
        List<DoctorDto> doctors = adminService.getDoctors();
        return ResponseEntity.ok(ApiResponse.success("Doctors retrieved", doctors));
    }

    @PostMapping("/doctors")
    @Operation(summary = "Add a new doctor account")
    public ResponseEntity<ApiResponse<DoctorDto>> addDoctor(@Valid @RequestBody DoctorCreateRequest request) {
        DoctorDto doctor = adminService.addDoctor(request);
        return ResponseEntity.ok(ApiResponse.success("Doctor added successfully", doctor));
    }

    @PutMapping("/doctors/{id}")
    @Operation(summary = "Update doctor details")
    public ResponseEntity<ApiResponse<DoctorDto>> updateDoctor(@PathVariable String id, @Valid @RequestBody DoctorDto dto) {
        DoctorDto updated = adminService.updateDoctor(id, dto);
        return ResponseEntity.ok(ApiResponse.success("Doctor updated successfully", updated));
    }

    @DeleteMapping("/doctors/{id}")
    @Operation(summary = "Deactivate doctor account")
    public ResponseEntity<ApiResponse<Void>> deleteDoctor(@PathVariable String id) {
        adminService.deleteDoctor(id);
        return ResponseEntity.ok(ApiResponse.success("Doctor deactivated successfully"));
    }

    @GetMapping("/departments")
    @Operation(summary = "Get all departments")
    public ResponseEntity<ApiResponse<List<Department>>> getDepartments() {
        List<Department> departments = adminService.getDepartments();
        return ResponseEntity.ok(ApiResponse.success("Departments retrieved", departments));
    }

    @PostMapping("/departments")
    @Operation(summary = "Add a new department")
    public ResponseEntity<ApiResponse<Department>> addDepartment(@RequestBody Department department) {
        Department created = adminService.addDepartment(department);
        return ResponseEntity.ok(ApiResponse.success("Department added successfully", created));
    }

    @PutMapping("/departments/{id}")
    @Operation(summary = "Update department details")
    public ResponseEntity<ApiResponse<Department>> updateDepartment(@PathVariable String id, @RequestBody Department department) {
        Department updated = adminService.updateDepartment(id, department);
        return ResponseEntity.ok(ApiResponse.success("Department updated successfully", updated));
    }

    @DeleteMapping("/departments/{id}")
    @Operation(summary = "Deactivate department")
    public ResponseEntity<ApiResponse<Void>> deleteDepartment(@PathVariable String id) {
        adminService.deleteDepartment(id);
        return ResponseEntity.ok(ApiResponse.success("Department deactivated successfully"));
    }

    @GetMapping("/appointments")
    @Operation(summary = "Get all hospital appointments")
    public ResponseEntity<ApiResponse<List<AppointmentResponseDto>>> getAllAppointments() {
        List<AppointmentResponseDto> appointments = adminService.getAllAppointments();
        return ResponseEntity.ok(ApiResponse.success("All appointments retrieved", appointments));
    }

    @PutMapping("/appointments/{id}/status")
    @Operation(summary = "Update appointment status by admin")
    public ResponseEntity<ApiResponse<AppointmentResponseDto>> updateAppointmentStatus(
            @PathVariable String id,
            @RequestBody Map<String, String> body) {
        String status = body.get("status");
        AppointmentResponseDto updated = adminService.updateAppointmentStatus(id, status);
        return ResponseEntity.ok(ApiResponse.success("Appointment status updated to " + status, updated));
    }

    @GetMapping("/users")
    @Operation(summary = "Get list of all system users")
    public ResponseEntity<ApiResponse<List<User>>> getAllUsers() {
        List<User> users = adminService.getAllUsers();
        return ResponseEntity.ok(ApiResponse.success("Users retrieved", users));
    }
}

