document.addEventListener('DOMContentLoaded', () => {
    if (window.location.pathname.includes('/patient/')) {
        if (checkAuth('PATIENT')) {
            initPatientPortal();
        }
    }
});

function initPatientPortal() {
    const user = getUser();
    if (document.getElementById('patientNameSpan')) {
        document.getElementById('patientNameSpan').textContent = user.fullName || user.username;
    }

    loadPatientDashboard();
    loadDoctors();
    loadAppointments();
    loadMedicalRecords();
    loadPrescriptions();
    loadNotifications();
    loadProfile();
}

async function loadPatientDashboard() {
    try {
        const res = await apiFetch('/patients/dashboard');
        const stats = res.data;
        if (document.getElementById('statTotalApts')) document.getElementById('statTotalApts').textContent = stats.totalAppointments;
        if (document.getElementById('statUpcomingApts')) document.getElementById('statUpcomingApts').textContent = stats.upcomingAppointments;
        if (document.getElementById('statRecordsCount')) document.getElementById('statRecordsCount').textContent = stats.medicalRecordsCount;
        if (document.getElementById('statPrescriptionsCount')) document.getElementById('statPrescriptionsCount').textContent = stats.prescriptionsCount;
    } catch (e) {
        console.error(e);
    }
}

async function loadProfile() {
    try {
        const res = await apiFetch('/patients/profile');
        const p = res.data;
        if (document.getElementById('profileCode')) document.getElementById('profileCode').textContent = p.patientCode;
        if (document.getElementById('profileName')) document.getElementById('profileName').value = p.fullName || '';
        if (document.getElementById('profileEmail')) document.getElementById('profileEmail').value = p.email || '';
        if (document.getElementById('profilePhone')) document.getElementById('profilePhone').value = p.phone || '';
        if (document.getElementById('profileDob')) document.getElementById('profileDob').value = p.dateOfBirth || '';
        if (document.getElementById('profileGender')) document.getElementById('profileGender').value = p.gender || '';
        if (document.getElementById('profileAddress')) document.getElementById('profileAddress').value = p.address || '';
        if (document.getElementById('profileBlood')) document.getElementById('profileBlood').value = p.bloodGroup || '';
        if (document.getElementById('profileEmergency')) document.getElementById('profileEmergency').value = p.emergencyContact || '';
    } catch (e) {
        console.error(e);
    }
}

async function saveProfile(event) {
    event.preventDefault();
    const fullName = document.getElementById('profileName').value;
    const phone = document.getElementById('profilePhone').value;
    const dob = document.getElementById('profileDob').value;
    const gender = document.getElementById('profileGender').value;
    const address = document.getElementById('profileAddress').value;
    const bloodGroup = document.getElementById('profileBlood').value;
    const emergencyContact = document.getElementById('profileEmergency').value;

    try {
        await apiFetch('/patients/profile', {
            method: 'PUT',
            body: JSON.stringify({ fullName, phone, dateOfBirth: dob, gender, address, bloodGroup, emergencyContact })
        });
        showToast('Profile updated successfully!', 'success');
    } catch (e) {
        showToast(e.message, 'error');
    }
}

async function loadDoctors() {
    try {
        const deptFilter = document.getElementById('deptFilter') ? document.getElementById('deptFilter').value : '';
        const searchInput = document.getElementById('doctorSearch') ? document.getElementById('doctorSearch').value : '';
        
        let url = '/patients/doctors?';
        if (deptFilter) url += `departmentId=${deptFilter}&`;
        if (searchInput) url += `search=${encodeURIComponent(searchInput)}&`;

        const res = await apiFetch(url);
        const doctors = res.data;

        const container = document.getElementById('doctorsListContainer');
        if (!container) return;

        if (doctors.length === 0) {
            container.innerHTML = '<div class="col-12 text-center py-4 text-muted"><p>No active doctors found matching criteria.</p></div>';
            return;
        }

        container.innerHTML = doctors.map(d => `
            <div class="col-md-6 col-lg-4 mb-4">
                <div class="card h-100 shadow-sm border-0">
                    <div class="card-body">
                        <div class="d-flex align-items-center mb-3">
                            <div class="stat-icon bg-light-primary me-3">
                                <i class="bi bi-person-badge"></i>
                            </div>
                            <div>
                                <h5 class="card-title mb-0 fw-bold">${d.fullName}</h5>
                                <span class="badge bg-info text-dark">${d.departmentName || 'General'}</span>
                            </div>
                        </div>
                        <p class="mb-1 text-muted"><strong>Specialization:</strong> ${d.specialization}</p>
                        <p class="mb-1 text-muted"><strong>Qualification:</strong> ${d.qualification || 'N/A'}</p>
                        <p class="mb-1 text-muted"><strong>Experience:</strong> ${d.experience} Years</p>
                        <p class="mb-3 text-muted"><strong>Fee:</strong> $${d.consultationFee}</p>
                        <button class="btn btn-primary w-100" onclick="openBookingModal('${d.id}', '${d.fullName.replace(/'/g, "\\'")}', ${d.consultationFee})">
                            <i class="bi bi-calendar-plus me-1"></i> Book Appointment
                        </button>
                    </div>
                </div>
            </div>
        `).join('');
    } catch (e) {
        console.error(e);
    }
}

let selectedDoctorForBooking = null;

function openBookingModal(doctorId, doctorName, fee) {
    selectedDoctorForBooking = doctorId;
    document.getElementById('bookingDoctorName').textContent = doctorName;
    document.getElementById('bookingDoctorFee').textContent = '$' + fee;
    
    // Set min date to today
    const today = new Date().toISOString().split('T')[0];
    document.getElementById('bookingDate').min = today;
    document.getElementById('bookingDate').value = today;

    const modal = new bootstrap.Modal(document.getElementById('bookingModal'));
    modal.show();
}

async function submitBooking(event) {
    event.preventDefault();
    const date = document.getElementById('bookingDate').value;
    const time = document.getElementById('bookingTime').value;
    const reason = document.getElementById('bookingReason').value.trim();

    try {
        await apiFetch('/patients/appointments', {
            method: 'POST',
            body: JSON.stringify({
                doctorId: selectedDoctorForBooking,
                appointmentDate: date,
                appointmentTime: time,
                reason: reason
            })
        });

        showToast('Appointment booked successfully!', 'success');
        const modalEl = document.getElementById('bookingModal');
        const modal = bootstrap.Modal.getInstance(modalEl);
        if (modal) modal.hide();

        loadPatientDashboard();
        loadAppointments();
    } catch (e) {
        showToast(e.message, 'error');
    }
}

async function loadAppointments() {
    try {
        const res = await apiFetch('/patients/appointments');
        const apts = res.data;
        const tbody = document.getElementById('appointmentsTableBody');
        if (!tbody) return;

        if (apts.length === 0) {
            tbody.innerHTML = '<tr><td colspan="7" class="text-center py-4 text-muted">No appointments found.</td></tr>';
            return;
        }

        tbody.innerHTML = apts.map(a => `
            <tr>
                <td><strong>${a.appointmentCode}</strong></td>
                <td>${a.doctorName} <br><small class="text-muted">${a.departmentName || ''}</small></td>
                <td>${a.appointmentDate}</td>
                <td>${a.appointmentTime}</td>
                <td>${a.reason}</td>
                <td><span class="badge badge-status badge-${a.status.toLowerCase()}">${a.status}</span></td>
                <td>
                    ${(a.status === 'CONFIRMED' || a.status === 'PENDING' || a.status === 'RESCHEDULED') ? `
                        <button class="btn btn-sm btn-outline-danger me-1" onclick="cancelAppointment('${a.id}')"><i class="bi bi-x-circle"></i> Cancel</button>
                    ` : '<span class="text-muted">-</span>'}
                </td>
            </tr>
        `).join('');
    } catch (e) {
        console.error(e);
    }
}

async function cancelAppointment(id) {
    if (!confirm('Are you sure you want to cancel this appointment?')) return;
    try {
        await apiFetch(`/patients/appointments/${id}`, { method: 'DELETE' });
        showToast('Appointment cancelled successfully', 'success');
        loadAppointments();
        loadPatientDashboard();
    } catch (e) {
        showToast(e.message, 'error');
    }
}

async function loadMedicalRecords() {
    try {
        const res = await apiFetch('/patients/medical-records');
        const records = res.data;
        const container = document.getElementById('recordsContainer');
        if (!container) return;

        if (records.length === 0) {
            container.innerHTML = '<div class="alert alert-info text-center">No medical records found.</div>';
            return;
        }

        container.innerHTML = records.map(r => `
            <div class="card shadow-sm border-0 mb-3">
                <div class="card-header bg-white fw-bold d-flex justify-content-between align-items-center">
                    <span><i class="bi bi-file-earmark-medical me-2 text-primary"></i>Diagnosis: ${r.diagnosis}</span>
                    <span class="badge bg-secondary">${r.recordDate}</span>
                </div>
                <div class="card-body">
                    <p class="mb-1"><strong>Doctor:</strong> ${r.doctorName}</p>
                    <p class="mb-1"><strong>Symptoms:</strong> ${r.symptoms}</p>
                    <p class="mb-1"><strong>Treatment:</strong> ${r.treatment}</p>
                    <p class="mb-0 text-muted"><strong>Notes:</strong> ${r.notes || 'N/A'}</p>
                </div>
            </div>
        `).join('');
    } catch (e) {
        console.error(e);
    }
}

async function loadPrescriptions() {
    try {
        const res = await apiFetch('/patients/prescriptions');
        const prescriptions = res.data;
        const tbody = document.getElementById('prescriptionsTableBody');
        if (!tbody) return;

        if (prescriptions.length === 0) {
            tbody.innerHTML = '<tr><td colspan="7" class="text-center py-4 text-muted">No prescriptions found.</td></tr>';
            return;
        }

        tbody.innerHTML = prescriptions.map(p => `
            <tr>
                <td><strong>${p.medicineName}</strong></td>
                <td>${p.dosage}</td>
                <td>${p.frequency}</td>
                <td>${p.duration}</td>
                <td>${p.instructions || 'N/A'}</td>
                <td>${p.doctorName}</td>
                <td>${p.prescribedDate}</td>
            </tr>
        `).join('');
    } catch (e) {
        console.error(e);
    }
}

async function loadNotifications() {
    try {
        const res = await apiFetch('/patients/notifications');
        const notifications = res.data;
        const container = document.getElementById('notificationsContainer');
        if (!container) return;

        if (notifications.length === 0) {
            container.innerHTML = '<div class="text-center text-muted py-3">No notifications.</div>';
            return;
        }

        container.innerHTML = notifications.map(n => `
            <div class="alert alert-light border shadow-sm mb-2">
                <div class="d-flex justify-content-between align-items-center mb-1">
                    <h6 class="mb-0 fw-bold text-primary">${n.title}</h6>
                    <small class="text-muted">${new Date(n.createdAt).toLocaleDateString()}</small>
                </div>
                <p class="mb-0 text-dark small">${n.message}</p>
            </div>
        `).join('');
    } catch (e) {
        console.error(e);
    }
}

