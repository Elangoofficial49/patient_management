async function handleLogin(event) {
    event.preventDefault();
    const usernameInput = document.getElementById('loginUsername').value.trim();
    const passwordInput = document.getElementById('loginPassword').value;

    const btn = document.getElementById('btnLogin');
    const originalText = btn.innerHTML;
    btn.disabled = true;
    btn.innerHTML = '<span class="spinner-border spinner-border-sm me-2"></span>Logging in...';

    try {
        const response = await apiFetch('/auth/login', {
            method: 'POST',
            body: JSON.stringify({ username: usernameInput, password: passwordInput })
        });

        const authData = response.data;
        setToken(authData.accessToken);
        setUser(authData);

        showToast('Login successful! Redirecting...', 'success');

        setTimeout(() => {
            if (authData.role === 'PATIENT') {
                window.location.href = '/patient/dashboard.html';
            } else if (authData.role === 'DOCTOR') {
                window.location.href = '/doctor/dashboard.html';
            } else if (authData.role === 'ADMIN') {
                window.location.href = '/admin/dashboard.html';
            } else {
                window.location.href = '/index.html';
            }
        }, 1000);

    } catch (error) {
        showToast(error.message || 'Invalid username or password', 'error');
        btn.disabled = false;
        btn.innerHTML = originalText;
    }
}

async function handleRegister(event) {
    event.preventDefault();
    
    const username = document.getElementById('regUsername').value.trim();
    const email = document.getElementById('regEmail').value.trim();
    const password = document.getElementById('regPassword').value;
    const fullName = document.getElementById('regFullName').value.trim();
    const phone = document.getElementById('regPhone').value.trim();
    const dob = document.getElementById('regDob').value;
    const gender = document.getElementById('regGender').value;
    const address = document.getElementById('regAddress').value.trim();
    const bloodGroup = document.getElementById('regBloodGroup').value;
    const emergencyContact = document.getElementById('regEmergencyContact').value.trim();

    const btn = document.getElementById('btnRegister');
    const originalText = btn.innerHTML;
    btn.disabled = true;
    btn.innerHTML = '<span class="spinner-border spinner-border-sm me-2"></span>Registering...';

    try {
        const response = await apiFetch('/auth/register', {
            method: 'POST',
            body: JSON.stringify({
                username, email, password, fullName, phone,
                dateOfBirth: dob || null, gender, address, bloodGroup, emergencyContact
            })
        });

        const authData = response.data;
        setToken(authData.accessToken);
        setUser(authData);

        showToast('Registration successful! Redirecting to dashboard...', 'success');

        setTimeout(() => {
            window.location.href = '/patient/dashboard.html';
        }, 1200);

    } catch (error) {
        showToast(error.message || 'Registration failed', 'error');
        btn.disabled = false;
        btn.innerHTML = originalText;
    }
}

function checkAuth(requiredRole = null) {
    const user = getUser();
    const token = getToken();

    if (!token || !user) {
        window.location.href = '/login.html';
        return false;
    }

    if (requiredRole && user.role !== requiredRole && user.role !== 'ADMIN') {
        showToast('Unauthorized access to page', 'error');
        if (user.role === 'PATIENT') window.location.href = '/patient/dashboard.html';
        else if (user.role === 'DOCTOR') window.location.href = '/doctor/dashboard.html';
        else window.location.href = '/index.html';
        return false;
    }

    return true;
}

function handleLogout() {
    clearToken();
    showToast('Logged out successfully', 'success');
    setTimeout(() => {
        window.location.href = '/login.html';
    }, 500);
}

// Global Tab Handler for Dashboard Navigation
document.addEventListener('DOMContentLoaded', () => {
    setupSidebarTabNavigation();
});

function setupSidebarTabNavigation() {
    document.querySelectorAll('.sidebar-nav a[data-bs-toggle="tab"]').forEach(link => {
        link.addEventListener('click', (e) => {
            e.preventDefault();
            const targetId = link.getAttribute('data-bs-target') || link.getAttribute('href');
            if (!targetId || targetId === '#') return;

            // Highlight sidebar item
            const nav = link.closest('.sidebar-nav');
            if (nav) {
                nav.querySelectorAll('a').forEach(a => a.classList.remove('active'));
            }
            link.classList.add('active');

            // Switch tab pane
            const targetPane = document.querySelector(targetId);
            if (targetPane) {
                const content = targetPane.closest('.tab-content');
                if (content) {
                    content.querySelectorAll('.tab-pane').forEach(pane => {
                        pane.classList.remove('show', 'active');
                    });
                }
                targetPane.classList.add('show', 'active');
            }
        });
    });
}


