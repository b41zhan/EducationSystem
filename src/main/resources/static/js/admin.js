document.addEventListener('DOMContentLoaded', function() {
    loadAdminData();
    loadUsers();
});

async function loadAdminData() {
    try {
        const userData = await ApiService.get('/auth/me');
        document.getElementById('welcome-message').textContent =
            `Добро пожаловать, Администратор!`;
    } catch (error) {
        console.error('Error loading admin data:', error);
    }
}

async function loadUsers() {
    try {
        const users = await ApiService.get('/users');
        displayUsers(users);
        updateSystemStats(users);
    } catch (error) {
        console.error('Error loading users:', error);
        document.getElementById('users-list').innerHTML =
            '<p>Ошибка загрузки пользователей</p>';
    }
}

function displayUsers(users) {
    const usersList = document.getElementById('users-list');

    if (!users || users.length === 0) {
        usersList.innerHTML = '<p>Пользователи не найдены</p>';
        return;
    }

    usersList.innerHTML = '';

    users.forEach(user => {
        const userElement = document.createElement('div');
        userElement.className = 'assignment-item';

        const roles = user.roles ? user.roles.join(', ') : 'нет роли';

        userElement.innerHTML = `
            <div class="assignment-title">${user.firstName} ${user.lastName}</div>
            <div class="assignment-meta">
                Email: ${user.email} | 
                Роли: ${roles} |
                Школа: ${user.schoolName || 'Не указана'}
            </div>
            <div>Зарегистрирован: ${new Date(user.createdAt).toLocaleDateString('ru-RU')}</div>
        `;
        usersList.appendChild(userElement);
    });
}

function updateSystemStats(users) {
    const totalUsers = users.length;
    const totalTeachers = users.filter(u => u.roles && u.roles.includes('teacher')).length;
    const totalStudents = users.filter(u => u.roles && u.roles.includes('student')).length;
    const totalParents = users.filter(u => u.roles && u.roles.includes('parent')).length;

    document.getElementById('total-users').textContent = totalUsers;
    document.getElementById('total-teachers').textContent = totalTeachers;
    document.getElementById('total-students').textContent = totalStudents;
    document.getElementById('total-parents').textContent = totalParents;
}

function showRegisterTeacherModal() {
    document.getElementById('registerTeacherModal').style.display = 'block';
}

function closeRegisterTeacherModal() {
    document.getElementById('registerTeacherModal').style.display = 'none';
    document.getElementById('registerTeacherForm').reset();
}

function showRegisterStudentModal() {
    document.getElementById('registerStudentModal').style.display = 'block';
}

function closeRegisterStudentModal() {
    document.getElementById('registerStudentModal').style.display = 'none';
    document.getElementById('registerStudentForm').reset();
}

function showRegisterParentModal() {
    alert('Регистрация родителя будет реализована позже');
}

document.getElementById('registerTeacherForm').addEventListener('submit', async function(e) {
    e.preventDefault();

    const formData = {
        email: document.getElementById('teacherEmail').value,
        passwordHash: document.getElementById('teacherPassword').value,
        firstName: document.getElementById('teacherFirstName').value,
        lastName: document.getElementById('teacherLastName').value,
        patronymic: document.getElementById('teacherPatronymic').value
    };

    try {
        await ApiService.post('/admin/register/teacher', formData);
        alert('Учитель успешно зарегистрирован!');
        closeRegisterTeacherModal();
        loadUsers(); // Обновляем список пользователей
    } catch (error) {
        console.error('Error registering teacher:', error);
        alert('Ошибка при регистрации учителя: ' + error.message);
    }
});

document.getElementById('registerStudentForm').addEventListener('submit', async function(e) {
    e.preventDefault();

    const classId = document.getElementById('studentClass').value;
    const formData = {
        email: document.getElementById('studentEmail').value,
        passwordHash: document.getElementById('studentPassword').value,
        firstName: document.getElementById('studentFirstName').value,
        lastName: document.getElementById('studentLastName').value,
        patronymic: document.getElementById('studentPatronymic').value
    };

    try {
        await ApiService.post(`/admin/register/student?classId=${classId}`, formData);
        alert('Студент успешно зарегистрирован!');
        closeRegisterStudentModal();
        loadUsers(); // Обновляем список пользователей
    } catch (error) {
        console.error('Error registering student:', error);
        alert('Ошибка при регистрации студента: ' + error.message);
    }
});

function searchUsers() {
    const searchTerm = document.getElementById('search-users').value.toLowerCase();
    alert(`Поиск: ${searchTerm} (функция будет реализована позже)`);
}