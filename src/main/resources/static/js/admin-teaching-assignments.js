const AdminTeachingPage = {
    token: localStorage.getItem('token'),
    schools: [],
    teachers: [],
    classes: [],
    subjects: [],
    assignments: [],
    selectedSchoolId: null
};

function logout() {
    localStorage.clear();
    window.location.href = '/login.html';
}
window.logout = logout;

function escapeHtml(str) {
    if (str === null || str === undefined) return '';
    return String(str)
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;')
        .replace(/'/g, '&#039;');
}

function showMessage(text, type = 'success') {
    const el = document.getElementById('pageMessage');
    el.className = `message ${type}`;
    el.textContent = text;
    el.style.display = 'block';

    setTimeout(() => {
        el.style.display = 'none';
    }, 4000);
}

async function loadSchools() {
    AdminTeachingPage.schools = await ApiService.get('/schools');

    const select = document.getElementById('schoolSelect');
    select.innerHTML = '<option value="">Выберите школу</option>';

    AdminTeachingPage.schools.forEach(school => {
        const option = document.createElement('option');
        option.value = school.id;
        option.textContent = school.name;
        select.appendChild(option);
    });
}

async function loadSubjects() {
    AdminTeachingPage.subjects = await ApiService.get('/subjects');

    const select = document.getElementById('subjectSelect');
    select.innerHTML = '<option value="">Выберите предмет</option>';

    AdminTeachingPage.subjects.forEach(subject => {
        const option = document.createElement('option');
        option.value = subject.id;
        option.textContent = subject.name;
        select.appendChild(option);
    });
}

async function loadSchoolRelatedData() {
    if (!AdminTeachingPage.selectedSchoolId) {
        document.getElementById('teacherSelect').innerHTML = '<option value="">Сначала выбери школу</option>';
        document.getElementById('classSelect').innerHTML = '<option value="">Сначала выбери школу</option>';
        document.getElementById('assignmentsList').innerHTML = '<div class="empty">Выберите школу</div>';
        return;
    }

    const schoolId = Number(AdminTeachingPage.selectedSchoolId);

    const [teachers, classes, assignments] = await Promise.all([
        ApiService.get(`/admin/teaching-assignments/school/${schoolId}/teachers`),
        ApiService.get(`/admin/structure/schools/${schoolId}/classes`),
        ApiService.get(`/admin/teaching-assignments/school/${schoolId}`)
    ]);

    AdminTeachingPage.teachers = Array.isArray(teachers) ? teachers : [];
    AdminTeachingPage.classes = Array.isArray(classes) ? classes : [];
    AdminTeachingPage.assignments = Array.isArray(assignments) ? assignments : [];

    fillTeacherSelect();
    fillClassSelect();
    renderAssignments();
}

function fillTeacherSelect() {
    const select = document.getElementById('teacherSelect');
    select.innerHTML = '<option value="">Выберите учителя</option>';

    AdminTeachingPage.teachers.forEach(teacher => {
        const option = document.createElement('option');
        option.value = teacher.teacherId;
        option.textContent = teacher.email
            ? `${teacher.fullName} — ${teacher.email}`
            : teacher.fullName;
        select.appendChild(option);
    });
}

function fillClassSelect() {
    const select = document.getElementById('classSelect');
    select.innerHTML = '<option value="">Выберите класс</option>';

    AdminTeachingPage.classes.forEach(cls => {
        const option = document.createElement('option');
        option.value = cls.id;
        option.textContent = cls.academicYear
            ? `${cls.name} (${cls.academicYear})`
            : cls.name;
        select.appendChild(option);
    });
}

function renderAssignments() {
    const container = document.getElementById('assignmentsList');

    if (!AdminTeachingPage.assignments.length) {
        container.innerHTML = '<div class="empty">Для этой школы пока нет назначений</div>';
        return;
    }

    container.innerHTML = AdminTeachingPage.assignments.map(item => `
        <div class="item">
            <div class="item-head">
                <div>
                    <div style="font-weight:700;font-size:16px;">
                        ${escapeHtml(item.teacherFullName || '—')}
                    </div>
                    <div class="meta">
                        <span>Класс: ${escapeHtml(item.className || '—')}</span>
                        <span>Год: ${escapeHtml(item.academicYear || '—')}</span>
                        <span>Предмет: ${escapeHtml(item.subjectName || '—')}</span>
                        <span>Статус: ${item.active ? 'Активно' : 'Неактивно'}</span>
                    </div>
                </div>

                ${item.active ? `
                    <button class="btn btn-danger" onclick="deactivateTeachingAssignment(${item.id})">
                        Снять
                    </button>
                ` : ''}
            </div>
        </div>
    `).join('');
}

async function createTeachingAssignment() {
    const teacherId = document.getElementById('teacherSelect').value;
    const classId = document.getElementById('classSelect').value;
    const subjectId = document.getElementById('subjectSelect').value;

    if (!teacherId || !classId || !subjectId) {
        showMessage('Выбери учителя, класс и предмет', 'error');
        return;
    }

    try {
        await ApiService.post('/admin/teaching-assignments', {
            teacherId: Number(teacherId),
            classId: Number(classId),
            subjectId: Number(subjectId)
        });

        showMessage('Назначение создано');
        await loadSchoolRelatedData();
    } catch (e) {
        showMessage(e.message || 'Ошибка создания назначения', 'error');
    }
}

async function deactivateTeachingAssignment(id) {
    try {
        await ApiService.delete(`/admin/teaching-assignments/${id}`);
        showMessage('Назначение снято');
        await loadSchoolRelatedData();
    } catch (e) {
        showMessage(e.message || 'Ошибка снятия назначения', 'error');
    }
}
window.deactivateTeachingAssignment = deactivateTeachingAssignment;

document.addEventListener('DOMContentLoaded', async () => {
    if (!AdminTeachingPage.token) {
        window.location.href = '/login.html';
        return;
    }

    try {
        await Promise.all([
            loadSchools(),
            loadSubjects()
        ]);

        document.getElementById('schoolSelect').addEventListener('change', async (e) => {
            AdminTeachingPage.selectedSchoolId = e.target.value || null;
            await loadSchoolRelatedData();
        });

        document.getElementById('createBtn').addEventListener('click', createTeachingAssignment);
        document.getElementById('refreshBtn').addEventListener('click', loadSchoolRelatedData);
    } catch (e) {
        showMessage(e.message || 'Ошибка инициализации страницы', 'error');
    }
});