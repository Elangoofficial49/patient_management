package com.patientmanagement.service.impl;

import com.patientmanagement.dto.*;
import com.patientmanagement.entity.*;
import com.patientmanagement.exception.ResourceNotFoundException;
import com.patientmanagement.exception.UnauthorizedException;
import com.patientmanagement.repository.*;
import com.patientmanagement.service.DoctorService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class DoctorServiceImpl implements DoctorService {

    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;
    private final DepartmentRepository departmentRepository;
    private final AppointmentRepository appointmentRepository;
    private final MedicalRecordRepository medicalRecordRepository;
    private final PrescriptionRepository prescriptionRepository;
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    public DoctorServiceImpl(DoctorRepository doctorRepository, PatientRepository patientRepository,
                             DepartmentRepository departmentRepository, AppointmentRepository appointmentRepository,
                             MedicalRecordRepository medicalRecordRepository, PrescriptionRepository prescriptionRepository,
                             NotificationRepository notificationRepository, UserRepository userRepository) {
        this.doctorRepository = doctorRepository;
        this.patientRepository = patientRepository;
        this.departmentRepository = departmentRepository;
        this.appointmentRepository = appointmentRepository;
        this.medicalRecordRepository = medicalRecordRepository;
        this.prescriptionRepository = prescriptionRepository;
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
    }

    @Override
    public DoctorDto getDoctorProfile(String userId) {
        Doctor doctor = doctorRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor profile not found for user: " + userId));
        return mapToDoctorDto(doctor);
    }

    @Override
    public DoctorDto updateDoctorProfile(String userId, DoctorDto doctorDto) {
        Doctor doctor = doctorRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor profile not found"));

        doctor.setFullName(doctorDto.getFullName());
        doctor.setSpecialization(doctorDto.getSpecialization());
        doctor.setQualification(doctorDto.getQualification());
        doctor.setExperience(doctorDto.getExperience());
        doctor.setPhone(doctorDto.getPhone());
        doctor.setConsultationFee(doctorDto.getConsultationFee());
        if (doctorDto.getAvailableDays() != null) doctor.setAvailableDays(doctorDto.getAvailableDays());
        if (doctorDto.getAvailableFrom() != null) doctor.setAvailableFrom(doctorDto.getAvailableFrom());
        if (doctorDto.getAvailableTo() != null) doctor.setAvailableTo(doctorDto.getAvailableTo());

        Doctor saved = doctorRepository.save(doctor);
        return mapToDoctorDto(saved);
    }

    @Override
    public List<AppointmentResponseDto> getDoctorAppointments(String userId) {
        Doctor doctor = doctorRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor profile not found"));

        return appointmentRepository.findByDoctorId(doctor.getId()).stream()
                .map(this::mapToAppointmentResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<AppointmentResponseDto> getTodayAppointments(String userId) {
        Doctor doctor = doctorRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor profile not found"));

        return appointmentRepository.findByDoctorIdAndAppointmentDate(doctor.getId(), LocalDate.now()).stream()
                .map(this::mapToAppointmentResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public PatientProfileDto getPatientDetails(String userId, String patientId) {
        Doctor doctor = doctorRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor profile not found"));

        // Verify doctor has an appointment with this patient
        List<Appointment> doctorAppointments = appointmentRepository.findByDoctorId(doctor.getId());
        boolean hasAccess = doctorAppointments.stream().anyMatch(a -> a.getPatientId().equals(patientId));
        if (!hasAccess) {
            throw new UnauthorizedException("You can only view patient details for patients assigned to your appointments");
        }

        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found"));

        User user = userRepository.findById(patient.getUserId()).orElse(null);

        PatientProfileDto dto = new PatientProfileDto();
        dto.setId(patient.getId());
        dto.setUserId(patient.getUserId());
        dto.setPatientCode(patient.getPatientCode());
        dto.setFullName(patient.getFullName());
        dto.setUsername(user != null ? user.getUsername() : "");
        dto.setEmail(patient.getEmail());
        dto.setPhone(patient.getPhone());
        dto.setDateOfBirth(patient.getDateOfBirth());
        dto.setGender(patient.getGender());
        dto.setAddress(patient.getAddress());
        dto.setBloodGroup(patient.getBloodGroup());
        dto.setEmergencyContact(patient.getEmergencyContact());
        return dto;
    }

    @Override
    public List<MedicalRecordDto> getPatientMedicalRecords(String userId, String patientId) {
        Doctor doctor = doctorRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor profile not found"));

        return medicalRecordRepository.findByPatientId(patientId).stream()
                .map(this::mapToMedicalRecordDto)
                .collect(Collectors.toList());
    }

    @Override
    public MedicalRecordDto createMedicalRecord(String userId, MedicalRecordDto dto) {
        Doctor doctor = doctorRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor profile not found"));

        MedicalRecord record = new MedicalRecord();
        record.setPatientId(dto.getPatientId());
        record.setDoctorId(doctor.getId());
        record.setAppointmentId(dto.getAppointmentId());
        record.setDiagnosis(dto.getDiagnosis());
        record.setSymptoms(dto.getSymptoms());
        record.setTreatment(dto.getTreatment());
        record.setNotes(dto.getNotes());
        record.setRecordDate(LocalDate.now());

        MedicalRecord saved = medicalRecordRepository.save(record);

        // Notify Patient
        patientRepository.findById(dto.getPatientId()).ifPresent(p -> {
            notificationRepository.save(new Notification(p.getUserId(), "New Medical Record",
                    "Dr. " + doctor.getFullName() + " added a medical record for your consultation.", "MEDICAL"));
        });

        return mapToMedicalRecordDto(saved);
    }

    @Override
    public MedicalRecordDto updateMedicalRecord(String userId, String recordId, MedicalRecordDto dto) {
        Doctor doctor = doctorRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor profile not found"));

        MedicalRecord record = medicalRecordRepository.findById(recordId)
                .orElseThrow(() -> new ResourceNotFoundException("Medical record not found"));

        if (!record.getDoctorId().equals(doctor.getId())) {
            throw new UnauthorizedException("You are not authorized to update medical records created by another doctor");
        }

        record.setDiagnosis(dto.getDiagnosis());
        record.setSymptoms(dto.getSymptoms());
        record.setTreatment(dto.getTreatment());
        record.setNotes(dto.getNotes());

        MedicalRecord updated = medicalRecordRepository.save(record);
        return mapToMedicalRecordDto(updated);
    }

    @Override
    public PrescriptionDto createPrescription(String userId, PrescriptionDto dto) {
        Doctor doctor = doctorRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor profile not found"));

        Prescription prescription = new Prescription();
        prescription.setPatientId(dto.getPatientId());
        prescription.setDoctorId(doctor.getId());
        prescription.setAppointmentId(dto.getAppointmentId());
        prescription.setMedicineName(dto.getMedicineName());
        prescription.setDosage(dto.getDosage());
        prescription.setFrequency(dto.getFrequency());
        prescription.setDuration(dto.getDuration());
        prescription.setInstructions(dto.getInstructions());
        prescription.setPrescribedDate(LocalDate.now());

        Prescription saved = prescriptionRepository.save(prescription);

        patientRepository.findById(dto.getPatientId()).ifPresent(p -> {
            notificationRepository.save(new Notification(p.getUserId(), "New Prescription Issued",
                    "Dr. " + doctor.getFullName() + " prescribed: " + dto.getMedicineName() + " (" + dto.getDosage() + ")", "MEDICAL"));
        });

        return mapToPrescriptionDto(saved);
    }

    @Override
    public PrescriptionDto updatePrescription(String userId, String prescriptionId, PrescriptionDto dto) {
        Doctor doctor = doctorRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor profile not found"));

        Prescription prescription = prescriptionRepository.findById(prescriptionId)
                .orElseThrow(() -> new ResourceNotFoundException("Prescription not found"));

        if (!prescription.getDoctorId().equals(doctor.getId())) {
            throw new UnauthorizedException("You are not authorized to update prescriptions created by another doctor");
        }

        prescription.setMedicineName(dto.getMedicineName());
        prescription.setDosage(dto.getDosage());
        prescription.setFrequency(dto.getFrequency());
        prescription.setDuration(dto.getDuration());
        prescription.setInstructions(dto.getInstructions());

        Prescription updated = prescriptionRepository.save(prescription);
        return mapToPrescriptionDto(updated);
    }

    @Override
    public AppointmentResponseDto updateAppointmentStatus(String userId, String appointmentId, String status) {
        Doctor doctor = doctorRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor profile not found"));

        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found"));

        if (!appointment.getDoctorId().equals(doctor.getId())) {
            throw new UnauthorizedException("You can only update status for your assigned appointments");
        }

        appointment.setStatus(status);
        Appointment updated = appointmentRepository.save(appointment);

        patientRepository.findById(appointment.getPatientId()).ifPresent(p -> {
            notificationRepository.save(new Notification(p.getUserId(), "Appointment Status Updated",
                    "Your appointment " + updated.getAppointmentCode() + " status changed to: " + status, "APPOINTMENT"));
        });

        return mapToAppointmentResponseDto(updated);
    }

    @Override
    public DoctorDashboardStatsDto getDashboardStats(String userId) {
        Doctor doctor = doctorRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor profile not found"));

        List<Appointment> allDoctorAppointments = appointmentRepository.findByDoctorId(doctor.getId());
        long today = allDoctorAppointments.stream()
                .filter(a -> LocalDate.now().equals(a.getAppointmentDate()))
                .count();

        long upcoming = allDoctorAppointments.stream()
                .filter(a -> ("CONFIRMED".equals(a.getStatus()) || "RESCHEDULED".equals(a.getStatus()))
                        && a.getAppointmentDate().isAfter(LocalDate.now()))
                .count();

        long completed = allDoctorAppointments.stream()
                .filter(a -> "COMPLETED".equals(a.getStatus()))
                .count();

        Set<String> uniquePatientIds = allDoctorAppointments.stream()
                .map(Appointment::getPatientId)
                .collect(Collectors.toSet());

        return new DoctorDashboardStatsDto(today, upcoming, completed, uniquePatientIds.size());
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

