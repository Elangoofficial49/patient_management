document.addEventListener('DOMContentLoaded', () => {
    if (window.location.pathname.includes('/doctor/')) {
        if (checkAuth('DOCTOR')) {
            initDoctorPortal();
        }
    }
});

function initDoctorPortal() {
    const user = getUser();
    const displayName = user.fullName || user.username;
    if (document.getElementById('doctorNameSpan')) {
        document.getElementById('doctorNameSpan').textContent = displayName;
    }
    if (document.getElementById('docNameHeader')) {
        document.getElementById('docNameHeader').textContent = displayName;
    }
    if (document.getElementById('docAvatarPill')) {
        document.getElementById('docAvatarPill').textContent = displayName.charAt(0).toUpperCase();
    }

    loadDoctorDashboard();
    loadTodayAppointments();
    loadAllDoctorAppointments();
    loadDoctorProfile();
}

function switchToDocTab(targetSelector) {
    const link = document.querySelector(`.sidebar-nav a[data-bs-target="${targetSelector}"]`) || document.querySelector(`.sidebar-nav a[href="${targetSelector}"]`);
    if (link) {
        link.click();
    }
}

async function loadDoctorDashboard() {
    try {
        const res = await apiFetch('/doctors/dashboard');
        const stats = res.data;
        if (document.getElementById('docTodayApts')) document.getElementById('docTodayApts').textContent = stats.todayAppointments;
        if (document.getElementById('docUpcomingApts')) document.getElementById('docUpcomingApts').textContent = stats.upcomingAppointments;
        if (document.getElementById('docCompletedApts')) document.getElementById('docCompletedApts').textContent = stats.completedAppointments;
        if (document.getElementById('docTotalPatients')) document.getElementById('docTotalPatients').textContent = stats.totalPatients;
    } catch (e) {
        console.error(e);
    }
}

async function loadDoctorProfile() {
    try {
        const res = await apiFetch('/doctors/profile');
        const d = res.data;
        if (document.getElementById('docCode')) document.getElementById('docCode').textContent = d.doctorCode;
        if (document.getElementById('docFullName')) document.getElementById('docFullName').value = d.fullName || '';
        if (document.getElementById('docSpec')) document.getElementById('docSpec').value = d.specialization || '';
        if (document.getElementById('docQual')) document.getElementById('docQual').value = d.qualification || '';
        if (document.getElementById('docExp')) document.getElementById('docExp').value = d.experience || 0;
        if (document.getElementById('docPhone')) document.getElementById('docPhone').value = d.phone || '';
        if (document.getElementById('docFee')) document.getElementById('docFee').value = d.consultationFee || 0;
    } catch (e) {
        console.error(e);
    }
}

async function saveDoctorProfile(event) {
    event.preventDefault();
    const fullName = document.getElementById('docFullName').value;
    const specialization = document.getElementById('docSpec').value;
    const qualification = document.getElementById('docQual').value;
    const experience = parseInt(document.getElementById('docExp').value);
    const phone = document.getElementById('docPhone').value;
    const consultationFee = parseFloat(document.getElementById('docFee').value);

    try {
        await apiFetch('/doctors/profile', {
            method: 'PUT',
            body: JSON.stringify({ fullName, specialization, qualification, experience, phone, consultationFee })
        });
        showToast('Doctor profile updated successfully!', 'success');
    } catch (e) {
        showToast(e.message, 'error');
    }
}

async function loadTodayAppointments() {
    try {
        const res = await apiFetch('/doctors/appointments/today');
        const apts = res.data;
        const tbody = document.getElementById('todayAptsTableBody');
        if (!tbody) return;

        if (apts.length === 0) {
            tbody.innerHTML = '<tr><td colspan="6" class="text-center py-4 text-muted">No appointments scheduled for today.</td></tr>';
            return;
        }

        tbody.innerHTML = apts.map(a => `
            <tr>
                <td><strong>${a.appointmentCode}</strong></td>
                <td>${a.patientName} <br><small class="text-muted">${a.patientPhone || ''}</small></td>
                <td>${a.appointmentTime}</td>
                <td>${a.reason}</td>
                <td><span class="badge badge-status badge-${a.status.toLowerCase()}">${a.status}</span></td>
                <td>
                    <button class="btn btn-sm btn-success me-1" onclick="openAddRecordModal('${a.patientId}', '${a.id}', '${a.patientName.replace(/'/g, "\\'")}')">
                        <i class="bi bi-file-medical"></i> Record
                    </button>
                    <button class="btn btn-sm btn-primary me-1" onclick="openAddPrescriptionModal('${a.patientId}', '${a.id}', '${a.patientName.replace(/'/g, "\\'")}')">
                        <i class="bi bi-capsule"></i> Rx
                    </button>
                    <button class="btn btn-sm btn-outline-dark" onclick="updateStatus('${a.id}', 'COMPLETED')">
                        <i class="bi bi-check-lg"></i> Done
                    </button>
                </td>
            </tr>
        `).join('');
    } catch (e) {
        console.error(e);
    }
}

async function loadAllDoctorAppointments() {
    try {
        const res = await apiFetch('/doctors/appointments');
        const apts = res.data;
        const tbody = document.getElementById('allDoctorAptsTableBody');
        if (!tbody) return;

        if (apts.length === 0) {
            tbody.innerHTML = '<tr><td colspan="7" class="text-center py-4 text-muted">No appointments found.</td></tr>';
            return;
        }

        tbody.innerHTML = apts.map(a => `
            <tr>
                <td><strong>${a.appointmentCode}</strong></td>
                <td>${a.patientName}</td>
                <td>${a.appointmentDate}</td>
                <td>${a.appointmentTime}</td>
                <td>${a.reason}</td>
                <td><span class="badge badge-status badge-${a.status.toLowerCase()}">${a.status}</span></td>
                <td>
                    <div class="dropdown">
                        <button class="btn btn-sm btn-outline-secondary dropdown-toggle" type="button" data-bs-toggle="dropdown">Action</button>
                        <ul class="dropdown-menu">
                            <li><a class="dropdown-link dropdown-item" href="#" onclick="updateStatus('${a.id}', 'COMPLETED')">Mark Completed</a></li>
                            <li><a class="dropdown-link dropdown-item text-danger" href="#" onclick="updateStatus('${a.id}', 'CANCELLED')">Cancel Appointment</a></li>
                        </ul>
                    </div>
                </td>
            </tr>
        `).join('');
    } catch (e) {
        console.error(e);
    }
}

async function updateStatus(appointmentId, status) {
    try {
        await apiFetch(`/doctors/appointments/${appointmentId}/status`, {
            method: 'PUT',
            body: JSON.stringify({ status })
        });
        showToast(`Appointment status updated to ${status}`, 'success');
        loadDoctorDashboard();
        loadTodayAppointments();
        loadAllDoctorAppointments();
    } catch (e) {
        showToast(e.message, 'error');
    }
}

let activeRecordPatientId = null;
let activeRecordAptId = null;

function openAddRecordModal(patientId, appointmentId, patientName) {
    activeRecordPatientId = patientId;
    activeRecordAptId = appointmentId;
    document.getElementById('recordPatientName').textContent = patientName;
    const modal = new bootstrap.Modal(document.getElementById('recordModal'));
    modal.show();
}

async function submitRecord(event) {
    event.preventDefault();
    const diagnosis = document.getElementById('recordDiagnosis').value;
    const symptoms = document.getElementById('recordSymptoms').value;
    const treatment = document.getElementById('recordTreatment').value;
    const notes = document.getElementById('recordNotes').value;

    try {
        await apiFetch('/doctors/medical-records', {
            method: 'POST',
            body: JSON.stringify({
                patientId: activeRecordPatientId,
                appointmentId: activeRecordAptId,
                diagnosis, symptoms, treatment, notes
            })
        });

        showToast('Medical record added successfully!', 'success');
        const modalEl = document.getElementById('recordModal');
        const modal = bootstrap.Modal.getInstance(modalEl);
        if (modal) modal.hide();
    } catch (e) {
        showToast(e.message, 'error');
    }
}

let activePrescriptionPatientId = null;
let activePrescriptionAptId = null;

function openAddPrescriptionModal(patientId, appointmentId, patientName) {
    activePrescriptionPatientId = patientId;
    activePrescriptionAptId = appointmentId;
    document.getElementById('rxPatientName').textContent = patientName;
    const modal = new bootstrap.Modal(document.getElementById('rxModal'));
    modal.show();
}

async function submitPrescription(event) {
    event.preventDefault();
    const medicineName = document.getElementById('rxMedicine').value;
    const dosage = document.getElementById('rxDosage').value;
    const frequency = document.getElementById('rxFrequency').value;
    const duration = document.getElementById('rxDuration').value;
    const instructions = document.getElementById('rxInstructions').value;

    try {
        await apiFetch('/doctors/prescriptions', {
            method: 'POST',
            body: JSON.stringify({
                patientId: activePrescriptionPatientId,
                appointmentId: activePrescriptionAptId,
                medicineName, dosage, frequency, duration, instructions
            })
        });

        showToast('Prescription issued successfully!', 'success');
        const modalEl = document.getElementById('rxModal');
        const modal = bootstrap.Modal.getInstance(modalEl);
        if (modal) modal.hide();
    } catch (e) {
        showToast(e.message, 'error');
    }
}

