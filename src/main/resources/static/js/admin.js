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
        usersList.innerHTML = `
            <div class="empty-state">
                <i class="fas fa-user-slash"></i>
                <h3>Пользователи не найдены</h3>
                <p>Попробуйте изменить запрос поиска</p>
            </div>
        `;
        return;
    }

    usersList.innerHTML = '';

    users.forEach(user => {
        const row = document.createElement('div');
        row.className = 'user-item';

        const roles = user.roles ? user.roles.join(', ') : 'нет роли';
        const formattedDate = new Date(user.createdAt).toLocaleDateString('ru-RU');

        row.innerHTML = `
            <div class="user-info">
                <div class="user-avatar">${user.firstName[0]}${user.lastName[0]}</div>
                <div class="user-details">
                    <h4>${user.lastName} ${user.firstName}</h4>
                    ${user.className ? `<span class="user-class">${user.className}</span>` : ''}
                </div>
            </div>

            <div class="user-email">${user.email}</div>

            <div class="user-role ${user.roles[0]}">${user.roles[0]}</div>

            <div class="user-date">${formattedDate}</div>

            <div class="user-actions">
                <button class="btn-icon edit" onclick="showEditUserModal(${JSON.stringify(user).replace(/"/g, '&quot;')})">
                    <i class="fas fa-pencil-alt"></i>
                </button>

                <button class="btn-icon delete" onclick="deleteUserById('${user.id}')">
                    <i class="fas fa-trash"></i>
                </button>
            </div>
        `;

        usersList.appendChild(row);
    });
}


let currentEditingUser = null;

function showEditUserModal(user) {
    currentEditingUser = user;

    document.getElementById('editUserId').value = user.id;
    document.getElementById('editEmail').value = user.email;
    document.getElementById('editFirstName').value = user.firstName;
    document.getElementById('editLastName').value = user.lastName;
    document.getElementById('editPatronymic').value = user.patronymic || '';

    if (user.roles && user.roles.length > 0) {
        document.getElementById('editRole').value = user.roles[0];
    }

    document.getElementById('editPassword').value = '';

    displayUserHistory(user);

    document.getElementById('editUserModal').style.display = 'block';
}

function displayUserHistory(user) {
    const historyContainer = document.getElementById('editUserHistory');
    const lastModified = user.lastModifiedAt ?
        new Date(user.lastModifiedAt).toLocaleString('ru-RU') :
        new Date(user.createdAt).toLocaleString('ru-RU');

    const modifiedBy = user.lastModifiedBy ?
        `администратором ID: ${user.lastModifiedBy}` :
        'системой';

    historyContainer.innerHTML = `
        <div style="font-size: 14px;">
            <strong>История изменений:</strong><br>
            Создан: ${new Date(user.createdAt).toLocaleString('ru-RU')}<br>
            Последнее изменение: ${lastModified} ${modifiedBy}
        </div>
    `;
}

function closeEditUserModal() {
    document.getElementById('editUserModal').style.display = 'none';
    document.getElementById('editUserForm').reset();
    currentEditingUser = null;
}

document.getElementById('editUserForm').addEventListener('submit', async function(e) {
    e.preventDefault();

    const userId = document.getElementById('editUserId').value;
    const formData = {
        email: document.getElementById('editEmail').value,
        firstName: document.getElementById('editFirstName').value,
        lastName: document.getElementById('editLastName').value,
        patronymic: document.getElementById('editPatronymic').value,
        password: document.getElementById('editPassword').value,
        role: document.getElementById('editRole').value
    };

    try {
        await ApiService.put(`/admin/users/${userId}`, formData);
        alert('Пользователь успешно обновлен!');
        closeEditUserModal();
        loadUsers();

    } catch (error) {
        console.error('Error updating user:', error);
        alert('Ошибка при обновлении пользователя: ' + error.message);
    }
});

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
        loadUsers();
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
        loadUsers();
    } catch (error) {
        console.error('Error registering student:', error);
        alert('Ошибка при регистрации студента: ' + error.message);
    }
});


function searchUsers() {
    const searchTerm = document.getElementById('search-users').value.toLowerCase();
    alert(`Поиск: ${searchTerm} (функция будет реализована позже)`);
}