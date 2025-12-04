class ScheduleManager {
    constructor() {
        this.currentDate = new Date();
        this.currentView = 'day'; // 'day' или 'week'
        this.currentStudentClass = null;
        this.init();
    }

    async init() {
        await this.loadStudentInfo();
        await this.loadSchedule();
        this.setupEventListeners();
        this.updatePeriodText();
    }

    async loadStudentInfo() {
        try {
            const studentInfo = await ApiService.get('/students/me');
            if (studentInfo.schoolClass) {
                this.currentStudentClass = studentInfo.schoolClass;
            }
        } catch (error) {
            console.error('Error loading student info:', error);
        }
    }

    async loadSchedule() {
        try {
            const container = document.getElementById('schedule-container');
            container.innerHTML = `
                <div class="loading-state">
                    <i>⏳</i>
                    <p>Загрузка расписания...</p>
                </div>
            `;

            if (this.currentView === 'day') {
                await this.loadDaySchedule();
            } else {
                await this.loadWeekSchedule();
            }

            this.updatePeriodText();

        } catch (error) {
            console.error('Error loading schedule:', error);
            this.showError('Ошибка загрузки расписания');
        }
    }

    async loadDaySchedule() {
        const today = this.formatDate(this.currentDate);
        const lessons = await ApiService.get(`/schedule/student/my?date=${today}`);
        this.displayDaySchedule(lessons);
    }

    async loadWeekSchedule() {
        const startDate = this.getWeekStart(this.currentDate);
        const formattedDate = this.formatDate(startDate);
        const weekData = await ApiService.get(`/schedule/student/week?startDate=${formattedDate}`);
        this.displayWeekSchedule(weekData);
    }

    displayDaySchedule(lessons) {
        const container = document.getElementById('schedule-container');
        const dateStr = this.formatDisplayDate(this.currentDate);

        let html = `
            <div class="schedule-header">
                <h3>${dateStr}</h3>
            </div>
        `;

        if (!lessons || lessons.length === 0) {
            html += `
                <div class="no-schedule">
                    <div class="no-schedule-icon">📅</div>
                    <div class="no-schedule-text">На этот день уроков нет</div>
                    <div class="no-schedule-hint">Отдыхайте! 😊</div>
                </div>
            `;
        } else {
            html += `<div class="lessons-list">`;

            // Сортируем уроки по номеру
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
                                <span class="classroom">
                                    <i>🏫</i> ${lesson.classroom}
                                </span>
                                ${lesson.teacherName ? `
                                <span class="teacher">
                                    <i>👤</i> ${lesson.teacherName}
                                </span>
                                ` : ''}
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
        const weekStart = this.getWeekStart(this.currentDate);
        const weekEnd = new Date(weekStart);
        weekEnd.setDate(weekEnd.getDate() + 6);

        let html = `
            <div class="schedule-header">
                <h3>${weekStart.toLocaleDateString('ru-RU', { day: 'numeric', month: 'short' })} - ${weekEnd.toLocaleDateString('ru-RU', { day: 'numeric', month: 'short', year: 'numeric' })}</h3>
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
                            <div class="day-date">${dayData ? new Date(dayData.date).toLocaleDateString('ru-RU', { day: 'numeric', month: 'short' }) : ''}</div>
                            ${dayData && dayData.isHoliday ? '<div class="holiday-badge">Выходной</div>' : ''}
                        </div>
                `;

                if (dayData && !dayData.isHoliday && dayData.lessons && dayData.lessons.length > 0) {
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

    // Навигация
    previous() {
        if (this.currentView === 'day') {
            this.currentDate.setDate(this.currentDate.getDate() - 1);
            this.loadDaySchedule();
        } else {
            this.currentDate.setDate(this.currentDate.getDate() - 7);
            this.loadWeekSchedule();
        }
    }

    next() {
        if (this.currentView === 'day') {
            this.currentDate.setDate(this.currentDate.getDate() + 1);
            this.loadDaySchedule();
        } else {
            this.currentDate.setDate(this.currentDate.getDate() + 7);
            this.loadWeekSchedule();
        }
    }

    goToday() {
        this.currentDate = new Date();
        this.loadSchedule();
    }

    // Переключение видов
    switchToDayView() {
        this.currentView = 'day';
        document.getElementById('day-view-btn').classList.add('active');
        document.getElementById('week-view-btn').classList.remove('active');
        this.loadDaySchedule();
    }

    switchToWeekView() {
        this.currentView = 'week';
        document.getElementById('day-view-btn').classList.remove('active');
        document.getElementById('week-view-btn').classList.add('active');
        this.loadWeekSchedule();
    }

    // Вспомогательные методы
    updatePeriodText() {
        const periodElement = document.getElementById('schedule-period');

        if (this.currentView === 'day') {
            const today = new Date();
            const isToday = this.currentDate.toDateString() === today.toDateString();
            periodElement.textContent = isToday ? 'Сегодня' : this.currentDate.toLocaleDateString('ru-RU', {
                weekday: 'short',
                day: 'numeric',
                month: 'short'
            });
        } else {
            const weekStart = this.getWeekStart(this.currentDate);
            const weekEnd = new Date(weekStart);
            weekEnd.setDate(weekEnd.getDate() + 6);

            periodElement.textContent = `${weekStart.toLocaleDateString('ru-RU', { day: 'numeric', month: 'short' })} - ${weekEnd.toLocaleDateString('ru-RU', { day: 'numeric', month: 'short' })}`;
        }
    }

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
        return timeString ? timeString.substring(0, 5) : '';
    }

    isCurrentLesson(lesson) {
        if (!lesson.startTime || !lesson.endTime) return false;

        const now = new Date();
        const currentTime = now.getHours() * 60 + now.getMinutes();
        const startTime = this.timeToMinutes(lesson.startTime);
        const endTime = this.timeToMinutes(lesson.endTime);

        return currentTime >= startTime && currentTime <= endTime;
    }

    timeToMinutes(timeString) {
        if (!timeString) return 0;
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

        // Добавляем кнопку "Сегодня" в навигацию
        const scheduleNavigation = document.querySelector('.schedule-navigation');
        if (scheduleNavigation) {
            const todayBtn = document.createElement('button');
            todayBtn.className = 'today-btn';
            todayBtn.innerHTML = '<i>⏰</i> Сегодня';
            todayBtn.onclick = () => this.goToday();
            scheduleNavigation.querySelector('.schedule-period').after(todayBtn);
        }
    }

    showError(message) {
        const container = document.getElementById('schedule-container');
        container.innerHTML = `
            <div class="error-message">
                <i>❌</i>
                <p>${message}</p>
                <button onclick="scheduleManager.loadSchedule()" class="btn-action btn-secondary">
                    Попробовать снова
                </button>
            </div>
        `;
    }
}

const scheduleManager = new ScheduleManager();