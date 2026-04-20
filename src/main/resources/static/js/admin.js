const AdminState = {
    allUsers: [],
    filteredUsers: [],
    schools: [],
    selectedSchoolId: null,
    classes: [],
    classDetails: null,
    unassignedStudents: [],
    allSchoolStudents: [],
    availableParents: [],
    parentChildLinks: []
};

document.addEventListener('DOMContentLoaded', async function () {
    await initializeAdminPage();
});

/* ===============================
   INIT
================================= */

async function initializeAdminPage() {
    try {
        await loadAdminData();
        await loadUsers();
        await loadAllSchools();
        await initOrganizationBlock();
        initForms();
    } catch (e) {
        console.error('Admin page init error:', e);
        alert('Ошибка инициализации админ-панели: ' + (e.message || e));
    }
}

function initForms() {
    const registerTeacherForm = document.getElementById('registerTeacherForm');
    const registerStudentForm = document.getElementById('registerStudentForm');
    const registerParentForm = document.getElementById('registerParentForm');
    const editUserForm = document.getElementById('editUserForm');
    const classForm = document.getElementById('classForm');

    if (registerTeacherForm) {
        registerTeacherForm.addEventListener('submit', handleRegisterTeacher);
    }

    if (registerStudentForm) {
        registerStudentForm.addEventListener('submit', handleRegisterStudent);
    }

    if (registerParentForm) {
        registerParentForm.addEventListener('submit', handleRegisterParent);
    }

    if (editUserForm) {
        editUserForm.addEventListener('submit', handleEditUser);
    }

    if (classForm) {
        classForm.addEventListener('submit', handleSaveClass);
    }
}

/* ===============================
   BASE ADMIN DATA
================================= */

async function loadAdminData() {
    try {
        const me = await ApiService.get('/auth/me');
        const name = [me.lastName, me.firstName].filter(Boolean).join(' ').trim();
        document.getElementById('welcome-message').textContent =
            name ? `Добро пожаловать, ${name}!` : 'Добро пожаловать, Администратор!';
    } catch (e) {
        console.error('loadAdminData error:', e);
        document.getElementById('welcome-message').textContent = 'Добро пожаловать, Администратор!';
    }
}

async function loadUsers() {
    try {
        const users = await ApiService.get('/users');
        AdminState.allUsers = Array.isArray(users) ? users : [];
        AdminState.filteredUsers = [...AdminState.allUsers];

        displayUsers(AdminState.filteredUsers);
        updateSystemStats(AdminState.allUsers);
    } catch (e) {
        console.error('loadUsers error:', e);
        document.getElementById('users-list').innerHTML = `
            <div class="loading-state">
                <i class="fas fa-circle-exclamation"></i>
                <p>Ошибка загрузки пользователей</p>
            </div>
        `;
    }
}

function updateSystemStats(users) {
    document.getElementById('total-users').textContent = users.length;
    document.getElementById('total-teachers').textContent =
        users.filter(u => Array.isArray(u.roles) && u.roles.includes('teacher')).length;
    document.getElementById('total-students').textContent =
        users.filter(u => Array.isArray(u.roles) && u.roles.includes('student')).length;
    document.getElementById('total-parents').textContent =
        users.filter(u => Array.isArray(u.roles) && u.roles.includes('parent')).length;
    document.getElementById('users-total-count').textContent = users.length;
}

function displayUsers(users) {
    const container = document.getElementById('users-list');
    container.innerHTML = '';

    if (!users || users.length === 0) {
        container.innerHTML = `<div class="empty-state">Пользователи не найдены</div>`;
        return;
    }

    users.forEach(user => {
        const row = document.createElement('div');
        row.className = 'user-item';
        row.style.display = 'grid';
        row.style.gridTemplateColumns = '1.4fr 1.2fr 0.8fr 1fr 1fr 0.8fr';
        row.style.alignItems = 'center';
        row.style.gap = '12px';
        row.style.padding = '14px 0';
        row.style.borderBottom = '1px solid rgba(0,0,0,0.06)';

        const fullName = `${user.lastName || ''} ${user.firstName || ''} ${user.patronymic || ''}`.replace(/\s+/g, ' ').trim();
        const roleText = Array.isArray(user.roles) && user.roles.length > 0 ? user.roles.join(', ') : '—';
        const createdAt = user.createdAt ? formatDateTime(user.createdAt) : '—';
        const schoolText = user.schoolName || '—';

        row.innerHTML = `
            <div>
                <div style="font-weight:700;color:#111827;">${escapeHtml(fullName || 'Без имени')}</div>
            </div>
            <div>${escapeHtml(user.email || '—')}</div>
            <div>${escapeHtml(roleText)}</div>
            <div>${escapeHtml(schoolText)}</div>
            <div>${escapeHtml(createdAt)}</div>
            <div>
                <button class="btn-action btn-secondary btn-small" onclick="openEditUserModal(${user.id})">
                    <i class="fas fa-edit"></i> Редактировать
                </button>
            </div>
        `;
        container.appendChild(row);
    });
}

function searchUsers() {
    const query = (document.getElementById('search-users').value || '').trim().toLowerCase();

    if (!query) {
        AdminState.filteredUsers = [...AdminState.allUsers];
        displayUsers(AdminState.filteredUsers);
        return;
    }

    AdminState.filteredUsers = AdminState.allUsers.filter(user => {
        const fullName = `${user.lastName || ''} ${user.firstName || ''} ${user.patronymic || ''}`.toLowerCase();
        const email = (user.email || '').toLowerCase();
        const schoolName = (user.schoolName || '').toLowerCase();
        const roles = Array.isArray(user.roles) ? user.roles.join(' ').toLowerCase() : '';

        return fullName.includes(query) || email.includes(query) || schoolName.includes(query) || roles.includes(query);
    });

    displayUsers(AdminState.filteredUsers);
}

/* ===============================
   LOAD SCHOOLS / CLASSES / STUDENTS HELPERS
================================= */

async function loadAllSchools() {
    try {
        const schools = await ApiService.get('/schools');
        AdminState.schools = Array.isArray(schools) ? schools : [];

        fillSchoolSelect('teacherSchoolSelect', AdminState.schools, 'Выберите школу');
        fillSchoolSelect('studentSchoolSelect', AdminState.schools, 'Выберите школу');
        fillSchoolSelect('parentSchoolSelect', AdminState.schools, 'Выберите школу');
        fillSchoolSelect('organizationSchoolSelect', AdminState.schools, 'Выберите школу');
        fillSchoolSelect('classSchoolSelect', AdminState.schools, 'Выберите школу');

        if (AdminState.schools.length > 0) {
            const defaultSchoolId = AdminState.schools[0].id;
            document.getElementById('organizationSchoolSelect').value = String(defaultSchoolId);
            AdminState.selectedSchoolId = defaultSchoolId;
        }
    } catch (e) {
        console.error('loadAllSchools error:', e);
        AdminState.schools = [];
    }
}

function fillSchoolSelect(selectId, schools, placeholder = 'Выберите школу') {
    const select = document.getElementById(selectId);
    if (!select) return;

    select.innerHTML =
        `<option value="">${placeholder}</option>` +
        schools.map(s => `<option value="${s.id}">${escapeHtml(s.name)}</option>`).join('');
}

async function loadClasses(schoolId, selectId, options = {}) {
    const select = document.getElementById(selectId);
    if (!select) return [];

    const placeholder = options.placeholder || 'Выберите класс';
    const includeInactive = options.includeInactive ?? false;

    if (!schoolId) {
        select.innerHTML = `<option value="">Сначала выберите школу</option>`;
        return [];
    }

    let classes = await ApiService.get(`/schools/${schoolId}/classes`);
    classes = Array.isArray(classes) ? classes : [];

    if (!includeInactive) {
        classes = classes.filter(c => c.active !== false);
    }

    select.innerHTML =
        `<option value="">${placeholder}</option>` +
        classes.map(c => {
            const title = c.academicYear ? `${c.name} (${c.academicYear})` : c.name;
            return `<option value="${c.id}">${escapeHtml(title)}</option>`;
        }).join('');

    return classes;
}

async function loadStudentsByClass(classId, selectId, options = {}) {
    const select = document.getElementById(selectId);
    if (!select) return [];

    const placeholder = options.placeholder || 'Выберите ученика';

    if (!classId) {
        select.innerHTML = `<option value="">Сначала выберите класс</option>`;
        return [];
    }

    const students = await ApiService.get(`/students/classes/${classId}/students`);
    const safeStudents = Array.isArray(students) ? students : [];

    if (safeStudents.length === 0) {
        select.innerHTML = `<option value="">В этом классе нет учеников</option>`;
        return [];
    }

    select.innerHTML =
        `<option value="">${placeholder}</option>` +
        safeStudents.map(s => {
            const fio = `${s.lastName || ''} ${s.firstName || ''}${s.patronymic ? ' ' + s.patronymic : ''}`.replace(/\s+/g, ' ').trim();
            return `<option value="${s.id}">${escapeHtml(fio)}</option>`;
        }).join('');

    return safeStudents;
}

/* ===============================
   USER REGISTRATION MODALS
================================= */

async function showRegisterTeacherModal() {
    document.getElementById('registerTeacherModal').style.display = 'block';
    fillSchoolSelect('teacherSchoolSelect', AdminState.schools, 'Выберите школу');
}

function closeRegisterTeacherModal() {
    document.getElementById('registerTeacherModal').style.display = 'none';
    document.getElementById('registerTeacherForm').reset();
}

async function handleRegisterTeacher(e) {
    e.preventDefault();

    const schoolId = document.getElementById('teacherSchoolSelect').value;
    if (!schoolId) {
        alert('Выберите школу');
        return;
    }

    const data = {
        email: document.getElementById('teacherEmail').value.trim(),
        passwordHash: document.getElementById('teacherPassword').value,
        firstName: document.getElementById('teacherFirstName').value.trim(),
        lastName: document.getElementById('teacherLastName').value.trim(),
        patronymic: document.getElementById('teacherPatronymic').value.trim()
    };

    try {
        await ApiService.post(`/admin/register/teacher?schoolId=${schoolId}`, data);
        alert('Учитель зарегистрирован');
        closeRegisterTeacherModal();
        await loadUsers();
        await refreshStructureData();
    } catch (error) {
        console.error('handleRegisterTeacher error:', error);
        alert('Ошибка регистрации учителя: ' + (error.message || error));
    }
}

async function showRegisterStudentModal() {
    document.getElementById('registerStudentModal').style.display = 'block';
    fillSchoolSelect('studentSchoolSelect', AdminState.schools, 'Выберите школу');

    const schoolSelect = document.getElementById('studentSchoolSelect');
    const classSelect = document.getElementById('studentClassSelect');

    classSelect.innerHTML = `<option value="">Сначала выберите школу</option>`;

    schoolSelect.onchange = async function () {
        await loadClasses(this.value, 'studentClassSelect', { placeholder: 'Выберите класс' });
    };

    if (!schoolSelect.value && schoolSelect.options.length > 1) {
        schoolSelect.selectedIndex = 1;
        await loadClasses(schoolSelect.value, 'studentClassSelect', { placeholder: 'Выберите класс' });
    }
}

function closeRegisterStudentModal() {
    document.getElementById('registerStudentModal').style.display = 'none';
    document.getElementById('registerStudentForm').reset();
}

async function handleRegisterStudent(e) {
    e.preventDefault();

    const classId = document.getElementById('studentClassSelect').value;
    if (!classId) {
        alert('Выберите класс');
        return;
    }

    const data = {
        email: document.getElementById('studentEmail').value.trim(),
        passwordHash: document.getElementById('studentPassword').value,
        firstName: document.getElementById('studentFirstName').value.trim(),
        lastName: document.getElementById('studentLastName').value.trim(),
        patronymic: document.getElementById('studentPatronymic').value.trim()
    };

    try {
        await ApiService.post(`/admin/register/student?classId=${classId}`, data);
        alert('Студент зарегистрирован');
        closeRegisterStudentModal();
        await loadUsers();
        await refreshStructureData();
    } catch (error) {
        console.error('handleRegisterStudent error:', error);
        alert('Ошибка регистрации студента: ' + (error.message || error));
    }
}

async function showRegisterParentModal() {
    document.getElementById('registerParentModal').style.display = 'block';
    fillSchoolSelect('parentSchoolSelect', AdminState.schools, 'Выберите школу');

    const schoolSelect = document.getElementById('parentSchoolSelect');
    const classSelect = document.getElementById('parentClassSelect');
    const studentsSelect = document.getElementById('parentStudentsSelect');

    classSelect.innerHTML = `<option value="">Сначала выберите школу</option>`;
    studentsSelect.innerHTML = `<option value="">Сначала выберите класс</option>`;

    schoolSelect.onchange = async function () {
        await loadClasses(this.value, 'parentClassSelect', { placeholder: 'Выберите класс' });
        studentsSelect.innerHTML = `<option value="">Сначала выберите класс</option>`;
    };

    classSelect.onchange = async function () {
        await loadStudentsByClass(this.value, 'parentStudentsSelect', { placeholder: 'Выберите ученика' });
    };

    if (!schoolSelect.value && schoolSelect.options.length > 1) {
        schoolSelect.selectedIndex = 1;
        await loadClasses(schoolSelect.value, 'parentClassSelect', { placeholder: 'Выберите класс' });
    }
}

function closeRegisterParentModal() {
    document.getElementById('registerParentModal').style.display = 'none';
    document.getElementById('registerParentForm').reset();
}

async function handleRegisterParent(e) {
    e.preventDefault();

    const schoolId = document.getElementById('parentSchoolSelect').value;
    const selectedStudentIds = Array.from(document.getElementById('parentStudentsSelect').selectedOptions).map(o => Number(o.value));

    if (!schoolId) {
        alert('Выберите школу');
        return;
    }

    if (!selectedStudentIds.length) {
        alert('Выберите хотя бы одного ребёнка');
        return;
    }

    const data = {
        email: document.getElementById('parentEmail').value.trim(),
        passwordHash: document.getElementById('parentPassword').value,
        firstName: document.getElementById('parentFirstName').value.trim(),
        lastName: document.getElementById('parentLastName').value.trim(),
        patronymic: document.getElementById('parentPatronymic').value.trim(),
        schoolId: Number(schoolId),
        studentIds: selectedStudentIds
    };

    try {
        await ApiService.post('/admin/register/parent', data);
        alert('Родитель зарегистрирован');
        closeRegisterParentModal();
        await loadUsers();
        await refreshStructureData();
    } catch (error) {
        console.error('handleRegisterParent error:', error);
        alert('Ошибка регистрации родителя: ' + (error.message || error));
    }
}

/* ===============================
   EDIT USER
================================= */

function openEditUserModal(userId) {
    const user = AdminState.allUsers.find(u => u.id === userId);
    if (!user) {
        alert('Пользователь не найден');
        return;
    }

    document.getElementById('editUserId').value = user.id;
    document.getElementById('editEmail').value = user.email || '';
    document.getElementById('editLastName').value = user.lastName || '';
    document.getElementById('editFirstName').value = user.firstName || '';
    document.getElementById('editPatronymic').value = user.patronymic || '';
    document.getElementById('editPassword').value = '';

    const role = Array.isArray(user.roles) && user.roles.length > 0 ? user.roles[0] : 'student';
    document.getElementById('editRole').value = role;

    document.getElementById('editUserModal').style.display = 'block';
}

function closeEditUserModal() {
    document.getElementById('editUserModal').style.display = 'none';
    document.getElementById('editUserForm').reset();
}

async function handleEditUser(e) {
    e.preventDefault();

    const userId = document.getElementById('editUserId').value;
    const payload = {
        email: document.getElementById('editEmail').value.trim(),
        firstName: document.getElementById('editFirstName').value.trim(),
        lastName: document.getElementById('editLastName').value.trim(),
        patronymic: document.getElementById('editPatronymic').value.trim(),
        password: document.getElementById('editPassword').value,
        role: document.getElementById('editRole').value
    };

    try {
        await ApiService.put(`/admin/users/${userId}`, payload);
        alert('Пользователь обновлён');
        closeEditUserModal();
        await loadUsers();
        await refreshStructureData();
    } catch (error) {
        console.error('handleEditUser error:', error);
        alert('Ошибка обновления пользователя: ' + (error.message || error));
    }
}

/* ===============================
   ORGANIZATION BLOCK
================================= */

async function initOrganizationBlock() {
    const organizationSchoolSelect = document.getElementById('organizationSchoolSelect');
    if (!organizationSchoolSelect) return;

    organizationSchoolSelect.addEventListener('change', async function () {
        AdminState.selectedSchoolId = this.value ? Number(this.value) : null;
        await refreshStructureData();
    });

    if (AdminState.selectedSchoolId) {
        await refreshStructureData();
    }
}

async function refreshStructureData() {
    if (!AdminState.selectedSchoolId) {
        renderEmptyOrganizationState();
        return;
    }

    hideStructureMessages();

    try {
        await Promise.all([
            loadManagedClasses(),
            loadSchoolStudents(),
            loadUnassignedStudents(),
            loadAvailableParents(),
            loadParentChildLinks()
        ]);
        showStructureInfo('Данные структуры школы обновлены');
    } catch (e) {
        console.error('refreshStructureData error:', e);
        showStructureError('Ошибка загрузки структуры школы: ' + (e.message || e));
    }
}

function renderEmptyOrganizationState() {
    document.getElementById('classesManagementList').innerHTML =
        `<div class="empty-state">Выберите школу, чтобы загрузить классы</div>`;
    document.getElementById('classDetailsContainer').innerHTML =
        `Выберите класс, чтобы увидеть его состав`;
    document.getElementById('parentChildLinksList').innerHTML =
        `<div class="empty-state">Выберите школу, чтобы загрузить связи</div>`;
}

async function loadManagedClasses() {
    const classes = await ApiService.get(`/admin/structure/schools/${AdminState.selectedSchoolId}/classes`);
    AdminState.classes = Array.isArray(classes) ? classes : [];

    renderClassesManagementList();
    fillManagedClassSelects();
}

function renderClassesManagementList() {
    const container = document.getElementById('classesManagementList');

    if (!AdminState.classes.length) {
        container.innerHTML = `<div class="empty-state">Для этой школы пока нет классов</div>`;
        return;
    }

    container.innerHTML = AdminState.classes.map(item => `
        <div class="management-item">
            <div class="management-item-head">
                <div>
                    <div class="management-item-title">${escapeHtml(item.name)} ${item.academicYear ? `<span class="status-pill">${escapeHtml(item.academicYear)}</span>` : ''}</div>
                    <div class="management-item-meta">
                        <span>Школа: ${escapeHtml(item.schoolName || '—')}</span>
                        <span>Учеников: ${item.studentsCount ?? 0}</span>
                        ${item.active ? '<span class="status-pill">Активный</span>' : '<span class="status-pill inactive">Архивный</span>'}
                    </div>
                </div>

                <div class="management-item-actions">
                    <button class="btn-action btn-secondary btn-small" onclick="selectClassDetails(${item.id})">
                        <i class="fas fa-users"></i> Состав
                    </button>
                    <button class="btn-action btn-secondary btn-small" onclick="openEditClassModal(${item.id})">
                        <i class="fas fa-edit"></i> Редактировать
                    </button>
                    ${item.active ? `
                    <button class="btn-action btn-secondary btn-small" onclick="archiveClass(${item.id})">
                        <i class="fas fa-box-archive"></i> Архивировать
                    </button>` : ''}
                </div>
            </div>
        </div>
    `).join('');
}

function fillManagedClassSelects() {
    const classDetailsSelect = document.getElementById('classDetailsSelect');
    const assignTargetClassSelect = document.getElementById('assignTargetClassSelect');
    const transferTargetClassSelect = document.getElementById('transferTargetClassSelect');

    const activeClasses = AdminState.classes.filter(c => c.active !== false);

    const options = `<option value="">Выберите класс</option>` + activeClasses.map(c => {
        const label = c.academicYear ? `${c.name} (${c.academicYear})` : c.name;
        return `<option value="${c.id}">${escapeHtml(label)}</option>`;
    }).join('');

    classDetailsSelect.innerHTML = options;
    assignTargetClassSelect.innerHTML = options;
    transferTargetClassSelect.innerHTML = options;
}

async function loadSelectedClassDetails() {
    const classId = document.getElementById('classDetailsSelect').value;
    if (!classId) {
        document.getElementById('classDetailsContainer').innerHTML = 'Выберите класс, чтобы увидеть его состав';
        return;
    }

    await loadClassDetails(Number(classId));
}

async function selectClassDetails(classId) {
    const select = document.getElementById('classDetailsSelect');
    select.value = String(classId);
    await loadClassDetails(classId);
}

async function loadClassDetails(classId) {
    try {
        const details = await ApiService.get(`/admin/structure/classes/${classId}`);
        AdminState.classDetails = details;
        renderClassDetails();
    } catch (e) {
        console.error('loadClassDetails error:', e);
        document.getElementById('classDetailsContainer').innerHTML =
            `<div class="empty-state">Ошибка загрузки состава класса</div>`;
    }
}

function renderClassDetails() {
    const container = document.getElementById('classDetailsContainer');
    const details = AdminState.classDetails;

    if (!details || !details.schoolClass) {
        container.innerHTML = `<div class="empty-state">Нет данных по классу</div>`;
        return;
    }

    const cls = details.schoolClass;
    const students = Array.isArray(details.students) ? details.students : [];

    container.innerHTML = `
        <div style="margin-bottom:12px;">
            <div style="font-size:18px;font-weight:700;color:#111827;">
                ${escapeHtml(cls.name)} ${cls.academicYear ? `<span class="status-pill">${escapeHtml(cls.academicYear)}</span>` : ''}
            </div>
            <div class="management-item-meta" style="margin-top:6px;">
                <span>Школа: ${escapeHtml(cls.schoolName || '—')}</span>
                <span>${cls.active ? 'Активный класс' : 'Архивный класс'}</span>
                <span>Учеников: ${students.length}</span>
            </div>
        </div>

        ${students.length ? `
        <div class="data-table-wrap">
            <table class="data-table">
                <thead>
                    <tr>
                        <th>Ученик</th>
                        <th>Email</th>
                        <th>Школа</th>
                    </tr>
                </thead>
                <tbody>
                    ${students.map(student => `
                        <tr>
                            <td>${escapeHtml(student.fullName || '—')}</td>
                            <td>${escapeHtml(student.email || '—')}</td>
                            <td>${escapeHtml(student.schoolName || '—')}</td>
                        </tr>
                    `).join('')}
                </tbody>
            </table>
        </div>` : `
        <div class="empty-state">В этом классе пока нет учеников</div>`}
    `;
}

async function loadSchoolStudents() {
    const students = await ApiService.get(`/admin/structure/schools/${AdminState.selectedSchoolId}/students`);
    AdminState.allSchoolStudents = Array.isArray(students) ? students : [];

    const transferStudentSelect = document.getElementById('transferStudentSelect');
    transferStudentSelect.innerHTML =
        `<option value="">Выберите ученика</option>` +
        AdminState.allSchoolStudents.map(student => {
            const suffix = student.className ? ` — текущий класс: ${student.className}` : ' — без класса';
            return `<option value="${student.studentId}">${escapeHtml(student.fullName || 'Без имени')}${escapeHtml(suffix)}</option>`;
        }).join('');
}

async function loadUnassignedStudents() {
    const students = await ApiService.get(`/admin/structure/schools/${AdminState.selectedSchoolId}/students/unassigned`);
    AdminState.unassignedStudents = Array.isArray(students) ? students : [];

    const assignStudentSelect = document.getElementById('assignStudentSelect');
    if (!AdminState.unassignedStudents.length) {
        assignStudentSelect.innerHTML = `<option value="">Нет учеников без класса</option>`;
        return;
    }

    assignStudentSelect.innerHTML =
        `<option value="">Выберите ученика</option>` +
        AdminState.unassignedStudents.map(student => `
            <option value="${student.studentId}">${escapeHtml(student.fullName || 'Без имени')}</option>
        `).join('');
}

async function loadAvailableParents() {
    const parents = await ApiService.get(`/admin/structure/schools/${AdminState.selectedSchoolId}/parents`);
    AdminState.availableParents = Array.isArray(parents) ? parents : [];

    const linkParentSelect = document.getElementById('linkParentSelect');
    linkParentSelect.innerHTML =
        `<option value="">Выберите родителя</option>` +
        AdminState.availableParents.map(parent => `
            <option value="${parent.parentId}">${escapeHtml(parent.parentName || 'Без имени')} — ${escapeHtml(parent.parentEmail || '—')}</option>
        `).join('');

    const linkStudentSelect = document.getElementById('linkStudentSelect');
    linkStudentSelect.innerHTML =
        `<option value="">Выберите ученика</option>` +
        AdminState.allSchoolStudents.map(student => `
            <option value="${student.studentId}">${escapeHtml(student.fullName || 'Без имени')}${student.className ? ` — ${escapeHtml(student.className)}` : ''}</option>
        `).join('');
}

async function loadParentChildLinks() {
    const links = await ApiService.get(`/admin/structure/schools/${AdminState.selectedSchoolId}/parent-child-links`);
    AdminState.parentChildLinks = Array.isArray(links) ? links : [];
    renderParentChildLinks();
}

function renderParentChildLinks() {
    const container = document.getElementById('parentChildLinksList');

    if (!AdminState.parentChildLinks.length) {
        container.innerHTML = `<div class="empty-state">Для этой школы пока нет parent-child связей</div>`;
        return;
    }

    container.innerHTML = AdminState.parentChildLinks.map(link => `
        <div class="management-item">
            <div class="management-item-head">
                <div>
                    <div class="management-item-title">${escapeHtml(link.parentName || 'Родитель')} → ${escapeHtml(link.studentName || 'Ученик')}</div>
                    <div class="management-item-meta">
                        <span>Родитель: ${escapeHtml(link.parentEmail || '—')}</span>
                        <span>Ученик: ${escapeHtml(link.studentEmail || '—')}</span>
                        <span>Класс: ${escapeHtml(link.className || '—')}</span>
                    </div>
                </div>

                <div class="management-item-actions">
                    <button class="btn-action btn-secondary btn-small" onclick="unlinkParentFromStudent(${link.parentId}, ${link.studentId})">
                        <i class="fas fa-unlink"></i> Отвязать
                    </button>
                </div>
            </div>
        </div>
    `).join('');
}

async function assignStudentToClass() {
    const studentId = document.getElementById('assignStudentSelect').value;
    const classId = document.getElementById('assignTargetClassSelect').value;

    if (!studentId || !classId) {
        showStructureError('Выберите ученика и класс для назначения');
        return;
    }

    try {
        await ApiService.put('/admin/structure/students/assign-class', {
            studentId: Number(studentId),
            classId: Number(classId)
        });

        showStructureSuccess('Ученик успешно назначен в класс');
        await refreshStructureData();
    } catch (e) {
        console.error('assignStudentToClass error:', e);
        showStructureError('Ошибка назначения ученика: ' + (e.message || e));
    }
}

async function transferStudentToClass() {
    const studentId = document.getElementById('transferStudentSelect').value;
    const toClassId = document.getElementById('transferTargetClassSelect').value;

    if (!studentId || !toClassId) {
        showStructureError('Выберите ученика и целевой класс для перевода');
        return;
    }

    try {
        await ApiService.put('/admin/structure/students/transfer-class', {
            studentId: Number(studentId),
            toClassId: Number(toClassId)
        });

        showStructureSuccess('Ученик успешно переведён');
        await refreshStructureData();
    } catch (e) {
        console.error('transferStudentToClass error:', e);
        showStructureError('Ошибка перевода ученика: ' + (e.message || e));
    }
}

async function linkParentToStudent() {
    const parentId = document.getElementById('linkParentSelect').value;
    const studentId = document.getElementById('linkStudentSelect').value;

    if (!parentId || !studentId) {
        showStructureError('Выберите родителя и ученика для привязки');
        return;
    }

    try {
        await ApiService.post('/admin/structure/parent-child/link', {
            parentId: Number(parentId),
            studentId: Number(studentId)
        });

        showStructureSuccess('Родитель успешно привязан к ребёнку');
        await refreshStructureData();
    } catch (e) {
        console.error('linkParentToStudent error:', e);
        showStructureError('Ошибка привязки: ' + (e.message || e));
    }
}

async function unlinkParentFromStudent(parentId, studentId) {
    if (!confirm('Удалить связь родителя и ребёнка?')) {
        return;
    }

    try {
        await ApiService.delete(`/admin/structure/parent-child/link?parentId=${parentId}&studentId=${studentId}`);
        showStructureSuccess('Связь parent-child удалена');
        await refreshStructureData();
    } catch (e) {
        console.error('unlinkParentFromStudent error:', e);
        showStructureError('Ошибка удаления связи: ' + (e.message || e));
    }
}

/* ===============================
   CLASS MODAL
================================= */

function openCreateClassModal() {
    document.getElementById('classModalTitle').innerHTML = '<i class="fas fa-layer-group"></i> Новый класс';
    document.getElementById('classEditId').value = '';
    document.getElementById('classForm').reset();
    document.getElementById('classActiveGroup').style.display = 'none';

    fillSchoolSelect('classSchoolSelect', AdminState.schools, 'Выберите школу');
    if (AdminState.selectedSchoolId) {
        document.getElementById('classSchoolSelect').value = String(AdminState.selectedSchoolId);
    }

    document.getElementById('classModal').style.display = 'block';
}

function openEditClassModal(classId) {
    const item = AdminState.classes.find(c => c.id === classId);
    if (!item) {
        showStructureError('Класс не найден');
        return;
    }

    document.getElementById('classModalTitle').innerHTML = '<i class="fas fa-edit"></i> Редактирование класса';
    document.getElementById('classEditId').value = item.id;
    document.getElementById('classNameInput').value = item.name || '';
    document.getElementById('classAcademicYearInput').value = item.academicYear || '';
    document.getElementById('classActiveGroup').style.display = 'block';
    document.getElementById('classActiveSelect').value = String(item.active !== false);

    fillSchoolSelect('classSchoolSelect', AdminState.schools, 'Выберите школу');
    document.getElementById('classSchoolSelect').value = String(item.schoolId || AdminState.selectedSchoolId || '');

    document.getElementById('classModal').style.display = 'block';
}

function closeClassModal() {
    document.getElementById('classModal').style.display = 'none';
    document.getElementById('classForm').reset();
}

async function handleSaveClass(e) {
    e.preventDefault();

    const editId = document.getElementById('classEditId').value;
    const schoolId = document.getElementById('classSchoolSelect').value;
    const name = document.getElementById('classNameInput').value.trim();
    const academicYear = document.getElementById('classAcademicYearInput').value.trim();

    if (!schoolId || !name || !academicYear) {
        showStructureError('Заполните школу, название класса и учебный год');
        return;
    }

    try {
        if (editId) {
            await ApiService.put(`/admin/structure/classes/${editId}`, {
                name,
                academicYear,
                active: document.getElementById('classActiveSelect').value === 'true'
            });
            showStructureSuccess('Класс успешно обновлён');
        } else {
            await ApiService.post('/admin/structure/classes', {
                schoolId: Number(schoolId),
                name,
                academicYear
            });
            showStructureSuccess('Класс успешно создан');
        }

        closeClassModal();

        if (AdminState.selectedSchoolId !== Number(schoolId)) {
            AdminState.selectedSchoolId = Number(schoolId);
            document.getElementById('organizationSchoolSelect').value = String(schoolId);
        }

        await refreshStructureData();
    } catch (e2) {
        console.error('handleSaveClass error:', e2);
        showStructureError('Ошибка сохранения класса: ' + (e2.message || e2));
    }
}

async function archiveClass(classId) {
    const item = AdminState.classes.find(c => c.id === classId);
    if (!item) return;

    if (!confirm(`Архивировать класс ${item.name}?`)) {
        return;
    }

    try {
        await ApiService.put(`/admin/structure/classes/${classId}/archive`, {});
        showStructureSuccess('Класс архивирован');
        await refreshStructureData();
    } catch (e) {
        console.error('archiveClass error:', e);
        showStructureError('Ошибка архивирования класса: ' + (e.message || e));
    }
}

/* ===============================
   STRUCTURE MESSAGES
================================= */

function hideStructureMessages() {
    document.getElementById('structureSuccessBox').style.display = 'none';
    document.getElementById('structureErrorBox').style.display = 'none';
    document.getElementById('structureInfoBox').style.display = 'none';
}

function showStructureSuccess(message) {
    hideStructureMessages();
    const box = document.getElementById('structureSuccessBox');
    box.textContent = message;
    box.style.display = 'block';
}

function showStructureError(message) {
    hideStructureMessages();
    const box = document.getElementById('structureErrorBox');
    box.textContent = message;
    box.style.display = 'block';
}

function showStructureInfo(message) {
    hideStructureMessages();
    const box = document.getElementById('structureInfoBox');
    box.textContent = message;
    box.style.display = 'block';
}

/* ===============================
   UTILS
================================= */

function formatDateTime(value) {
    try {
        const date = new Date(value);
        if (Number.isNaN(date.getTime())) return '—';
        return date.toLocaleString('ru-RU');
    } catch {
        return '—';
    }
}

function escapeHtml(value) {
    if (value === null || value === undefined) return '';
    return String(value)
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;')
        .replace(/'/g, '&#039;');
}