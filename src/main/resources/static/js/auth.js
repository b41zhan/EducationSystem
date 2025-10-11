// document.addEventListener('DOMContentLoaded', function() {
//     checkAuthStatus();
//
//     const loginForm = document.getElementById('loginForm');
//     if (loginForm) {
//         loginForm.addEventListener('submit', handleLogin);
//     }
// });
//
// async function handleLogin(event) {
//     event.preventDefault();
//
//     const email = document.getElementById('email').value;
//     const password = document.getElementById('password').value;
//     const errorDiv = document.getElementById('error-message');
//
//     try {
//         const response = await ApiService.post('/auth/login', {
//             email: email,
//             password: password
//         });
//
//         if (response.token) {
//             localStorage.setItem('token', response.token);
//             localStorage.setItem('userRole', response.role);
//             localStorage.setItem('userId', response.userId);
//             localStorage.setItem('userName', `${response.firstName} ${response.lastName}`);
//
//             redirectToDashboard(response.role);
//         }
//     } catch (error) {
//         errorDiv.textContent = 'Ошибка входа: проверьте email и пароль';
//         errorDiv.style.display = 'block';
//     }
// }
//
// function redirectToDashboard(role) {
//     switch(role) {
//         case 'teacher':
//             window.location.href = '/teacher-dashboard.html';
//             break;
//         case 'student':
//             window.location.href = '/student-dashboard.html';
//             break;
//         case 'admin':
//             window.location.href = '/admin-dashboard.html';
//             break;
//         default:
//             console.error('Unknown role:', role);
//             window.location.href = '/login.html';
//     }
// }
// function checkAuthStatus() {
//     const token = localStorage.getItem('token');
//     const currentPage = window.location.pathname;
//
//     console.log('🔍 Simple auth check - Page:', currentPage, 'Token:', !!token);
//
//     // Минимальная логика: если на логине и есть токен - редирект
//     if (currentPage.includes('login.html') && token) {
//         const role = localStorage.getItem('userRole');
//         console.log('Redirecting to dashboard for role:', role);
//         // Увеличиваем задержку до 2 секунд для тестирования
//         setTimeout(() => {
//             redirectToDashboard(role);
//         }, 2000);
//     }
//
//     // Если не на логине и нет токена - на логин
//     if (!currentPage.includes('login.html') && !token) {
//         console.log('No token, going to login');
//         window.location.href = '/login.html';
//     }
// }
//
// // async function validateToken(token) {
// //     try {
// //         const response = await ApiService.get('/auth/validate');
// //         console.log('Token is valid');
// //     } catch (error) {
// //         console.log('Token invalid, clearing storage and redirecting');
// //         localStorage.clear();
// //         window.location.href = '/login.html';
// //     }
// // }
//
// function logout() {
//     localStorage.removeItem('token');
//     localStorage.removeItem('userRole');
//     localStorage.removeItem('userId');
//     localStorage.removeItem('userName');
//     window.location.href = '/login.html';
// }



document.addEventListener('DOMContentLoaded', function() {
    console.log('🔍 DOM loaded, checking auth status...');

    // Добавляем небольшую задержку перед проверкой
    setTimeout(() => {
        checkAuthStatus();
    }, 100);

    const loginForm = document.getElementById('loginForm');
    if (loginForm) {
        console.log('✅ Login form found, adding event listener');
        loginForm.addEventListener('submit', handleLogin);
    } else {
        console.log('❌ Login form not found');
    }
});

async function handleLogin(event) {
    event.preventDefault();
    console.log('🔄 Login form submitted');

    const email = document.getElementById('email').value;
    const password = document.getElementById('password').value;
    const errorDiv = document.getElementById('error-message');

    try {
        console.log('📤 Sending login request for:', email);
        const response = await ApiService.post('/auth/login', {
            email: email,
            password: password
        });

        console.log('✅ Login successful, response:', response);

        if (response.token) {
            localStorage.setItem('token', response.token);
            localStorage.setItem('userRole', response.role);
            localStorage.setItem('userId', response.userId);
            localStorage.setItem('userName', `${response.firstName} ${response.lastName}`);

            console.log('🔑 Token saved, redirecting to:', response.role);
            // Редирект без задержки после успешного логина
            redirectToDashboard(response.role);
        }
    } catch (error) {
        console.error('❌ Login error:', error);
        errorDiv.textContent = 'Ошибка входа: проверьте email и пароль';
        errorDiv.style.display = 'block';
    }
}

function checkAuthStatus() {
    const token = localStorage.getItem('token');
    const currentPage = window.location.pathname;

    console.log('📄 Current page:', currentPage);
    console.log('🔑 Token exists:', !!token);
    console.log('👤 User role:', localStorage.getItem('userRole'));

    // Если мы на странице логина
    if (currentPage.includes('login.html')) {
        // И есть валидный токен - редиректим
        if (token) {
            const role = localStorage.getItem('userRole');
            console.log('🚀 Already logged in, redirecting to dashboard for role:', role);
            // Редирект без setTimeout
            redirectToDashboard(role);
        }
        // Если нет токена - остаемся на странице логина (ничего не делаем)
        return;
    }

    // Если мы НЕ на странице логина и нет токена - на логин
    if (!currentPage.includes('login.html') && !token) {
        console.log('❌ No token found, redirecting to login');
        window.location.href = '/login.html';
    }

    // Если есть токен и не на логине - остаемся на текущей странице
    console.log('✅ Auth check passed, staying on page');
}

function redirectToDashboard(role) {
    console.log('🎯 Redirecting to dashboard for role:', role);

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
            console.error('❌ Unknown role:', role);
            // При неизвестной роли очищаем storage и остаемся на логине
            localStorage.clear();
    }
}

function logout() {
    console.log('👋 Logging out...');
    localStorage.removeItem('token');
    localStorage.removeItem('userRole');
    localStorage.removeItem('userId');
    localStorage.removeItem('userName');
    window.location.href = '/login.html';
}