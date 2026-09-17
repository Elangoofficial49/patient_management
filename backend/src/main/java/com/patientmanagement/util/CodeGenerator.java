package com.patientmanagement.util;

import java.util.UUID;

public class CodeGenerator {

    public static String generatePatientCode() {
        return "PAT-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }

    public static String generateDoctorCode() {
        return "DOC-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }

    public static String generateAppointmentCode() {
        return "APT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}

