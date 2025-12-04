let currentTeacherId = null;
let currentClassId = null;
const token = localStorage.getItem('token');

// Проверка авторизации
if (!token) {
    window.location.href = '/login.html';
}

// Загрузка при старте
document.addEventListener('DOMContentLoaded', function() {
    loadTeachers();
    loadClasses();
    document.getElementById('scheduleDate').value = new Date().toISOString().split('T')[0];
    document.getElementById('teacherScheduleDate').value = new Date().toISOString().split('T')[0];
});

// Загрузка списка учителей
async function loadTeachers() {
    try {
        const response = await fetch('/api/users?role=teacher', {
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });

        if (response.ok) {
            const teachers = await response.json();
            displayTeachers(teachers);
        } else {
            showError('Ошибка загрузки учителей');
        }
    } catch (error) {
        showError('Ошибка соединения');
    }
}

// Отображение списка учителей
function displayTeachers(teachers) {
    const teacherList = document.getElementById('teacherList');
    teacherList.innerHTML = '';

    teachers.forEach(teacher => {
        const teacherItem = document.createElement('div');
        teacherItem.className = 'teacher-item';
        teacherItem.onclick = (event) => selectTeacher(event, teacher);

        teacherItem.innerHTML = `
            <div class="teacher-name">${teacher.firstName} ${teacher.lastName}</div>
            <div class="teacher-subject">${teacher.roles && teacher.roles.length > 0 ? teacher.roles[0] : 'Учитель'}</div>
        `;

        teacherList.appendChild(teacherItem);
    });
}

// Выбор учителя
function selectTeacher(event, teacher) {
    document.querySelectorAll('.teacher-item').forEach(item => item.classList.remove('active'));
    event.currentTarget.classList.add('active');

    currentTeacherId = teacher.id;
    document.getElementById('selected-teacher-name').textContent =
        `Расписание: ${teacher.firstName} ${teacher.lastName}`;

    document.getElementById('no-selection').style.display = 'none';
    document.getElementById('class-schedule').style.display = 'none';
    document.getElementById('teacher-schedule').style.display = 'block';

    loadTeacherSchedule();
}

// Загрузка расписания учителя
async function loadTeacherSchedule() {
    if (!currentTeacherId) return;

    const date = document.getElementById('teacherScheduleDate').value;

    try {
        const response = await fetch(`/api/admin/schedule/teacher/${currentTeacherId}?date=${date}`, {
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });

        if (response.ok) {
            const schedule = await response.json();
            displayTeacherSchedule(schedule);
        } else {
            showError('Ошибка загрузки расписания');
        }
    } catch (error) {
        showError('Ошибка соединения');
    }
}

function displayTeacherSchedule(lessons) {
    const content = document.getElementById('teacher-schedule-content');

    if (!lessons.length) {
        content.innerHTML = '<div class="loading">Нет уроков на выбранную дату</div>';
        return;
    }

    // сортируем по времени
    lessons.sort((a, b) => a.startTime.localeCompare(b.startTime));

    // дата/день недели берём из первого урока
    const first = lessons[0];
    const formattedDate = formatDate(first.date);
    const dayOfWeek = getRussianDayOfWeek(first.dayOfWeek);

    let html = `
        <div class="day-schedule">
            <div class="day-header">
                <div class="day-title">${formattedDate} (${dayOfWeek})</div>
            </div>

            <div class="lessons-card">
                <div class="lessons-header">
                    <div class="header-cell">Время</div>
                    <div class="header-cell">Предмет</div>
                    <div class="header-cell">Класс</div>
                    <div class="header-cell">Кабинет</div>
                    <div class="header-cell header-actions-cell">Действия</div>
                </div>

                <div class="lessons-list">
    `;

    lessons.forEach(lesson => {
        html += `
            <div class="lesson-row">
                <div class="lesson-time">
                    ${lesson.startTime} – ${lesson.endTime}
                </div>

                <div class="lesson-subject">
                    ${lesson.subjectName}
                </div>

                <div class="lesson-class">
                    ${lesson.className || '—'}
                </div>

                <div class="lesson-room">
                    ${lesson.classroom || '—'}
                </div>

                <div class="lesson-actions">
                    <button class="btn-icon delete" onclick="deleteLesson(${lesson.id})" title="Удалить">
                        🗑
                    </button>
                </div>
            </div>
        `;
    });

    html += `
                </div>
            </div>
        </div>
    `;

    content.innerHTML = html;
}

function getRussianDayOfWeek(day) {
    const map = {
        'MONDAY': 'Понедельник',
        'TUESDAY': 'Вторник',
        'WEDNESDAY': 'Среда',
        'THURSDAY': 'Четверг',
        'FRIDAY': 'Пятница',
        'SATURDAY': 'Суббота',
        'SUNDAY': 'Воскресенье'
    };
    return map[day] || day;
}

function formatDate(dateString) {
    const date = new Date(dateString);
    return date.toLocaleDateString('ru-RU', {
        day: 'numeric',
        month: 'long',
        year: 'numeric'
    });
}

function searchTeachers() {
    const search = document.getElementById('teacherSearch').value.toLowerCase();
    document.querySelectorAll('.teacher-item').forEach(item => {
        const name = item.querySelector('.teacher-name').textContent.toLowerCase();
        const subj = item.querySelector('.teacher-subject').textContent.toLowerCase();
        item.style.display = (name.includes(search) || subj.includes(search)) ? "block" : "none";
    });
}

function switchTab(tabName) {
    document.querySelectorAll('.tab').forEach(tab => tab.classList.remove('active'));
    document.querySelector(`.tab[onclick="switchTab('${tabName}')"]`).classList.add('active');

    document.querySelectorAll('.tab-content').forEach(t => t.classList.remove('active'));
    document.getElementById(`${tabName}-tab`).classList.add('active');
}

// Загрузка списка классов
async function loadClasses() {
    try {
        const response = await fetch('/api/school-classes', {
            headers: { 'Authorization': `Bearer ${token}` }
        });

        if (response.ok) {
            const classes = await response.json();
            displayClasses(classes);
        }
    } catch (error) {
        console.error('Error loading classes:', error);
    }
}

function displayClasses(classes) {
    const classSelect = document.getElementById('classSelect');
    const lessonClass = document.getElementById('lessonClass');

    classes.forEach(cls => {
        const option = document.createElement('option');
        option.value = cls.id;
        option.textContent = cls.name;

        classSelect.appendChild(option.cloneNode(true));
        lessonClass.appendChild(option);
    });
}

async function loadClassSchedule() {
    const classId = document.getElementById('classSelect').value;
    const date = document.getElementById('scheduleDate').value;
    if (!classId) return;

    currentClassId = classId;
    const className = document.getElementById('classSelect').selectedOptions[0].text;

    document.getElementById('selected-class-name').textContent = `Расписание: ${className}`;

    document.getElementById('no-selection').style.display = 'none';
    document.getElementById('teacher-schedule').style.display = 'none';
    document.getElementById('class-schedule').style.display = 'block';

    document.getElementById('class-schedule-content').innerHTML = `
        <div class="loading">Функциональность расписания классов в разработке</div>
    `;
}

async function showAddLessonModal() {
    if (!currentTeacherId) {
        showError('Сначала выберите учителя');
        return;
    }

    await loadSubjects();

    document.getElementById('lessonSubject').value = '';
    document.getElementById('lessonClass').value = '';
    document.getElementById('lessonClassroom').value = '';
    document.getElementById('lessonNumber').value = '1';
    document.getElementById('lessonDate').value = new Date().toISOString().split('T')[0];

    document.getElementById('addLessonModal').style.display = 'block';
}

function hideAddLessonModal() {
    document.getElementById('addLessonModal').style.display = 'none';
}

async function loadSubjects() {
    try {
        const response = await fetch('/api/subjects', {
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });

        if (response.ok) {
            const subjects = await response.json();
            const subjectSelect = document.getElementById('lessonSubject');
            subjectSelect.innerHTML = '<option value="">-- Выберите предмет --</option>';

            subjects.forEach(subject => {
                const option = document.createElement('option');
                option.value = subject.id;
                option.textContent = subject.name;
                subjectSelect.appendChild(option);
            });
        } else {
            showError('Ошибка загрузки предметов');
        }
    } catch (error) {
        showError('Ошибка соединения при загрузке предметов');
    }
}

async function addLesson() {
    const lessonDate = document.getElementById('lessonDate').value;
    const lessonNumber = document.getElementById('lessonNumber').value;
    const subjectId = document.getElementById('lessonSubject').value;
    const classId = document.getElementById('lessonClass').value;
    const classroom = document.getElementById('lessonClassroom').value;

    if (!lessonDate || !subjectId || !classId || !classroom) {
        showError('Заполните все обязательные поля');
        return;
    }

    try {
        const dayIdResponse = await fetch('/api/admin/schedule/day-id', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${token}`
            },
            body: JSON.stringify({ date: lessonDate, classId: classId })
        });

        if (!dayIdResponse.ok) {
            const error = await dayIdResponse.json();
            throw new Error(error.error || 'Ошибка создания расписания');
        }

        const { dayId } = await dayIdResponse.json();

        const lessonData = {
            teacherId: currentTeacherId,
            dayId: dayId,
            lessonNumber: parseInt(lessonNumber),
            startTime: getStartTime(lessonNumber),
            endTime: getEndTime(lessonNumber),
            subjectId: parseInt(subjectId),
            classId: parseInt(classId),
            classroom: classroom
        };

        const response = await fetch('/api/admin/schedule/teacher', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${token}`
            },
            body: JSON.stringify(lessonData)
        });

        if (response.ok) {
            showSuccess('Урок успешно добавлен');
            hideAddLessonModal();
            loadTeacherSchedule();
        } else {
            const error = await response.json();
            showError(error.error || 'Ошибка добавления урока');
        }

    } catch (error) {
        showError('Ошибка: ' + error.message);
    }
}

async function deleteLesson(lessonId) {
    if (!confirm('Вы уверены, что хотите удалить этот урок?')) return;

    try {
        const response = await fetch(`/api/admin/schedule/teacher/lesson/${lessonId}`, {
            method: 'DELETE',
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });

        if (response.ok) {
            showSuccess('Урок успешно удален');
            loadTeacherSchedule();
        } else {
            showError('Ошибка удаления урока');
        }
    } catch (error) {
        showError('Ошибка соединения');
    }
}

function getStartTime(number) {
    const times = {
        1: '08:00:00', 2: '08:45:00', 3: '09:35:00', 4: '10:20:00',
        5: '11:10:00', 6: '11:55:00', 7: '12:45:00', 8: '13:30:00'
    };
    return times[number];
}

function getEndTime(number) {
    const times = {
        1: '08:40:00', 2: '09:25:00', 3: '10:15:00', 4: '11:00:00',
        5: '11:50:00', 6: '12:35:00', 7: '13:25:00', 8: '14:10:00'
    };
    return times[number];
}

function showSuccess(message) {
    const alert = document.createElement('div');
    alert.className = 'alert alert-success';
    alert.textContent = message;
    document.querySelector('.main-content').prepend(alert);
    setTimeout(() => alert.remove(), 3000);
}

function showError(message) {
    const alert = document.createElement('div');
    alert.className = 'alert alert-error';
    alert.textContent = message;
    document.querySelector('.main-content').prepend(alert);
    setTimeout(() => alert.remove(), 3000);
}

function goBack() {
    window.history.back();
}

function logout() {
    localStorage.removeItem('token');
    window.location.href = '/login.html';
}
