package com.patientmanagement.service.impl;

import com.patientmanagement.dto.*;
import com.patientmanagement.entity.*;
import com.patientmanagement.exception.AppointmentConflictException;
import com.patientmanagement.exception.BadRequestException;
import com.patientmanagement.exception.ResourceNotFoundException;
import com.patientmanagement.exception.UnauthorizedException;
import com.patientmanagement.repository.*;
import com.patientmanagement.service.PatientService;
import com.patientmanagement.util.CodeGenerator;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class PatientServiceImpl implements PatientService {

    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final DepartmentRepository departmentRepository;
    private final AppointmentRepository appointmentRepository;
    private final MedicalRecordRepository medicalRecordRepository;
    private final PrescriptionRepository prescriptionRepository;
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    public PatientServiceImpl(PatientRepository patientRepository, DoctorRepository doctorRepository,
                              DepartmentRepository departmentRepository, AppointmentRepository appointmentRepository,
                              MedicalRecordRepository medicalRecordRepository, PrescriptionRepository prescriptionRepository,
                              NotificationRepository notificationRepository, UserRepository userRepository) {
        this.patientRepository = patientRepository;
        this.doctorRepository = doctorRepository;
        this.departmentRepository = departmentRepository;
        this.appointmentRepository = appointmentRepository;
        this.medicalRecordRepository = medicalRecordRepository;
        this.prescriptionRepository = prescriptionRepository;
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
    }

    @Override
    public PatientProfileDto getPatientProfile(String userId) {
        Patient patient = patientRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient profile not found for user: " + userId));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User record not found"));
        return mapToProfileDto(patient, user);
    }

    @Override
    public PatientProfileDto updatePatientProfile(String userId, PatientProfileDto profileDto) {
        Patient patient = patientRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient profile not found"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User record not found"));

        patient.setFullName(profileDto.getFullName());
        patient.setPhone(profileDto.getPhone());
        patient.setDateOfBirth(profileDto.getDateOfBirth());
        patient.setGender(profileDto.getGender());
        patient.setAddress(profileDto.getAddress());
        patient.setBloodGroup(profileDto.getBloodGroup());
        patient.setEmergencyContact(profileDto.getEmergencyContact());

        patientRepository.save(patient);
        return mapToProfileDto(patient, user);
    }

    @Override
    public List<DoctorDto> getDoctors(String departmentId, String search) {
        List<Doctor> doctors;

        if (departmentId != null && !departmentId.isBlank()) {
            doctors = doctorRepository.findByDepartmentId(departmentId);
        } else if (search != null && !search.isBlank()) {
            doctors = doctorRepository.findByFullNameContainingIgnoreCase(search);
            if (doctors.isEmpty()) {
                doctors = doctorRepository.findBySpecializationContainingIgnoreCase(search);
            }
        } else {
            doctors = doctorRepository.findAll();
        }

        return doctors.stream()
                .filter(d -> "ACTIVE".equalsIgnoreCase(d.getStatus()))
                .map(this::mapToDoctorDto)
                .collect(Collectors.toList());
    }

    @Override
    public DoctorDto getDoctorById(String doctorId) {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found with ID: " + doctorId));
        return mapToDoctorDto(doctor);
    }

    @Override
    public AppointmentResponseDto bookAppointment(String userId, AppointmentBookingRequest request) {
        Patient patient = patientRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient profile not found for user: " + userId));

        Doctor doctor = doctorRepository.findById(request.getDoctorId())
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found with ID: " + request.getDoctorId()));

        if (!"ACTIVE".equalsIgnoreCase(doctor.getStatus())) {
            throw new BadRequestException("Doctor is currently inactive and cannot accept appointments");
        }

        if (request.getAppointmentDate().isBefore(LocalDate.now())) {
            throw new BadRequestException("Appointment date cannot be in the past");
        }

        // Check double booking
        List<Appointment> existing = appointmentRepository.findByDoctorIdAndAppointmentDateAndAppointmentTimeAndStatusNot(
                doctor.getId(), request.getAppointmentDate(), request.getAppointmentTime(), "CANCELLED");
        if (!existing.isEmpty()) {
            throw new AppointmentConflictException("Doctor is already booked at " + request.getAppointmentTime() + " on " + request.getAppointmentDate());
        }

        Appointment appointment = new Appointment();
        appointment.setAppointmentCode(CodeGenerator.generateAppointmentCode());
        appointment.setPatientId(patient.getId());
        appointment.setDoctorId(doctor.getId());
        appointment.setAppointmentDate(request.getAppointmentDate());
        appointment.setAppointmentTime(request.getAppointmentTime());
        appointment.setReason(request.getReason());
        appointment.setStatus("CONFIRMED");

        Appointment saved = appointmentRepository.save(appointment);

        // Notify patient & doctor
        notificationRepository.save(new Notification(userId, "Appointment Booked",
                "Your appointment " + saved.getAppointmentCode() + " with Dr. " + doctor.getFullName() + " on " + saved.getAppointmentDate() + " at " + saved.getAppointmentTime() + " is confirmed.",
                "APPOINTMENT"));

        notificationRepository.save(new Notification(doctor.getUserId(), "New Appointment",
                "New appointment booked by patient " + patient.getFullName() + " for " + saved.getAppointmentDate() + " at " + saved.getAppointmentTime() + ".",
                "APPOINTMENT"));

        return mapToAppointmentResponseDto(saved);
    }

    @Override
    public List<AppointmentResponseDto> getPatientAppointments(String userId) {
        Patient patient = patientRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient profile not found"));

        return appointmentRepository.findByPatientId(patient.getId()).stream()
                .map(this::mapToAppointmentResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public AppointmentResponseDto rescheduleAppointment(String userId, String appointmentId, String newDate, String newTime) {
        Patient patient = patientRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient profile not found"));

        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found"));

        if (!appointment.getPatientId().equals(patient.getId())) {
            throw new UnauthorizedException("You are not authorized to reschedule this appointment");
        }

        LocalDate date = LocalDate.parse(newDate);
        if (date.isBefore(LocalDate.now())) {
            throw new BadRequestException("New appointment date cannot be in the past");
        }

        List<Appointment> existing = appointmentRepository.findByDoctorIdAndAppointmentDateAndAppointmentTimeAndStatusNot(
                appointment.getDoctorId(), date, newTime, "CANCELLED");
        if (!existing.isEmpty() && !existing.get(0).getId().equals(appointmentId)) {
            throw new AppointmentConflictException("Doctor is already booked at " + newTime + " on " + date);
        }

        appointment.setAppointmentDate(date);
        appointment.setAppointmentTime(newTime);
        appointment.setStatus("RESCHEDULED");

        Appointment updated = appointmentRepository.save(appointment);

        notificationRepository.save(new Notification(userId, "Appointment Rescheduled",
                "Your appointment " + updated.getAppointmentCode() + " was rescheduled to " + date + " at " + newTime, "APPOINTMENT"));

        return mapToAppointmentResponseDto(updated);
    }

    @Override
    public AppointmentResponseDto cancelAppointment(String userId, String appointmentId) {
        Patient patient = patientRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient profile not found"));

        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found"));

        if (!appointment.getPatientId().equals(patient.getId())) {
            throw new UnauthorizedException("You are not authorized to cancel this appointment");
        }

        appointment.setStatus("CANCELLED");
        Appointment updated = appointmentRepository.save(appointment);

        notificationRepository.save(new Notification(userId, "Appointment Cancelled",
                "Your appointment " + updated.getAppointmentCode() + " has been cancelled.", "APPOINTMENT"));

        return mapToAppointmentResponseDto(updated);
    }

    @Override
    public List<MedicalRecordDto> getMedicalRecords(String userId) {
        Patient patient = patientRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient profile not found"));

        return medicalRecordRepository.findByPatientId(patient.getId()).stream()
                .map(this::mapToMedicalRecordDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<PrescriptionDto> getPrescriptions(String userId) {
        Patient patient = patientRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient profile not found"));

        return prescriptionRepository.findByPatientId(patient.getId()).stream()
                .map(this::mapToPrescriptionDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<Notification> getNotifications(String userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    @Override
    public PatientDashboardStatsDto getDashboardStats(String userId) {
        Patient patient = patientRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient profile not found"));

        List<Appointment> appointments = appointmentRepository.findByPatientId(patient.getId());
        long totalAppointments = appointments.size();
        long upcomingAppointments = appointments.stream()
                .filter(a -> ("CONFIRMED".equals(a.getStatus()) || "RESCHEDULED".equals(a.getStatus()))
                        && !a.getAppointmentDate().isBefore(LocalDate.now()))
                .count();

        long medicalRecordsCount = medicalRecordRepository.findByPatientId(patient.getId()).size();
        long prescriptionsCount = prescriptionRepository.findByPatientId(patient.getId()).size();
        long unreadNotifications = notificationRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .filter(n -> !n.isRead()).count();

        return new PatientDashboardStatsDto(totalAppointments, upcomingAppointments, medicalRecordsCount, prescriptionsCount, unreadNotifications);
    }

    private PatientProfileDto mapToProfileDto(Patient patient, User user) {
        PatientProfileDto dto = new PatientProfileDto();
        dto.setId(patient.getId());
        dto.setUserId(patient.getUserId());
        dto.setPatientCode(patient.getPatientCode());
        dto.setFullName(patient.getFullName());
        dto.setUsername(user.getUsername());
        dto.setEmail(patient.getEmail());
        dto.setPhone(patient.getPhone());
        dto.setDateOfBirth(patient.getDateOfBirth());
        dto.setGender(patient.getGender());
        dto.setAddress(patient.getAddress());
        dto.setBloodGroup(patient.getBloodGroup());
        dto.setEmergencyContact(patient.getEmergencyContact());
        return dto;
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

    private MedicalRecordDto mapToMedicalRecordDto(MedicalRecord record) {
        MedicalRecordDto dto = new MedicalRecordDto();
        dto.setId(record.getId());
        dto.setPatientId(record.getPatientId());
        dto.setDoctorId(record.getDoctorId());
        dto.setAppointmentId(record.getAppointmentId());
        dto.setDiagnosis(record.getDiagnosis());
        dto.setSymptoms(record.getSymptoms());
        dto.setTreatment(record.getTreatment());
        dto.setNotes(record.getNotes());
        dto.setRecordDate(record.getRecordDate());

        patientRepository.findById(record.getPatientId()).ifPresent(p -> dto.setPatientName(p.getFullName()));
        doctorRepository.findById(record.getDoctorId()).ifPresent(d -> dto.setDoctorName(d.getFullName()));

        return dto;
    }

    private PrescriptionDto mapToPrescriptionDto(Prescription p) {
        PrescriptionDto dto = new PrescriptionDto();
        dto.setId(p.getId());
        dto.setPatientId(p.getPatientId());
        dto.setDoctorId(p.getDoctorId());
        dto.setAppointmentId(p.getAppointmentId());
        dto.setMedicineName(p.getMedicineName());
        dto.setDosage(p.getDosage());
        dto.setFrequency(p.getFrequency());
        dto.setDuration(p.getDuration());
        dto.setInstructions(p.getInstructions());
        dto.setPrescribedDate(p.getPrescribedDate());

        patientRepository.findById(p.getPatientId()).ifPresent(pat -> dto.setPatientName(pat.getFullName()));
        doctorRepository.findById(p.getDoctorId()).ifPresent(doc -> dto.setDoctorName(doc.getFullName()));

        return dto;
    }
}

