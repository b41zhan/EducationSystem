document.addEventListener('DOMContentLoaded', function() {
    checkAuthStatus();

    const loginForm = document.getElementById('loginForm');
    if (loginForm) {
        loginForm.addEventListener('submit', handleLogin);
    }
});

async function handleLogin(event) {
    event.preventDefault();

    const email = document.getElementById('email').value;
    const password = document.getElementById('password').value;
    const errorDiv = document.getElementById('error-message');

    try {
        const response = await ApiService.post('/auth/login', {
            email: email,
            password: password
        });

        if (response.token) {
            localStorage.setItem('token', response.token);
            localStorage.setItem('userRole', response.role);
            localStorage.setItem('userId', response.userId);
            localStorage.setItem('userName', `${response.firstName} ${response.lastName}`);

            redirectToDashboard(response.role);
        }
    } catch (error) {
        errorDiv.textContent = 'Ошибка входа: проверьте email и пароль';
        errorDiv.style.display = 'block';
    }
}

function redirectToDashboard(role) {
    switch(role) {
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
            console.error('Unknown role:', role);
            window.location.href = '/login.html';
    }
}

function checkAuthStatus() {
    const token = localStorage.getItem('token');
    const currentPage = window.location.pathname;

    if (currentPage.includes('login.html') && token) {
        const role = localStorage.getItem('userRole');
        redirectToDashboard(role);
    }

    if (!currentPage.includes('login.html') && !token) {
        window.location.href = '/login.html';
    }
}

function logout() {
    localStorage.removeItem('token');
    localStorage.removeItem('userRole');
    localStorage.removeItem('userId');
    localStorage.removeItem('userName');
    window.location.href = '/login.html';
}