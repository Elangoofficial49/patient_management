# CarePulse - Patient Management System

A complete, production-ready Patient Management System web application built with **Java 17/25**, **Spring Boot 3.3.5**, **Spring Data MongoDB**, **Spring Security + JWT**, **Jakarta Bean Validation**, and a responsive **Bootstrap 5 Healthcare Dashboard**.

---

## Features

### Patient Portal
- Patient registration & secure JWT login
- View & update patient profile details
- Search & filter doctors by department or specialization
- Real-time appointment booking with double-booking prevention
- View appointment history, reschedule, or cancel appointments
- View clinical medical records & digital prescriptions
- In-app notification center

### Doctor Portal
- Doctor login & clinical dashboard
- View today's schedule & upcoming appointments
- View patient medical history
- Create & update medical records (diagnosis, symptoms, treatment, notes)
- Create & update prescriptions (medicine, dosage, frequency, duration, instructions)
- Mark appointments as completed or cancelled
- Update doctor profile & consultation fee

### Admin Portal
- Hospital dashboard statistics (Total Patients, Doctors, Appointments, Pending, Completed, Departments)
- Manage doctors (Onboard new doctors, edit details, deactivate accounts)
- Manage departments (Add, edit, deactivate)
- Patient directory & search
- System-wide appointment management
- User registry & audit

---

## Technology Stack

- **Backend**: Spring Boot 3.3.5, Java 17+, Spring Web, Spring Data MongoDB, Spring Security, JJWT (0.12.6), Jakarta Validation, Springdoc OpenAPI (Swagger UI).
- **Database**: MongoDB with pure Java in-memory MongoDB server (`mongo-java-server`) for zero-configuration, instant execution out of the box.
- **Frontend**: HTML5, CSS3, JavaScript, Bootstrap 5.3, Bootstrap Icons, Fetch API with JWT Bearer token authentication.

---

## Getting Started & Running the Project

### Prerequisites
- JDK 17 or later
- Apache Maven 3.8+

### How to Run

1. Open a terminal in the `backend/` directory:
   ```bash
   cd backend
   mvn spring-boot:run
   ```

2. Access the application in your browser:
   - **Frontend Web Application**: `http://localhost:8080`
   - **Swagger API Documentation**: `http://localhost:8080/swagger-ui.html`

---

## Default Seeded Accounts

| Role | Username | Password | Email |
| :--- | :--- | :--- | :--- |
| **Admin** | `admin` | `Admin@123` | `admin@hospital.com` |
| **Doctor** | `drsarah` | `Doctor@123` | `doc.sarah@hospital.com` |
| **Patient** | `johndoe` | `Patient@123` | `john.doe@patient.com` |

---

## REST API Summary

- **Auth**: `POST /api/auth/register`, `POST /api/auth/login`, `GET /api/auth/me`
- **Patient**: `GET /api/patients/dashboard`, `GET /api/patients/profile`, `GET /api/patients/doctors`, `POST /api/patients/appointments`, `DELETE /api/patients/appointments/{id}`, `GET /api/patients/medical-records`, `GET /api/patients/prescriptions`, `GET /api/patients/notifications`
- **Doctor**: `GET /api/doctors/dashboard`, `GET /api/doctors/appointments/today`, `POST /api/doctors/medical-records`, `POST /api/doctors/prescriptions`, `PUT /api/doctors/appointments/{id}/status`
- **Admin**: `GET /api/admin/dashboard`, `GET /api/admin/doctors`, `POST /api/admin/doctors`, `GET /api/admin/departments`, `POST /api/admin/departments`, `GET /api/admin/appointments`, `GET /api/admin/users`

