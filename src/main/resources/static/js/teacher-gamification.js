const token = localStorage.getItem('token');
let currentClassId = null;
let classStudents = [];

document.addEventListener('DOMContentLoaded', function() {
    if (!token) {
        window.location.href = 'login.html';
        return;
    }

    loadTeacherClasses();
});

// Загрузка классов учителя
async function loadTeacherClasses() {
    try {
        const response = await fetch('/api/statistics/teacher/classes', {
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });

        if (response.ok) {
            const classes = await response.json();
            updateClassSelector(classes);
        }
    } catch (error) {
        console.error('Error loading teacher classes:', error);
    }
}

// Обновление селектора
function updateClassSelector(classes) {
    const select = document.querySelector('.filter-select');

    classes.forEach(schoolClass => {
        const option = document.createElement('option');
        option.value = schoolClass.id;
        option.textContent = schoolClass.name;
        select.appendChild(option);
    });
}

// Загрузка статистики класса
async function loadClassStats(classId) {
    currentClassId = classId;

    if (!classId) {
        document.getElementById('classStats').innerHTML = `
            <div class="stat-item loading-stat">
                <i class="fas fa-info-circle"></i>
                <div>Выберите класс</div>
            </div>
        `;
        return;
    }

    try {
        // Показываем загрузку
        document.getElementById('classStats').innerHTML = `
            <div class="stat-item loading-stat">
                <i class="fas fa-spinner fa-spin"></i>
                <div>Загрузка...</div>
            </div>
        `;

        await Promise.all([
            loadLeaderboard(classId),
            loadClassAchievements(classId),
            loadStudentsList(classId)
        ]);

        updateClassStats();
    } catch (error) {
        console.error('Error loading class stats:', error);
        document.getElementById('classStats').innerHTML = `
            <div class="stat-item loading-stat">
                <i class="fas fa-exclamation-triangle"></i>
                <div>Ошибка загрузки</div>
            </div>
        `;
    }
}

// Лидерборд
async function loadLeaderboard(classId) {
    const response = await fetch(`/api/gamification/leaderboard?classId=${classId}`, {
        headers: { 'Authorization': `Bearer ${token}` }
    });

    if (response.ok) {
        classStudents = await response.json();
        displayClassLeaderboard();

        // Обновляем статистику в бейдже
        const statsBadge = document.getElementById('leaderboardStats');
        statsBadge.innerHTML = `
            <span class="badge-count">${classStudents.length}</span> студентов
        `;
    }
}

// Рендер лидерборда
function displayClassLeaderboard() {
    const podium = document.getElementById('classPodium');
    const leaderboard = document.getElementById('classLeaderboard');

    podium.innerHTML = '';
    leaderboard.innerHTML = '';

    if (classStudents.length === 0) {
        podium.innerHTML = `
            <div style="grid-column: 1 / -1; text-align: center; padding: 3rem; color: var(--text-light);">
                <i class="fas fa-trophy" style="font-size: 3rem; opacity: 0.3; margin-bottom: 1rem;"></i>
                <div>Нет данных для отображения</div>
            </div>
        `;
        return;
    }

    const topThree = classStudents.slice(0, 3);

    topThree.forEach((student, index) => {
        const podiumItem = document.createElement('div');
        podiumItem.className = `podium-item place-${index + 1}`;

        podiumItem.innerHTML = `
            <div class="podium-rank">${index + 1}</div>
            <div class="student-avatar">${getInitials(student.studentName)}</div>
            <div class="student-info">
                <div class="student-name">${student.studentName}</div>
                <div class="student-class">${student.className}</div>
            </div>
            <div class="student-stats">
                <div class="xp">${student.totalXp} XP</div>
                <div class="level">Ур. ${student.level}</div>
            </div>
            <div class="achievements-count">
                <i class="fas fa-medal"></i> ${student.achievementsCount}
            </div>
        `;

        podium.appendChild(podiumItem);
    });

    const remaining = classStudents.slice(3);

    if (remaining.length > 0) {
        remaining.forEach((student, index) => {
            const item = document.createElement('div');
            item.className = 'leaderboard-item';

            item.innerHTML = `
                <div class="rank">${index + 4}</div>
                <div class="student-avatar">${getInitials(student.studentName)}</div>
                <div class="student-info">
                    <div class="student-name">${student.studentName}</div>
                    <div class="student-class">${student.className}</div>
                </div>
                <div class="student-stats">
                    <div class="xp">${student.totalXp} XP</div>
                    <div class="level">Ур. ${student.level}</div>
                </div>
                <div class="achievements-count">
                    <i class="fas fa-medal"></i> ${student.achievementsCount}
                </div>
            `;

            leaderboard.appendChild(item);
        });
    } else {
        leaderboard.innerHTML = `
            <div style="text-align: center; padding: 2rem; color: var(--text-light);">
                <i class="fas fa-user-friends" style="font-size: 2rem; opacity: 0.3; margin-bottom: 0.5rem;"></i>
                <div>Другие студенты отсутствуют</div>
            </div>
        `;
    }
}

// Студенты класса
async function loadStudentsList(classId) {
    try {
        const response = await fetch(`/api/statistics/class/${classId}`, {
            headers: { 'Authorization': `Bearer ${token}` }
        });

        if (response.ok) {
            const classStats = await response.json();
            displayStudentsList(classStats.students);
        }
    } catch (error) {
        console.error('Error loading students list:', error);
    }
}

function displayStudentsList(students) {
    const container = document.getElementById('studentsList');
    container.innerHTML = '';

    if (students.length === 0) {
        container.innerHTML = `
            <div style="text-align: center; padding: 3rem; color: var(--text-light);">
                <i class="fas fa-user-graduate" style="font-size: 3rem; opacity: 0.3; margin-bottom: 1rem;"></i>
                <div>Нет данных о студентах</div>
            </div>
        `;
        return;
    }

    students.forEach((student, index) => {
        const item = document.createElement('div');
        item.className = 'student-item';

        item.innerHTML = `
            <div class="student-rank">${index + 1}</div>
            <div class="student-avatar">${getInitials(student.studentName)}</div>
            <div class="student-main-info">
                <div class="student-name">${student.studentName}</div>
                <div class="student-details">
                    <span>${student.completedAssignments} выполнено</span>
                    <span>${student.averageGrade || 0}% успеваемость</span>
                    <span>${student.totalAssignments} всего заданий</span>
                </div>
            </div>
            <button class="view-details-btn" onclick="viewStudentDetails(${student.studentId})">
                <i class="fas fa-external-link-alt"></i> Подробнее
            </button>
        `;

        container.appendChild(item);
    });
}

// Обновление статистики класса
function updateClassStats() {
    const container = document.getElementById('classStats');

    if (classStudents.length === 0) {
        container.innerHTML = `
            <div class="stat-item">
                <div class="stat-value">0</div>
                <div class="stat-label">Студентов</div>
            </div>
            <div class="stat-item">
                <div class="stat-value">0</div>
                <div class="stat-label">Средний XP</div>
            </div>
            <div class="stat-item">
                <div class="stat-value">0</div>
                <div class="stat-label">Средний уровень</div>
            </div>
            <div class="stat-item">
                <div class="stat-value">0</div>
                <div class="stat-label">Всего достижений</div>
            </div>
        `;
        return;
    }

    const totalStudents = classStudents.length;
    const totalXP = classStudents.reduce((sum, s) => sum + s.totalXp, 0);
    const avgXP = Math.round(totalXP / totalStudents);
    const avgLevel = Math.round(classStudents.reduce((s, t) => s + t.level, 0) / totalStudents);
    const totalAchievements = classStudents.reduce((s, t) => s + t.achievementsCount, 0);

    container.innerHTML = `
        <div class="stat-item">
            <div class="stat-value">${totalStudents}</div>
            <div class="stat-label">Всего студентов</div>
        </div>
        <div class="stat-item">
            <div class="stat-value">${avgXP}</div>
            <div class="stat-label">Средний XP</div>
        </div>
        <div class="stat-item">
            <div class="stat-value">${avgLevel}</div>
            <div class="stat-label">Средний уровень</div>
        </div>
        <div class="stat-item">
            <div class="stat-value">${totalAchievements}</div>
            <div class="stat-label">Всего достижений</div>
        </div>
    `;
}

// Загрузка достижений класса (заглушка)
async function loadClassAchievements(classId) {
    // Заглушка для API
    const achievements = [
        { id: 1, name: 'Первый урок', description: 'Проведен первый урок', unlocked: true },
        { id: 2, name: 'Активный класс', description: 'Весь класс выполнил задание', unlocked: true },
        { id: 3, name: 'Высший балл', description: 'Средняя оценка выше 90%', unlocked: false }
    ];

    displayClassAchievements(achievements);

    // Обновляем статистику в бейдже
    const unlocked = achievements.filter(a => a.unlocked).length;
    const statsBadge = document.getElementById('achievementsStats');
    statsBadge.innerHTML = `
        <span class="badge-count">${unlocked}/${achievements.length}</span> достижений
    `;
}

function displayClassAchievements(achievements) {
    const container = document.getElementById('classAchievements');
    container.innerHTML = '';

    achievements.forEach(achievement => {
        const card = document.createElement('div');
        card.className = `achievement-card ${achievement.unlocked ? 'unlocked' : ''}`;

        card.innerHTML = `
            <div class="achievement-icon">
                <i class="fas fa-medal"></i>
            </div>
            <div class="achievement-content">
                <div class="achievement-name">${achievement.name}</div>
                <div class="achievement-desc">${achievement.description}</div>
                <span class="achievement-status ${achievement.unlocked ? 'status-unlocked' : 'status-locked'}">
                    ${achievement.unlocked ? 'Получено' : 'Не получено'}
                </span>
            </div>
        `;

        container.appendChild(card);
    });
}

// Поиск студентов
function searchStudents(query) {
    const items = document.querySelectorAll('.student-item');
    const term = query.toLowerCase();

    items.forEach(item => {
        const name = item.querySelector('.student-name').textContent.toLowerCase();
        item.style.display = name.includes(term) ? 'flex' : 'none';
    });
}

// Обновление статистики
function refreshStats() {
    if (currentClassId) {
        loadClassStats(currentClassId);
    } else {
        alert('Выберите класс для обновления');
    }
}

function viewStudentDetails(studentId) {
    window.open(`student-gamification-details.html?studentId=${studentId}`, '_blank');
}

function switchTab(tabName) {
    document.querySelectorAll('.tab-content').forEach(tab => tab.classList.remove('active'));
    document.querySelectorAll('.tab-btn').forEach(btn => btn.classList.remove('active'));

    document.getElementById(`${tabName}-tab`).classList.add('active');
    event.target.classList.add('active');
}

function getInitials(name) {
    return name.split(' ').map(n => n[0]).join('').toUpperCase().substring(0, 2);
}