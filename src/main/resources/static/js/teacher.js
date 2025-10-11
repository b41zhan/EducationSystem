document.addEventListener('DOMContentLoaded', function() {
    loadTeacherData();
    loadClasses();
    loadTeacherAssignments();
    loadSubmissionsToGrade();
});

// Добавьте эти переменные в начало файла
let allAssignments = [];
let currentPage = 1;
const assignmentsPerPage = 5;
let filteredAssignments = [];

// Обновите функцию loadTeacherAssignments
async function loadTeacherAssignments() {
    try {
        console.log('Loading teacher assignments...');

        // Пробуем получить задания учителя
        try {
            allAssignments = await ApiService.get('/teacher/assignments/my');
            console.log('Loaded teacher assignments:', allAssignments);
        } catch (error) {
            console.log('Teacher assignments endpoint failed, trying general assignments...');
            allAssignments = await ApiService.get('/assignments');
        }

        // Инициализируем отфильтрованные задания
        filteredAssignments = [...allAssignments];

        // Отображаем первую страницу
        displayTeacherAssignmentsPage(1);

        // Обновляем статистику
        const submissions = await ApiService.get('/submissions/my');
        updateTeacherStats(allAssignments, submissions);

    } catch (error) {
        console.error('Error loading assignments:', error);

        // Если ошибка - показываем пустой список
        allAssignments = [];
        filteredAssignments = [];
        displayTeacherAssignmentsPage(1);
    }
}

// Функция поиска заданий
function searchTeacherAssignments() {
    const searchTerm = document.getElementById('searchAssignments').value.toLowerCase().trim();

    filterTeacherAssignments();

    if (searchTerm === '') {
        // Если поиск пустой, показываем все задания
        filteredAssignments = [...allAssignments];
    } else {
        // Фильтруем задания по названию
        filteredAssignments = allAssignments.filter(assignment =>
            assignment.title && assignment.title.toLowerCase().includes(searchTerm)
        );
    }

    // Возвращаемся на первую страницу после поиска
    currentPage = 1;
    displayTeacherAssignmentsPage(currentPage);
}


function clearFilters() {
    document.getElementById('searchAssignments').value = '';
    document.getElementById('classFilter').value = '';

    // Возвращаем все задания
    filteredAssignments = [...allAssignments];
    currentPage = 1;
    displayTeacherAssignmentsPage(currentPage);
}

function getClassNameById(classId) {
    const classItem = allClasses.find(c => c.id == classId);
    return classItem ? classItem.name : '';
}

// Функция отображения страницы с заданиями
function displayTeacherAssignmentsPage(page) {
    const assignmentsList = document.getElementById('assignments-list');
    const pagination = document.getElementById('pagination');

    // Создаем элемент для информации о результатах
    let resultsInfo = document.getElementById('results-info');
    if (!resultsInfo) {
        resultsInfo = document.createElement('div');
        resultsInfo.id = 'results-info';
        resultsInfo.className = 'results-info';
        assignmentsList.parentNode.insertBefore(resultsInfo, assignmentsList);
    }

    if (!filteredAssignments || filteredAssignments.length === 0) {
        const searchTerm = document.getElementById('searchAssignments').value;
        const classFilterValue = document.getElementById('classFilter').value;
        const className = classFilterValue ? getClassNameById(classFilterValue) : '';

        let message = '';
        if (searchTerm && classFilterValue) {
            message = `По запросу "${searchTerm}" и классу "${className}" заданий не найдено`;
        } else if (searchTerm) {
            message = `По запросу "${searchTerm}" заданий не найдено`;
        } else if (classFilterValue) {
            message = `Для класса "${className}" заданий не найдено`;
        } else {
            message = 'Нет созданных заданий';
        }

        assignmentsList.innerHTML = `
            <div class="no-assignments">
                <p>${message}</p>
                <small>${!searchTerm && !classFilterValue ? 'Создайте первое задание, используя кнопку "Создать задание"' : 'Попробуйте изменить параметры поиска'}</small>
            </div>
        `;
        resultsInfo.textContent = '';
        pagination.style.display = 'none';
        return;
    }

    // Показываем информацию о результатах
    const searchTerm = document.getElementById('searchAssignments').value;
    const classFilterValue = document.getElementById('classFilter').value;
    const className = classFilterValue ? getClassNameById(classFilterValue) : '';

    let resultsText = `Найдено заданий: ${filteredAssignments.length}`;
    if (searchTerm || classFilterValue) {
        resultsText += ' (';
        if (searchTerm) resultsText += `поиск: "${searchTerm}"`;
        if (searchTerm && classFilterValue) resultsText += ', ';
        if (classFilterValue) resultsText += `класс: ${className}`;
        resultsText += ')';
    }

    resultsInfo.textContent = resultsText;

    // Рассчитываем индексы для текущей страницы
    const totalPages = Math.ceil(filteredAssignments.length / assignmentsPerPage);
    const startIndex = (page - 1) * assignmentsPerPage;
    const endIndex = Math.min(startIndex + assignmentsPerPage, filteredAssignments.length);
    const currentAssignments = filteredAssignments.slice(startIndex, endIndex);

    // Отображаем задания
    assignmentsList.innerHTML = '';

    currentAssignments.forEach(assignment => {
        const assignmentElement = document.createElement('div');
        assignmentElement.className = 'assignment-item';
        assignmentElement.innerHTML = `
            <div class="assignment-title">${assignment.title || 'Без названия'}</div>
            <div class="assignment-meta">
                Класс: ${assignment.className || 'Не указан'} |
                Тип: ${getAssignmentTypeName(assignment.type)} |
                Макс. оценка: ${assignment.maxGrade || 'N/A'}
            </div>
            <div class="assignment-deadline">
                Срок: ${assignment.deadline ? new Date(assignment.deadline).toLocaleDateString('ru-RU') : 'Не указан'}
            </div>
            <button class="btn-secondary" onclick="viewAssignmentSubmissions(${assignment.id})">
                Просмотреть сдачи
            </button>
        `;
        assignmentsList.appendChild(assignmentElement);
    });

    // Обновляем пагинацию
    updatePagination(page, totalPages, filteredAssignments.length);
}

// Функция обновления пагинации
function updatePagination(currentPage, totalPages, totalAssignments) {
    const pagination = document.getElementById('pagination');
    const pageInfo = document.getElementById('page-info');
    const prevButton = document.getElementById('prev-page');
    const nextButton = document.getElementById('next-page');

    if (totalPages <= 1) {
        pagination.style.display = 'none';
    } else {
        pagination.style.display = 'flex';

        // Обновляем информацию о странице
        pageInfo.textContent = `Страница ${currentPage} из ${totalPages}`;

        // Обновляем состояние кнопок
        prevButton.disabled = currentPage === 1;
        nextButton.disabled = currentPage === totalPages;

        // Добавляем/убираем стили для disabled кнопок
        prevButton.style.opacity = currentPage === 1 ? '0.5' : '1';
        nextButton.style.opacity = currentPage === totalPages ? '0.5' : '1';
    }
}

// Функция смены страницы
function changePage(direction) {
    const totalPages = Math.ceil(filteredAssignments.length / assignmentsPerPage);
    const newPage = currentPage + direction;

    if (newPage >= 1 && newPage <= totalPages) {
        currentPage = newPage;
        displayTeacherAssignmentsPage(currentPage);
    }
}

// ВАЖНО: Остальные функции должны остаться без изменений!

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

        // Сохраняем классы для фильтрации
        allClasses = classes;

        const classesList = document.getElementById('classes-list');
        const classSelect = document.getElementById('assignmentClass');
        const classFilter = document.getElementById('classFilter');

        // Очищаем списки
        classesList.innerHTML = '';
        classSelect.innerHTML = '<option value="">Выберите класс</option>';
        classFilter.innerHTML = '<option value="">Все классы</option>';

        if (classes && classes.length > 0) {
            classes.forEach(classItem => {
                // Для списка классов в левой колонке
                const classElement = document.createElement('div');
                classElement.className = 'assignment-item';
                classElement.innerHTML = `
                    <div class="assignment-title">${classItem.name}</div>
                    <div class="assignment-meta">Учебный год: ${classItem.academicYear}</div>
                `;
                classesList.appendChild(classElement);

                // Для выпадающего списка в форме создания задания
                const option = document.createElement('option');
                option.value = classItem.id;
                option.textContent = `${classItem.name} (${classItem.academicYear})`;
                classSelect.appendChild(option);

                // Для фильтра по классам
                const filterOption = document.createElement('option');
                filterOption.value = classItem.id;
                filterOption.textContent = `${classItem.name} (${classItem.academicYear})`;
                classFilter.appendChild(filterOption);
            });
        } else {
            classesList.innerHTML = '<p>Нет доступных классов</p>';
            classSelect.innerHTML = '<option value="">Нет доступных классов</option>';
            classFilter.innerHTML = '<option value="">Нет доступных классов</option>';
        }

    } catch (error) {
        console.error('Error loading classes:', error);

        const classesList = document.getElementById('classes-list');
        const classSelect = document.getElementById('assignmentClass');
        const classFilter = document.getElementById('classFilter');

        classesList.innerHTML = '<p>Ошибка загрузки классов</p>';
        classSelect.innerHTML = '<option value="">Ошибка загрузки классов</option>';
        classFilter.innerHTML = '<option value="">Ошибка загрузки классов</option>';
    }
}

function filterTeacherAssignments() {
    const searchTerm = document.getElementById('searchAssignments').value.toLowerCase().trim();
    const classFilterValue = document.getElementById('classFilter').value;

    console.log('Filtering - Search:', searchTerm, 'Class:', classFilterValue);

    // Начинаем со всех заданий
    filteredAssignments = [...allAssignments];

    // Применяем поиск по названию
    if (searchTerm !== '') {
        filteredAssignments = filteredAssignments.filter(assignment =>
            assignment.title && assignment.title.toLowerCase().includes(searchTerm)
        );
    }

    // Применяем фильтр по классу
    if (classFilterValue !== '') {
        filteredAssignments = filteredAssignments.filter(assignment =>
            assignment.classId == classFilterValue || assignment.className === getClassNameById(classFilterValue)
        );
    }

    // Возвращаемся на первую страницу после фильтрации
    currentPage = 1;
    displayTeacherAssignmentsPage(currentPage);
}



async function loadSubmissionsToGrade() {
    try {
        const submissions = await ApiService.get('/submissions/my');
        displaySubmissionsToGrade(submissions);
        updateTeacherStats(allAssignments, submissions);
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

function testDownload() {
    window.open('/api/files/download-test', '_blank');
}

function downloadSubmissionFile(filePath) {
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

    const formData = {
        title: title,
        description: description,
        type: type,
        maxGrade: maxGrade,
        deadline: deadline,
        classId: parseInt(classId),
        subjectId: parseInt(subjectId)
    };

    try {
        const response = await ApiService.post('/teacher/assignments', formData);

        alert('Задание создано успешно!');
        closeCreateAssignmentModal();

        // Перезагружаем задания чтобы показать новое
        await loadTeacherAssignments();

        // Сбрасываем фильтры
        clearFilters();

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