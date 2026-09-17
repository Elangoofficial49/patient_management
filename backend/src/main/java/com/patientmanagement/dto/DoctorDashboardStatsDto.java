package com.patientmanagement.dto;

public class DoctorDashboardStatsDto {

    private long todayAppointments;
    private long upcomingAppointments;
    private long completedAppointments;
    private long totalPatients;

    public DoctorDashboardStatsDto() {}

    public DoctorDashboardStatsDto(long todayAppointments, long upcomingAppointments, long completedAppointments, long totalPatients) {
        this.todayAppointments = todayAppointments;
        this.upcomingAppointments = upcomingAppointments;
        this.completedAppointments = completedAppointments;
        this.totalPatients = totalPatients;
    }

    public long getTodayAppointments() {
        return todayAppointments;
    }

    public void setTodayAppointments(long todayAppointments) {
        this.todayAppointments = todayAppointments;
    }

    public long getUpcomingAppointments() {
        return upcomingAppointments;
    }

    public void setUpcomingAppointments(long upcomingAppointments) {
        this.upcomingAppointments = upcomingAppointments;
    }

    public long getCompletedAppointments() {
        return completedAppointments;
    }

    public void setCompletedAppointments(long completedAppointments) {
        this.completedAppointments = completedAppointments;
    }

    public long getTotalPatients() {
        return totalPatients;
    }

    public void setTotalPatients(long totalPatients) {
        this.totalPatients = totalPatients;
    }
}

