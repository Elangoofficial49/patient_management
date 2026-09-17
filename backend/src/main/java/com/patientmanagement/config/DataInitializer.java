package com.patientmanagement.config;

import com.patientmanagement.entity.*;
import com.patientmanagement.repository.*;
import com.patientmanagement.util.CodeGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRepository userRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final DepartmentRepository departmentRepository;
    private final AppointmentRepository appointmentRepository;
    private final MedicalRecordRepository medicalRecordRepository;
    private final PrescriptionRepository prescriptionRepository;
    private final NotificationRepository notificationRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository, PatientRepository patientRepository,
                           DoctorRepository doctorRepository, DepartmentRepository departmentRepository,
                           AppointmentRepository appointmentRepository, MedicalRecordRepository medicalRecordRepository,
                           PrescriptionRepository prescriptionRepository, NotificationRepository notificationRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.patientRepository = patientRepository;
        this.doctorRepository = doctorRepository;
        this.departmentRepository = departmentRepository;
        this.appointmentRepository = appointmentRepository;
        this.medicalRecordRepository = medicalRecordRepository;
        this.prescriptionRepository = prescriptionRepository;
        this.notificationRepository = notificationRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        if (userRepository.count() == 0) {
            log.info("Seeding initial database records...");

            // 1. Seed Admin User
            User admin = new User("admin", "admin@hospital.com", passwordEncoder.encode("Admin@123"), "ADMIN");
            userRepository.save(admin);

            // 2. Seed Departments
            Department cardiology = departmentRepository.save(new Department("Cardiology", "Heart and vascular health management"));
            Department neurology = departmentRepository.save(new Department("Neurology", "Brain, spinal cord and nervous system care"));
            Department orthopedics = departmentRepository.save(new Department("Orthopedics", "Bone, joint and muscle treatment"));
            Department pediatrics = departmentRepository.save(new Department("Pediatrics", "Child healthcare and development"));
            Department dermatology = departmentRepository.save(new Department("Dermatology", "Skin, hair and nail treatments"));
            Department generalMed = departmentRepository.save(new Department("General Medicine", "Primary healthcare and general diagnosis"));

            // 3. Seed Doctors
            User docUser1 = userRepository.save(new User("drsarah", "doc.sarah@hospital.com", passwordEncoder.encode("Doctor@123"), "DOCTOR"));
            Doctor doctor1 = new Doctor();
            doctor1.setUserId(docUser1.getId());
            doctor1.setDoctorCode(CodeGenerator.generateDoctorCode());
            doctor1.setFullName("Dr. Sarah Jenkins");
            doctor1.setEmail("doc.sarah@hospital.com");
            doctor1.setPhone("+1-555-0192");
            doctor1.setSpecialization("Cardiology Specialist");
            doctor1.setDepartmentId(cardiology.getId());
            doctor1.setQualification("MD, FACC");
            doctor1.setExperience(12);
            doctor1.setConsultationFee(new BigDecimal("150.00"));
            doctor1.setAvailableDays(List.of("MONDAY", "WEDNESDAY", "FRIDAY"));
            doctor1.setAvailableFrom("09:00");
            doctor1.setAvailableTo("16:00");
            doctor1.setStatus("ACTIVE");
            Doctor savedDoc1 = doctorRepository.save(doctor1);

            User docUser2 = userRepository.save(new User("drrobert", "doc.robert@hospital.com", passwordEncoder.encode("Doctor@123"), "DOCTOR"));
            Doctor doctor2 = new Doctor();
            doctor2.setUserId(docUser2.getId());
            doctor2.setDoctorCode(CodeGenerator.generateDoctorCode());
            doctor2.setFullName("Dr. Robert Chen");
            doctor2.setEmail("doc.robert@hospital.com");
            doctor2.setPhone("+1-555-0183");
            doctor2.setSpecialization("Senior Neurologist");
            doctor2.setDepartmentId(neurology.getId());
            doctor2.setQualification("MD, PhD");
            doctor2.setExperience(15);
            doctor2.setConsultationFee(new BigDecimal("180.00"));
            doctor2.setAvailableDays(List.of("TUESDAY", "THURSDAY"));
            doctor2.setAvailableFrom("10:00");
            doctor2.setAvailableTo("17:00");
            doctor2.setStatus("ACTIVE");
            doctorRepository.save(doctor2);

            // 4. Seed Patient
            User patUser = userRepository.save(new User("johndoe", "john.doe@patient.com", passwordEncoder.encode("Patient@123"), "PATIENT"));
            Patient patient = new Patient();
            patient.setUserId(patUser.getId());
            patient.setPatientCode(CodeGenerator.generatePatientCode());
            patient.setFullName("John Doe");
            patient.setEmail("john.doe@patient.com");
            patient.setPhone("+1-555-0144");
            patient.setDateOfBirth(LocalDate.of(1990, 5, 14));
            patient.setGender("Male");
            patient.setAddress("742 Evergreen Terrace, Springfield");
            patient.setBloodGroup("O+");
            patient.setEmergencyContact("+1-555-0999");
            Patient savedPatient = patientRepository.save(patient);

            // 5. Seed Appointment
            Appointment apt = new Appointment();
            apt.setAppointmentCode(CodeGenerator.generateAppointmentCode());
            apt.setPatientId(savedPatient.getId());
            apt.setDoctorId(savedDoc1.getId());
            apt.setAppointmentDate(LocalDate.now());
            apt.setAppointmentTime("10:00 AM");
            apt.setReason("Routine cardiovascular checkup and ECG evaluation");
            apt.setStatus("COMPLETED");
            Appointment savedApt = appointmentRepository.save(apt);

            // 6. Seed Medical Record
            MedicalRecord record = new MedicalRecord();
            record.setPatientId(savedPatient.getId());
            record.setDoctorId(savedDoc1.getId());
            record.setAppointmentId(savedApt.getId());
            record.setDiagnosis("Mild hypertension");
            record.setSymptoms("Occasional headaches and elevated BP reading");
            record.setTreatment("Lifestyle modifications, reduced sodium diet, daily exercise");
            record.setNotes("Follow up in 4 weeks for blood pressure tracking.");
            record.setRecordDate(LocalDate.now());
            medicalRecordRepository.save(record);

            // 7. Seed Prescription
            Prescription prescription = new Prescription();
            prescription.setPatientId(savedPatient.getId());
            prescription.setDoctorId(savedDoc1.getId());
            prescription.setAppointmentId(savedApt.getId());
            prescription.setMedicineName("Amlodipine 5mg");
            prescription.setDosage("1 Tablet");
            prescription.setFrequency("Once daily (Morning)");
            prescription.setDuration("30 Days");
            prescription.setInstructions("Take with water after breakfast");
            prescription.setPrescribedDate(LocalDate.now());
            prescriptionRepository.save(prescription);

            // 8. Seed Welcome Notifications
            notificationRepository.save(new Notification(patUser.getId(), "Welcome to Hospital Portal",
                    "Your patient account has been created successfully. You can now book appointments and view records.", "SYSTEM"));

            log.info("Database seeding completed successfully.");
        }
    }
}
