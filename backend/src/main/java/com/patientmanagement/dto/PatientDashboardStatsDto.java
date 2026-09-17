package com.patientmanagement.dto;

public class PatientDashboardStatsDto {

    private long totalAppointments;
    private long upcomingAppointments;
    private long medicalRecordsCount;
    private long prescriptionsCount;
    private long unreadNotificationsCount;

    public PatientDashboardStatsDto() {}

    public PatientDashboardStatsDto(long totalAppointments, long upcomingAppointments, long medicalRecordsCount, long prescriptionsCount, long unreadNotificationsCount) {
        this.totalAppointments = totalAppointments;
        this.upcomingAppointments = upcomingAppointments;
        this.medicalRecordsCount = medicalRecordsCount;
        this.prescriptionsCount = prescriptionsCount;
        this.unreadNotificationsCount = unreadNotificationsCount;
    }

    public long getTotalAppointments() {
        return totalAppointments;
    }

    public void setTotalAppointments(long totalAppointments) {
        this.totalAppointments = totalAppointments;
    }

    public long getUpcomingAppointments() {
        return upcomingAppointments;
    }

    public void setUpcomingAppointments(long upcomingAppointments) {
        this.upcomingAppointments = upcomingAppointments;
    }

    public long getMedicalRecordsCount() {
        return medicalRecordsCount;
    }

    public void setMedicalRecordsCount(long medicalRecordsCount) {
        this.medicalRecordsCount = medicalRecordsCount;
    }

    public long getPrescriptionsCount() {
        return prescriptionsCount;
    }

    public void setPrescriptionsCount(long prescriptionsCount) {
        this.prescriptionsCount = prescriptionsCount;
    }

    public long getUnreadNotificationsCount() {
        return unreadNotificationsCount;
    }

    public void setUnreadNotificationsCount(long unreadNotificationsCount) {
        this.unreadNotificationsCount = unreadNotificationsCount;
    }
}

