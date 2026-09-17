document.addEventListener('DOMContentLoaded', () => {
    if (window.location.pathname.includes('/admin/')) {
        if (checkAuth('ADMIN')) {
            initAdminPortal();
        }
    }
});

function initAdminPortal() {
    const user = getUser();
    if (document.getElementById('adminNameSpan')) {
        document.getElementById('adminNameSpan').textContent = user.fullName || user.username;
    }

    loadAdminDashboard();
    loadAdminDoctors();
    loadAdminDepartments();
    loadAdminPatients();
    loadAdminAppointments();
    loadAdminUsers();
}

async function loadAdminDashboard() {
    try {
        const res = await apiFetch('/admin/dashboard');
        const s = res.data;
        if (document.getElementById('admPatients')) document.getElementById('admPatients').textContent = s.totalPatients;
        if (document.getElementById('admDoctors')) document.getElementById('admDoctors').textContent = s.totalDoctors;
        if (document.getElementById('admApts')) document.getElementById('admApts').textContent = s.totalAppointments;
        if (document.getElementById('admPendingApts')) document.getElementById('admPendingApts').textContent = s.pendingAppointments;
        if (document.getElementById('admCompletedApts')) document.getElementById('admCompletedApts').textContent = s.completedAppointments;
        if (document.getElementById('admDepts')) document.getElementById('admDepts').textContent = s.totalDepartments;
    } catch (e) {
        console.error(e);
    }
}

async function loadAdminDoctors() {
    try {
        const res = await apiFetch('/admin/doctors');
        const doctors = res.data;
        const tbody = document.getElementById('adminDoctorsTableBody');
        if (!tbody) return;

        if (doctors.length === 0) {
            tbody.innerHTML = '<tr><td colspan="7" class="text-center py-4 text-muted">No doctors found.</td></tr>';
            return;
        }

        tbody.innerHTML = doctors.map(d => `
            <tr>
                <td><strong>${d.doctorCode}</strong></td>
                <td>${d.fullName}</td>
                <td>${d.specialization}</td>
                <td>${d.departmentName || 'N/A'}</td>
                <td>${d.phone}</td>
                <td>$${d.consultationFee}</td>
                <td><span class="badge ${d.status === 'ACTIVE' ? 'bg-success' : 'bg-danger'}">${d.status}</span></td>
                <td>
                    <button class="btn btn-sm btn-outline-danger" onclick="deleteDoctor('${d.id}')">
                        <i class="bi bi-person-x"></i> Deactivate
                    </button>
                </td>
            </tr>
        `).join('');

        // Populate dropdown in doctor add modal
        const deptSelect = document.getElementById('newDocDept');
        if (deptSelect && deptSelect.children.length <= 1) {
            const deptsRes = await apiFetch('/admin/departments');
            deptSelect.innerHTML = deptsRes.data.map(dept => `<option value="${dept.id}">${dept.departmentName}</option>`).join('');
        }
    } catch (e) {
        console.error(e);
    }
}

function openAddDoctorModal() {
    const modal = new bootstrap.Modal(document.getElementById('addDoctorModal'));
    modal.show();
}

async function submitAddDoctor(event) {
    event.preventDefault();
    const username = document.getElementById('newDocUsername').value;
    const email = document.getElementById('newDocEmail').value;
    const password = document.getElementById('newDocPassword').value;
    const fullName = document.getElementById('newDocName').value;
    const phone = document.getElementById('newDocPhone').value;
    const specialization = document.getElementById('newDocSpec').value;
    const departmentId = document.getElementById('newDocDept').value;
    const qualification = document.getElementById('newDocQual').value;
    const experience = parseInt(document.getElementById('newDocExp').value);
    const consultationFee = parseFloat(document.getElementById('newDocFee').value);

    try {
        await apiFetch('/admin/doctors', {
            method: 'POST',
            body: JSON.stringify({
                username, email, password, fullName, phone, specialization,
                departmentId, qualification, experience, consultationFee
            })
        });

        showToast('Doctor added successfully!', 'success');
        const modalEl = document.getElementById('addDoctorModal');
        const modal = bootstrap.Modal.getInstance(modalEl);
        if (modal) modal.hide();

        loadAdminDoctors();
        loadAdminDashboard();
    } catch (e) {
        showToast(e.message, 'error');
    }
}

async function deleteDoctor(id) {
    if (!confirm('Are you sure you want to deactivate this doctor?')) return;
    try {
        await apiFetch(`/admin/doctors/${id}`, { method: 'DELETE' });
        showToast('Doctor deactivated successfully', 'success');
        loadAdminDoctors();
        loadAdminDashboard();
    } catch (e) {
        showToast(e.message, 'error');
    }
}

async function loadAdminDepartments() {
    try {
        const res = await apiFetch('/admin/departments');
        const depts = res.data;
        const tbody = document.getElementById('adminDeptsTableBody');
        if (!tbody) return;

        if (depts.length === 0) {
            tbody.innerHTML = '<tr><td colspan="4" class="text-center py-4 text-muted">No departments created.</td></tr>';
            return;
        }

        tbody.innerHTML = depts.map(d => `
            <tr>
                <td><strong>${d.departmentName}</strong></td>
                <td>${d.description}</td>
                <td><span class="badge ${d.status === 'ACTIVE' ? 'bg-success' : 'bg-secondary'}">${d.status}</span></td>
                <td>
                    <button class="btn btn-sm btn-outline-danger" onclick="deleteDept('${d.id}')">
                        <i class="bi bi-trash"></i> Deactivate
                    </button>
                </td>
            </tr>
        `).join('');
    } catch (e) {
        console.error(e);
    }
}

function openAddDeptModal() {
    const modal = new bootstrap.Modal(document.getElementById('addDeptModal'));
    modal.show();
}

async function submitAddDept(event) {
    event.preventDefault();
    const name = document.getElementById('deptNameInput').value;
    const desc = document.getElementById('deptDescInput').value;

    try {
        await apiFetch('/admin/departments', {
            method: 'POST',
            body: JSON.stringify({ departmentName: name, description: desc, status: 'ACTIVE' })
        });

        showToast('Department created successfully!', 'success');
        const modalEl = document.getElementById('addDeptModal');
        const modal = bootstrap.Modal.getInstance(modalEl);
        if (modal) modal.hide();

        loadAdminDepartments();
        loadAdminDashboard();
    } catch (e) {
        showToast(e.message, 'error');
    }
}

async function deleteDept(id) {
    if (!confirm('Deactivate department?')) return;
    try {
        await apiFetch(`/admin/departments/${id}`, { method: 'DELETE' });
        showToast('Department deactivated', 'success');
        loadAdminDepartments();
        loadAdminDashboard();
    } catch (e) {
        showToast(e.message, 'error');
    }
}

async function loadAdminPatients() {
    try {
        const res = await apiFetch('/admin/patients');
        const patients = res.data;
        const tbody = document.getElementById('adminPatientsTableBody');
        if (!tbody) return;

        if (patients.length === 0) {
            tbody.innerHTML = '<tr><td colspan="6" class="text-center py-4 text-muted">No patients found.</td></tr>';
            return;
        }

        tbody.innerHTML = patients.map(p => `
            <tr>
                <td><strong>${p.patientCode}</strong></td>
                <td>${p.fullName} <br><small class="text-muted">Username: ${p.username || 'N/A'}</small></td>
                <td>${p.email}</td>
                <td>${p.phone}</td>
                <td>${p.bloodGroup || 'N/A'}</td>
                <td>${p.emergencyContact || 'N/A'}</td>
            </tr>
        `).join('');
    } catch (e) {
        console.error(e);
    }
}

async function loadAdminAppointments() {
    try {
        const res = await apiFetch('/admin/appointments');
        const apts = res.data;
        const tbody = document.getElementById('adminAptsTableBody');
        if (!tbody) return;

        if (apts.length === 0) {
            tbody.innerHTML = '<tr><td colspan="7" class="text-center py-4 text-muted">No appointments found.</td></tr>';
            return;
        }

        tbody.innerHTML = apts.map(a => `
            <tr>
                <td><strong>${a.appointmentCode}</strong></td>
                <td>${a.patientName}</td>
                <td>${a.doctorName}</td>
                <td>${a.appointmentDate} ${a.appointmentTime}</td>
                <td>${a.reason}</td>
                <td><span class="badge badge-status badge-${a.status.toLowerCase()}">${a.status}</span></td>
                <td>
                    <select class="form-select form-select-sm" onchange="updateAdminAptStatus('${a.id}', this.value)">
                        <option value="PENDING" ${a.status === 'PENDING' ? 'selected' : ''}>PENDING</option>
                        <option value="CONFIRMED" ${a.status === 'CONFIRMED' ? 'selected' : ''}>CONFIRMED</option>
                        <option value="COMPLETED" ${a.status === 'COMPLETED' ? 'selected' : ''}>COMPLETED</option>
                        <option value="CANCELLED" ${a.status === 'CANCELLED' ? 'selected' : ''}>CANCELLED</option>
                        <option value="RESCHEDULED" ${a.status === 'RESCHEDULED' ? 'selected' : ''}>RESCHEDULED</option>
                    </select>
                </td>
            </tr>
        `).join('');
    } catch (e) {
        console.error(e);
    }
}

async function updateAdminAptStatus(id, status) {
    try {
        await apiFetch(`/admin/appointments/${id}/status`, {
            method: 'PUT',
            body: JSON.stringify({ status })
        });
        showToast('Appointment status updated', 'success');
        loadAdminAppointments();
        loadAdminDashboard();
    } catch (e) {
        showToast(e.message, 'error');
    }
}

async function loadAdminUsers() {
    try {
        const res = await apiFetch('/admin/users');
        const users = res.data;
        const tbody = document.getElementById('adminUsersTableBody');
        if (!tbody) return;

        tbody.innerHTML = users.map(u => `
            <tr>
                <td>${u.id}</td>
                <td><strong>${u.username}</strong></td>
                <td>${u.email}</td>
                <td><span class="badge bg-dark">${u.role}</span></td>
                <td><span class="badge ${u.enabled ? 'bg-success' : 'bg-danger'}">${u.enabled ? 'Enabled' : 'Disabled'}</span></td>
            </tr>
        `).join('');
    } catch (e) {
        console.error(e);
    }
}

