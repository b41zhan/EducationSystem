const token = localStorage.getItem('token');
const urlParams = new URLSearchParams(window.location.search);
const studentId = urlParams.get('studentId');
let studentStats = null;

document.addEventListener('DOMContentLoaded', function() {
    if (!token || !studentId) {
        window.close();
        return;
    }

    loadStudentStats();
});

async function loadStudentStats() {
    try {
        // Показываем состояние загрузки
        showLoadingState();

        const response = await fetch(`/api/gamification/teacher/student/${studentId}/stats`, {
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });

        if (response.ok) {
            const stats = await response.json();
            studentStats = stats;
            displayStudentStats(stats);
        } else {
            showErrorState();
        }
    } catch (error) {
        console.error('Error loading student stats:', error);
        showErrorState();
    }
}

// Отображение статистики студента
function displayStudentStats(stats) {
    // Обновление заголовка
    document.getElementById('studentName').textContent = stats.studentName;
    document.getElementById('className').textContent = stats.className;
    document.getElementById('levelBadge').innerHTML = `
        <i class="fas fa-star"></i> Уровень ${stats.level}
    `;

    // Обновление аватара с инициалами
    const avatar = document.getElementById('studentAvatar');
    avatar.innerHTML = getInitials(stats.studentName);
    avatar.title = stats.studentName;

    // Основная статистика
    updateMainStats(stats);

    // Прогресс уровня
    updateProgressBar(stats);

    // Достижения
    updateAchievements(stats);

    // История активности
    updateActivityHistory(stats.recentActivity || []);
}

// Обновление основной статистики
function updateMainStats(stats) {
    document.getElementById('mainStats').innerHTML = `
        <div class="stat-item">
            <div class="stat-icon">
                <i class="fas fa-bolt"></i>
            </div>
            <div class="stat-value">${stats.totalXp || 0}</div>
            <div class="stat-label">XP</div>
        </div>
        <div class="stat-item">
            <div class="stat-icon">
                <i class="fas fa-check-circle"></i>
            </div>
            <div class="stat-value">${stats.completedAssignments || 0}</div>
            <div class="stat-label">Выполнено заданий</div>
        </div>
        <div class="stat-item">
            <div class="stat-icon">
                <i class="fas fa-star"></i>
            </div>
            <div class="stat-value">${stats.perfectAssignments || 0}</div>
            <div class="stat-label">Отличных работ</div>
        </div>
        <div class="stat-item">
            <div class="stat-icon">
                <i class="fas fa-medal"></i>
            </div>
            <div class="stat-value">${stats.achievementsUnlocked || 0}/${stats.totalAchievements || 0}</div>
            <div class="stat-label">Достижения</div>
        </div>
    `;
}

// Обновление прогресс-бара
function updateProgressBar(stats) {
    const progressPercent = Math.round(((stats.currentLevelXp || 0) / (stats.nextLevelXp || 1)) * 100);

    document.getElementById('progressPercent').textContent = `${progressPercent}%`;
    document.getElementById('progressFill').style.width = `${progressPercent}%`;
    document.getElementById('currentXp').textContent = `${stats.currentLevelXp || 0} XP`;
    document.getElementById('nextLevelXp').textContent = `${stats.nextLevelXp || 0} XP`;
}

// Обновление достижений
function updateAchievements(stats) {
    const unlocked = stats.achievements || [];
    const locked = stats.availableAchievements || [];
    const totalUnlocked = unlocked.length;
    const totalAchievements = totalUnlocked + locked.length;

    // Обновляем счетчик
    document.getElementById('achievementsCount').innerHTML = `
        <span class="count">${totalUnlocked}</span> из <span class="total">${totalAchievements}</span>
    `;

    // Отображаем полученные достижения
    const unlockedGrid = document.getElementById('unlockedAchievementsGrid');
    if (unlocked.length > 0) {
        unlockedGrid.innerHTML = unlocked.map(achievement => `
            <div class="achievement-card unlocked">
                <div class="achievement-icon">
                    <i class="fas fa-medal"></i>
                </div>
                <div class="achievement-content">
                    <div class="achievement-name">${achievement.name}</div>
                    <div class="achievement-desc">${achievement.description}</div>
                    ${achievement.unlockedAt
            ? `<div class="achievement-date">Получено: ${formatDateTime(achievement.unlockedAt)}</div>`
            : ''}
                </div>
                <div class="achievement-status status-unlocked">Получено</div>
            </div>
        `).join('');
    } else {
        unlockedGrid.innerHTML = `
            <div class="no-data">
                <i class="fas fa-medal"></i>
                <div>Нет полученных достижений</div>
            </div>
        `;
    }

    // Отображаем доступные достижения
    const lockedGrid = document.getElementById('lockedAchievementsGrid');
    if (locked.length > 0) {
        lockedGrid.innerHTML = locked.map(achievement => `
            <div class="achievement-card locked">
                <div class="achievement-icon">
                    <i class="fas fa-lock"></i>
                </div>
                <div class="achievement-content">
                    <div class="achievement-name">${achievement.name}</div>
                    <div class="achievement-desc">${achievement.description}</div>
                    ${achievement.requirement ? `<div class="achievement-date">Требуется: ${achievement.requirement}</div>` : ''}
                </div>
                <div class="achievement-status status-locked">Не получено</div>
            </div>
        `).join('');
    } else {
        lockedGrid.innerHTML = `
            <div class="no-data">
                <i class="fas fa-trophy"></i>
                <div>Все достижения получены!</div>
            </div>
        `;
    }
}
function formatDateTime(dateString) {
    const d = new Date(dateString);
    return d.toLocaleString('ru-RU', {
        day: '2-digit',
        month: '2-digit',
        year: '2-digit',
        hour: '2-digit',
        minute: '2-digit'
    });
}
function renderUnlockedAchievements(list) {
    const container = document.getElementById("unlockedAchievementsList");
    container.innerHTML = "";

    list.forEach(a => {
        container.innerHTML += `
            <div class="achievement-card unlocked">
                <div class="ach-icon">
                    <img src="${a.icon}" alt="">
                </div>

                <div class="ach-info">
                    <div class="ach-name">${a.name}</div>
                    <div class="ach-desc">${a.description}</div>

                    ${a.unlockedAt
            ? `<div class="ach-date">Получено: ${formatDateTime(a.unlockedAt)}</div>`
            : ""
        }
                </div>

                <div class="ach-status">Получено</div>
            </div>
        `;
    });
}


// Обновление истории активности
function updateActivityHistory(activities) {
    const activityList = document.getElementById('activityList');

    if (activities.length > 0) {
        activityList.innerHTML = activities.map(activity => `
            <div class="activity-item">
                <div class="activity-icon">
                    ${getActivityIcon(activity.type)}
                </div>
                <div class="activity-content">
                    <div class="activity-title">${activity.title}</div>
                    <div class="activity-desc">${activity.description}</div>
                </div>
                <div class="activity-time">${formatTime(activity.timestamp)}</div>
            </div>
        `).join('');
    } else {
        activityList.innerHTML = `
            <div class="no-data">
                <i class="fas fa-history"></i>
                <div>Нет данных об активности</div>
            </div>
        `;
    }
}

// Состояние загрузки
function showLoadingState() {
    document.getElementById('studentName').textContent = 'Загрузка...';
    document.getElementById('className').textContent = 'Загрузка...';

    document.getElementById('mainStats').innerHTML = `
        <div class="stat-item loading-stat">
            <div class="stat-icon">
                <i class="fas fa-spinner fa-spin"></i>
            </div>
            <div class="stat-value">-</div>
            <div class="stat-label">Загрузка...</div>
        </div>
    `.repeat(4);
}

// Состояние ошибки
function showErrorState() {
    document.getElementById('studentName').textContent = 'Ошибка загрузки';
    document.getElementById('className').textContent = 'Попробуйте позже';

    document.getElementById('mainStats').innerHTML = `
        <div class="no-data">
            <i class="fas fa-exclamation-triangle"></i>
            <div>Ошибка загрузки данных</div>
        </div>
    `;
}

// Переключение вкладок достижений
function switchAchievementsTab(tabName) {
    // Убираем активный класс со всех кнопок
    document.querySelectorAll('.tab-btn').forEach(btn => btn.classList.remove('active'));
    // Добавляем активный класс нажатой кнопке
    event.target.classList.add('active');

    // Скрываем все вкладки
    document.querySelectorAll('.tab-content').forEach(tab => tab.classList.remove('active'));
    // Показываем нужную вкладку
    document.getElementById(`${tabName}-achievements`).classList.add('active');
}

// Вспомогательные функции
function getInitials(name) {
    if (!name) return '?';
    return name.split(' ').map(n => n[0]).join('').toUpperCase().substring(0, 2);
}

function formatDate(dateString) {
    const date = new Date(dateString);
    return date.toLocaleDateString('ru-RU', {
        day: 'numeric',
        month: 'short',
        year: 'numeric'
    });
}

function formatTime(timestamp) {
    const date = new Date(timestamp);
    const now = new Date();
    const diff = now - date;

    if (diff < 60000) return 'только что';
    if (diff < 3600000) return `${Math.floor(diff / 60000)} мин назад`;
    if (diff < 86400000) return `${Math.floor(diff / 3600000)} ч назад`;
    return formatDate(timestamp);
}

function getActivityIcon(type) {
    const icons = {
        'assignment': 'fas fa-tasks',
        'achievement': 'fas fa-medal',
        'level': 'fas fa-star',
        'login': 'fas fa-sign-in-alt',
        'xp': 'fas fa-bolt'
    };
    return `<i class="${icons[type] || 'fas fa-bell'}"></i>`;
}

// Закрытие окна
function closeWindow() {
    window.close();
}