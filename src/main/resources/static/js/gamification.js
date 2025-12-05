const token = localStorage.getItem('token');

let userStats = null;
let achievements = [];
let leaderboard = [];
let classes = [];

// ГРАФИКИ
let xpChart = null;
let achievementChart = null;

document.addEventListener('DOMContentLoaded', function () {
    if (!token) {
        window.location.href = 'login.html';
        return;
    }

    loadUserStats();
    loadAchievements();
    loadClasses();
    loadLeaderboard();
});

// ============================
//        СТУДЕНТ СТАТИСТИКА
// ============================

async function loadUserStats() {
    try {
        const response = await fetch('/api/gamification/student/stats', {
            headers: { 'Authorization': `Bearer ${token}` }
        });

        if (response.ok) {
            userStats = await response.json();
            updateUserStats();
            drawCharts();
        } else {
            throw new Error('Failed to load stats');
        }
    } catch (error) {
        console.error('Error loading user stats:', error);
        showError('Ошибка загрузки статистики');
    }
}

function updateUserStats() {
    if (!userStats) return;

    document.getElementById('currentLevel').textContent = userStats.level;
    document.getElementById('currentXP').textContent = userStats.currentLevelXp;
    document.getElementById('nextLevelXP').textContent = userStats.nextLevelXp;
    document.getElementById('completedAssignments').textContent = userStats.completedAssignments;
    document.getElementById('perfectAssignments').textContent = userStats.perfectAssignments;
    document.getElementById('currentStreak').textContent = userStats.currentStreak;
    document.getElementById('achievementsCount').textContent =
        `${userStats.achievementsUnlocked}/${userStats.totalAchievements}`;

    let progressPercentage = 0;
    if (userStats.nextLevelXp > 0) {
        progressPercentage = Math.round((userStats.currentLevelXp / userStats.nextLevelXp) * 100);
        progressPercentage = Math.max(0, Math.min(100, progressPercentage)); // от 0 до 100
    }

    const xpBar = document.getElementById('xpProgress');
    if (xpBar) {
        xpBar.style.width = `${progressPercentage}%`;
    }


    document.getElementById('totalXP').textContent = userStats.totalXp;
    document.getElementById('maxStreak').textContent = userStats.maxStreak;
    document.getElementById('rankPosition').textContent = userStats.rank || '-';

    const completionRate =
        userStats.totalAchievements > 0
            ? Math.round((userStats.achievementsUnlocked / userStats.totalAchievements) * 100) + '%'
            : '0%';

    document.getElementById('completionRate').textContent = completionRate;

    updateRecentAchievements();
}

// ============================
//           ДОСТИЖЕНИЯ
// ============================

async function loadAchievements() {
    try {
        const response = await fetch('/api/gamification/student/achievements', {
            headers: { 'Authorization': `Bearer ${token}` }
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

function displayAchievements(filter) {
    const container = document.getElementById('achievementsList');
    const filtered = filter === 'all' ? achievements : achievements.filter(a => a.type === filter);

    if (filtered.length === 0) {
        container.innerHTML = `
            <div class="empty-state">
                <i class="fas fa-medal"></i>
                <div>Достижения не найдены</div>
            </div>
        `;
        return;
    }

    container.innerHTML = '';

    filtered.forEach(achievement => {
        const el = document.createElement('div');
        el.className = `achievement-card ${achievement.unlocked ? 'unlocked' : ''}`;
        el.style.cursor = "pointer";
        el.onclick = () => openAchievementModal?.(achievement);

        el.innerHTML = `
            <div class="achievement-icon">
                <i class="fas ${achievement.unlocked ? 'fa-trophy' : 'fa-lock'}"></i>
            </div>
            <div class="achievement-content">
                <div class="achievement-name">${achievement.name}</div>
                <div class="achievement-desc">${achievement.description}</div>
                <div class="progress-container">
                    <div class="progress-info">
                        <span>${achievement.progress || 0}/${achievement.requiredValue}</span>
                        <span>${achievement.progressPercentage}%</span>
                    </div>
                    <div class="progress-bar">
                        <div class="progress-fill" style="width:${achievement.progressPercentage}%"></div>
                    </div>
                </div>
            </div>
            <div class="achievement-status ${achievement.unlocked ? 'status-unlocked' : 'status-locked'}">
                ${achievement.unlocked ? 'Получено' : 'Заблокировано'}
            </div>
        `;

        container.appendChild(el);
    });
}

function updateAchievementsCount() {
    const unlocked = achievements.filter(a => a.unlocked).length;
    const total = achievements.length;

    // Исправленные строки 153-154:
    const unlockedCountEl = document.getElementById('unlockedCount');
    const totalAchievementsEl = document.getElementById('totalAchievements');

    if (unlockedCountEl) unlockedCountEl.textContent = unlocked;
    if (totalAchievementsEl) totalAchievementsEl.textContent = total;

    const statsBadge = document.getElementById('achievementsStats');
    if (statsBadge) {
        statsBadge.innerHTML =
            `<span class="badge-count">${unlocked}</span> из <span class="badge-total">${total}</span> открыто`;
    }
}
// ============================
//      МОДАЛКА ДОСТИЖЕНИЯ
// ============================

function mapAchievementType(type) {
    switch (type) {
        case 'assignments': return 'Задания';
        case 'perfect_assignments': return 'Отличные работы';
        case 'streak': return 'Серия дней';
        case 'level': return 'Уровень';
        default: return 'Достижение';
    }
}

function openAchievementModal(a) {
    const modal = document.getElementById('achievementModal');
    if (!modal || !a) return;

    // Элементы модалки
    const nameEl = document.getElementById('achievementModalName');
    const typeEl = document.getElementById('achievementModalType');
    const descEl = document.getElementById('achievementModalDesc');
    const xpEl = document.getElementById('achievementModalXP');
    const statusEl = document.getElementById('achievementModalStatus');
    const progressFill = document.getElementById('achievementModalProgressFill');
    const progressText = document.getElementById('achievementModalProgressText');

    // Вычисление прогресса
    const progress = a.progress || 0;
    const required = a.requiredValue || 1;
    const percent = a.progressPercentage != null
        ? a.progressPercentage
        : Math.min(100, Math.round(progress / required * 100));

    // Заполнение значений
    if (nameEl) nameEl.textContent = a.name || "Достижение";
    if (typeEl) typeEl.textContent = mapAchievementType(a.type);
    if (descEl) descEl.textContent = a.description || "";
    if (xpEl) xpEl.textContent = `+${a.xpReward || 0} XP`;

    if (progressFill) {
        progressFill.style.width = `${percent}%`;
    }
    if (progressText) {
        progressText.textContent = `${progress} / ${required} (${percent}%)`;
    }

    // Статус
    if (statusEl) {
        statusEl.textContent = a.unlocked ? "Получено" : "Еще не получено";
        statusEl.classList.toggle("unlocked", !!a.unlocked);
        statusEl.classList.toggle("locked", !a.unlocked);
    }

    // Показать модалку
    modal.classList.remove('hidden');
    modal.classList.add('visible');
}


function closeAchievementModal() {
    const modal = document.getElementById('achievementModal');
    if (!modal) return;

    modal.classList.remove('visible');
    modal.classList.add('hidden');
}



// Обработчики закрытия модалки
document.addEventListener('click', function (e) {
    const modal = document.getElementById('achievementModal');
    if (!modal || modal.classList.contains('hidden')) return;

    if (e.target.classList.contains('achievement-modal-backdrop') ||
        e.target.classList.contains('achievement-modal-close')) {
        closeAchievementModal();
    }
});

document.addEventListener('keydown', function (e) {
    if (e.key === 'Escape') {
        closeAchievementModal();
    }
});


// ============================
//         ЛИДЕРБОРД
// ============================

async function loadClasses() {
    try {
        const response = await fetch('/api/school-classes', {
            headers: { 'Authorization': `Bearer ${token}` }
        });

        if (response.ok) {
            classes = await response.json();
            updateClassFilter();
        }
    } catch (error) {
        console.error('Error loading classes:', error);
    }
}

function updateClassFilter() {
    const select = document.querySelector('#leaderboard-tab .filter-select');
    select.innerHTML = '<option value="">Все классы</option>';

    classes.forEach(cls => {
        const option = document.createElement('option');
        option.value = cls.id;
        option.textContent = cls.name;
        select.appendChild(option);
    });
}

async function loadLeaderboard(classId = '') {
    try {
        const url = classId
            ? `/api/gamification/leaderboard?classId=${classId}`
            : '/api/gamification/leaderboard';

        const response = await fetch(url, {
            headers: { 'Authorization': `Bearer ${token}` }
        });

        if (response.ok) {
            leaderboard = await response.json();
            displayLeaderboard();
        }
    } catch (error) {
        console.error('Error loading leaderboard:', error);
    }
}

function displayLeaderboard() {
    const podium = document.getElementById('podium');
    const list = document.getElementById('leaderboardList');

    podium.innerHTML = '';
    list.innerHTML = '';

    const topThree = leaderboard.slice(0, 3);

    if (topThree.length === 0) {
        podium.innerHTML = `
            <div class="empty-state">
                <i class="fas fa-trophy"></i>
                <div>Нет данных для отображения</div>
            </div>`;
        return;
    }

    topThree.forEach((student, index) => {
        const item = document.createElement('div');
        item.className = `podium-item place-${index + 1}`;

        item.innerHTML = `
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
            <div class="achievements-count"><i class="fas fa-medal"></i> ${student.achievementsCount}</div>
        `;

        podium.appendChild(item);
    });

    leaderboard.slice(3).forEach((student, index) => {
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
            <div class="achievements-count"><i class="fas fa-medal"></i> ${student.achievementsCount}</div>
        `;

        list.appendChild(item);
    });
}

// ============================
//       ПОСЛЕДНИЕ ДОСТИЖЕНИЯ
// ============================

function updateRecentAchievements() {
    const container = document.getElementById('recentAchievements');

    if (!userStats.recentAchievements || userStats.recentAchievements.length === 0) {
        container.innerHTML = `
            <div class="empty-state">
                <i class="fas fa-medal"></i>
                <div>Нет последних достижений</div>
            </div>`;
        return;
    }

    container.innerHTML = '';

    userStats.recentAchievements.forEach(a => {
        const el = document.createElement('div');
        el.className = 'achievement-card unlocked';

        el.innerHTML = `
            <div class="achievement-icon"><i class="fas fa-trophy"></i></div>
            <div class="achievement-content">
                <div class="achievement-name">${a.name}</div>
                <div class="achievement-desc">${a.description}</div>
            </div>
            <div class="achievement-status status-unlocked">Получено</div>
        `;

        container.appendChild(el);
    });
}

// ============================
//           ГРАФИКИ
// ============================

function drawCharts() {
    drawXpHistoryChart();
    drawAchievementsChart();
}

function drawXpHistoryChart() {
    const ctx = document.getElementById('xpHistoryChart');

    if (!ctx) return;

    const xp = userStats.totalXp;
    const next = userStats.nextLevelXp;

    if (xpChart) xpChart.destroy();

    xpChart = new Chart(ctx, {
        type: 'line',
        data: {
            labels: ['Начало', 'Сейчас', 'Следующий уровень'],
            datasets: [{
                label: 'XP',
                data: [0, xp, next],
                borderWidth: 3,
                borderColor: '#4b7bec',
                fill: false,
                tension: 0.3
            }]
        },
        options: {
            responsive: true,
            scales: { y: { beginAtZero: true } }
        }
    });
}

function drawAchievementsChart() {
    const ctx = document.getElementById('achievementsChart');
    if (!ctx) return;

    const unlocked = achievements.filter(a => a.unlocked).length;
    const locked = achievements.length - unlocked;

    if (achievementChart) achievementChart.destroy();

    achievementChart = new Chart(ctx, {
        type: 'pie',
        data: {
            labels: ['Открыто', 'Закрыто'],
            datasets: [{
                data: [unlocked, locked],
                backgroundColor: ['#20bf6b', '#eb3b5a']
            }]
        },
        options: { responsive: true }
    });
}

// ============================
//          ВСПОМОГАТЕЛЬНОЕ
// ============================

function switchTab(name) {
    document.querySelectorAll('.tab-content').forEach(tab => tab.classList.remove('active'));
    document.getElementById(name + '-tab').classList.add('active');

    document.querySelectorAll('.tab-btn').forEach(btn => btn.classList.remove('active'));
    event.target.classList.add('active');
}

function getInitials(name) {
    return name.split(' ').map(n => n[0]).join('').toUpperCase();
}

function showError(message) {
    console.error(message);
}
