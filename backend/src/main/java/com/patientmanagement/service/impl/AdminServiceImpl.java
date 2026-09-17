package com.patientmanagement.service.impl;

import com.patientmanagement.dto.*;
import com.patientmanagement.entity.*;
import com.patientmanagement.exception.BadRequestException;
import com.patientmanagement.exception.ResourceNotFoundException;
import com.patientmanagement.repository.*;
import com.patientmanagement.service.AdminService;
import com.patientmanagement.util.CodeGenerator;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AdminServiceImpl implements AdminService {

    private final UserRepository userRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final DepartmentRepository departmentRepository;
    private final AppointmentRepository appointmentRepository;
    private final NotificationRepository notificationRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminServiceImpl(UserRepository userRepository, PatientRepository patientRepository,
                            DoctorRepository doctorRepository, DepartmentRepository departmentRepository,
                            AppointmentRepository appointmentRepository, NotificationRepository notificationRepository,
                            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.patientRepository = patientRepository;
        this.doctorRepository = doctorRepository;
        this.departmentRepository = departmentRepository;
        this.appointmentRepository = appointmentRepository;
        this.notificationRepository = notificationRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public AdminDashboardStatsDto getDashboardStats() {
        long totalPatients = patientRepository.count();
        long totalDoctors = doctorRepository.count();
        long totalAppointments = appointmentRepository.count();
        long pendingAppointments = appointmentRepository.countByStatus("PENDING");
        long completedAppointments = appointmentRepository.countByStatus("COMPLETED");
        long totalDepartments = departmentRepository.count();

        return new AdminDashboardStatsDto(totalPatients, totalDoctors, totalAppointments, pendingAppointments, completedAppointments, totalDepartments);
    }

    @Override
    public List<PatientProfileDto> getPatients() {
        return patientRepository.findAll().stream().map(p -> {
            PatientProfileDto dto = new PatientProfileDto();
            dto.setId(p.getId());
            dto.setUserId(p.getUserId());
            dto.setPatientCode(p.getPatientCode());
            dto.setFullName(p.getFullName());
            dto.setEmail(p.getEmail());
            dto.setPhone(p.getPhone());
            dto.setDateOfBirth(p.getDateOfBirth());
            dto.setGender(p.getGender());
            dto.setAddress(p.getAddress());
            dto.setBloodGroup(p.getBloodGroup());
            dto.setEmergencyContact(p.getEmergencyContact());
            userRepository.findById(p.getUserId()).ifPresent(u -> dto.setUsername(u.getUsername()));
            return dto;
        }).collect(Collectors.toList());
    }

    @Override
    public List<DoctorDto> getDoctors() {
        return doctorRepository.findAll().stream().map(this::mapToDoctorDto).collect(Collectors.toList());
    }

    @Override
    public DoctorDto addDoctor(DoctorCreateRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BadRequestException("Username is already taken!");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email is already registered!");
        }

        User user = new User(request.getUsername(), request.getEmail(), passwordEncoder.encode(request.getPassword()), "DOCTOR");
        User savedUser = userRepository.save(user);

        Doctor doctor = new Doctor();
        doctor.setUserId(savedUser.getId());
        doctor.setDoctorCode(CodeGenerator.generateDoctorCode());
        doctor.setFullName(request.getFullName());
        doctor.setEmail(request.getEmail());
        doctor.setPhone(request.getPhone());
        doctor.setSpecialization(request.getSpecialization());
        doctor.setDepartmentId(request.getDepartmentId());
        doctor.setQualification(request.getQualification());
        doctor.setExperience(request.getExperience());
        doctor.setConsultationFee(request.getConsultationFee());
        doctor.setAvailableDays(request.getAvailableDays() != null ? request.getAvailableDays() : List.of("MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY"));
        doctor.setAvailableFrom(request.getAvailableFrom() != null ? request.getAvailableFrom() : "09:00");
        doctor.setAvailableTo(request.getAvailableTo() != null ? request.getAvailableTo() : "17:00");
        doctor.setStatus("ACTIVE");

        Doctor savedDoctor = doctorRepository.save(doctor);
        return mapToDoctorDto(savedDoctor);
    }

    @Override
    public DoctorDto updateDoctor(String doctorId, DoctorDto dto) {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found with ID: " + doctorId));

        doctor.setFullName(dto.getFullName());
        doctor.setSpecialization(dto.getSpecialization());
        doctor.setDepartmentId(dto.getDepartmentId());
        doctor.setQualification(dto.getQualification());
        doctor.setExperience(dto.getExperience());
        doctor.setPhone(dto.getPhone());
        doctor.setConsultationFee(dto.getConsultationFee());
        if (dto.getAvailableDays() != null) doctor.setAvailableDays(dto.getAvailableDays());
        if (dto.getAvailableFrom() != null) doctor.setAvailableFrom(dto.getAvailableFrom());
        if (dto.getAvailableTo() != null) doctor.setAvailableTo(dto.getAvailableTo());
        if (dto.getStatus() != null) doctor.setStatus(dto.getStatus());

        Doctor updated = doctorRepository.save(doctor);
        return mapToDoctorDto(updated);
    }

    @Override
    public void deleteDoctor(String doctorId) {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found with ID: " + doctorId));
        doctor.setStatus("INACTIVE");
        doctorRepository.save(doctor);
    }

    @Override
    public List<Department> getDepartments() {
        return departmentRepository.findAll();
    }

    @Override
    public Department addDepartment(Department department) {
        if (departmentRepository.existsByDepartmentName(department.getDepartmentName())) {
            throw new BadRequestException("Department name already exists!");
        }
        return departmentRepository.save(department);
    }

    @Override
    public Department updateDepartment(String id, Department department) {
        Department existing = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with ID: " + id));

        existing.setDepartmentName(department.getDepartmentName());
        existing.setDescription(department.getDescription());
        if (department.getStatus() != null) existing.setStatus(department.getStatus());

        return departmentRepository.save(existing);
    }

    @Override
    public void deleteDepartment(String id) {
        Department existing = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with ID: " + id));
        existing.setStatus("INACTIVE");
        departmentRepository.save(existing);
    }

    @Override
    public List<AppointmentResponseDto> getAllAppointments() {
        return appointmentRepository.findAll().stream().map(this::mapToAppointmentResponseDto).collect(Collectors.toList());
    }

    @Override
    public AppointmentResponseDto updateAppointmentStatus(String id, String status) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found with ID: " + id));

        appointment.setStatus(status);
        Appointment updated = appointmentRepository.save(appointment);

        patientRepository.findById(appointment.getPatientId()).ifPresent(p -> {
            notificationRepository.save(new Notification(p.getUserId(), "Appointment Updated by Admin",
                    "Status for appointment " + updated.getAppointmentCode() + " has been changed to: " + status, "APPOINTMENT"));
        });

        return mapToAppointmentResponseDto(updated);
    }

    @Override
    public List<User> getAllUsers() {
        List<User> users = userRepository.findAll();
        users.forEach(u -> u.setPassword("[PROTECTED]"));
        return users;
    }

    private DoctorDto mapToDoctorDto(Doctor doctor) {
        DoctorDto dto = new DoctorDto();
        dto.setId(doctor.getId());
        dto.setUserId(doctor.getUserId());
        dto.setDoctorCode(doctor.getDoctorCode());
        dto.setFullName(doctor.getFullName());
        dto.setSpecialization(doctor.getSpecialization());
        dto.setDepartmentId(doctor.getDepartmentId());
        if (doctor.getDepartmentId() != null) {
            departmentRepository.findById(doctor.getDepartmentId())
                    .ifPresent(dept -> dto.setDepartmentName(dept.getDepartmentName()));
        }
        dto.setQualification(doctor.getQualification());
        dto.setExperience(doctor.getExperience());
        dto.setPhone(doctor.getPhone());
        dto.setEmail(doctor.getEmail());
        dto.setConsultationFee(doctor.getConsultationFee());
        dto.setAvailableDays(doctor.getAvailableDays());
        dto.setAvailableFrom(doctor.getAvailableFrom());
        dto.setAvailableTo(doctor.getAvailableTo());
        dto.setProfileImage(doctor.getProfileImage());
        dto.setStatus(doctor.getStatus());
        return dto;
    }

    private AppointmentResponseDto mapToAppointmentResponseDto(Appointment appointment) {
        AppointmentResponseDto dto = new AppointmentResponseDto();
        dto.setId(appointment.getId());
        dto.setAppointmentCode(appointment.getAppointmentCode());
        dto.setPatientId(appointment.getPatientId());
        dto.setDoctorId(appointment.getDoctorId());
        dto.setAppointmentDate(appointment.getAppointmentDate());
        dto.setAppointmentTime(appointment.getAppointmentTime());
        dto.setReason(appointment.getReason());
        dto.setStatus(appointment.getStatus());
        dto.setCreatedAt(appointment.getCreatedAt());

        patientRepository.findById(appointment.getPatientId()).ifPresent(p -> {
            dto.setPatientName(p.getFullName());
            dto.setPatientCode(p.getPatientCode());
            dto.setPatientPhone(p.getPhone());
        });

        doctorRepository.findById(appointment.getDoctorId()).ifPresent(d -> {
            dto.setDoctorName(d.getFullName());
            dto.setDoctorSpecialization(d.getSpecialization());
            dto.setConsultationFee(d.getConsultationFee());
            if (d.getDepartmentId() != null) {
                departmentRepository.findById(d.getDepartmentId())
                        .ifPresent(dept -> dto.setDepartmentName(dept.getDepartmentName()));
            }
        });

        return dto;
    }
}

