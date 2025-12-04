/* ================================
   GLOBAL STATE
================================ */
let currentBio = '';
let isEditingBio = false;
let userRole = '';

/* ================================
   INIT
================================ */
document.addEventListener('DOMContentLoaded', () => {
    initProfile();
});

function initProfile() {
    loadProfile();

    const uploadInput = document.getElementById('avatar-upload');
    if (uploadInput) {
        uploadInput.addEventListener('change', e => {
            if (e.target.files.length > 0) uploadAvatar(e.target.files[0]);
        });
    }

    const backBtn = document.getElementById('back-to-dashboard');
    if (backBtn) {
        backBtn.addEventListener('click', e => {
            e.preventDefault();
            goBackToDashboard();
        });
    }

    const msgBtn = document.getElementById('send-message-btn');
    if (msgBtn) {
        msgBtn.addEventListener('click', () => {
            window.location.href = '/chat.html';
        });
    }
}

/* ================================
   LOAD PROFILE DATA
================================ */
async function loadProfile() {
    try {
        const profileData = await ApiService.get('/profile');
        displayProfile(profileData);

        document.getElementById('loading').style.display = 'none';
        document.getElementById('profile-content').style.display = 'grid';

    } catch (error) {
        document.getElementById('loading').innerHTML = `
            <div class="error-message">
                Ошибка загрузки профиля: ${error.message}
                <button onclick="loadProfile()">Повторить</button>
            </div>`;
    }
}

/* ================================
   DISPLAY PROFILE
================================ */
function displayProfile(profileData) {
    console.log('📋 Профиль:', profileData);

    // Имя + email
    document.getElementById('user-name').textContent =
        `${profileData.firstName} ${profileData.lastName}`;
    document.getElementById('user-email').textContent = profileData.email;
    document.getElementById('user-initials').textContent =
        getInitials(profileData.firstName, profileData.lastName);

    // Определяем и выводим роль
    userRole = determineUserRole(profileData);
    displayUserRole(userRole);
    updateBackLink(userRole);

    // Показывать теги только студентам
    const tagsSection = document.getElementById('tags-section');
    if (tagsSection) tagsSection.style.display = userRole === 'student' ? 'block' : 'none';

    // Аватар
    updateAvatar(profileData.profilePhotoPath);

    // Био
    currentBio = profileData.bio || '';
    displayBio(currentBio);
}

/* ================================
   AVATAR HANDLING
================================ */
function updateAvatar(photoPath) {
    const placeholder = document.getElementById('avatar-placeholder');
    const img = document.getElementById('avatar-image');
    const removeBtn = document.getElementById('remove-avatar-btn');

    if (!photoPath) {
        placeholder.style.display = 'flex';
        img.style.display = 'none';
        removeBtn.style.display = 'none';
        return;
    }

    const url = `/uploads/${photoPath}?t=${Date.now()}`;
    img.src = url;

    img.onload = () => {
        console.log('Аватар загружен:', url);
        placeholder.style.display = 'none';
        img.style.display = 'block';
        removeBtn.style.display = 'block';
    };

    img.onerror = () => {
        console.error('Ошибка загрузки аватара:', url);
        placeholder.style.display = 'flex';
        img.style.display = 'none';
        removeBtn.style.display = 'none';
    };
}

async function uploadAvatar(file) {
    if (!file) return;

    if (!file.type.startsWith('image/')) {
        showMessage('Выберите изображение', 'error');
        return;
    }
    if (file.size > 5 * 1024 * 1024) {
        showMessage('Размер изображения не должен превышать 5MB', 'error');
        return;
    }

    try {
        const formData = new FormData();
        formData.append('file', file);

        const response = await fetch('/api/profile/avatar', {
            method: 'POST',
            headers: { 'Authorization': `Bearer ${localStorage.getItem('token')}` },
            body: formData
        });

        if (!response.ok) {
            throw new Error(await response.text());
        }

        showMessage('Аватар обновлён!', 'success');
        await loadProfile();
        document.getElementById('avatar-upload').value = '';

    } catch (error) {
        showMessage(`Ошибка загрузки аватара: ${error.message}`, 'error');
    }
}

async function removeAvatar() {
    if (!confirm('Удалить аватар?')) return;

    try {
        await ApiService.delete('/profile/avatar');
        showMessage('Аватар удалён', 'success');
        loadProfile();
    } catch (error) {
        showMessage(`Ошибка удаления: ${error.message}`, 'error');
    }
}

/* ================================
   BIO
================================ */
function displayBio(bio) {
    const empty = document.getElementById('bio-empty');
    const content = document.getElementById('bio-content');
    const editBtn = document.getElementById('edit-bio-btn');

    if (bio?.trim()) {
        empty.style.display = 'none';
        content.style.display = 'block';
        content.textContent = bio;
        editBtn.textContent = '✏️ Редактировать';
    } else {
        empty.style.display = 'block';
        content.style.display = 'none';
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

        showMessage('Информация сохранена!', 'success');

    } catch (error) {
        showMessage(`Ошибка: ${error.message}`, 'error');
    }
}

/* ================================
   ROLE PROCESSING
================================ */
function determineUserRole(data) {
    if (data.role) return data.role;

    if (data.email?.includes('teacher')) return 'teacher';
    if (data.teacherClasses?.length) return 'teacher';

    return 'student';
}

function displayUserRole(role) {
    const el = document.getElementById('user-role');
    if (!el) return;

    const names = {
        teacher: '👨‍🏫 Учитель',
        student: '👨‍🎓 Студент',
        admin: '👨‍💼 Администратор'
    };

    el.textContent = names[role] || role;
}

function updateBackLink(role) {
    const link = document.getElementById('back-to-dashboard');

    const pages = {
        teacher: '/teacher-dashboard.html',
        student: '/student-dashboard.html',
        admin: '/admin.html'
    };

    const titles = {
        teacher: 'Панель учителя',
        student: 'Главное меню',
        admin: 'Панель администратора'
    };

    link.href = pages[role] || '/student-dashboard.html';
    link.textContent = `← Назад в ${titles[role]}`;
}

function goBackToDashboard() {
    const pages = {
        teacher: '/teacher-dashboard.html',
        student: '/student-dashboard.html',
        admin: '/admin.html'
    };

    window.location.href = pages[userRole] || '/student-dashboard.html';
}

/* ================================
   HELPERS
================================ */
function getInitials(first, last) {
    return `${first?.[0] || ''}${last?.[0] || ''}`.toUpperCase();
}

function showMessage(message, type) {
    const container = document.getElementById('message-container');
    const div = document.createElement('div');

    div.className = type === 'error' ? 'error-message' : 'success-message';
    div.textContent = message;

    container.appendChild(div);

    setTimeout(() => div.remove(), 5000);
}

function logout() {
    localStorage.removeItem('token');
    window.location.href = '/login.html';
}
