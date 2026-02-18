document.addEventListener('DOMContentLoaded', async () => {
    await initParentDashboard();
});

let activeChildId = null;
let scheduleDay = "today";

async function initParentDashboard() {
    await loadChildren();
    wireParentUI();

    // если есть активный ребёнок — загрузим данные
    if (activeChildId) {
        await refreshAllBlocks();
    }
}

function wireParentUI() {
    document.getElementById('activeChildSelect').onchange = async function () {
        activeChildId = Number(this.value);
        await refreshAllBlocks();
    };

    document.getElementById('btnToday').onclick = async () => {
        scheduleDay = "today";
        await loadSchedule();
    };

    document.getElementById('btnTomorrow').onclick = async () => {
        scheduleDay = "tomorrow";
        await loadSchedule();
    };

    document.getElementById('gradesLimit').onchange = async () => {
        await loadGrades();
    };
}

async function refreshAllBlocks() {
    await Promise.all([
        loadSchedule(),
        loadGrades(),
        loadNotifications()
    ]);
}

/* =========================
   1) Мои дети
========================= */
async function loadChildren() {
    const children = await ApiService.get('/parent/children');
    renderChildrenCards(children);
    renderChildSelect(children);
}

function renderChildrenCards(children) {
    const box = document.getElementById('childrenCards');
    box.innerHTML = '';

    if (!children || children.length === 0) {
        box.innerHTML = `<p>У вас пока нет привязанных детей.</p>`;
        return;
    }

    children.forEach(ch => {
        const div = document.createElement('div');
        div.className = 'child-card';
        div.innerHTML = `
      <div class="title">${ch.fio}</div>
      <div class="meta">${ch.className ?? '-'} • ${ch.schoolName ?? '-'}</div>
      <div class="actions">
        <button class="btn" data-id="${ch.id}">Открыть профиль</button>
      </div>
    `;
        div.querySelector('button').onclick = async () => {
            activeChildId = ch.id;
            document.getElementById('activeChildSelect').value = String(ch.id);
            await refreshAllBlocks();
        };
        box.appendChild(div);
    });
}

function renderChildSelect(children) {
    const sel = document.getElementById('activeChildSelect');
    sel.innerHTML = '';

    if (!children || children.length === 0) {
        sel.innerHTML = `<option value="">Нет детей</option>`;
        activeChildId = null;
        return;
    }

    children.forEach(ch => {
        sel.innerHTML += `<option value="${ch.id}">${ch.fio} (${ch.className ?? '-'})</option>`;
    });

    // выбираем первого по умолчанию
    activeChildId = children[0].id;
    sel.value = String(activeChildId);
}

/* =========================
   2) Расписание
========================= */
async function loadSchedule() {
    const list = document.getElementById('scheduleList');

    if (!activeChildId) {
        list.innerHTML = `<p>Выберите ребёнка</p>`;
        return;
    }

    const lessons = await ApiService.get(`/parent/children/${activeChildId}/schedule?day=${scheduleDay}`);

    if (!lessons || lessons.length === 0) {
        list.innerHTML = `<p>Расписание пока недоступно (или нет уроков).</p>`;
        return;
    }

    // ожидаем формат:
    // [{timeFrom:"08:00", timeTo:"08:45", subject:"Math", teacher:"Ivanov", room:"203"}]
    list.innerHTML = lessons.map(x => `
    <div class="lesson-row">
      <div class="time">${x.timeFrom ?? ''} - ${x.timeTo ?? ''}</div>
      <div class="info">
        <div class="subject">${x.subject ?? '-'}</div>
        <div class="meta">${x.teacher ?? ''}${x.room ? ' • каб. ' + x.room : ''}</div>
      </div>
    </div>
  `).join('');
}

/* =========================
   3) Оценки
========================= */
async function loadGrades() {
    const tbody = document.getElementById('gradesTbody');

    if (!activeChildId) {
        tbody.innerHTML = `<tr><td colspan="3">Выберите ребёнка</td></tr>`;
        return;
    }

    const limit = Number(document.getElementById('gradesLimit').value || 20);
    const rows = await ApiService.get(`/parent/children/${activeChildId}/grades?limit=${limit}`);

    if (!rows || rows.length === 0) {
        tbody.innerHTML = `<tr><td colspan="3">Оценок пока нет</td></tr>`;
        return;
    }

    tbody.innerHTML = rows.map(r => `
    <tr>
      <td>${formatDateTime(r.date)}</td>
      <td>${r.subject ?? '-'}</td>
      <td>${renderGradeBadge(r.grade)}</td>

    </tr>
  `).join('');
}

function formatDateTime(dt) {
    if (!dt) return '-';
    // если бэк отдаёт ISO-строку
    const d = new Date(dt);
    if (isNaN(d.getTime())) return String(dt);
    return d.toLocaleString();
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
    const items = await ApiService.get('/parent/notifications');

    if (!items || items.length === 0) {
        box.innerHTML = `<p>Нет новых уведомлений.</p>`;
        return;
    }

    box.innerHTML = items.map(n => `
    <div class="notif">
      <div class="title">${n.title ?? 'Уведомление'}</div>
      <div class="text">${n.text ?? ''}</div>
      <div class="meta">${n.date ? formatDateTime(n.date) : ''}</div>
    </div>
  `).join('');
}
