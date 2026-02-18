// document.addEventListener('DOMContentLoaded', function() {
//     loadAdminData();
//     loadUsers();
// });
//
// async function loadAdminData() {
//     try {
//         const userData = await ApiService.get('/auth/me');
//         document.getElementById('welcome-message').textContent =
//             `Добро пожаловать, Администратор!`;
//     } catch (error) {
//         console.error('Error loading admin data:', error);
//     }
// }
//
// async function loadUsers() {
//     try {
//         const users = await ApiService.get('/users');
//         displayUsers(users);
//         updateSystemStats(users);
//     } catch (error) {
//         console.error('Error loading users:', error);
//         document.getElementById('users-list').innerHTML =
//             '<p>Ошибка загрузки пользователей</p>';
//     }
// }
//
// function displayUsers(users) {
//     const usersList = document.getElementById('users-list');
//
//     if (!users || users.length === 0) {
//         usersList.innerHTML = `
//             <div class="empty-state">
//                 <i class="fas fa-user-slash"></i>
//                 <h3>Пользователи не найдены</h3>
//                 <p>Попробуйте изменить запрос поиска</p>
//             </div>
//         `;
//         return;
//     }
//
//     usersList.innerHTML = '';
//
//     users.forEach(user => {
//         const row = document.createElement('div');
//         row.className = 'user-item';
//
//         const roles = user.roles ? user.roles.join(', ') : 'нет роли';
//         const formattedDate = new Date(user.createdAt).toLocaleDateString('ru-RU');
//
//         row.innerHTML = `
//             <div class="user-info">
//                 <div class="user-avatar">${user.firstName[0]}${user.lastName[0]}</div>
//                 <div class="user-details">
//                     <h4>${user.lastName} ${user.firstName}</h4>
//                     ${user.className ? `<span class="user-class">${user.className}</span>` : ''}
//                 </div>
//             </div>
//
//             <div class="user-email">${user.email}</div>
//
//             <div class="user-role ${user.roles[0]}">${user.roles[0]}</div>
//
//             <div class="user-date">${formattedDate}</div>
//
//             <div class="user-actions">
//                 <button class="btn-icon edit" onclick="showEditUserModal(${JSON.stringify(user).replace(/"/g, '&quot;')})">
//                     <i class="fas fa-pencil-alt"></i>
//                 </button>
//
//                 <button class="btn-icon delete" onclick="deleteUserById('${user.id}')">
//                     <i class="fas fa-trash"></i>
//                 </button>
//             </div>
//         `;
//
//         usersList.appendChild(row);
//     });
// }
//
//
// let currentEditingUser = null;
//
// function showEditUserModal(user) {
//     currentEditingUser = user;
//
//     document.getElementById('editUserId').value = user.id;
//     document.getElementById('editEmail').value = user.email;
//     document.getElementById('editFirstName').value = user.firstName;
//     document.getElementById('editLastName').value = user.lastName;
//     document.getElementById('editPatronymic').value = user.patronymic || '';
//
//     if (user.roles && user.roles.length > 0) {
//         document.getElementById('editRole').value = user.roles[0];
//     }
//
//     document.getElementById('editPassword').value = '';
//
//     displayUserHistory(user);
//
//     document.getElementById('editUserModal').style.display = 'block';
// }
//
// function displayUserHistory(user) {
//     const historyContainer = document.getElementById('editUserHistory');
//     const lastModified = user.lastModifiedAt ?
//         new Date(user.lastModifiedAt).toLocaleString('ru-RU') :
//         new Date(user.createdAt).toLocaleString('ru-RU');
//
//     const modifiedBy = user.lastModifiedBy ?
//         `администратором ID: ${user.lastModifiedBy}` :
//         'системой';
//
//     historyContainer.innerHTML = `
//         <div style="font-size: 14px;">
//             <strong>История изменений:</strong><br>
//             Создан: ${new Date(user.createdAt).toLocaleString('ru-RU')}<br>
//             Последнее изменение: ${lastModified} ${modifiedBy}
//         </div>
//     `;
// }
//
// function closeEditUserModal() {
//     document.getElementById('editUserModal').style.display = 'none';
//     document.getElementById('editUserForm').reset();
//     currentEditingUser = null;
// }
//
// document.getElementById('editUserForm').addEventListener('submit', async function(e) {
//     e.preventDefault();
//
//     const userId = document.getElementById('editUserId').value;
//     const formData = {
//         email: document.getElementById('editEmail').value,
//         firstName: document.getElementById('editFirstName').value,
//         lastName: document.getElementById('editLastName').value,
//         patronymic: document.getElementById('editPatronymic').value,
//         password: document.getElementById('editPassword').value,
//         role: document.getElementById('editRole').value
//     };
//
//     try {
//         await ApiService.put(`/admin/users/${userId}`, formData);
//         alert('Пользователь успешно обновлен!');
//         closeEditUserModal();
//         loadUsers();
//
//     } catch (error) {
//         console.error('Error updating user:', error);
//         alert('Ошибка при обновлении пользователя: ' + error.message);
//     }
// });
//
// function updateSystemStats(users) {
//     const totalUsers = users.length;
//     const totalTeachers = users.filter(u => u.roles && u.roles.includes('teacher')).length;
//     const totalStudents = users.filter(u => u.roles && u.roles.includes('student')).length;
//     const totalParents = users.filter(u => u.roles && u.roles.includes('parent')).length;
//
//     document.getElementById('total-users').textContent = totalUsers;
//     document.getElementById('total-teachers').textContent = totalTeachers;
//     document.getElementById('total-students').textContent = totalStudents;
//     document.getElementById('total-parents').textContent = totalParents;
// }
//
// function showRegisterTeacherModal() {
//     document.getElementById('registerTeacherModal').style.display = 'block';
// }
//
// function closeRegisterTeacherModal() {
//     document.getElementById('registerTeacherModal').style.display = 'none';
//     document.getElementById('registerTeacherForm').reset();
// }
//
// function showRegisterStudentModal() {
//     document.getElementById('registerStudentModal').style.display = 'block';
// }
//
// function closeRegisterStudentModal() {
//     document.getElementById('registerStudentModal').style.display = 'none';
//     document.getElementById('registerStudentForm').reset();
// }
//
// function showRegisterParentModal() {
//     alert('Регистрация родителя будет реализована позже');
// }
//
// document.getElementById('registerTeacherForm').addEventListener('submit', async function(e) {
//     e.preventDefault();
//
//     const formData = {
//         email: document.getElementById('teacherEmail').value,
//         passwordHash: document.getElementById('teacherPassword').value,
//         firstName: document.getElementById('teacherFirstName').value,
//         lastName: document.getElementById('teacherLastName').value,
//         patronymic: document.getElementById('teacherPatronymic').value
//     };
//
//     try {
//         await ApiService.post('/admin/register/teacher', formData);
//         alert('Учитель успешно зарегистрирован!');
//         closeRegisterTeacherModal();
//         loadUsers();
//     } catch (error) {
//         console.error('Error registering teacher:', error);
//         alert('Ошибка при регистрации учителя: ' + error.message);
//     }
// });
//
// document.getElementById('registerStudentForm').addEventListener('submit', async function(e) {
//     e.preventDefault();
//
//     const classId = document.getElementById('studentClass').value;
//     const formData = {
//         email: document.getElementById('studentEmail').value,
//         passwordHash: document.getElementById('studentPassword').value,
//         firstName: document.getElementById('studentFirstName').value,
//         lastName: document.getElementById('studentLastName').value,
//         patronymic: document.getElementById('studentPatronymic').value
//     };
//
//     try {
//         await ApiService.post(`/admin/register/student?classId=${classId}`, formData);
//         alert('Студент успешно зарегистрирован!');
//         closeRegisterStudentModal();
//         loadUsers();
//     } catch (error) {
//         console.error('Error registering student:', error);
//         alert('Ошибка при регистрации студента: ' + error.message);
//     }
// });
//
//
// function searchUsers() {
//     const searchTerm = document.getElementById('search-users').value.toLowerCase();
//     alert(`Поиск: ${searchTerm} (функция будет реализована позже)`);
// }


document.addEventListener('DOMContentLoaded', function () {
    loadAdminData();
    loadUsers();
});

/* ===============================
   ЗАГРУЗКА ОСНОВНЫХ ДАННЫХ
=================================*/

async function loadAdminData() {
    try {
        await ApiService.get('/auth/me');
        document.getElementById('welcome-message').textContent =
            'Добро пожаловать, Администратор!';
    } catch (e) {
        console.error(e);
    }
}

async function loadUsers() {
    try {
        const users = await ApiService.get('/users');
        displayUsers(users);
        updateSystemStats(users);
    } catch (e) {
        console.error(e);
    }
}

/* ===============================
   СПИСОК ПОЛЬЗОВАТЕЛЕЙ
=================================*/

function displayUsers(users) {
    const container = document.getElementById('users-list');
    container.innerHTML = '';

    if (!users || users.length === 0) {
        container.innerHTML = '<p>Нет пользователей</p>';
        return;
    }

    users.forEach(user => {
        const div = document.createElement('div');
        div.className = 'user-item';
        div.innerHTML = `
            <div>${user.lastName} ${user.firstName}</div>
            <div>${user.email}</div>
            <div>${user.roles[0]}</div>
        `;
        container.appendChild(div);
    });
}

function updateSystemStats(users) {
    document.getElementById('total-users').textContent = users.length;
    document.getElementById('total-teachers').textContent =
        users.filter(u => u.roles.includes('teacher')).length;
    document.getElementById('total-students').textContent =
        users.filter(u => u.roles.includes('student')).length;
    document.getElementById('total-parents').textContent =
        users.filter(u => u.roles.includes('parent')).length;
}

/* ===============================
   ЗАГРУЗКА ШКОЛ И КЛАССОВ
=================================*/

async function loadSchools(selectId) {
    const schools = await ApiService.get('/schools');
    const select = document.getElementById(selectId);
    if (!select) return;

    select.innerHTML =
        `<option value="">Выберите школу</option>` +
        schools.map(s => `<option value="${s.id}">${s.name}</option>`).join('');
}

async function loadClasses(schoolId, selectId) {
    const select = document.getElementById(selectId);
    if (!select) return;

    if (!schoolId) {
        select.innerHTML = `<option value="">Сначала выберите школу</option>`;
        return;
    }

    const classes = await ApiService.get(`/schools/${schoolId}/classes`);

    select.innerHTML =
        `<option value="">Выберите класс</option>` +
        classes.map(c => `<option value="${c.id}">${c.name}</option>`).join('');
}

async function loadStudentsByClass(classId, selectId) {
    const select = document.getElementById(selectId);
    if (!select) return;

    if (!classId) {
        select.innerHTML = `<option value="">Сначала выберите класс</option>`;
        return;
    }

    const students = await ApiService.get(`/students/classes/${classId}/students`);
    console.log("students =", students);

    if (!students || students.length === 0) {
        select.innerHTML = `<option value="">В этом классе нет учеников</option>`;
        return;
    }

    select.innerHTML = students.map(s => {
        const fio = `${s.lastName} ${s.firstName}${s.patronymic ? " " + s.patronymic : ""}`;
        return `<option value="${s.id}">${fio}</option>`;
    }).join("");
}



/* ===============================
   МОДАЛКА УЧИТЕЛЯ
=================================*/

async function showRegisterTeacherModal() {
    document.getElementById('registerTeacherModal').style.display = 'block';
    await loadSchools('teacherSchoolSelect');
}

function closeRegisterTeacherModal() {
    document.getElementById('registerTeacherModal').style.display = 'none';
    document.getElementById('registerTeacherForm').reset();
}

document.getElementById('registerTeacherForm')
    .addEventListener('submit', async function (e) {
        e.preventDefault();

        const schoolId = document.getElementById('teacherSchoolSelect').value;
        if (!schoolId) {
            alert('Выберите школу');
            return;
        }

        const data = {
            email: document.getElementById('teacherEmail').value,
            passwordHash: document.getElementById('teacherPassword').value,
            firstName: document.getElementById('teacherFirstName').value,
            lastName: document.getElementById('teacherLastName').value,
            patronymic: document.getElementById('teacherPatronymic').value
        };

        await ApiService.post(`/admin/register/teacher?schoolId=${schoolId}`, data);

        alert('Учитель зарегистрирован');
        closeRegisterTeacherModal();
        loadUsers();
    });

/* ===============================
   МОДАЛКА СТУДЕНТА
=================================*/

async function showRegisterStudentModal() {
    document.getElementById('registerStudentModal').style.display = 'block';

    // 1) Загружаем школы
    await loadSchools('studentSchoolSelect');

    const schoolSelect = document.getElementById('studentSchoolSelect');

    // 2) Если сейчас выбран placeholder (value пустой) — выбираем первую реальную школу
    if (!schoolSelect.value && schoolSelect.options.length > 1) {
        schoolSelect.selectedIndex = 1;
    }

    // 3) Грузим классы по выбранной школе
    await loadClasses(schoolSelect.value, 'studentClassSelect');

    // 4) При смене школы — обновляем классы
    schoolSelect.onchange = async function () {
        await loadClasses(this.value, 'studentClassSelect');
    };
}


function closeRegisterStudentModal() {
    document.getElementById('registerStudentModal').style.display = 'none';
    document.getElementById('registerStudentForm').reset();
}

document.getElementById('registerStudentForm')
    .addEventListener('submit', async function (e) {
        e.preventDefault();

        const classId = document.getElementById('studentClassSelect').value;
        if (!classId) {
            alert('Выберите класс');
            return;
        }

        const data = {
            email: document.getElementById('studentEmail').value,
            passwordHash: document.getElementById('studentPassword').value,
            firstName: document.getElementById('studentFirstName').value,
            lastName: document.getElementById('studentLastName').value,
            patronymic: document.getElementById('studentPatronymic').value
        };

        await ApiService.post(`/admin/register/student?classId=${classId}`, data);

        alert('Студент зарегистрирован');
        closeRegisterStudentModal();
        loadUsers();
    });

/* ===============================
   РОДИТЕЛЬ (пока заглушка)
=================================*/

async function showRegisterParentModal() {
    document.getElementById('registerParentModal').style.display = 'block';

    // 1) школы
    await loadSchools('parentSchoolSelect');

    const schoolSel = document.getElementById('parentSchoolSelect');
    const classSel  = document.getElementById('parentClassSelect');

    // 2) выбрать первую школу (как ты делал раньше)
    if (!schoolSel.value && schoolSel.options.length > 1) {
        schoolSel.selectedIndex = 1;
    }

    // 3) классы для выбранной школы
    await loadClasses(schoolSel.value, 'parentClassSelect');

    // 4) выбрать первый класс
    if (!classSel.value && classSel.options.length > 1) {
        classSel.selectedIndex = 1;
    }

    // 5) ✅ ЗАГРУЗИТЬ ДЕТЕЙ
    await loadStudentsByClass(classSel.value, 'parentStudentsSelect');

    // 6) если меняем школу → обновляем классы и детей
    schoolSel.onchange = async function () {
        await loadClasses(this.value, 'parentClassSelect');

        // выбрать первый класс после обновления
        if (classSel.options.length > 1) classSel.selectedIndex = 1;

        await loadStudentsByClass(classSel.value, 'parentStudentsSelect');
    };

    // 7) если меняем класс → обновляем детей
    classSel.onchange = async function () {
        await loadStudentsByClass(this.value, 'parentStudentsSelect');
    };
}



function closeRegisterParentModal() {
    document.getElementById('registerParentModal').style.display = 'none';
    document.getElementById('registerParentForm').reset();
    document.getElementById('parentStudentsSelect').innerHTML = "";
}

document.getElementById('registerParentForm').addEventListener('submit', async function(e) {
    e.preventDefault();

    const studentsSelect = document.getElementById('parentStudentsSelect');
    const studentIds = Array.from(studentsSelect.selectedOptions).map(o => Number(o.value));

    if (studentIds.length === 0) {
        alert("Выберите хотя бы одного ребенка");
        return;
    }

    const payload = {
        email: document.getElementById('parentEmail').value,
        passwordHash: document.getElementById('parentPassword').value,
        firstName: document.getElementById('parentFirstName').value,
        lastName: document.getElementById('parentLastName').value,
        patronymic: document.getElementById('parentPatronymic').value,
        studentIds: studentIds
    };

    try {
        await ApiService.post('/admin/register/parent', payload);
        alert('Родитель успешно зарегистрирован!');
        closeRegisterParentModal();
        loadUsers();
    } catch (err) {
        console.error(err);
        alert('Ошибка регистрации родителя: ' + err.message);
    }
});

