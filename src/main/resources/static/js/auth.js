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
        console.log("LOGIN RESPONSE:", response);

        const token =
            response?.token ||
            response?.accessToken ||
            response?.jwt ||
            response?.data?.token ||
            response?.data?.accessToken;

        const role =
            response?.role ||
            response?.userRole ||
            response?.data?.role ||
            response?.data?.userRole;

        const userId =
            response?.userId ||
            response?.id ||
            response?.data?.userId ||
            response?.data?.id;

        if (!token) {
            errorDiv.textContent = 'Логин успешен, но токен не пришёл. Смотри Console -> LOGIN RESPONSE';
            errorDiv.style.display = 'block';
            return;
        }

        localStorage.setItem('token', token);
        if (role) localStorage.setItem('userRole', role);
        if (userId) localStorage.setItem('userId', String(userId));
        if (response?.firstName || response?.lastName) {
            localStorage.setItem('userName', `${response.firstName ?? ''} ${response.lastName ?? ''}`.trim());
        }

        redirectToDashboard(role);
    } catch (e) {
        console.error(e);
        errorDiv.textContent = 'Ошибка входа: ' + (e.message || 'проверьте email и пароль');
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
        case 'parent':
            window.location.href = '/parent-dashboard.html';
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
