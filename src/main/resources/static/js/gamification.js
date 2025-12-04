const token = localStorage.getItem('token');

// Основные данные
let userStats = null;
let achievements = [];
let leaderboard = [];
let classes = [];

// Инициализация при загрузке страницы
document.addEventListener('DOMContentLoaded', function() {
    if (!token) {
        window.location.href = 'login.html';
        return;
    }

    loadUserStats();
    loadAchievements();
    loadClasses();
    loadLeaderboard();
});

// Загрузка статистики пользователя
async function loadUserStats() {
    try {
        const response = await fetch('/api/gamification/student/stats', {
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });

        if (response.ok) {
            userStats = await response.json();
            updateUserStats();
        } else {
            throw new Error('Failed to load stats');
        }
    } catch (error) {
        console.error('Error loading user stats:', error);
        showError('Ошибка загрузки статистики');
    }
}

// Обновление статистики на странице
function updateUserStats() {
    if (!userStats) return;

    // Основная статистика
    document.getElementById('currentLevel').textContent = userStats.level;
    document.getElementById('currentXP').textContent = userStats.currentLevelXp;
    document.getElementById('nextLevelXP').textContent = userStats.nextLevelXp;
    document.getElementById('completedAssignments').textContent = userStats.completedAssignments;
    document.getElementById('perfectAssignments').textContent = userStats.perfectAssignments;
    document.getElementById('currentStreak').textContent = userStats.currentStreak;
    document.getElementById('achievementsCount').textContent = `${userStats.achievementsUnlocked}/${userStats.totalAchievements}`;

    // Прогресс бар
    const progressPercentage = Math.round((userStats.currentLevelXp / userStats.nextLevelXp) * 100);
    document.getElementById('xpProgress').style.width = `${progressPercentage}%`;

    // Подробная статистика
    document.getElementById('totalXP').textContent = userStats.totalXp;
    document.getElementById('maxStreak').textContent = userStats.maxStreak;
    document.getElementById('rankPosition').textContent = userStats.rank || '-';
    document.getElementById('completionRate').textContent =
        userStats.totalAchievements > 0
            ? Math.round((userStats.achievementsUnlocked / userStats.totalAchievements) * 100) + '%'
            : '0%';

    // Последние достижения
    updateRecentAchievements();
}

// Загрузка достижений
async function loadAchievements() {
    try {
        const response = await fetch('/api/gamification/student/achievements', {
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });

        if (response.ok) {
            achievements = await response.json();
            displayAchievements('all');
            updateAchievementsCount();
        }
    } catch (error) {
        console.error('Error loading achievements:', error);
        document.getElementById('achievementsList').innerHTML = `
            <div class="empty-state">
                <i class="fas fa-exclamation-circle"></i>
                <div>Не удалось загрузить достижения</div>
            </div>
        `;
    }
}

// Отображение достижений
function displayAchievements(filter) {
    const container = document.getElementById('achievementsList');
    const filteredAchievements = filter === 'all'
        ? achievements
        : achievements.filter(a => a.type === filter);

    if (filteredAchievements.length === 0) {
        container.innerHTML = `
            <div class="empty-state">
                <i class="fas fa-medal"></i>
                <div>Достижения не найдены</div>
            </div>
        `;
        return;
    }

    container.innerHTML = '';

    filteredAchievements.forEach(achievement => {
        const achievementEl = document.createElement('div');
        achievementEl.className = `achievement-card ${achievement.unlocked ? 'unlocked' : ''}`;

        achievementEl.innerHTML = `
            <div class="achievement-icon">
                <i class="fas ${achievement.unlocked ? 'fa-trophy' : 'fa-lock'}"></i>
            </div>
            <div class="achievement-content">
                <div class="achievement-name">${achievement.name}</div>
                <div class="achievement-desc">${achievement.description}</div>
                <div class="progress-container">
                    <div class="progress-info">
                        <span>${achievement.progress || 0}/${achievement.requiredValue || 1}</span>
                        <span>${achievement.progressPercentage || 0}%</span>
                    </div>
                    <div class="progress-bar">
                        <div class="progress-fill" style="width: ${achievement.progressPercentage || 0}%"></div>
                    </div>
                </div>
            </div>
            <div class="achievement-status ${achievement.unlocked ? 'status-unlocked' : 'status-locked'}">
                ${achievement.unlocked ? 'Получено' : 'Заблокировано'}
            </div>
        `;

        container.appendChild(achievementEl);
    });
}

// Обновление счетчика достижений
function updateAchievementsCount() {
    const unlocked = achievements.filter(a => a.unlocked).length;
    const total = achievements.length;

    document.getElementById('unlockedCount').textContent = unlocked;
    document.getElementById('totalAchievements').textContent = total;

    const statsBadge = document.getElementById('achievementsStats');
    if (statsBadge) {
        statsBadge.innerHTML = `
            <span class="badge-count">${unlocked}</span> из <span class="badge-total">${total}</span> открыто
        `;
    }
}

// Загрузка классов
async function loadClasses() {
    try {
        const response = await fetch('/api/school-classes', {
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });

        if (response.ok) {
            classes = await response.json();
            updateClassFilter();
        }
    } catch (error) {
        console.error('Error loading classes:', error);
    }
}

// Обновление фильтра классов
function updateClassFilter() {
    const select = document.querySelector('#leaderboard-tab .filter-select');
    select.innerHTML = '<option value="">Все классы</option>';

    classes.forEach(schoolClass => {
        const option = document.createElement('option');
        option.value = schoolClass.id;
        option.textContent = schoolClass.name;
        select.appendChild(option);
    });
}

// Загрузка таблицы лидеров
async function loadLeaderboard(classId = '') {
    try {
        const url = classId
            ? `/api/gamification/leaderboard?classId=${classId}`
            : '/api/gamification/leaderboard';

        const response = await fetch(url, {
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });

        if (response.ok) {
            leaderboard = await response.json();
            displayLeaderboard();
        }
    } catch (error) {
        console.error('Error loading leaderboard:', error);
        const podium = document.getElementById('podium');
        const leaderboardList = document.getElementById('leaderboardList');

        podium.innerHTML = `
            <div class="empty-state">
                <i class="fas fa-exclamation-circle"></i>
                <div>Не удалось загрузить рейтинг</div>
            </div>
        `;
        leaderboardList.innerHTML = '';
    }
}

// Отображение таблицы лидеров
function displayLeaderboard() {
    const podium = document.getElementById('podium');
    const leaderboardList = document.getElementById('leaderboardList');

    // Подиум (топ-3)
    podium.innerHTML = '';
    const topThree = leaderboard.slice(0, 3);

    if (topThree.length === 0) {
        podium.innerHTML = `
            <div style="grid-column: 1 / -1; text-align: center; padding: 3rem; color: var(--text-light);">
                <i class="fas fa-trophy" style="font-size: 3rem; opacity: 0.3; margin-bottom: 1rem;"></i>
                <div>Нет данных для отображения</div>
            </div>
        `;
        leaderboardList.innerHTML = '';
        return;
    }

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
                <i class="fas fa-medal"></i> ${student.achievementsCount || 0}
            </div>
        `;

        podium.appendChild(podiumItem);
    });

    // Остальные участники
    leaderboardList.innerHTML = '';
    const remainingStudents = leaderboard.slice(3);

    if (remainingStudents.length === 0) {
        leaderboardList.innerHTML = `
            <div style="text-align: center; padding: 2rem; color: var(--text-light);">
                <i class="fas fa-user-friends" style="font-size: 2rem; opacity: 0.3; margin-bottom: 0.5rem;"></i>
                <div>Другие участники отсутствуют</div>
            </div>
        `;
        return;
    }

    remainingStudents.forEach((student, index) => {
        const leaderboardItem = document.createElement('div');
        leaderboardItem.className = 'leaderboard-item';

        leaderboardItem.innerHTML = `
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
                <i class="fas fa-medal"></i> ${student.achievementsCount || 0}
            </div>
        `;

        leaderboardList.appendChild(leaderboardItem);
    });
}

// Обновление последних достижений
function updateRecentAchievements() {
    if (!userStats || !userStats.recentAchievements || userStats.recentAchievements.length === 0) {
        const container = document.getElementById('recentAchievements');
        container.innerHTML = `
            <div class="empty-state">
                <i class="fas fa-medal"></i>
                <div>Нет последних достижений</div>
            </div>
        `;
        return;
    }

    const container = document.getElementById('recentAchievements');
    container.innerHTML = '';

    userStats.recentAchievements.forEach(achievement => {
        const achievementEl = document.createElement('div');
        achievementEl.className = 'achievement-card unlocked';

        achievementEl.innerHTML = `
            <div class="achievement-icon">
                <i class="fas fa-trophy"></i>
            </div>
            <div class="achievement-content">
                <div class="achievement-name">${achievement.name}</div>
                <div class="achievement-desc">${achievement.description}</div>
            </div>
            <div class="achievement-status status-unlocked">Получено</div>
        `;

        container.appendChild(achievementEl);
    });
}

// Вспомогательные функции
function switchTab(tabName) {
    // Скрыть все табы
    document.querySelectorAll('.tab-content').forEach(tab => {
        tab.classList.remove('active');
    });

    // Убрать активный класс со всех кнопок
    document.querySelectorAll('.tab-btn').forEach(btn => {
        btn.classList.remove('active');
    });

    // Показать выбранный таб
    document.getElementById(`${tabName}-tab`).classList.add('active');

    // Добавить активный класс к кнопке
    event.target.classList.add('active');
}

function filterAchievements(type) {
    displayAchievements(type);
}

function getInitials(name) {
    return name.split(' ').map(n => n[0]).join('').toUpperCase().substring(0, 2);
}

function showError(message) {
    // Можно заменить на более красивый toast
    console.error(message);
}

// Автоматическое обновление данных каждые 30 секунд
setInterval(() => {
    loadUserStats();
    loadLeaderboard();
}, 30000);