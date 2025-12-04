document.addEventListener('DOMContentLoaded', () => {
    setTimeout(checkAuthStatus, 100);

    const loginForm = document.getElementById('loginForm');
    if (loginForm) {
        loginForm.addEventListener('submit', handleLogin);
    }
});

async function handleLogin(event) {
    event.preventDefault();

    const email = document.getElementById('email').value.trim();
    const password = document.getElementById('password').value.trim();
    const errorDiv = document.getElementById('error-message');

    try {
        const response = await ApiService.post('/auth/login', { email, password });

        if (response.token) {
            localStorage.setItem('token', response.token);
            localStorage.setItem('userRole', response.role);
            localStorage.setItem('userId', response.userId);
            localStorage.setItem('userName', `${response.firstName} ${response.lastName}`);

            redirectToDashboard(response.role);
        }
    } catch (e) {
        errorDiv.textContent = 'Ошибка входа: проверьте email и пароль';
        errorDiv.style.display = 'block';
    }
}

function checkAuthStatus() {
    const token = localStorage.getItem('token');
    const currentPage = window.location.pathname;

    if (currentPage.includes('login.html')) {
        if (token) redirectToDashboard(localStorage.getItem('userRole'));
        return;
    }

    if (!token) window.location.href = '/login.html';
}

function redirectToDashboard(role) {
    switch (role) {
        case 'teacher':
            window.location.href = '/teacher-dashboard.html';
            break;
        case 'student':
            window.location.href = '/student-dashboard.html';
            break;
        case 'admin':
            window.location.href = '/admin-dashboard.html';
            break;
        default:
            localStorage.clear();
            window.location.href = '/login.html';
    }
}

function logout() {
    localStorage.clear();
    window.location.href = '/login.html';
}

window.logout = logout;
