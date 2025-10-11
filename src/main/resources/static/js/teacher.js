document.addEventListener('DOMContentLoaded', function() {
    loadTeacherData();
    loadClasses();
    loadTeacherAssignments();
    loadSubmissionsToGrade();
});

async function loadTeacherData() {
    try {
        const userData = await ApiService.get('/auth/me');
        document.getElementById('welcome-message').textContent =
            `Добро пожаловать, ${userData.firstName} ${userData.lastName}!`;
    } catch (error) {
        console.error('Error loading teacher data:', error);
    }
}
async function loadClasses() {
    try {
        const classes = await ApiService.get('/school-classes');
        console.log('Classes loaded:', classes);

        const classesList = document.getElementById('classes-list');
        const classSelect = document.getElementById('assignmentClass');

        classesList.innerHTML = '';
        classSelect.innerHTML = '<option value="">Выберите класс</option>';

        if (classes && classes.length > 0) {
            classes.forEach(classItem => {
                // Для списка классов
                const classElement = document.createElement('div');
                classElement.className = 'assignment-item';
                classElement.innerHTML = `
                    <div class="assignment-title">${classItem.name}</div>
                    <div class="assignment-meta">Учебный год: ${classItem.academicYear}</div>
                `;
                classesList.appendChild(classElement);

                // Для выпадающего списка в форме
                const option = document.createElement('option');
                option.value = classItem.id;
                option.textContent = `${classItem.name} (${classItem.academicYear})`;
                classSelect.appendChild(option);
            });
        } else {
            classesList.innerHTML = '<p>Нет доступных классов</p>';
            classSelect.innerHTML = '<option value="">Нет доступных классов</option>';
        }

    } catch (error) {
        console.error('Error loading classes:', error);

        // Уберите fallback на статические данные, чтобы видеть ошибку
        const classesList = document.getElementById('classes-list');
        const classSelect = document.getElementById('assignmentClass');

        classesList.innerHTML = '<p>Ошибка загрузки классов</p>';
        classSelect.innerHTML = '<option value="">Ошибка загрузки классов</option>';
    }
}

async function loadTeacherAssignments() {
    try {
        const assignments = await ApiService.get('/assignments');
        displayTeacherAssignments(assignments);

        const submissions = await ApiService.get('/submissions/my');
        updateTeacherStats(assignments, submissions);

    } catch (error) {
        console.error('Error loading teacher assignments:', error);
        const assignments = [
            {
                id: 1,
                title: 'Домашнее задание по математике',
                type: 'homework',
                deadline: '2024-12-25T23:59:00',
                className: '7А',
                maxGrade: 100
            },
            {
                id: 2,
                title: 'Тест по физике',
                type: 'test',
                deadline: '2024-12-20T23:59:00',
                className: '8Б',
                maxGrade: 100
            }
        ];
        displayTeacherAssignments(assignments);
    }
}

function displayTeacherAssignments(assignments) {
    const assignmentsList = document.getElementById('assignments-list');

    if (!assignments || assignments.length === 0) {
        assignmentsList.innerHTML = '<p>Нет созданных заданий</p>';
        return;
    }

    assignmentsList.innerHTML = '';

    assignments.forEach(assignment => {
        const assignmentElement = document.createElement('div');
        assignmentElement.className = 'assignment-item';
        assignmentElement.innerHTML = `
            <div class="assignment-title">${assignment.title}</div>
            <div class="assignment-meta">
                Класс: ${assignment.className} | 
                Тип: ${getAssignmentTypeName(assignment.type)} |
                Макс. оценка: ${assignment.maxGrade}
            </div>
            <div class="assignment-deadline">
                Срок: ${new Date(assignment.deadline).toLocaleDateString('ru-RU')}
            </div>
            <button class="btn-secondary" onclick="viewAssignmentSubmissions(${assignment.id})">
                Просмотреть сдачи
            </button>
        `;
        assignmentsList.appendChild(assignmentElement);
    });
}

async function loadSubmissionsToGrade() {
    try {
        const submissions = await ApiService.get('/submissions/my');
        displaySubmissionsToGrade(submissions);
        updateTeacherStats([], submissions); // Обновляем статистику
    } catch (error) {
        console.error('Error loading submissions to grade:', error);
        document.getElementById('submissions-to-grade').innerHTML =
            '<p>Ошибка загрузки заданий</p>';
    }
}

function displaySubmissionsToGrade(submissions) {
    const container = document.getElementById('submissions-to-grade');

    if (!submissions || submissions.length === 0) {
        container.innerHTML = '<p>Нет заданий на проверку</p>';
        return;
    }

    const submissionsToGrade = submissions.filter(sub => sub.status === 'submitted');

    if (submissionsToGrade.length === 0) {
        container.innerHTML = '<p>Все задания проверены! 🎉</p>';
        return;
    }

    container.innerHTML = '';

    submissionsToGrade.forEach(submission => {
        const submissionElement = document.createElement('div');
        submissionElement.className = 'assignment-item';
        submissionElement.innerHTML = `
            <div class="assignment-title">${submission.assignmentTitle}</div>
            <div class="assignment-meta">
                Студент: <strong>${submission.studentName}</strong> | 
                Файл: ${submission.fileName} | 
                Размер: ${(submission.fileSize / 1024 / 1024).toFixed(2)} MB
            </div>
            <div class="assignment-meta">
                Сдано: ${new Date(submission.submittedAt).toLocaleString('ru-RU')}
            </div>
            <div class="submission-actions">
                <button class="btn-primary" onclick="viewSubmission(${submission.id})">
                    Просмотреть и оценить
                </button>
                <button class="btn-secondary" onclick="downloadSubmissionFile('${submission.filePath}')">
                    Скачать файл
                </button>
            </div>
        `;
        container.appendChild(submissionElement);
    });
}

async function viewSubmission(submissionId) {
    try {
        const submissions = await ApiService.get('/submissions/my');
        const submission = submissions.find(sub => sub.id === submissionId);

        if (!submission) {
            alert('Задание не найдено');
            return;
        }

        document.getElementById('submissionId').value = submission.id;

        const submissionDetails = document.getElementById('submission-details');
        submissionDetails.innerHTML = `
            <div class="submission-info">
                <h3>${submission.assignmentTitle}</h3>
                <p><strong>Студент:</strong> ${submission.studentName}</p>
                <p><strong>Файл:</strong> ${submission.fileName}</p>
                <p><strong>Размер:</strong> ${(submission.fileSize / 1024 / 1024).toFixed(2)} MB</p>
                <p><strong>Сдано:</strong> ${new Date(submission.submittedAt).toLocaleString('ru-RU')}</p>
                ${submission.comment ? `<p><strong>Комментарий студента:</strong> ${submission.comment}</p>` : ''}
                <div class="file-preview">
                    <button class="btn-secondary" onclick="downloadSubmissionFile('${submission.filePath}')">
                        📎 Скачать файл задания
                    </button>
                </div>
            </div>
        `;

        document.getElementById('gradeSubmissionModal').style.display = 'block';

    } catch (error) {
        console.error('Error viewing submission:', error);
        alert('Ошибка при загрузке задания: ' + error.message);
    }
}

function downloadSubmissionFile(filePath) {
    // Открываем файл в новой вкладке
    window.open(`/api/files/download/${filePath}`, '_blank');
}

async function viewAssignmentSubmissions(assignmentId) {
    try {
        const submissions = await ApiService.get(`/submissions/assignment/${assignmentId}`);

        if (submissions.length === 0) {
            alert('По этому заданию еще нет сдач');
            return;
        }

        let message = `Сдачи задания:\n\n`;
        submissions.forEach(sub => {
            const status = sub.status === 'graded' ? `✅ Оценено: ${sub.grade}/100` : '⏳ Ожидает проверки';
            message += `${sub.studentName}: ${status}\n`;
        });

        alert(message);

    } catch (error) {
        console.error('Error viewing assignment submissions:', error);
        alert('Ошибка при загрузке сдач задания');
    }
}

function updateTeacherStats(assignments, submissions) {
    const totalAssignments = assignments.length;
    const pendingSubmissions = submissions.filter(sub => sub.status === 'submitted').length;
    const gradedSubmissions = submissions.filter(sub => sub.status === 'graded').length;

    document.getElementById('total-assignments').textContent = totalAssignments;
    document.getElementById('pending-submissions').textContent = pendingSubmissions;
    document.getElementById('graded-submissions').textContent = gradedSubmissions;

    document.getElementById('pending-count').textContent = pendingSubmissions;
}

function getAssignmentTypeName(type) {
    const types = {
        'homework': 'Домашнее задание',
        'test': 'Тест',
        'quiz': 'Викторина',
        'sor': 'СОР',
        'soch': 'СОЧ'
    };
    return types[type] || type;
}

function showCreateAssignmentModal() {
    document.getElementById('createAssignmentModal').style.display = 'block';
}

function closeCreateAssignmentModal() {
    document.getElementById('createAssignmentModal').style.display = 'none';
    document.getElementById('createAssignmentForm').reset();
}

function closeGradeSubmissionModal() {
    document.getElementById('gradeSubmissionModal').style.display = 'none';
    document.getElementById('gradeSubmissionForm').reset();
}

document.getElementById('createAssignmentForm').addEventListener('submit', async function(e) {
    e.preventDefault();

    const title = document.getElementById('assignmentTitle').value;
    const description = document.getElementById('assignmentDescription').value;
    const type = document.getElementById('assignmentType').value;
    const maxGrade = parseInt(document.getElementById('assignmentMaxGrade').value);
    const deadline = document.getElementById('assignmentDeadline').value;
    const classId = document.getElementById('assignmentClass').value;
    const subjectId = document.getElementById('assignmentSubject').value;

    if (!title || !type || !deadline || !classId || !subjectId) {
        alert('Пожалуйста, заполните все обязательные поля');
        return;
    }

    if (classId === "") {
        alert('Пожалуйста, выберите класс');
        return;
    }

    const formData = {
        title: title,
        description: description,
        type: type,
        maxGrade: maxGrade,
        deadline: deadline,
        classId: parseInt(classId),
        subjectId: parseInt(subjectId)
    };

    console.log('Creating assignment with data:', formData);

    try {
        const response = await ApiService.post('/teacher/assignments', formData);
        console.log('Assignment created:', response);

        alert('Задание создано успешно!');
        closeCreateAssignmentModal();
        loadTeacherAssignments(); // Обновляем список

    } catch (error) {
        console.error('Error creating assignment:', error);
        alert('Ошибка при создании задания: ' + error.message);
    }
});

document.getElementById('gradeSubmissionForm').addEventListener('submit', async function(e) {
    e.preventDefault();

    const submissionId = document.getElementById('submissionId').value;
    const gradeValue = parseInt(document.getElementById('gradeValue').value);
    const comment = document.getElementById('teacherComment').value;

    if (gradeValue < 0 || gradeValue > 100) {
        alert('Оценка должна быть от 0 до 100');
        return;
    }

    try {
        const gradeData = {
            submissionId: parseInt(submissionId),
            gradeValue: gradeValue,
            comment: comment
        };

        await ApiService.post('/submissions/grade', gradeData);

        alert('Оценка успешно поставлена!');
        closeGradeSubmissionModal();

        loadSubmissionsToGrade();
        loadTeacherAssignments();

    } catch (error) {
        console.error('Error grading submission:', error);
        alert('Ошибка при оценке задания: ' + error.message);
    }
});

function loadAssignmentsToGrade() {
    loadSubmissionsToGrade();
}