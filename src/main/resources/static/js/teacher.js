const token = localStorage.getItem('token');

let teacherTeachingPairs = [];

let allClasses = [];
let allSubjects = [];
let currentClassId = null;

let allAssignments = [];
let currentPage = 1;
const assignmentsPerPage = 5;
let filteredAssignments = [];

document.addEventListener('DOMContentLoaded', async function () {
    await loadTeacherData();
    await loadClasses();
    await loadSubjects();
    await loadTeacherAssignments();
    await loadSubmissionsToGrade();

    const urlParams = new URLSearchParams(window.location.search);
    const submissionId = urlParams.get('submissionId');

    if (submissionId) {
        setTimeout(() => {
            if (typeof viewSubmission === 'function') {
                viewSubmission(Number(submissionId));
            } else {
                console.error('viewSubmission function not found');
            }
            window.history.replaceState({}, document.title, '/teacher-dashboard.html');
        }, 500);
    }
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

function normalizeClasses(classes) {
    if (!Array.isArray(classes)) {
        return [];
    }

    const unique = [];
    const seen = new Set();

    classes.forEach(cls => {
        if (!cls || cls.id == null) {
            return;
        }

        const key = String(cls.id);
        if (seen.has(key)) {
            return;
        }

        seen.add(key);
        unique.push(cls);
    });

    unique.sort((a, b) => {
        const nameA = (a.name || '').toLowerCase();
        const nameB = (b.name || '').toLowerCase();
        if (nameA !== nameB) {
            return nameA.localeCompare(nameB, 'ru');
        }

        const yearA = (a.academicYear || '').toLowerCase();
        const yearB = (b.academicYear || '').toLowerCase();
        return yearA.localeCompare(yearB, 'ru');
    });

    return unique;
}

function normalizeTeachingPairs(pairs) {
    if (!Array.isArray(pairs)) {
        return [];
    }

    const unique = [];
    const seen = new Set();

    pairs.forEach(pair => {
        if (!pair || pair.classId == null || pair.subjectId == null) {
            return;
        }

        const key = `${pair.classId}_${pair.subjectId}`;
        if (seen.has(key)) {
            return;
        }

        seen.add(key);
        unique.push(pair);
    });

    unique.sort((a, b) => {
        const classNameA = (a.className || '').toLowerCase();
        const classNameB = (b.className || '').toLowerCase();

        if (classNameA !== classNameB) {
            return classNameA.localeCompare(classNameB, 'ru');
        }

        const yearA = (a.academicYear || '').toLowerCase();
        const yearB = (b.academicYear || '').toLowerCase();

        if (yearA !== yearB) {
            return yearA.localeCompare(yearB, 'ru');
        }

        const subjectA = (a.subjectName || '').toLowerCase();
        const subjectB = (b.subjectName || '').toLowerCase();
        return subjectA.localeCompare(subjectB, 'ru');
    });

    return unique;
}

function extractClassesFromPairs(pairs) {
    const classesMap = new Map();

    pairs.forEach(pair => {
        if (pair.classId == null) {
            return;
        }

        const key = String(pair.classId);
        if (!classesMap.has(key)) {
            classesMap.set(key, {
                id: pair.classId,
                name: pair.className,
                academicYear: pair.academicYear || ''
            });
        }
    });

    return normalizeClasses(Array.from(classesMap.values()));
}

function extractSubjectsFromPairs(pairs) {
    const subjectsMap = new Map();

    pairs.forEach(pair => {
        if (pair.subjectId == null) {
            return;
        }

        const key = String(pair.subjectId);
        if (!subjectsMap.has(key)) {
            subjectsMap.set(key, {
                id: pair.subjectId,
                name: pair.subjectName
            });
        }
    });

    const subjects = Array.from(subjectsMap.values());

    subjects.sort((a, b) => {
        const nameA = (a.name || '').toLowerCase();
        const nameB = (b.name || '').toLowerCase();
        return nameA.localeCompare(nameB, 'ru');
    });

    return subjects;
}

async function loadTeachingPairs() {
    try {
        const pairs = await ApiService.get('/teacher/teaching-assignments/pairs');
        teacherTeachingPairs = normalizeTeachingPairs(pairs);
        console.log('Teacher teaching pairs loaded:', teacherTeachingPairs);
    } catch (error) {
        console.error('Error loading teacher teaching pairs:', error);
        teacherTeachingPairs = [];
    }
}

async function loadClasses() {
    await loadTeachingPairs();

    const classesList = document.getElementById('classes-list');
    const classSelect = document.getElementById('assignmentClass');
    const classFilter = document.getElementById('classFilter');

    try {
        allClasses = extractClassesFromPairs(teacherTeachingPairs);

        console.log('Teacher classes loaded from teaching assignments:', allClasses);

        if (classesList) {
            classesList.innerHTML = '';
        }

        if (classSelect) {
            classSelect.innerHTML = '<option value="">Выберите класс</option>';
        }

        if (classFilter) {
            classFilter.innerHTML = '<option value="">Все классы</option>';
        }

        if (allClasses.length > 0) {
            allClasses.forEach(classItem => {
                if (classesList) {
                    const classElement = document.createElement('div');
                    classElement.className = 'assignment-item';
                    classElement.innerHTML = `
                        <div class="assignment-title">${classItem.name}</div>
                        <div class="assignment-meta">Учебный год: ${classItem.academicYear || '—'}</div>
                    `;
                    classesList.appendChild(classElement);
                }

                if (classSelect) {
                    const option = document.createElement('option');
                    option.value = classItem.id;
                    option.textContent = classItem.academicYear
                        ? `${classItem.name} (${classItem.academicYear})`
                        : classItem.name;
                    classSelect.appendChild(option);
                }

                if (classFilter) {
                    const filterOption = document.createElement('option');
                    filterOption.value = classItem.id;
                    filterOption.textContent = classItem.academicYear
                        ? `${classItem.name} (${classItem.academicYear})`
                        : classItem.name;
                    classFilter.appendChild(filterOption);
                }
            });

            currentClassId = allClasses[0].id;

            if (classSelect) {
                classSelect.onchange = async function () {
                    currentClassId = this.value ? Number(this.value) : null;
                    await loadSubjects();
                    loadGamificationPreview();
                };
            }

            loadGamificationPreview();
        } else {
            if (classesList) {
                classesList.innerHTML = '<p>Нет назначенных классов</p>';
            }
            if (classSelect) {
                classSelect.innerHTML = '<option value="">Нет назначенных классов</option>';
            }
            if (classFilter) {
                classFilter.innerHTML = '<option value="">Нет назначенных классов</option>';
            }
            currentClassId = null;
        }

    } catch (error) {
        console.error('Error loading teacher classes:', error);

        if (classesList) {
            classesList.innerHTML = '<p>Ошибка загрузки классов</p>';
        }
        if (classSelect) {
            classSelect.innerHTML = '<option value="">Ошибка загрузки классов</option>';
        }
        if (classFilter) {
            classFilter.innerHTML = '<option value="">Ошибка загрузки классов</option>';
        }
        currentClassId = null;
    }
}

async function loadSubjects() {
    const subjectSelect = document.getElementById('assignmentSubject');
    const classSelect = document.getElementById('assignmentClass');

    if (!subjectSelect) {
        return;
    }

    try {
        if (!Array.isArray(teacherTeachingPairs) || teacherTeachingPairs.length === 0) {
            await loadTeachingPairs();
        }

        const selectedClassId = classSelect && classSelect.value
            ? String(classSelect.value)
            : null;

        let filteredPairs = teacherTeachingPairs;

        if (selectedClassId) {
            filteredPairs = teacherTeachingPairs.filter(
                pair => String(pair.classId) === selectedClassId
            );
        }

        allSubjects = extractSubjectsFromPairs(filteredPairs);

        subjectSelect.innerHTML = '<option value="">Выберите предмет</option>';

        if (allSubjects.length === 0) {
            subjectSelect.innerHTML = '<option value="">Нет доступных предметов</option>';
            return;
        }

        allSubjects.forEach(subject => {
            const option = document.createElement('option');
            option.value = subject.id;
            option.textContent = subject.name;
            subjectSelect.appendChild(option);
        });

    } catch (error) {
        console.error('Error loading subjects:', error);
        subjectSelect.innerHTML = '<option value="">Ошибка загрузки предметов</option>';
    }
}

async function loadTeacherAssignments() {
    try {
        console.log('Loading teacher assignments...');

        try {
            allAssignments = await ApiService.get('/teacher/assignments/my');
            console.log('Loaded teacher assignments:', allAssignments);
        } catch (error) {
            console.log('Teacher assignments endpoint failed, trying general assignments...');
            allAssignments = await ApiService.get('/assignments');
        }

        filteredAssignments = [...allAssignments];
        displayTeacherAssignmentsPage(1);

        const submissions = await ApiService.get('/submissions/my');
        updateTeacherStats(allAssignments, submissions);
    } catch (error) {
        console.error('Error loading assignments:', error);
        allAssignments = [];
        filteredAssignments = [];
        displayTeacherAssignmentsPage(1);
    }
}

async function loadGamificationPreview() {
    const previewEl = document.getElementById('gamificationPreview');
    if (!previewEl || !token || !currentClassId) {
        return;
    }

    try {
        const response = await fetch('/api/gamification/leaderboard?classId=' + currentClassId, {
            headers: { 'Authorization': `Bearer ${token}` }
        });

        if (response.ok) {
            const leaderboard = await response.json();
            const top5 = leaderboard.slice(0, 5);

            previewEl.innerHTML = `
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

function searchTeacherAssignments() {
    const searchTerm = document.getElementById('searchAssignments').value.toLowerCase().trim();

    filterTeacherAssignments();

    if (searchTerm === '') {
        filteredAssignments = [...allAssignments];
    } else {
        filteredAssignments = allAssignments.filter(assignment =>
            assignment.title && assignment.title.toLowerCase().includes(searchTerm)
        );
    }

    currentPage = 1;
    displayTeacherAssignmentsPage(currentPage);
}

function clearFilters() {
    document.getElementById('searchAssignments').value = '';
    document.getElementById('classFilter').value = '';

    filteredAssignments = [...allAssignments];
    currentPage = 1;
    displayTeacherAssignmentsPage(currentPage);
}

function getClassNameById(classId) {
    const classItem = allClasses.find(c => String(c.id) === String(classId));
    return classItem ? classItem.name : '';
}

function filterTeacherAssignments() {
    const searchTerm = document.getElementById('searchAssignments').value.toLowerCase().trim();
    const classFilterValue = document.getElementById('classFilter').value;

    filteredAssignments = [...allAssignments];

    if (searchTerm !== '') {
        filteredAssignments = filteredAssignments.filter(assignment =>
            assignment.title && assignment.title.toLowerCase().includes(searchTerm)
        );
    }

    if (classFilterValue !== '') {
        filteredAssignments = filteredAssignments.filter(assignment =>
            String(assignment.classId) === String(classFilterValue) ||
            assignment.className === getClassNameById(classFilterValue)
        );
    }

    currentPage = 1;
    displayTeacherAssignmentsPage(currentPage);
}

function displayTeacherAssignmentsPage(page) {
    const assignmentsList = document.getElementById('assignments-list');
    const pagination = document.getElementById('pagination');

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

    const totalPages = Math.ceil(filteredAssignments.length / assignmentsPerPage);
    const startIndex = (page - 1) * assignmentsPerPage;
    const endIndex = Math.min(startIndex + assignmentsPerPage, filteredAssignments.length);
    const currentAssignments = filteredAssignments.slice(startIndex, endIndex);

    assignmentsList.innerHTML = `
        <div class="assignments-list">
            <div class="assignments-header">
                <div class="header-cell"><span>Название задания</span><i>▼</i></div>
                <div class="header-cell"><span>Класс</span></div>
                <div class="header-cell"><span>Тип</span></div>
                <div class="header-cell"><span>Макс. оценка</span></div>
                <div class="header-cell"><span>Срок сдачи</span></div>
                <div class="header-cell"><span>Действия</span></div>
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
                                    ${deadlineDate.toLocaleTimeString('ru-RU', { hour: '2-digit', minute: '2-digit' })}
                                </span>
                                ` : ''}
                                ${isUrgent ? '<span class="deadline-urgent">СРОЧНО!</span>' : ''}
                            </div>
                        </div>
                        <div class="assignment-cell">
                            <div class="assignment-actions">
                                <button class="btn-table btn-view" onclick="viewAssignmentSubmissions(${assignment.id})" title="Просмотреть сдачи"><i>👁️</i></button>
                                <button class="btn-table btn-edit" onclick="editAssignment(${assignment.id})" title="Редактировать"><i>✏️</i></button>
                                <button class="btn-table btn-delete" onclick="deleteAssignment(${assignment.id})" title="Удалить"><i>🗑️</i></button>
                            </div>
                        </div>
                    </div>
                    `;
    }).join('')}
            </div>
        </div>
    `;

    updatePagination(page, totalPages, filteredAssignments.length, startIndex, endIndex);
}

function isDeadlineUrgent(deadlineDate) {
    const now = new Date();
    const diffHours = (deadlineDate - now) / (1000 * 60 * 60);
    return diffHours > 0 && diffHours < 48;
}

function updatePagination(currentPageValue, totalPages, totalItems, startIndex, endIndex) {
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
                <button class="pagination-btn" onclick="changePage(-1)" ${currentPageValue === 1 ? 'disabled' : ''}><i>←</i></button>
                <div class="page-numbers">
                    ${generatePageNumbers(currentPageValue, totalPages)}
                </div>
                <button class="pagination-btn" onclick="changePage(1)" ${currentPageValue === totalPages ? 'disabled' : ''}><i>→</i></button>
            </div>
        </div>
    `;
}

function generatePageNumbers(currentPageValue, totalPages) {
    let pages = [];
    pages.push(1);

    let startPage = Math.max(2, currentPageValue - 1);
    let endPage = Math.min(totalPages - 1, currentPageValue + 1);

    if (startPage > 2) {
        pages.push('...');
    }

    for (let i = startPage; i <= endPage; i++) {
        pages.push(i);
    }

    if (endPage < totalPages - 1) {
        pages.push('...');
    }

    if (totalPages > 1) {
        pages.push(totalPages);
    }

    return pages.map(page => {
        if (page === '...') {
            return '<span class="page-dots">...</span>';
        }
        return `
            <button class="page-number ${page === currentPageValue ? 'active' : ''}" onclick="goToPage(${page})">
                ${page}
            </button>
        `;
    }).join('');
}

function goToPage(page) {
    currentPage = page;
    displayTeacherAssignmentsPage(currentPage);
}

function changePage(direction) {
    const totalPages = Math.ceil(filteredAssignments.length / assignmentsPerPage);
    const newPage = currentPage + direction;

    if (newPage >= 1 && newPage <= totalPages) {
        currentPage = newPage;
        displayTeacherAssignmentsPage(currentPage);
    }
}

function editAssignment(assignmentId) {
    alert(`Редактирование задания ${assignmentId} - функция в разработке`);
}

function deleteAssignment(assignmentId) {
    if (confirm('Вы уверены, что хотите удалить это задание?')) {
        ApiService.delete(`/teacher/assignments/${assignmentId}`)
            .then(() => {
                alert('Задание успешно удалено');
                loadTeacherAssignments();
            })
            .catch(error => {
                console.error('Error deleting assignment:', error);
                alert('Ошибка при удалении задания');
            });
    }
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

    submissionsToGrade.sort((a, b) => new Date(b.submittedAt) - new Date(a.submittedAt));

    container.innerHTML = `
        <div class="submissions-list">
            <div class="submissions-header">
                <div class="header-cell"><span>Задание</span></div>
                <div class="header-cell"><span>Студент</span></div>
                <div class="header-cell"><span>Файл</span></div>
                <div class="header-cell"><span>Дата сдачи</span></div>
                <div class="header-cell"><span>Действия</span></div>
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
                                    ${submittedDate.toLocaleTimeString('ru-RU', { hour: '2-digit', minute: '2-digit' })}
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

    document.getElementById('pending-count').textContent = submissionsToGrade.length;
}

function isSubmissionRecent(submittedDate) {
    const now = new Date();
    const diffHours = (now - submittedDate) / (1000 * 60 * 60);
    return diffHours < 24;
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
                    в ${submittedDate.toLocaleTimeString('ru-RU', { hour: '2-digit', minute: '2-digit' })}
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
        homework: 'Домашнее задание',
        test: 'Тест',
        quiz: 'Викторина',
        sor: 'СОР',
        soch: 'СОЧ'
    };
    return types[type] || type;
}

function showCreateAssignmentModal() {
    document.getElementById('createAssignmentModal').style.display = 'block';
}

function closeCreateAssignmentModal() {
    document.getElementById('createAssignmentModal').style.display = 'none';
    document.getElementById('createAssignmentForm').reset();
    loadSubjects();
}

function closeGradeSubmissionModal() {
    document.getElementById('gradeSubmissionModal').style.display = 'none';
    document.getElementById('gradeSubmissionForm').reset();
}

document.getElementById('createAssignmentForm').addEventListener('submit', async function (e) {
    e.preventDefault();

    const title = document.getElementById('assignmentTitle').value.trim();
    const description = document.getElementById('assignmentDescription').value.trim();
    const type = document.getElementById('assignmentType').value;
    const maxGrade = parseInt(document.getElementById('assignmentMaxGrade').value, 10);
    const deadline = document.getElementById('assignmentDeadline').value;
    const classId = document.getElementById('assignmentClass').value;
    const subjectId = document.getElementById('assignmentSubject').value;

    if (!title || !type || !deadline || !classId || !subjectId) {
        alert('Пожалуйста, заполните все обязательные поля');
        return;
    }

    if (Number.isNaN(maxGrade) || maxGrade < 1) {
        alert('Максимальная оценка должна быть больше 0');
        return;
    }

    const formData = {
        title: title,
        description: description,
        type: type,
        maxGrade: maxGrade,
        deadline: deadline,
        classId: parseInt(classId, 10),
        subjectId: parseInt(subjectId, 10)
    };

    try {
        await ApiService.post('/teacher/assignments', formData);

        alert('Задание создано успешно!');
        closeCreateAssignmentModal();
        await loadTeacherAssignments();
        clearFilters();

    } catch (error) {
        console.error('Error creating assignment:', error);
        alert('Ошибка при создании задания: ' + error.message);
    }
});

document.getElementById('gradeSubmissionForm').addEventListener('submit', async function (e) {
    e.preventDefault();

    const submissionId = document.getElementById('submissionId').value;
    const gradeValue = parseInt(document.getElementById('gradeValue').value, 10);
    const comment = document.getElementById('teacherComment').value;

    if (gradeValue < 0 || gradeValue > 100) {
        alert('Оценка должна быть от 0 до 100');
        return;
    }

    try {
        const gradeData = {
            submissionId: parseInt(submissionId, 10),
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