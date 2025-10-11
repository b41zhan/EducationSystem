document.addEventListener('DOMContentLoaded', function() {
    console.log('=== STUDENT DASHBOARD LOADING ===');
    initializeStudentDashboard();
});

async function initializeStudentDashboard() {
    try {
        await loadStudentInfo();
        await loadStudentAssignments();
        loadStudentGrades();
    } catch (error) {
        console.error('Error initializing dashboard:', error);
    }
}

async function loadStudentInfo() {
    try {
        console.log('Loading student info...');

        const userData = await ApiService.get('/auth/me');
        document.getElementById('welcome-message').textContent =
            `Добро пожаловать, ${userData.firstName} ${userData.lastName}!`;

        const studentData = await ApiService.get('/students/me');
        console.log('Student data:', studentData);

        if (studentData && studentData.schoolClass) {
            document.getElementById('student-class').textContent =
                `Класс: ${studentData.schoolClass.name}`;
            window.studentClassId = studentData.schoolClass.id;
            console.log('Class ID:', window.studentClassId);
        } else {
            document.getElementById('student-class').textContent = 'Класс: Не назначен';
        }

    } catch (error) {
        console.error('Error loading student info:', error);
        document.getElementById('student-class').textContent = 'Класс: Ошибка загрузки';
    }
}

async function loadStudentAssignments() {
    try {
        console.log('Loading student assignments...');

        const assignmentsList = document.getElementById('active-assignments');
        const assignmentSelect = document.getElementById('assignmentSelect');

        assignmentsList.innerHTML = '<p>Загрузка заданий...</p>';
        assignmentSelect.innerHTML = '<option value="">Загрузка...</option>';

        let assignments = [];

        try {
            assignments = await ApiService.get('/students/assignments/my-class');
            console.log('Assignments loaded:', assignments);
        } catch (error) {
            console.log('Primary endpoint failed, trying alternatives...');

            if (window.studentClassId) {
                try {
                    assignments = await ApiService.get(`/assignments/class/${window.studentClassId}`);
                } catch (e) {
                    console.log('Class endpoint failed');
                }
            }

            if (assignments.length === 0) {
                try {
                    const allAssignments = await ApiService.get('/assignments');
                    assignments = allAssignments || [];
                } catch (e) {
                    console.log('All assignments endpoint failed');
                }
            }
        }

        displayAssignments(assignments);
        updateAssignmentDropdown(assignments);
        updateStats(assignments);

    } catch (error) {
        console.error('Error loading assignments:', error);
        document.getElementById('active-assignments').innerHTML =
            '<p>Ошибка загрузки заданий</p>';
        document.getElementById('assignmentSelect').innerHTML =
            '<option value="">Ошибка загрузки</option>';
    }
}

function displayAssignments(assignments) {
    const container = document.getElementById('active-assignments');

    if (!assignments || assignments.length === 0) {
        container.innerHTML = `
            <div class="no-assignments">
                <p>Нет активных заданий</p>
                <small>Все задания выполнены или ожидайте новых от учителя</small>
            </div>
        `;
        return;
    }

    container.innerHTML = '';

    assignments.forEach(assignment => {
        const deadline = new Date(assignment.deadline);
        const now = new Date();
        const isOverdue = deadline < now;

        const assignmentElement = document.createElement('div');
        assignmentElement.className = 'assignment-item';
        assignmentElement.innerHTML = `
            <div class="assignment-title">${assignment.title || 'Без названия'}</div>
            <div class="assignment-meta">
                ${assignment.subjectName ? `Предмет: ${assignment.subjectName} | ` : ''}
                Тип: ${getAssignmentType(assignment.type)} |
                Макс. балл: ${assignment.maxGrade || 'N/A'}
            </div>
            <div class="assignment-description">
                ${assignment.description || 'Описание отсутствует'}
            </div>
            <div class="assignment-deadline ${isOverdue ? 'status-overdue' : ''}">
                📅 Срок: ${deadline.toLocaleDateString('ru-RU')} 
                ${isOverdue ? ' (ПРОСРОЧЕНО)' : ''}
            </div>
            <div class="assignment-status status-${isOverdue ? 'overdue' : 'active'}">
                ${isOverdue ? 'ПРОСРОЧЕНО' : 'АКТИВНО'}
            </div>
        `;
        container.appendChild(assignmentElement);
    });
}

function updateAssignmentDropdown(assignments) {
    const select = document.getElementById('assignmentSelect');
    select.innerHTML = '<option value="">Выберите задание</option>';

    if (assignments && assignments.length > 0) {
        assignments.forEach(assignment => {
            const option = document.createElement('option');
            option.value = assignment.id;
            option.textContent = `${assignment.title} (до ${new Date(assignment.deadline).toLocaleDateString('ru-RU')})`;
            select.appendChild(option);
        });
    } else {
        select.innerHTML = '<option value="">Нет доступных заданий</option>';
    }
}

function updateStats(assignments) {
    if (!assignments) assignments = [];

    const total = assignments.length;
    const overdue = assignments.filter(a => new Date(a.deadline) < new Date()).length;

    document.getElementById('total-assignments').textContent = total;
    document.getElementById('overdue-assignments').textContent = overdue;
    document.getElementById('assignments-count').textContent = `(${total})`;
}

function getAssignmentType(type) {
    const types = {
        'homework': 'Домашнее задание',
        'test': 'Тест',
        'quiz': 'Контрольная',
        'sor': 'СОР',
        'soch': 'СОЧ',
        'HOMEWORK': 'Домашнее задание',
        'TEST': 'Тест'
    };
    return types[type] || type;
}

function loadStudentGrades() {
    const grades = [
        { assignment: 'Математика - ДЗ', grade: 95, comment: 'Отлично!', date: '2024-12-10' },
        { assignment: 'Физика - Тест', grade: 87, comment: 'Хорошо', date: '2024-12-05' },
        { assignment: 'Химия - СОР', grade: 92, comment: 'Отличная работа', date: '2024-11-28' }
    ];

    const container = document.getElementById('grades-list');
    container.innerHTML = '';

    grades.forEach(grade => {
        const element = document.createElement('div');
        element.className = 'assignment-item';
        element.innerHTML = `
            <div class="assignment-title">${grade.assignment}</div>
            <div class="assignment-meta">
                Оценка: <strong>${grade.grade}/100</strong> |
                Дата: ${new Date(grade.date).toLocaleDateString('ru-RU')}
            </div>
            <div>${grade.comment}</div>
        `;
        container.appendChild(element);
    });
}

// ОБНОВЛЕННЫЙ ОБРАБОТЧИК ФОРМЫ - ДОБАВЛЕН КОММЕНТАРИЙ
document.getElementById('submitAssignmentForm').addEventListener('submit', async function(e) {
    e.preventDefault();

    const assignmentId = document.getElementById('assignmentSelect').value;
    const fileInput = document.getElementById('assignmentFile');
    const comment = document.getElementById('assignmentComment').value; // ← ПОЛУЧАЕМ КОММЕНТАРИЙ

    if (!assignmentId) {
        alert('Выберите задание');
        return;
    }

    if (!fileInput.files[0]) {
        alert('Выберите файл');
        return;
    }

    await submitAssignment(assignmentId, fileInput.files[0], comment);
});

// ОБНОВЛЕННАЯ ФУНКЦИЯ submitAssignment - ДОБАВЛЕН ПАРАМЕТР comment
async function submitAssignment(assignmentId, file, comment = '') {
    const btn = document.querySelector('#submitAssignmentForm button');
    btn.disabled = true;
    btn.textContent = 'Отправка...';

    try {
        console.log('Starting file upload...', file);
        console.log('Comment:', comment); // ← ЛОГИРУЕМ КОММЕНТАРИЙ

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
        console.log('Upload result:', uploadResult);

        if (!uploadResponse.ok) {
            throw new Error(uploadResult.error);
        }

        if (!uploadResult.filePath) {
            throw new Error('File path not returned from server');
        }

        const submissionData = {
            assignmentId: parseInt(assignmentId),
            filePath: uploadResult.filePath,
            fileName: uploadResult.fileName,
            fileSize: uploadResult.fileSize,
            comment: comment // ← ПЕРЕДАЕМ КОММЕНТАРИЙ НА БЭКЕНД
        };

        console.log('Submitting data:', submissionData);

        await ApiService.post('/submissions', submissionData);

        alert('✅ Задание успешно сдано!');
        document.getElementById('submitAssignmentForm').reset();

        await loadStudentAssignments(); // Обновляем список заданий

    } catch (error) {
        console.error('Submission error:', error);
        alert('❌ Ошибка: ' + error.message);
    } finally {
        btn.disabled = false;
        btn.textContent = 'Сдать задание';
    }
}

function logout() {
    localStorage.removeItem('token');
    window.location.href = '/login.html';
}

window.debugStudent = async function() {
    console.log('=== STUDENT DEBUG ===');

    try {
        const studentMe = await ApiService.get('/students/me');
        console.log('Student me:', studentMe);

        const assignments = await ApiService.get('/students/assignments/my-class');
        console.log('Assignments:', assignments);
    } catch (error) {
        console.error('Debug error:', error);
    }
};
