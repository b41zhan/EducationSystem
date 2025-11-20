// schedule.js
class ScheduleManager {
    constructor() {
        this.currentDate = new Date();
        this.currentView = 'day'; // 'day' или 'week'
        this.currentStudentClass = null;
        this.init();
    }

    async init() {
        await this.loadStudentInfo();
        await this.loadScheduleForToday();
        this.setupEventListeners();
    }

    async loadStudentInfo() {
        try {
            const studentInfo = await ApiService.get('/students/me');
            if (studentInfo.schoolClass) {
                this.currentStudentClass = studentInfo.schoolClass;
                document.getElementById('student-class').textContent = `Класс: ${studentInfo.schoolClass.name}`;
            }
        } catch (error) {
            console.error('Error loading student info:', error);
        }
    }

    async loadScheduleForToday() {
        try {
            const today = this.formatDate(this.currentDate);
            const lessons = await ApiService.get(`/schedule/student/my?date=${today}`);
            this.displayDaySchedule(lessons);
        } catch (error) {
            console.error('Error loading schedule:', error);
            this.showError('Ошибка загрузки расписания');
        }
    }

    async loadWeekSchedule(startDate) {
        try {
            const formattedDate = this.formatDate(startDate);
            const weekData = await ApiService.get(`/schedule/student/week?startDate=${formattedDate}`);
            this.displayWeekSchedule(weekData);
        } catch (error) {
            console.error('Error loading week schedule:', error);
            this.showError('Ошибка загрузки расписания на неделю');
        }
    }

    displayDaySchedule(lessons) {
        const container = document.getElementById('schedule-container');

        // ВСЕГДА показываем навигацию, даже если нет уроков
        let html = `
        <div class="schedule-header">
            <h3>Расписание на ${this.formatDisplayDate(this.currentDate)}</h3>
            <div class="schedule-actions">
                <button onclick="scheduleManager.previousDay()" class="btn-secondary">← Назад</button>
                <button onclick="scheduleManager.today()" class="btn-secondary">Сегодня</button>
                <button onclick="scheduleManager.nextDay()" class="btn-secondary">Вперед →</button>
            </div>
        </div>
    `;

        if (!lessons || lessons.length === 0) {
            html += `
            <div class="no-schedule">
                <div class="no-schedule-icon">📅</div>
                <div class="no-schedule-text">На ${this.formatDisplayDate(this.currentDate)} уроков нет</div>
                <div class="no-schedule-hint">Отдыхайте! 😊</div>
            </div>
        `;
        } else {
            html += `<div class="lessons-list">`;

            // СОРТИРУЕМ уроки по номеру
            lessons.sort((a, b) => a.lessonNumber - b.lessonNumber);

            lessons.forEach(lesson => {
                const isCurrent = this.isCurrentLesson(lesson);
                html += `
                <div class="lesson-item ${isCurrent ? 'current-lesson' : ''}">
                    <div class="lesson-time">
                        <div class="lesson-number">${lesson.lessonNumber} урок</div>
                        <div class="time-range">${this.formatTime(lesson.startTime)} - ${this.formatTime(lesson.endTime)}</div>
                    </div>
                    <div class="lesson-info">
                        <div class="subject-name">${lesson.subjectName}</div>
                        <div class="lesson-details">
                            <span class="classroom">${lesson.classroom}</span>
                            ${lesson.teacherName ? `<span class="teacher">${lesson.teacherName}</span>` : ''}
                        </div>
                    </div>
                    ${isCurrent ? '<div class="current-badge">Сейчас</div>' : ''}
                </div>
            `;
            });

            html += `</div>`;
        }

        container.innerHTML = html;
    }

    displayWeekSchedule(weekData) {
        const container = document.getElementById('schedule-container');

        // ВСЕГДА показываем навигацию, даже если нет данных
        let html = `
        <div class="schedule-header">
            <h3>Расписание на неделю</h3>
            <div class="schedule-actions">
                <button onclick="scheduleManager.previousWeek()" class="btn-secondary">← Предыдущая</button>
                <button onclick="scheduleManager.today()" class="btn-secondary">Сегодня</button>
                <button onclick="scheduleManager.nextWeek()" class="btn-secondary">Следующая →</button>
            </div>
        </div>
    `;

        if (!weekData || weekData.length === 0) {
            html += `
            <div class="no-schedule">
                <div class="no-schedule-icon">📅</div>
                <div class="no-schedule-text">Нет расписания на эту неделю</div>
                <div class="no-schedule-hint">Попробуйте другую дату</div>
            </div>
        `;
        } else {
            html += `<div class="week-schedule">`;

            const days = ['MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY', 'SUNDAY'];
            const dayNames = {
                'MONDAY': 'Понедельник',
                'TUESDAY': 'Вторник',
                'WEDNESDAY': 'Среда',
                'THURSDAY': 'Четверг',
                'FRIDAY': 'Пятница',
                'SATURDAY': 'Суббота',
                'SUNDAY': 'Воскресенье'
            };

            days.forEach(dayName => {
                const dayData = weekData.find(day => day.dayOfWeek === dayName);

                html += `
                <div class="week-day ${dayData && dayData.isHoliday ? 'holiday' : ''}">
                    <div class="day-header">
                        <div class="day-name">${dayNames[dayName]}</div>
                        <div class="day-date">${dayData ? this.formatDisplayDate(new Date(dayData.date)) : ''}</div>
                        ${dayData && dayData.isHoliday ? '<div class="holiday-badge">Выходной</div>' : ''}
                    </div>
            `;

                if (dayData && !dayData.isHoliday && dayData.lessons && dayData.lessons.length > 0) {
                    // СОРТИРУЕМ уроки по номеру для недельного вида тоже
                    const sortedLessons = dayData.lessons.sort((a, b) => a.lessonNumber - b.lessonNumber);

                    sortedLessons.forEach(lesson => {
                        html += `
                        <div class="week-lesson">
                            <div class="lesson-time">${lesson.lessonNumber}.</div>
                            <div class="lesson-subject">${lesson.subjectName}</div>
                            <div class="lesson-classroom">${lesson.classroom}</div>
                        </div>
                    `;
                    });
                } else if (dayData && dayData.isHoliday) {
                    html += `<div class="no-lessons">Выходной день</div>`;
                } else {
                    html += `<div class="no-lessons">Нет уроков</div>`;
                }

                html += `</div>`;
            });

            html += `</div>`;
        }

        container.innerHTML = html;
    }

    // Навигация по дням
    previousDay() {
        this.currentDate.setDate(this.currentDate.getDate() - 1);
        this.loadScheduleForToday();
    }

    nextDay() {
        this.currentDate.setDate(this.currentDate.getDate() + 1);
        this.loadScheduleForToday();
    }

    today() {
        this.currentDate = new Date();
        this.loadScheduleForToday();
    }

    // Навигация по неделям
    previousWeek() {
        this.currentDate.setDate(this.currentDate.getDate() - 7);
        this.loadWeekSchedule(this.getWeekStart(this.currentDate));
    }

    nextWeek() {
        this.currentDate.setDate(this.currentDate.getDate() + 7);
        this.loadWeekSchedule(this.getWeekStart(this.currentDate));
    }

    switchToDayView() {
        this.currentView = 'day';
        document.getElementById('day-view-btn').classList.add('active');
        document.getElementById('week-view-btn').classList.remove('active');
        this.loadScheduleForToday();
    }

    switchToWeekView() {
        this.currentView = 'week';
        document.getElementById('day-view-btn').classList.remove('active');
        document.getElementById('week-view-btn').classList.add('active');
        this.loadWeekSchedule(this.getWeekStart(this.currentDate));
    }

    // Вспомогательные методы
    formatDate(date) {
        return date.toISOString().split('T')[0];
    }

    formatDisplayDate(date) {
        return date.toLocaleDateString('ru-RU', {
            weekday: 'long',
            year: 'numeric',
            month: 'long',
            day: 'numeric'
        });
    }

    formatTime(timeString) {
        return timeString.substring(0, 5); // "08:00"
    }

    isCurrentLesson(lesson) {
        const now = new Date();
        const currentTime = now.getHours() * 60 + now.getMinutes();

        const startTime = this.timeToMinutes(lesson.startTime);
        const endTime = this.timeToMinutes(lesson.endTime);

        return currentTime >= startTime && currentTime <= endTime;
    }

    timeToMinutes(timeString) {
        const [hours, minutes] = timeString.split(':');
        return parseInt(hours) * 60 + parseInt(minutes);
    }

    getWeekStart(date) {
        const day = date.getDay();
        const diff = date.getDate() - day + (day === 0 ? -6 : 1);
        return new Date(date.setDate(diff));
    }

    setupEventListeners() {
        document.getElementById('day-view-btn').addEventListener('click', () => this.switchToDayView());
        document.getElementById('week-view-btn').addEventListener('click', () => this.switchToWeekView());
    }

    showError(message) {
        const container = document.getElementById('schedule-container');
        container.innerHTML = `
            <div class="error-message">
                <div>❌ ${message}</div>
                <button onclick="scheduleManager.loadScheduleForToday()" class="btn-secondary">Попробовать снова</button>
            </div>
        `;
    }
}

// Глобальный экземпляр
const scheduleManager = new ScheduleManager();