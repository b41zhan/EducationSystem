
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

async function loadGamificationPreview() {
    try {
        const response = await fetch('/api/gamification/leaderboard?classId=' + currentClassId, {
            headers: { 'Authorization': `Bearer ${token}` }
        });

        if (response.ok) {
            const leaderboard = await response.json();
            const top5 = leaderboard.slice(0, 5);

            document.getElementById('gamificationPreview').innerHTML = `
                <div style="display: flex; flex-direction: column; gap: 10px;">
                    ${top5.map((student, index) => `
                        <div style="display: flex; align-items: center; gap: 10px; padding: 8px; background: #f8f9fa; border-radius: 6px;">
                            <span style="font-weight: bold; color: #667eea;">${index + 1}</span>
                            <span style="flex: 1;">${student.studentName}</span>
                            <span style="font-weight: bold;">${student.totalXp} XP</span>
                        </div>
                    `).join('')}
                </div>
            `;
        }
    } catch (error) {
        console.error('Error loading gamification preview:', error);
    }
}

// Вызовите эту функцию при загрузке страницы статистики
loadGamificationPreview();


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
            <div class="assignments-list">
                <div class="no-assignments">
                    <i>📋</i>
                    <h3>${message}</h3>
                    <small>${!searchTerm && !classFilterValue ? 'Создайте первое задание, используя кнопку "Создать задание"' : 'Попробуйте изменить параметры поиска'}</small>
                </div>
            </div>
        `;
        resultsInfo.innerHTML = '';
        pagination.style.display = 'none';
        return;
    }

    // Показываем информацию о результатах
    const searchTerm = document.getElementById('searchAssignments').value;
    const classFilterValue = document.getElementById('classFilter').value;
    const className = classFilterValue ? getClassNameById(classFilterValue) : '';

    let resultsText = `🔍 Найдено заданий: ${filteredAssignments.length}`;
    if (searchTerm || classFilterValue) {
        resultsText += ' (';
        if (searchTerm) resultsText += `поиск: "${searchTerm}"`;
        if (searchTerm && classFilterValue) resultsText += ', ';
        if (classFilterValue) resultsText += `класс: ${className}`;
        resultsText += ')';
    }

    resultsInfo.innerHTML = `<i>📊</i> ${resultsText}`;

    // Рассчитываем индексы для текущей страницы
    const totalPages = Math.ceil(filteredAssignments.length / assignmentsPerPage);
    const startIndex = (page - 1) * assignmentsPerPage;
    const endIndex = Math.min(startIndex + assignmentsPerPage, filteredAssignments.length);
    const currentAssignments = filteredAssignments.slice(startIndex, endIndex);

    // Отображаем задания в табличном формате
    assignmentsList.innerHTML = `
        <div class="assignments-list">
            <div class="assignments-header">
                <div class="header-cell">
                    <span>Название задания</span>
                    <i>▼</i>
                </div>
                <div class="header-cell">
                    <span>Класс</span>
                </div>
                <div class="header-cell">
                    <span>Тип</span>
                </div>
                <div class="header-cell">
                    <span>Макс. оценка</span>
                </div>
                <div class="header-cell">
                    <span>Срок сдачи</span>
                </div>
                <div class="header-cell">
                    <span>Действия</span>
                </div>
            </div>
            <div class="assignments-table">
                ${currentAssignments.map(assignment => {
        const deadlineDate = assignment.deadline ? new Date(assignment.deadline) : null;
        const isUrgent = deadlineDate ? isDeadlineUrgent(deadlineDate) : false;

        return `
                    <div class="assignment-row">
                        <div class="assignment-cell">
                            <div class="assignment-title">${assignment.title || 'Без названия'}</div>
                            <div class="assignment-description">${assignment.description || 'Описание отсутствует'}</div>
                        </div>
                        <div class="assignment-cell">
                            <div class="assignment-class">${assignment.className || getClassNameById(assignment.classId) || 'Не указан'}</div>
                        </div>
                        <div class="assignment-cell">
                            <div class="assignment-type type-${assignment.type}">
                                ${getAssignmentTypeName(assignment.type)}
                            </div>
                        </div>
                        <div class="assignment-cell">
                            <div class="assignment-grade">${assignment.maxGrade || '100'}</div>
                        </div>
                        <div class="assignment-cell">
                            <div class="assignment-deadline">
                                <span class="deadline-date">
                                    ${deadlineDate ? deadlineDate.toLocaleDateString('ru-RU') : 'Не указан'}
                                </span>
                                ${deadlineDate ? `
                                <span class="deadline-time">
                                    ${deadlineDate.toLocaleTimeString('ru-RU', {
            hour: '2-digit',
            minute: '2-digit'
        })}
                                </span>
                                ` : ''}
                                ${isUrgent ? '<span class="deadline-urgent">СРОЧНО!</span>' : ''}
                            </div>
                        </div>
                        <div class="assignment-cell">
                            <div class="assignment-actions">
                                <button class="btn-table btn-view" onclick="viewAssignmentSubmissions(${assignment.id})" title="Просмотреть сдачи">
                                    <i>👁️</i>
                                </button>
                                <button class="btn-table btn-edit" onclick="editAssignment(${assignment.id})" title="Редактировать">
                                    <i>✏️</i>
                                </button>
                                <button class="btn-table btn-delete" onclick="deleteAssignment(${assignment.id})" title="Удалить">
                                    <i>🗑️</i>
                                </button>
                            </div>
                        </div>
                    </div>
                    `;
    }).join('')}
            </div>
        </div>
    `;

    // Обновляем пагинацию
    updatePagination(page, totalPages, filteredAssignments.length, startIndex, endIndex);
}

// Вспомогательная функция для определения срочности дедлайна
function isDeadlineUrgent(deadlineDate) {
    const now = new Date();
    const diffHours = (deadlineDate - now) / (1000 * 60 * 60);
    return diffHours > 0 && diffHours < 48; // Срочно если меньше 48 часов осталось
}

// Обновленная функция updatePagination для табличного формата
function updatePagination(currentPage, totalPages, totalItems, startIndex, endIndex) {
    const pagination = document.getElementById('pagination');

    if (totalPages <= 1) {
        pagination.style.display = 'none';
        return;
    }

    pagination.style.display = 'block';
    pagination.innerHTML = `
        <div class="table-pagination">
            <div class="pagination-info">
                Показано <strong>${startIndex + 1}-${endIndex}</strong> из <strong>${totalItems}</strong> заданий
            </div>
            <div class="pagination-controls">
                <button class="pagination-btn" onclick="changePage(-1)" ${currentPage === 1 ? 'disabled' : ''}>
                    <i>←</i>
                </button>
                
                <div class="page-numbers">
                    ${generatePageNumbers(currentPage, totalPages)}
                </div>
                
                <button class="pagination-btn" onclick="changePage(1)" ${currentPage === totalPages ? 'disabled' : ''}>
                    <i>→</i>
                </button>
            </div>
        </div>
    `;
}

function generatePageNumbers(currentPage, totalPages) {
    let pages = [];

    // Всегда показываем первую страницу
    pages.push(1);

    // Рассчитываем диапазон страниц вокруг текущей
    let startPage = Math.max(2, currentPage - 1);
    let endPage = Math.min(totalPages - 1, currentPage + 1);

    // Добавляем ... если нужно
    if (startPage > 2) {
        pages.push('...');
    }

    // Добавляем страницы в диапазоне
    for (let i = startPage; i <= endPage; i++) {
        pages.push(i);
    }

    // Добавляем ... если нужно
    if (endPage < totalPages - 1) {
        pages.push('...');
    }

    // Всегда показываем последнюю страницу, если есть
    if (totalPages > 1) {
        pages.push(totalPages);
    }

    // Генерируем HTML
    return pages.map(page => {
        if (page === '...') {
            return '<span class="page-dots">...</span>';
        }
        return `
            <button class="page-number ${page === currentPage ? 'active' : ''}" 
                    onclick="goToPage(${page})">
                ${page}
            </button>
        `;
    }).join('');
}

function goToPage(page) {
    currentPage = page;
    displayTeacherAssignmentsPage(currentPage);
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

// Функции для редактирования и удаления (добавьте их в ваш код)
function editAssignment(assignmentId) {
    alert(`Редактирование задания ${assignmentId} - функция в разработке`);
    // Реализуйте логику редактирования задания
}

function deleteAssignment(assignmentId) {
    if (confirm('Вы уверены, что хотите удалить это задание?')) {
        // Реализуйте логику удаления задания
        ApiService.delete(`/teacher/assignments/${assignmentId}`)
            .then(() => {
                alert('Задание успешно удалено');
                loadTeacherAssignments(); // Перезагружаем список
            })
            .catch(error => {
                console.error('Error deleting assignment:', error);
                alert('Ошибка при удалении задания');
            });
    }
}

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
        container.innerHTML = `
            <div class="submissions-list">
                <div class="no-submissions">
                    <i>📋</i>
                    <h3>Нет заданий на проверку</h3>
                    <p>Все задания проверены! 🎉</p>
                </div>
            </div>
        `;
        return;
    }

    const submissionsToGrade = submissions.filter(sub => sub.status === 'submitted');

    if (submissionsToGrade.length === 0) {
        container.innerHTML = `
            <div class="submissions-list">
                <div class="no-submissions">
                    <i>🎉</i>
                    <h3>Все задания проверены!</h3>
                    <p>Отличная работа! Ожидайте новых сдач</p>
                </div>
            </div>
        `;
        return;
    }

    // Сортируем по дате сдачи (новые сверху)
    submissionsToGrade.sort((a, b) => new Date(b.submittedAt) - new Date(a.submittedAt));

    container.innerHTML = `
        <div class="submissions-list">
            <div class="submissions-header">
                <div class="header-cell">
                    <span>Задание</span>
                </div>
                <div class="header-cell">
                    <span>Студент</span>
                </div>
                <div class="header-cell">
                    <span>Файл</span>
                </div>
                <div class="header-cell">
                    <span>Дата сдачи</span>
                </div>
                <div class="header-cell">
                    <span>Действия</span>
                </div>
            </div>
            <div class="submissions-table">
                ${submissionsToGrade.map(submission => {
        const submittedDate = new Date(submission.submittedAt);
        const fileSizeMB = (submission.fileSize / 1024 / 1024).toFixed(2);
        const isRecent = isSubmissionRecent(submittedDate);

        return `
                    <div class="submission-row">
                        <div class="submission-cell">
                            <div class="submission-title">${submission.assignmentTitle || 'Без названия'}</div>
                        </div>
                        <div class="submission-cell">
                            <div class="submission-student">${submission.studentName}</div>
                        </div>
                        <div class="submission-cell">
                            <div class="submission-file">
                                <span class="submission-file-icon">📎</span>
                                <span class="submission-file-name" title="${submission.fileName}">
                                    ${submission.fileName || 'Без названия'}
                                </span>
                                <span class="submission-size">${fileSizeMB} MB</span>
                            </div>
                        </div>
                        <div class="submission-cell">
                            <div class="submission-date">
                                ${submittedDate.toLocaleDateString('ru-RU')}
                                <div class="submission-time">
                                    ${submittedDate.toLocaleTimeString('ru-RU', {
            hour: '2-digit',
            minute: '2-digit'
        })}
                                </div>
                                ${isRecent ? '<span class="submission-urgent">НОВОЕ</span>' : ''}
                            </div>
                        </div>
                        <div class="submission-cell">
                            <div class="submission-actions">
                                <button class="btn-submission btn-submission-view" onclick="viewSubmission(${submission.id})">
                                    Просмотреть и оценить
                                </button>
                            </div>
                        </div>
                    </div>
                    `;
    }).join('')}
            </div>
        </div>
    `;

    // Обновляем счетчик заданий на проверку
    document.getElementById('pending-count').textContent = submissionsToGrade.length;
}

function isSubmissionRecent(submittedDate) {
    const now = new Date();
    const diffHours = (now - submittedDate) / (1000 * 60 * 60);
    return diffHours < 24; // Новое если сдано менее 24 часов назад
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
        const fileSizeMB = (submission.fileSize / 1024 / 1024).toFixed(2);
        const submittedDate = new Date(submission.submittedAt);

        submissionDetails.innerHTML = `
            <div class="submission-detail-item">
                <div class="submission-detail-label">Задание:</div>
                <div class="submission-detail-value">
                    <strong>${submission.assignmentTitle}</strong>
                </div>
            </div>
            
            <div class="submission-detail-item">
                <div class="submission-detail-label">Студент:</div>
                <div class="submission-detail-value">
                    <strong>${submission.studentName}</strong>
                </div>
            </div>
            
            <div class="submission-detail-item">
                <div class="submission-detail-label">Файл:</div>
                <div class="submission-detail-value">
                    <div style="display: flex; align-items: center; gap: 0.5rem;">
                        <span style="color: var(--primary); font-size: 1.25rem;">📎</span>
                        <span style="font-weight: 500;">${submission.fileName}</span>
                        <span style="background: #e2e8f0; padding: 0.125rem 0.5rem; border-radius: 12px; font-size: 0.8125rem;">
                            ${fileSizeMB} MB
                        </span>
                    </div>
                    <button class="file-download-btn" 
                            onclick="downloadSubmissionFile('${submission.filePath}', ${submission.id}); return false;">
                        <i>⬇️</i> Скачать файл
                    </button>
                </div>
            </div>
            
            <div class="submission-detail-item">
                <div class="submission-detail-label">Дата сдачи:</div>
                <div class="submission-detail-value">
                    ${submittedDate.toLocaleDateString('ru-RU')} 
                    в ${submittedDate.toLocaleTimeString('ru-RU', {
            hour: '2-digit',
            minute: '2-digit'
        })}
                </div>
            </div>
            
            ${submission.comment ? `
            <div class="submission-detail-item">
                <div class="submission-detail-label">Комментарий студента:</div>
                <div class="submission-detail-value">
                    <div style="background: white; padding: 0.75rem; border-radius: var(--radius); border: 1px solid var(--border);">
                        ${submission.comment}
                    </div>
                </div>
            </div>
            ` : ''}
        `;

        // Устанавливаем максимальную оценку
        const assignment = allAssignments.find(a => a.id === submission.assignmentId);
        if (assignment) {
            const maxGrade = assignment.maxGrade || 100;
            document.getElementById('max-grade').textContent = maxGrade;
            document.getElementById('gradeValue').max = maxGrade;
            document.getElementById('gradeValue').placeholder = `От 0 до ${maxGrade}`;
        }

        document.getElementById('gradeSubmissionModal').style.display = 'block';

    } catch (error) {
        console.error('Error viewing submission:', error);
        alert('Ошибка при загрузке задания: ' + error.message);
    }
}

function downloadSubmissionFile(filePath, submissionId = null) {
    if (submissionId) {
        // Используем endpoint для скачивания файлов учеников
        window.open(`/api/files/download/submission/${submissionId}`, '_blank');
    } else {
        console.error('Submission ID not provided');
    }
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
// Проверяем, есть ли параметр submissionId в URL при загрузке страницы
document.addEventListener('DOMContentLoaded', function() {
    const urlParams = new URLSearchParams(window.location.search);
    const submissionId = urlParams.get('submissionId');

    if (submissionId) {
        // Небольшая задержка, чтобы страница успела загрузиться и менеджеры инициализироваться
        setTimeout(() => {
            if (typeof viewSubmission === 'function') {
                viewSubmission(submissionId);
            } else {
                console.error('viewSubmission function not found');
            }
            // Убираем параметр из URL, чтобы при обновлении страницы окно не открывалось снова
            window.history.replaceState({}, document.title, "/teacher-dashboard.html");
        }, 500);
    }
});

function loadAssignmentsToGrade() {
    loadSubmissionsToGrade();
}

