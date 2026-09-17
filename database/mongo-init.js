// mongo-init.js - Patient Management System Database Initialization
db = db.getSiblingDB('patient_management');

// Create unique indexes
db.users.createIndex({ "username": 1 }, { unique: true });
db.users.createIndex({ "email": 1 }, { unique: true });

db.patients.createIndex({ "userId": 1 }, { unique: true });
db.patients.createIndex({ "patientCode": 1 }, { unique: true });

db.doctors.createIndex({ "userId": 1 }, { unique: true });
db.doctors.createIndex({ "doctorCode": 1 }, { unique: true });

db.departments.createIndex({ "departmentName": 1 }, { unique: true });

db.appointments.createIndex({ "appointmentCode": 1 }, { unique: true });
db.appointments.createIndex({ "doctorId": 1, "appointmentDate": 1, "appointmentTime": 1 });

print("Database patient_management initialized with indexes successfully.");

