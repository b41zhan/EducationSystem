document.addEventListener('DOMContentLoaded', function() {
    console.log('=== STUDENT DASHBOARD LOADING ===');
    initializeStudentDashboard();
});

async function initializeStudentDashboard() {
    try {
        await loadStudentInfo();
        await loadStudentAssignments();
        await loadStudentGrades();
        await loadProgressPreview();
        setupFileUpload();
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

        assignmentsList.innerHTML = `
            <div class="loading-state">
                <i>⏳</i>
                <p>Загрузка заданий...</p>
            </div>
        `;
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
        document.getElementById('active-assignments').innerHTML = `
            <div class="no-assignments">
                <i>❌</i>
                <h3>Ошибка загрузки</h3>
                <p>Не удалось загрузить задания</p>
            </div>
        `;
        document.getElementById('assignmentSelect').innerHTML =
            '<option value="">Ошибка загрузки</option>';
    }
}

function displayAssignments(assignments) {
    const container = document.getElementById('active-assignments');

    if (!assignments || assignments.length === 0) {
        container.innerHTML = `
            <div class="no-assignments">
                <i>🎉</i>
                <h3>Нет активных заданий</h3>
                <p>Все задания выполнены или ожидайте новых от учителя</p>
            </div>
        `;
        return;
    }

    // Сортируем по дедлайну (сначала ближайшие)
    assignments.sort((a, b) => new Date(a.deadline) - new Date(b.deadline));

    container.innerHTML = '';

    assignments.forEach(assignment => {
        const deadline = new Date(assignment.deadline);
        const now = new Date();
        const isOverdue = deadline < now;
        const daysLeft = Math.ceil((deadline - now) / (1000 * 60 * 60 * 24));

        let status = 'active';
        let statusText = 'АКТИВНО';

        if (isOverdue) {
            status = 'overdue';
            statusText = 'ПРОСРОЧЕНО';
        } else if (daysLeft <= 2) {
            status = 'warning';
            statusText = 'СКОРО СРОК';
        }

        const assignmentElement = document.createElement('div');
        assignmentElement.className = 'assignment-item';
        assignmentElement.innerHTML = `
            <div class="assignment-header">
                <div class="assignment-title">${assignment.title || 'Без названия'}</div>
                <div class="assignment-status status-${status}">${statusText}</div>
            </div>
            <div class="assignment-meta">
                <span>
                    <i>📚</i> ${assignment.subjectName || 'Не указано'}
                </span>
                <span>
                    <i>📝</i> ${getAssignmentType(assignment.type)}
                </span>
                <span>
                    <i>⭐</i> Макс. балл: ${assignment.maxGrade || '100'}
                </span>
            </div>
            <div class="assignment-description">
                ${assignment.description || 'Описание отсутствует'}
            </div>
            <div class="assignment-footer">
                <div class="assignment-deadline">
                    <i>📅</i> Срок: ${deadline.toLocaleDateString('ru-RU')}
                    <span style="color: ${isOverdue ? 'var(--danger)' : daysLeft <= 2 ? 'var(--warning)' : 'var(--text-light)'}">
                        (${isOverdue ? 'Просрочено' : `Осталось ${daysLeft} ${getDayWord(daysLeft)}`})
                    </span>
                </div>
            </div>
        `;
        container.appendChild(assignmentElement);
    });
}

function getDayWord(days) {
    if (days === 1) return 'день';
    if (days >= 2 && days <= 4) return 'дня';
    return 'дней';
}

function updateAssignmentDropdown(assignments) {
    const select = document.getElementById('assignmentSelect');
    select.innerHTML = '<option value="">Выберите задание</option>';

    if (assignments && assignments.length > 0) {
        // Фильтруем только задания, которые еще не просрочены
        const now = new Date();
        const activeAssignments = assignments.filter(a => new Date(a.deadline) > now);

        if (activeAssignments.length === 0) {
            select.innerHTML = '<option value="">Нет активных заданий</option>';
            return;
        }

        activeAssignments.forEach(assignment => {
            const deadline = new Date(assignment.deadline);
            const option = document.createElement('option');
            option.value = assignment.id;
            option.textContent = `${assignment.title} (до ${deadline.toLocaleDateString('ru-RU')})`;
            select.appendChild(option);
        });
    } else {
        select.innerHTML = '<option value="">Нет доступных заданий</option>';
    }
}

function updateStats(assignments) {
    if (!assignments) assignments = [];

    const now = new Date();
    const total = assignments.length;
    const overdue = assignments.filter(a => new Date(a.deadline) < now).length;
    const completed = 0; // TODO: Get from API

    document.getElementById('total-assignments').textContent = total;
    document.getElementById('overdue-assignments').textContent = overdue;
    document.getElementById('completed-assignments').textContent = completed;
    document.getElementById('assignments-count').textContent = total;
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

async function loadStudentGrades() {
    try {
        const gradesList = document.getElementById('grades-list');

        // TODO: Replace with real API call
        const grades = await ApiService.get('/students/grades');

        if (!grades || grades.length === 0) {
            gradesList.innerHTML = `
                <div class="no-assignments">
                    <i>📊</i>
                    <h3>Нет оценок</h3>
                    <p>Здесь будут отображаться ваши оценки</p>
                </div>
            `;
            return;
        }

        gradesList.innerHTML = '';

        grades.forEach(grade => {
            const gradeElement = document.createElement('div');
            gradeElement.className = 'grade-item';
            gradeElement.innerHTML = `
                <div class="grade-header">
                    <div class="grade-title">${grade.assignmentTitle}</div>
                    <div class="grade-value">${grade.grade}/100</div>
                </div>
                <div class="grade-meta">
                    <span><i>📅</i> ${new Date(grade.gradedAt).toLocaleDateString('ru-RU')}</span>
                    <span><i>📚</i> ${grade.subjectName || ''}</span>
                </div>
                ${grade.comment ? `
                <div class="grade-comment">
                    <strong>Комментарий:</strong> ${grade.comment}
                </div>
                ` : ''}
            `;
            gradesList.appendChild(gradeElement);
        });

        // Calculate average grade
        if (grades.length > 0) {
            const average = Math.round(grades.reduce((sum, g) => sum + g.grade, 0) / grades.length);
            document.getElementById('average-grade').textContent = average;
        }

    } catch (error) {
        console.error('Error loading grades:', error);
        document.getElementById('grades-list').innerHTML = `
            <div class="no-assignments">
                <i>❌</i>
                <h3>Ошибка загрузки</h3>
                <p>Не удалось загрузить оценки</p>
            </div>
        `;
    }
}

async function loadProgressPreview() {
    try {
        const response = await fetch('/api/gamification/student/stats', {
            headers: { 'Authorization': `Bearer ${localStorage.getItem('token')}` }
        });

        if (response.ok) {
            const stats = await response.json();
            const progressPercentage = Math.round((stats.currentLevelXp / stats.nextLevelXp) * 100);

            document.getElementById('progressPreview').innerHTML = `
                <div class="level-display">
                    <div class="level-number">Уровень ${stats.level}</div>
                    <div class="level-label">${stats.currentLevelXp} / ${stats.nextLevelXp} XP</div>
                </div>
                
                <div class="xp-progress">
                    <div class="xp-labels">
                        <span>${stats.currentLevelXp} XP</span>
                        <span>${stats.nextLevelXp} XP</span>
                    </div>
                    <div class="xp-bar">
                        <div class="xp-fill" style="width: ${progressPercentage}%"></div>
                    </div>
                </div>
                
                <div class="stats-grid-small">
                    <div class="stat-item-small">
                        <div class="stat-value-small">${stats.completedAssignments}</div>
                        <div class="stat-label-small">Выполнено</div>
                    </div>
                    <div class="stat-item-small">
                        <div class="stat-value-small">${stats.achievementsUnlocked}</div>
                        <div class="stat-label-small">Достижений</div>
                    </div>
                </div>
            `;
        } else {
            throw new Error('Failed to load progress');
        }
    } catch (error) {
        console.error('Error loading progress preview:', error);
        document.getElementById('progressPreview').innerHTML = `
            <div class="no-assignments">
                <i>❌</i>
                <h3>Не удалось загрузить прогресс</h3>
            </div>
        `;
    }
}

function setupFileUpload() {
    const fileInput = document.getElementById('assignmentFile');
    const fileInfo = document.getElementById('file-info');

    fileInput.addEventListener('change', function(e) {
        const file = e.target.files[0];
        if (file) {
            const fileSizeMB = (file.size / 1024 / 1024).toFixed(2);

            if (fileSizeMB > 10) {
                fileInfo.innerHTML = `
                    <div style="color: var(--danger);">
                        <i>❌</i> Файл слишком большой (${fileSizeMB} MB). Максимум: 10MB
                    </div>
                `;
                fileInput.value = '';
            } else {
                fileInfo.innerHTML = `
                    <div style="display: flex; align-items: center; gap: 0.5rem;">
                        <i>📎</i>
                        <span style="font-weight: 500;">${file.name}</span>
                        <span style="color: var(--text-light); font-size: 0.875rem;">
                            (${fileSizeMB} MB)
                        </span>
                    </div>
                `;
            }
        } else {
            fileInfo.innerHTML = '';
        }
    });
}

// Form submission handler
document.getElementById('submitAssignmentForm').addEventListener('submit', async function(e) {
    e.preventDefault();

    const assignmentId = document.getElementById('assignmentSelect').value;
    const fileInput = document.getElementById('assignmentFile');
    const comment = document.getElementById('assignmentComment').value;
    const submitBtn = document.getElementById('submit-btn');
    const submitMessage = document.getElementById('submit-message');

    if (!assignmentId) {
        submitMessage.innerHTML = `
            <div class="form-message error">
                <i>❌</i> Выберите задание
            </div>
        `;
        submitMessage.style.display = 'block';
        return;
    }

    if (!fileInput.files[0]) {
        submitMessage.innerHTML = `
            <div class="form-message error">
                <i>❌</i> Выберите файл
            </div>
        `;
        submitMessage.style.display = 'block';
        return;
    }

    await submitAssignment(assignmentId, fileInput.files[0], comment, submitBtn, submitMessage);
});

async function submitAssignment(assignmentId, file, comment = '', submitBtn, submitMessage) {
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
                <i>✅</i> Задание успешно сдано!
            </div>
        `;
        submitMessage.style.display = 'block';

        // Reset form
        document.getElementById('submitAssignmentForm').reset();
        document.getElementById('file-info').innerHTML = '';

        // Reload assignments
        await loadStudentAssignments();

        // Hide message after 5 seconds
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
        submitBtn.innerHTML = '<i>📤</i> Сдать задание';
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