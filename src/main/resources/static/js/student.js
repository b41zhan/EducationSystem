document.addEventListener('DOMContentLoaded', function() {
    console.log('=== STUDENT DASHBOARD LOADING ===');
    initializeStudentDashboard();
});

// =====================================================
//  ГЛОБАЛЬНЫЕ МАССИВЫ ДЛЯ ФИЛЬТРА
// =====================================================

let ACTIVE_LIST = [];
let OVERDUE_LIST = [];

const studentStats = {
    totalAssignments: 0,
    completedAssignments: 0,
    overdueAssignments: 0,
    averageGrade: 0
};

// =====================================================
//  ИНИЦИАЛИЗАЦИЯ ДАШБОРДА
// =====================================================

async function initializeStudentDashboard() {
    try {
        await loadStudentInfo();
        await loadStudentAssignments();
        await loadStudentGrades();
        await loadProgressPreview();
        setupFileUpload();
        setupActiveTasksFilter(); // ← ДОБАВИЛИ!
    } catch (error) {
        console.error('Error initializing dashboard:', error);
    }
}

// =====================================================
//  ГРУЗИМ ИНФУ О СТУДЕНТЕ
// =====================================================

async function loadStudentInfo() {
    try {
        const userData = await ApiService.get('/auth/me');
        document.getElementById('welcome-message').textContent =
            `Добро пожаловать, ${userData.firstName} ${userData.lastName}!`;

        const studentData = await ApiService.get('/students/me');

        if (studentData && studentData.schoolClass) {
            document.getElementById('student-class').textContent =
                `Класс: ${studentData.schoolClass.name}`;
            window.studentClassId = studentData.schoolClass.id;
        } else {
            document.getElementById('student-class').textContent = 'Класс: Не назначен';
        }

    } catch (error) {
        console.error('Error loading student info:', error);
    }
}

// =====================================================
//   ГРУЗИМ ВСЕ ЗАДАНИЯ И РАЗБИРАЕМ ПО КАТЕГОРИЯМ
// =====================================================

async function loadStudentAssignments() {
    try {
        const assignmentsList = document.getElementById('active-assignments');
        assignmentsList.innerHTML = `<p>Загрузка...</p>`;

        const assignmentSelect = document.getElementById('assignmentSelect');
        assignmentSelect.innerHTML = '<option>Загрузка...</option>';

        const [assignments, submissions] = await Promise.all([
            ApiService.get('/students/assignments/my-class'),
            ApiService.get('/submissions/my')
        ]);

        const allAssignments = assignments || [];
        const allSubmissions = submissions || [];

        const submissionsByAssignment = {};
        allSubmissions.forEach(s => {
            submissionsByAssignment[s.assignmentId] = s;
        });

        const now = new Date();
        const toSubmit = [];
        const active = [];
        const completed = [];
        const overdue = [];

        allAssignments.forEach(a => {
            const deadline = a.deadline ? new Date(a.deadline) : null;
            const sub = submissionsByAssignment[a.id];

            // Проверено
            if (sub && (sub.grade != null || sub.status === 'graded')) {
                completed.push({ assignment: a, submission: sub });
                return;
            }

            // Просрочено
            if (!sub && deadline && deadline < now) {
                overdue.push({ assignment: a, submission: null });
                return;
            }

            // Всё остальное – активные
            active.push({ assignment: a, submission: sub });

            // только несданные — в "Сдать задание"
            if (!sub) toSubmit.push({ assignment: a, submission: null });
        });

        // Запоминаем для фильтра
        ACTIVE_LIST = active;
        OVERDUE_LIST = overdue;

        updateAssignmentDropdown(toSubmit.map(t => t.assignment));

        // Показываем активные по умолчанию
        displayAssignments(ACTIVE_LIST);

        // Статистика
        studentStats.totalAssignments = allAssignments.length;
        studentStats.overdueAssignments = overdue.length;
        updateStats();

    } catch (error) {
        console.error('Error loading assignments:', error);
    }
}

// =====================================================
//    ФИЛЬТР "АКТИВНЫЕ / ПРОСРОЧЕННЫЕ"
// =====================================================

function setupActiveTasksFilter() {
    const filter = document.getElementById('activeTasksFilter');
    if (!filter) return;

    filter.addEventListener('change', (e) => {
        if (e.target.value === 'active') {
            displayAssignments(ACTIVE_LIST);
        } else if (e.target.value === 'overdue') {
            displayAssignments(OVERDUE_LIST);
        }
    });
}

// =====================================================
//  ОТОБРАЖЕНИЕ АКТИВНЫХ / ПРОСРОЧЕННЫХ
// =====================================================

function displayAssignments(items) {
    const container = document.getElementById('active-assignments');
    if (!container) return;

    if (!items || items.length === 0) {
        container.innerHTML = `
            <div class="no-assignments">
                <h3>Нет заданий</h3>
            </div>`;
        document.getElementById('assignments-count').textContent = "0";
        return;
    }

    container.innerHTML = "";
    document.getElementById('assignments-count').textContent = String(items.length);

    const now = new Date();

    items.forEach(item => {
        const a = item.assignment;
        const sub = item.submission;
        const deadline = a.deadline ? new Date(a.deadline) : null;

        let statusClass = "active";
        let statusText = "Активно";

        if (sub && sub.status === "submitted") {
            statusClass = "submitted";
            statusText = "Отправлено";
        }

        if (!sub && deadline && deadline < now) {
            statusClass = "overdue";
            statusText = "Просрочено";
        }

        container.innerHTML += `
            <div class="assignment-item ${statusClass}">
                <div class="assignment-header">
                    <h3>${a.title}</h3>
                    <div class="assignment-status ${statusClass}">
                        ${statusText}
                    </div>
                </div>
                <div class="assignment-body">
                    <p>${a.description || ""}</p>
                    <p><b>Дедлайн:</b> ${deadline ? deadline.toLocaleDateString('ru-RU') : "Нет"}</p>
                    ${sub ? `<p><b>Отправлено:</b> ${new Date(sub.submittedAt).toLocaleString('ru-RU')}</p>` : ""}
                </div>
            </div>
        `;
    });
}

function updateAssignmentDropdown(assignments) {
    const select = document.getElementById('assignmentSelect');
    if (!select) return;

    select.innerHTML = '<option value="">Выберите задание</option>';

    if (assignments && assignments.length > 0) {
        const now = new Date();
        const activeAssignments = assignments.filter(a => !a.deadline || new Date(a.deadline) > now);

        if (activeAssignments.length === 0) {
            select.innerHTML = '<option value="">Нет активных заданий</option>';
            return;
        }

        activeAssignments.forEach(assignment => {
            const deadline = assignment.deadline ? new Date(assignment.deadline) : null;
            const option = document.createElement('option');
            option.value = assignment.id;
            const deadlineText = deadline
                ? deadline.toLocaleDateString('ru-RU')
                : 'без дедлайна';
            option.textContent = `${assignment.title} (до ${deadlineText})`;
            select.appendChild(option);
        });
    } else {
        select.innerHTML = '<option value="">Нет доступных заданий</option>';
    }
}

function updateStats() {
    document.getElementById('total-assignments').textContent =
        String(studentStats.totalAssignments || 0);
    document.getElementById('overdue-assignments').textContent =
        String(studentStats.overdueAssignments || 0);
    document.getElementById('completed-assignments').textContent =
        String(studentStats.completedAssignments || 0);
    document.getElementById('average-grade').textContent =
        String(studentStats.averageGrade || 0);
}

function getAssignmentType(type) {
    const types = {
        HOMEWORK: 'Домашнее задание',
        CLASSWORK: 'Классная работа',
        TEST: 'Тест',
        PROJECT: 'Проект'
    };
    return types[type] || 'Задание';
}

// ============================ ОЦЕНКИ =============================

async function loadStudentGrades() {
    try {
        const gradesList = document.getElementById('grades-list');
        const grades = await ApiService.get('/students/grades');

        if (!grades || grades.length === 0) {
            gradesList.innerHTML = `
                <div class="no-assignments">
                    <i>📊</i>
                    <h3>Нет оценок</h3>
                    <p>Здесь будут отображаться ваши оценки</p>
                </div>
            `;
            studentStats.completedAssignments = 0;
            studentStats.averageGrade = 0;
            updateStats();
            return;
        }

        gradesList.innerHTML = '';
        grades.forEach(grade => {
            const gradeElement = document.createElement('div');
            gradeElement.className = 'grade-item';
            gradeElement.innerHTML = `
                <div class="grade-header">
                    <div class="grade-title">${grade.assignmentTitle || 'Задание'}</div>
                    <div class="grade-value">${grade.grade}/100</div>
                </div>
                <div class="grade-meta">
                    <span><i>📘</i> ${grade.subjectName || ''}</span>
                    <span><i>📅</i> ${grade.gradedAt ? new Date(grade.gradedAt).toLocaleDateString('ru-RU') : ''}</span>
                </div>
                ${grade.comment ? `<div class="grade-comment">${grade.comment}</div>` : ''}
            `;
            gradesList.appendChild(gradeElement);
        });

        studentStats.completedAssignments = grades.length;
        if (grades.length > 0) {
            const average = Math.round(
                grades.reduce((sum, g) => sum + (g.grade || 0), 0) / grades.length
            );
            studentStats.averageGrade = average;
        } else {
            studentStats.averageGrade = 0;
        }
        updateStats();

    } catch (error) {
        console.error('Error loading grades:', error);
        const gradesList = document.getElementById('grades-list');
        if (gradesList) {
            gradesList.innerHTML = `
                <div class="no-assignments">
                    <i>❌</i>
                    <h3>Ошибка загрузки</h3>
                    <p>Не удалось загрузить оценки</p>
                </div>
            `;
        }
    }
}

// ====================== ПРОГРЕСС (геймификация) ==================

async function loadProgressPreview() {
    try {
        const response = await fetch('/api/gamification/student/stats', {
            headers: { 'Authorization': `Bearer ${localStorage.getItem('token')}` }
        });

        if (!response.ok) {
            console.warn('Gamification stats not available');
            return;
        }

        const stats = await response.json();
        const progressPercentage = Math.round((stats.currentLevelXp / stats.nextLevelXp) * 100);

        const progressBar = document.getElementById('xpProgress');
        const currentXpEl = document.getElementById('currentXP');
        const nextLevelXpEl = document.getElementById('nextLevelXP');

        if (progressBar) {
            progressBar.style.width = `${progressPercentage}%`;
        }
        if (currentXpEl) {
            currentXpEl.textContent = stats.currentLevelXp;
        }
        if (nextLevelXpEl) {
            nextLevelXpEl.textContent = stats.nextLevelXp;
        }

    } catch (error) {
        console.error('Error loading progress preview:', error);
    }
}

// ========================= ОТПРАВКА ЗАДАНИЯ ======================

function setupFileUpload() {
    const fileInput = document.getElementById('assignmentFile');
    const fileInfo = document.getElementById('file-info');
    const form = document.getElementById('submitAssignmentForm');
    const submitBtn = document.getElementById('submit-btn');
    const submitMessage = document.getElementById('submit-message');

    if (!fileInput || !form) {
        return;
    }

    fileInput.addEventListener('change', function(e) {
        const file = e.target.files[0];
        if (!fileInfo) return;

        if (file) {
            const fileSizeMB = (file.size / 1024 / 1024).toFixed(2);
            if (fileSizeMB > 10) {
                fileInfo.innerHTML = `
                    <div style="color: var(--danger);">
                        <i>❌</i> Файл слишком большой (${fileSizeMB} MB). Максимум: 10MB
                    </div>
                `;
                fileInput.value = '';
                return;
            }

            fileInfo.innerHTML = `
                <div>
                    <i>📎</i> ${file.name} (${fileSizeMB} MB)
                </div>
            `;
        } else {
            fileInfo.innerHTML = '';
        }
    });

    form.addEventListener('submit', async function(e) {
        e.preventDefault();

        const assignmentId = document.getElementById('assignmentSelect').value;
        const comment = document.getElementById('assignmentComment').value || '';

        if (!assignmentId) {
            submitMessage.innerHTML = `
                <div class="form-message error">
                    <i>⚠️</i> Пожалуйста, выберите задание
                </div>
            `;
            submitMessage.style.display = 'block';
            return;
        }

        if (!fileInput.files || !fileInput.files[0]) {
            submitMessage.innerHTML = `
                <div class="form-message error">
                    <i>⚠️</i> Пожалуйста, выберите файл
                </div>
            `;
            submitMessage.style.display = 'block';
            return;
        }

        await submitAssignment(assignmentId, fileInput.files[0], comment, submitBtn, submitMessage);
    });
}

async function submitAssignment(assignmentId, file, comment = '', submitBtn, submitMessage) {
    if (!submitBtn || !submitMessage) return;

    submitBtn.disabled = true;
    submitBtn.innerHTML = '<i>⏳</i> Отправка...';

    try {
        const formData = new FormData();
        formData.append('file', file);

        const uploadResponse = await fetch('/api/files/upload/submission', {
            method: 'POST',
            headers: {
                'Authorization': `Bearer ${localStorage.getItem('token')}`
            },
            body: formData
        });

        const uploadResult = await uploadResponse.json();

        if (!uploadResponse.ok) {
            throw new Error(uploadResult.error || 'Ошибка загрузки файла');
        }

        if (!uploadResult.filePath) {
            throw new Error('Не удалось получить путь к файлу');
        }

        const submissionData = {
            assignmentId: parseInt(assignmentId),
            filePath: uploadResult.filePath,
            fileName: uploadResult.fileName,
            fileSize: uploadResult.fileSize,
            comment: comment
        };

        await ApiService.post('/submissions', submissionData);

        submitMessage.innerHTML = `
            <div class="form-message success">
                <i>✅</i> Задание успешно отправлено!
            </div>
        `;
        submitMessage.style.display = 'block';

        // Сброс формы
        document.getElementById('submitAssignmentForm').reset();
        const fileInfo = document.getElementById('file-info');
        if (fileInfo) fileInfo.innerHTML = '';

        // Перезагрузка заданий и оценок
        await loadStudentAssignments();
        await loadStudentGrades();

        setTimeout(() => {
            submitMessage.style.display = 'none';
        }, 5000);

    } catch (error) {
        console.error('Submission error:', error);
        submitMessage.innerHTML = `
            <div class="form-message error">
                <i>❌</i> Ошибка: ${error.message}
            </div>
        `;
        submitMessage.style.display = 'block';
    } finally {
        submitBtn.disabled = false;
        submitBtn.innerHTML = '📤 Сдать задание';
    }
}

// ============================ ПРОЧЕЕ =============================

function logout() {
    localStorage.removeItem('token');
    localStorage.removeItem('userId');
    localStorage.removeItem('userRole');
    window.location.href = '/login.html';
}

// Для отладки из консоли
window.debugStudent = async function() {
    console.log('=== STUDENT DEBUG ===');
    try {
        const studentMe = await ApiService.get('/students/me');
        console.log('Student me:', studentMe);

        const assignments = await ApiService.get('/students/assignments/my-class');
        console.log('Assignments:', assignments);

        const submissions = await ApiService.get('/submissions/my');
        console.log('Submissions:', submissions);
    } catch (error) {
        console.error('Debug error:', error);
    }
};
