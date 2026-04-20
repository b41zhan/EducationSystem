document.addEventListener('DOMContentLoaded', async () => {
    await initParentDashboard();
});

let activeChildId = null;
let scheduleMode = "day";
let selectedDate = new Date();

async function initParentDashboard() {
    try {
        wireParentUI();
        await loadChildren();

        if (activeChildId) {
            await refreshAllBlocks();
        } else {
            renderEmptyState();
        }
    } catch (e) {
        console.error("initParentDashboard error:", e);
        showParentError("Не удалось загрузить родительский кабинет");
    }
}

function wireParentUI() {
    const childSelect = document.getElementById('activeChildSelect');
    if (childSelect) {
        childSelect.onchange = async function () {
            const value = this.value ? Number(this.value) : null;
            activeChildId = Number.isNaN(value) ? null : value;

            if (activeChildId) {
                await refreshAllBlocks();
            } else {
                renderEmptyState();
            }
        };
    }

    const dateInput = document.getElementById('scheduleDate');
    if (dateInput) {
        dateInput.valueAsDate = selectedDate;

        dateInput.onchange = async () => {
            selectedDate = dateInput.valueAsDate || new Date();
            await loadSchedule();
        };
    }

    const btnDayMode = document.getElementById('btnDayMode');
    const btnWeekMode = document.getElementById('btnWeekMode');

    if (btnDayMode) {
        btnDayMode.onclick = async () => {
            scheduleMode = "day";
            btnDayMode.classList.add('active');
            if (btnWeekMode) btnWeekMode.classList.remove('active');
            await loadSchedule();
        };
    }

    if (btnWeekMode) {
        btnWeekMode.onclick = async () => {
            scheduleMode = "week";
            btnWeekMode.classList.add('active');
            if (btnDayMode) btnDayMode.classList.remove('active');
            await loadSchedule();
        };
    }

    const gradesLimit = document.getElementById('gradesLimit');
    if (gradesLimit) {
        gradesLimit.onchange = async () => {
            await loadGrades();
        };
    }
}

async function refreshAllBlocks() {
    await Promise.all([
        loadSchedule(),
        loadGrades(),
        loadNotifications()
    ]);
}

function renderEmptyState() {
    const scheduleList = document.getElementById('scheduleList');
    const gradesTbody = document.getElementById('gradesTbody');
    const notificationsList = document.getElementById('notificationsList');

    if (scheduleList) {
        scheduleList.innerHTML = `<p>У вас пока нет назначенных детей.</p>`;
    }

    if (gradesTbody) {
        gradesTbody.innerHTML = `<tr><td colspan="3">Нет данных</td></tr>`;
    }

    if (notificationsList) {
        notificationsList.innerHTML = `<p>Нет уведомлений.</p>`;
    }
}

/* =========================
   1) Мои дети
========================= */

async function loadChildren() {
    const children = await ApiService.get('/parent/children');
    const arr = Array.isArray(children) ? children : [];

    renderChildrenCards(arr);
    renderChildSelect(arr);
}

function renderChildrenCards(children) {
    const box = document.getElementById('childrenCards');
    if (!box) return;

    box.innerHTML = '';

    if (!children || children.length === 0) {
        box.innerHTML = `<p>У вас пока нет назначенных детей.</p>`;
        return;
    }

    children.forEach(ch => {
        const div = document.createElement('div');
        div.className = 'child-card';
        div.innerHTML = `
            <div class="title">${escapeHtml(ch.fio ?? '-')}</div>
            <div class="meta">${escapeHtml(ch.className ?? '-')} • ${escapeHtml(ch.schoolName ?? '-')}</div>
            <div class="actions">
                <button class="btn" data-id="${ch.id}">Подробнее</button>
            </div>
        `;

        div.querySelector('button').onclick = async () => {
            activeChildId = ch.id;

            const select = document.getElementById('activeChildSelect');
            if (select) {
                select.value = String(ch.id);
            }

            await refreshAllBlocks();
        };

        box.appendChild(div);
    });
}

function renderChildSelect(children) {
    const sel = document.getElementById('activeChildSelect');
    if (!sel) return;

    sel.innerHTML = '';

    if (!children || children.length === 0) {
        sel.innerHTML = `<option value="">Нет детей</option>`;
        activeChildId = null;
        return;
    }

    children.forEach(ch => {
        sel.innerHTML += `<option value="${ch.id}">${escapeHtml(ch.fio ?? '-')} (${escapeHtml(ch.className ?? '-')})</option>`;
    });

    if (!activeChildId || !children.some(ch => ch.id === activeChildId)) {
        activeChildId = children[0].id;
    }

    sel.value = String(activeChildId);
}

/* =========================
   2) Расписание
========================= */

async function loadSchedule() {
    const list = document.getElementById('scheduleList');
    if (!list) return;

    if (!activeChildId) {
        list.innerHTML = `<p>Выберите ребёнка</p>`;
        return;
    }

    if (scheduleMode === "day") {
        const dateStr = toYMD(selectedDate);
        const lessons = await ApiService.get(`/parent/children/${activeChildId}/schedule?date=${dateStr}`);
        renderDaySchedule(list, lessons, selectedDate);
        return;
    }

    const weekStart = getMonday(selectedDate);
    const weekStr = toYMD(weekStart);
    const weekMap = await ApiService.get(`/parent/children/${activeChildId}/schedule?weekStart=${weekStr}`);
    renderWeekSchedule(list, weekMap, weekStart);
}

function toYMD(d) {
    const year = d.getFullYear();
    const month = String(d.getMonth() + 1).padStart(2, '0');
    const day = String(d.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
}

function getMonday(d) {
    const x = new Date(d);
    const day = x.getDay();
    const diff = (day === 0 ? -6 : 1 - day);
    x.setDate(x.getDate() + diff);
    x.setHours(0, 0, 0, 0);
    return x;
}

function renderDaySchedule(container, lessons, dateObj) {
    const arr = Array.isArray(lessons) ? lessons : [];
    const dateTitle = formatDisplayDate(dateObj);

    let html = `
        <div class="schedule-header">
            <h3>${dateTitle}</h3>
        </div>
    `;

    if (arr.length === 0) {
        html += `
            <div class="no-schedule">
                <div class="no-schedule-icon">📅</div>
                <div class="no-schedule-text">На этот день уроков нет</div>
            </div>
        `;
        container.innerHTML = html;
        return;
    }

    arr.sort((a, b) => (a.lessonNumber ?? 0) - (b.lessonNumber ?? 0));

    html += `<div class="lessons-list">`;

    arr.forEach(lesson => {
        html += `
            <div class="lesson-item">
                <div class="lesson-time">
                    <div class="lesson-number">${lesson.lessonNumber ?? ''} урок</div>
                    <div class="time-range">${formatTime(lesson.startTime)} - ${formatTime(lesson.endTime)}</div>
                </div>

                <div class="lesson-info">
                    <div class="subject-name">${escapeHtml(lesson.subjectName ?? '-')}</div>
                    <div class="lesson-details">
                        <span class="classroom"><i>🏫</i> ${escapeHtml(lesson.classroom ?? '-')}</span>
                        ${lesson.teacherName ? `<span class="teacher"><i>👤</i> ${escapeHtml(lesson.teacherName)}</span>` : ''}
                    </div>
                </div>
            </div>
        `;
    });

    html += `</div>`;
    container.innerHTML = html;
}

function renderWeekSchedule(container, weekMap, weekStart) {
    const days = [];

    for (let i = 0; i < 7; i++) {
        const d = new Date(weekStart);
        d.setDate(weekStart.getDate() + i);
        const key = toYMD(d);
        days.push({
            date: d,
            key,
            lessons: (weekMap && weekMap[key]) ? weekMap[key] : []
        });
    }

    let html = `<div class="week-grid">`;

    days.forEach(day => {
        const title = day.date.toLocaleDateString('ru-RU', {
            weekday: 'long',
            day: 'numeric',
            month: 'long'
        });

        html += `
            <div class="week-day">
                <div class="week-day-head">${title}</div>
                <div class="week-day-body">
        `;

        if (!day.lessons || day.lessons.length === 0) {
            html += `<div class="week-empty">Нет уроков</div>`;
        } else {
            const arr = [...day.lessons].sort((a, b) => (a.lessonNumber ?? 0) - (b.lessonNumber ?? 0));

            arr.forEach(ls => {
                html += `
                    <div class="week-lesson">
                        <div class="wl-time">${formatTime(ls.startTime)}-${formatTime(ls.endTime)}</div>
                        <div class="wl-sub">${escapeHtml(ls.subjectName ?? '-')}</div>
                        <div class="wl-meta">${escapeHtml(ls.className ?? '')} • ${escapeHtml(ls.classroom ?? '')}</div>
                    </div>
                `;
            });
        }

        html += `</div></div>`;
    });

    html += `</div>`;
    container.innerHTML = html;
}

function formatTime(t) {
    if (!t) return '';
    return String(t).substring(0, 5);
}

function formatDisplayDate(d) {
    return d.toLocaleDateString('ru-RU', {
        weekday: 'long',
        year: 'numeric',
        month: 'long',
        day: 'numeric'
    });
}

/* =========================
   3) Оценки
========================= */

async function loadGrades() {
    const tbody = document.getElementById('gradesTbody');
    if (!tbody) return;

    if (!activeChildId) {
        tbody.innerHTML = `<tr><td colspan="3">Выберите ребёнка</td></tr>`;
        return;
    }

    const limit = Number(document.getElementById('gradesLimit')?.value || 20);
    const rows = await ApiService.get(`/parent/children/${activeChildId}/grades?limit=${limit}`);
    const arr = Array.isArray(rows) ? rows : [];

    if (arr.length === 0) {
        tbody.innerHTML = `<tr><td colspan="3">Оценок пока нет</td></tr>`;
        return;
    }

    tbody.innerHTML = arr.map(r => `
        <tr>
            <td>${formatDateTime(r.date)}</td>
            <td>${escapeHtml(r.subject ?? '-')}</td>
            <td>${renderGradeBadge(r.grade)}</td>
        </tr>
    `).join('');
}

function formatDateTime(dt) {
    if (!dt) return '-';

    const d = new Date(dt);
    if (isNaN(d.getTime())) return String(dt);

    return d.toLocaleString('ru-RU');
}

function renderGradeBadge(g) {
    if (g == null) return '-';
    const cls = `grade-badge grade-${g}`;
    return `<span class="${cls}">${g}</span>`;
}

/* =========================
   4) Уведомления
========================= */

async function loadNotifications() {
    const box = document.getElementById('notificationsList');
    if (!box) return;

    const items = await ApiService.get('/parent/notifications');
    const arr = Array.isArray(items) ? items : [];

    if (arr.length === 0) {
        box.innerHTML = `<p>Нет новых уведомлений.</p>`;
        return;
    }

    box.innerHTML = arr.map(n => `
        <div class="notif">
            <div class="title">${escapeHtml(n.title ?? 'Уведомление')}</div>
            <div class="text">${escapeHtml(n.text ?? '')}</div>
            <div class="meta">${n.date ? formatDateTime(n.date) : ''}</div>
        </div>
    `).join('');
}

/* =========================
   Helpers
========================= */

function escapeHtml(value) {
    return String(value ?? '')
        .replace(/&/g, "&amp;")
        .replace(/</g, "&lt;")
        .replace(/>/g, "&gt;")
        .replace(/"/g, "&quot;")
        .replace(/'/g, "&#039;");
}

function showParentError(message) {
    console.error(message);
}