let currentBio = '';
let isEditingBio = false;
let userRole = '';

document.addEventListener('DOMContentLoaded', function() {
    loadProfile();

    // Обработчик загрузки аватарки
    document.getElementById('avatar-upload').addEventListener('change', function(e) {
        if (e.target.files.length > 0) {
            uploadAvatar(e.target.files[0]);
        }
    });

    // Обработчик для кнопки "Назад"
    document.getElementById('back-to-dashboard').addEventListener('click', function(e) {
        e.preventDefault();
        goBackToDashboard();
    });
});

async function loadProfile() {
    try {
        const profileData = await ApiService.get('/profile');
        displayProfile(profileData);

        document.getElementById('loading').style.display = 'none';
        document.getElementById('profile-content').style.display = 'grid';

    } catch (error) {
        console.error('Ошибка загрузки профиля:', error);
        document.getElementById('loading').innerHTML = `
                    <div class="error-message">
                        Ошибка загрузки профиля: ${error.message}
                        <button onclick="loadProfile()" style="margin-left: 10px;">Повторить</button>
                    </div>
                `;
    }
}

// Обработчик кнопки "Написать сообщение"
document.getElementById('send-message-btn').addEventListener('click', function() {
    // Открываем чат с текущим пользователем (если это чужой профиль)
    // или переходим к списку чатов (если это свой профиль)
    const currentProfileUserId = getCurrentProfileUserId(); // Нужно реализовать эту функцию
    const currentUserId = localStorage.getItem('userId');

    if (currentProfileUserId && currentProfileUserId !== currentUserId) {
        // Если просматриваем чужой профиль - открываем чат с этим пользователем
        window.location.href = `/chat-conversation.html?userId=${currentProfileUserId}`;
    } else {
        // Если это свой профиль - переходим к общему списку чатов
        window.location.href = '/chat.html';
    }
});

// Функция для получения ID пользователя профиля (нужно адаптировать под вашу логику)
function getCurrentProfileUserId() {
    // Если страница профиля отображает другого пользователя
    const urlParams = new URLSearchParams(window.location.search);
    const profileUserId = urlParams.get('id');

    if (profileUserId) {
        return profileUserId;
    }

    // Если просматриваем свой профиль
    return localStorage.getItem('userId');
}

// Обработчик кнопки "Написать сообщение"
document.getElementById('send-message-btn').addEventListener('click', function() {
    // Всегда переходим к общему списку чатов
    window.location.href = '/chat.html';
});

// Убираем сложную логику, просто показываем кнопку
function updateMessageButton() {
    const button = document.getElementById('send-message-btn');
    if (button) {
        button.style.display = 'block';
        button.textContent = '✉️ Сообщения';
    }
}

function displayProfile(profileData) {
    console.log('📋 Получены данные профиля:', profileData);

    document.getElementById('user-name').textContent =
        `${profileData.firstName} ${profileData.lastName}`;
    document.getElementById('user-email').textContent = profileData.email;

    const initials = getInitials(profileData.firstName, profileData.lastName);
    document.getElementById('user-initials').textContent = initials;

    // Определяем роль пользователя и отображаем её
    userRole = determineUserRole(profileData);
    displayUserRole(userRole);

    const tagsSection = document.getElementById('tags-section');
    if (tagsSection) {
        if (userRole === 'student') {
            tagsSection.style.display = 'block';
        } else {
            tagsSection.style.display = 'none';
        }
    }


    updateBackLink(userRole);

    const avatarPlaceholder = document.getElementById('avatar-placeholder');
    const avatarImage = document.getElementById('avatar-image');
    const removeBtn = document.getElementById('remove-avatar-btn');

    if (profileData.profilePhotoPath) {
        console.log('🖼 Profile photo path из API:', profileData.profilePhotoPath);

        const avatarUrl = `/uploads/${profileData.profilePhotoPath}?t=${new Date().getTime()}`;
        console.log('🔗 Avatar URL для загрузки:', avatarUrl);

        avatarPlaceholder.style.display = 'none';
        avatarImage.style.display = 'block';
        avatarImage.src = avatarUrl;
        removeBtn.style.display = 'block';

        avatarImage.onload = function() {
            console.log('✅ Аватарка загружена успешно!');
            console.log('📏 Размеры:', this.naturalWidth + 'x' + this.naturalHeight);
        };

        avatarImage.onerror = function() {
            console.error('❌ Ошибка загрузки аватарки по URL:', avatarUrl);
            console.log('🔄 Возвращаем placeholder');

            avatarPlaceholder.style.display = 'flex';
            avatarImage.style.display = 'none';
            removeBtn.style.display = 'none';
        };
    } else {
        console.log('ℹ️ Аватарка не установлена');
        avatarPlaceholder.style.display = 'flex';
        avatarImage.style.display = 'none';
        removeBtn.style.display = 'none';
    }

    currentBio = profileData.bio || '';
    displayBio(currentBio);
}

function determineUserRole(profileData) {
    // Способ 1: Проверяем наличие специфичных полей в данных профиля
    if (profileData.role) {
        return profileData.role; // Если роль явно указана в API
    }

    // Способ 2: Проверяем по email или другим признакам
    if (profileData.email && profileData.email.includes('teacher')) {
        return 'teacher';
    }

    // Способ 3: Проверяем наличие учительских классов или заданий
    if (profileData.teacherClasses && profileData.teacherClasses.length > 0) {
        return 'teacher';
    }

    // Способ 4: Делаем дополнительный запрос для определения роли
    return detectUserRoleFromAPI();
}

async function detectUserRoleFromAPI() {
    try {
        // Пробуем получить данные учителя
        await ApiService.get('/teacher/assignments/my');
        return 'teacher';
    } catch (error) {
        // Если ошибка доступа - значит студент
        return 'student';
    }
}

// Функция для отображения роли пользователя
function displayUserRole(role) {
    const roleElement = document.getElementById('user-role');
    if (roleElement) {
        const roleNames = {
            'teacher': '👨‍🏫 Учитель',
            'student': '👨‍🎓 Студент',
            'admin': '👨‍💼 Администратор'
        };

        roleElement.textContent = roleNames[role] || role;
        roleElement.style.display = 'block';
    }
}

// Функция для обновления ссылки "Назад"
function updateBackLink(role) {
    const backLink = document.getElementById('back-to-dashboard');

    const dashboardLinks = {
        'teacher': '/teacher-dashboard.html',
        'student': '/student-dashboard.html',
        'admin': '/admin.html'
    };

    const targetPage = dashboardLinks[role] || '/student-dashboard.html';
    backLink.href = targetPage;

    // Также обновляем текст, если нужно
    const dashboardNames = {
        'teacher': 'Панель учителя',
        'student': 'Главное меню',
        'admin': 'Панель администратора'
    };

    backLink.textContent = `← Назад в ${dashboardNames[role] || 'главное меню'}`;
}

// Функция для перехода на соответствующую dashboard страницу
function goBackToDashboard() {
    const dashboardLinks = {
        'teacher': '/teacher-dashboard.html',
        'student': '/student-dashboard.html',
        'admin': '/admin.html'
    };

    const targetPage = dashboardLinks[userRole] || '/student-dashboard.html';
    window.location.href = targetPage;
}

// Альтернативный способ: проверка роли при загрузке страницы
async function checkUserRoleOnLoad() {
    try {
        // Пробуем получить данные учителя
        const teacherData = await ApiService.get('/teacher/assignments/my');
        userRole = 'teacher';
    } catch (error) {
        // Если не учитель, пробуем получить данные студента
        try {
            const studentData = await ApiService.get('/student/assignments');
            userRole = 'student';
        } catch (error2) {
            // Если ничего не работает, используем fallback
            userRole = 'student';
        }
    }

    updateBackLink(userRole);
}

// Остальные функции остаются без изменений
function getInitials(firstName, lastName) {
    const first = firstName ? firstName[0].toUpperCase() : '';
    const last = lastName ? lastName[0].toUpperCase() : '';
    return first + last;
}

function displayBio(bio) {
    const bioDisplay = document.getElementById('bio-display');
    const bioEmpty = document.getElementById('bio-empty');
    const bioContent = document.getElementById('bio-content');
    const editBtn = document.getElementById('edit-bio-btn');

    if (bio && bio.trim() !== '') {
        bioEmpty.style.display = 'none';
        bioContent.style.display = 'block';
        bioContent.textContent = bio;
        editBtn.textContent = '✏️ Редактировать';
    } else {
        bioEmpty.style.display = 'block';
        bioContent.style.display = 'none';
        editBtn.textContent = '✏️ Добавить информацию о себе';
    }
}

function editBio() {
    isEditingBio = true;

    document.getElementById('bio-display').style.display = 'none';
    document.getElementById('bio-edit').style.display = 'block';
    document.getElementById('edit-bio-btn').style.display = 'none';

    document.getElementById('bio-textarea').value = currentBio;
    document.getElementById('bio-textarea').focus();
}

function cancelEditBio() {
    isEditingBio = false;

    document.getElementById('bio-display').style.display = 'block';
    document.getElementById('bio-edit').style.display = 'none';
    document.getElementById('edit-bio-btn').style.display = 'block';
}

async function saveBio() {
    const newBio = document.getElementById('bio-textarea').value.trim();

    try {
        await ApiService.put('/profile/bio', { bio: newBio });

        currentBio = newBio;
        displayBio(currentBio);
        cancelEditBio();

        showMessage('Информация успешно сохранена!', 'success');

    } catch (error) {
        console.error('Ошибка сохранения:', error);
        showMessage(`Ошибка сохранения: ${error.message}`, 'error');
    }
}

async function uploadAvatar(file) {
    if (!file) return;

    if (!file.type.startsWith('image/')) {
        showMessage('Пожалуйста, выберите изображение', 'error');
        return;
    }

    if (file.size > 5 * 1024 * 1024) {
        showMessage('Размер файла не должен превышать 5MB', 'error');
        return;
    }

    try {
        const formData = new FormData();
        formData.append('file', file);

        console.log('Начало загрузки аватарки...', file.name);

        const response = await fetch('/api/profile/avatar', {
            method: 'POST',
            headers: {
                'Authorization': `Bearer ${localStorage.getItem('token')}`
            },
            body: formData
        });

        console.log('Ответ сервера:', response.status, response.statusText);

        if (!response.ok) {
            const errorText = await response.text();
            console.error('Ошибка сервера:', errorText);
            throw new Error(errorText || 'Ошибка загрузки');
        }

        const result = await response.json();
        console.log('Полный ответ от сервера:', result);

        if (!result.filePath) {
            console.error('В ответе отсутствует filePath');
            throw new Error('Не удалось получить путь к файлу');
        }
        showMessage('Аватарка успешно обновлена!', 'success');

        await loadProfile();

        document.getElementById('avatar-upload').value = '';

    } catch (error) {
        console.error('Ошибка загрузки аватарки:', error);
        showMessage(`Ошибка загрузки: ${error.message}`, 'error');
    }
}

async function removeAvatar() {
    if (!confirm('Удалить аватарку?')) return;

    try {
        await ApiService.delete('/profile/avatar');

        showMessage('Аватарка удалена', 'success');

        document.getElementById('avatar-placeholder').style.display = 'flex';
        document.getElementById('avatar-image').style.display = 'none';
        document.getElementById('remove-avatar-btn').style.display = 'none';

    } catch (error) {
        console.error('Ошибка удаления аватарки:', error);
        showMessage(`Ошибка удаления: ${error.message}`, 'error');
    }
}

function showMessage(message, type) {
    const container = document.getElementById('message-container');
    const messageDiv = document.createElement('div');
    messageDiv.className = type === 'error' ? 'error-message' : 'success-message';
    messageDiv.textContent = message;

    container.appendChild(messageDiv);

    setTimeout(() => {
        if (messageDiv.parentNode) {
            messageDiv.parentNode.removeChild(messageDiv);
        }
    }, 5000);
}

function logout() {
    localStorage.removeItem('token');
    window.location.href = '/login.html';
}