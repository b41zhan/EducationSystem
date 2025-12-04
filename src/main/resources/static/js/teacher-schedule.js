const token = localStorage.getItem('token');
let currentView = 'day';

if (!token) {
    window.location.href = '/login.html';
}

document.addEventListener('DOMContentLoaded', function() {
    document.getElementById('scheduleDate').value = new Date().toISOString().split('T')[0];

    // Устанавливаем начало недели (понедельник)
    const today = new Date();
    const monday = new Date(today);
    monday.setDate(today.getDate() - today.getDay() + (today.getDay() === 0 ? -6 : 1));
    document.getElementById('weekStartDate').value = monday.toISOString().split('T')[0];

    loadSchedule();
});

// Переключение вида
function switchView(view) {
    currentView = view;

    // Обновляем кнопки
    document.querySelectorAll('.toggle-btn').forEach(btn => btn.classList.remove('active'));
    event.target.classList.add('active');

    // Показываем/скрываем элементы управления
    if (view === 'day') {
        document.getElementById('dateGroup').style.display = 'block';
        document.getElementById('weekGroup').style.display = 'none';
    } else {
        document.getElementById('dateGroup').style.display = 'none';
        document.getElementById('weekGroup').style.display = 'block';
    }

    loadSchedule();
}

// Загрузка расписания
async function loadSchedule() {
    const scheduleContent = document.getElementById('schedule-content');
    scheduleContent.innerHTML = '<div class="loading"><i class="fas fa-spinner fa-spin"></i> Загрузка расписания...</div>';

    try {
        let url;
        if (currentView === 'day') {
            const date = document.getElementById('scheduleDate').value;
            url = `/api/teacher/schedule/my/day?date=${date}`;
        } else {
            const startDate = document.getElementById('weekStartDate').value;
            url = `/api/teacher/schedule/my/week?startDate=${startDate}`;
        }

        const response = await fetch(url, {
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });

        if (response.ok) {
            const lessons = await response.json();
            displaySchedule(lessons);
        } else {
            scheduleContent.innerHTML = '<div class="no-lessons"><i class="fas fa-exclamation-circle"></i> Ошибка загрузки расписания</div>';
        }
    } catch (error) {
        scheduleContent.innerHTML = '<div class="no-lessons"><i class="fas fa-wifi-slash"></i> Ошибка соединения</div>';
    }
}

// Отображение расписания
function displaySchedule(lessons) {
    const scheduleContent = document.getElementById('schedule-content');

    if (lessons.length === 0) {
        scheduleContent.innerHTML = '<div class="no-lessons"><i class="fas fa-calendar-times"></i> Нет уроков на выбранный период</div>';
        return;
    }

    if (currentView === 'day') {
        displayDayView(lessons);
    } else {
        displayWeekView(lessons);
    }
}

// Отображение вида "День" - АДАПТИРОВАННАЯ
function displayDayView(lessons) {
    const scheduleContent = document.getElementById('schedule-content');

    // Сортируем уроки по времени
    lessons.sort((a, b) => a.startTime.localeCompare(b.startTime));

    // Получаем дату из первого урока
    const lessonDate = lessons.length > 0 ? lessons[0].date : document.getElementById('scheduleDate').value;
    const dayOfWeek = lessons.length > 0 ? lessons[0].dayOfWeek : '';

    let html = `
        <div class="day-schedule">
            <div class="day-header">
                <span>${formatDate(lessonDate)}</span>
                <span>${dayOfWeek ? getRussianDayOfWeek(dayOfWeek) : ''}</span>
            </div>
            <div class="lessons-list">
    `;

    lessons.forEach(lesson => {
        html += `
            <div class="lesson-item">
                <div class="lesson-time">
                    <i class="far fa-clock"></i> ${lesson.startTime} - ${lesson.endTime}
                </div>
                <div class="lesson-info">
                    <div class="lesson-subject">
                        <i class="fas fa-book"></i> ${lesson.subjectName}
                    </div>
                    <div class="lesson-details">
                        ${lesson.className ? `<span><i class="fas fa-users"></i> ${lesson.className}</span>` : ''}
                        ${lesson.classroom ? `<span><i class="fas fa-door-open"></i> Каб. ${lesson.classroom}</span>` : ''}
                    </div>
                </div>
            </div>
        `;
    });

    html += `
            </div>
        </div>
    `;

    scheduleContent.innerHTML = html;
}

// Отображение вида "Неделя" - АДАПТИРОВАННАЯ
function displayWeekView(lessons) {
    const scheduleContent = document.getElementById('schedule-content');
    const startDate = new Date(document.getElementById('weekStartDate').value);

    // Группируем уроки по датам
    const lessonsByDate = {};
    lessons.forEach(lesson => {
        if (!lessonsByDate[lesson.date]) {
            lessonsByDate[lesson.date] = [];
        }
        lessonsByDate[lesson.date].push(lesson);
    });

    const dayNames = ['Вс', 'Пн', 'Вт', 'Ср', 'Чт', 'Пт', 'Сб'];
    const fullDayNames = ['Воскресенье', 'Понедельник', 'Вторник', 'Среда', 'Четверг', 'Пятница', 'Суббота'];

    let html = `
        <div class="week-schedule">
            <div class="week-grid">
    `;

    // Создаем колонки для каждого дня недели
    for (let i = 0; i < 7; i++) {
        const dayDate = new Date(startDate);
        dayDate.setDate(startDate.getDate() + i);
        const dateString = dayDate.toISOString().split('T')[0];
        const dayIndex = dayDate.getDay();

        html += `
            <div class="day-column">
                <div class="day-title">
                    ${fullDayNames[dayIndex]}<br>
                    <small>${formatDate(dateString)}</small>
                </div>
                <div class="day-lessons">
        `;

        const dayLessons = lessonsByDate[dateString] || [];

        if (dayLessons.length === 0) {
            html += '<div class="no-lessons">Нет уроков</div>';
        } else {
            // Сортируем уроки по времени
            dayLessons.sort((a, b) => a.startTime.localeCompare(b.startTime));

            dayLessons.forEach(lesson => {
                html += `
                    <div class="week-lesson">
                        <div class="time">${lesson.startTime}-${lesson.endTime}</div>
                        <div class="subject">${lesson.subjectName}</div>
                        <div class="details">
                            ${lesson.className || ''} ${lesson.classroom ? `• Каб. ${lesson.classroom}` : ''}
                        </div>
                    </div>
                `;
            });
        }

        html += `
                </div>
            </div>
        `;
    }

    html += `
            </div>
        </div>
    `;

    scheduleContent.innerHTML = html;
}

// Функция для получения русского названия дня недели
function getRussianDayOfWeek(englishDay) {
    const days = {
        'MONDAY': 'Понедельник',
        'TUESDAY': 'Вторник',
        'WEDNESDAY': 'Среда',
        'THURSDAY': 'Четверг',
        'FRIDAY': 'Пятница',
        'SATURDAY': 'Суббота',
        'SUNDAY': 'Воскресенье'
    };
    return days[englishDay] || englishDay;
}

// Форматирование даты
function formatDate(dateString) {
    const date = new Date(dateString);
    return date.toLocaleDateString('ru-RU', {
        day: 'numeric',
        month: 'long',
        year: 'numeric'
    });
}

// Утилиты
function goBack() {
    window.history.back();
}

function logout() {
    localStorage.removeItem('token');
    window.location.href = '/login.html';
}