document.addEventListener('DOMContentLoaded', async () => {
    await initParentDashboard();
});

let activeChildId = null;
let scheduleMode = "day";
let selectedDate = new Date();


async function initParentDashboard() {
    try {
        await loadChildren();
        wireParentUI();

        if (activeChildId) {
            await refreshAllBlocks();
        }
    } catch (e) {
        console.error("initParentDashboard error:", e);
    }
}

function wireParentUI() {

    // ===== выбор активного ребёнка =====
    document.getElementById('activeChildSelect').onchange = async function () {
        activeChildId = Number(this.value);
        await refreshAllBlocks();
    };

    // ===== ДАТА =====
    const dateInput = document.getElementById('scheduleDate');

    // если переменная ещё не объявлена — убедись что вверху файла есть:
    // let scheduleMode = "day";
    // let selectedDate = new Date();

    dateInput.valueAsDate = selectedDate;

    dateInput.onchange = async () => {
        selectedDate = dateInput.valueAsDate || new Date();
        await loadSchedule();
    };

    // ===== РЕЖИМ ДЕНЬ =====
    document.getElementById('btnDayMode').onclick = async () => {
        scheduleMode = "day";

        document.getElementById('btnDayMode').classList.add('active');
        document.getElementById('btnWeekMode').classList.remove('active');

        await loadSchedule();
    };

    // ===== РЕЖИМ НЕДЕЛЯ =====
    document.getElementById('btnWeekMode').onclick = async () => {
        scheduleMode = "week";

        document.getElementById('btnWeekMode').classList.add('active');
        document.getElementById('btnDayMode').classList.remove('active');

        await loadSchedule();
    };

    // ===== ЛИМИТ ОЦЕНОК =====
    document.getElementById('gradesLimit').onchange = async () => {
        await loadGrades();
    };
}


function setActiveTab(activeId) {
    document.getElementById('btnToday').classList.remove('active');
    document.getElementById('btnTomorrow').classList.remove('active');
    document.getElementById(activeId).classList.add('active');
}

async function refreshAllBlocks() {
    await Promise.all([loadSchedule(), loadGrades(), loadNotifications()]);
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
    box.innerHTML = '';

    if (!children || children.length === 0) {
        box.innerHTML = `<p>У вас пока нет привязанных детей.</p>`;
        return;
    }

    children.forEach(ch => {
        const div = document.createElement('div');
        div.className = 'child-card';
        div.innerHTML = `
      <div class="title">${ch.fio ?? '-'}</div>
      <div class="meta">${ch.className ?? '-'} • ${ch.schoolName ?? '-'}</div>
      <div class="actions">
        <button class="btn" data-id="${ch.id}">Подробнее</button>
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
        sel.innerHTML += `<option value="${ch.id}">${ch.fio ?? '-'} (${ch.className ?? '-'})</option>`;
    });

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

    // ===== DAY MODE =====
    if (scheduleMode === "day") {
        const dateStr = toYMD(selectedDate);
        const lessons = await ApiService.get(`/parent/children/${activeChildId}/schedule?date=${dateStr}`);
        renderDaySchedule(list, lessons, selectedDate);
        return;
    }

    // ===== WEEK MODE =====
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
    const day = x.getDay(); // 0..6 (0=Sun)
    const diff = (day === 0 ? -6 : 1 - day); // смещение к понедельнику
    x.setDate(x.getDate() + diff);
    x.setHours(0,0,0,0);
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
        html += `<div class="no-schedule">
      <div class="no-schedule-icon">📅</div>
      <div class="no-schedule-text">На этот день уроков нет</div>
    </div>`;
        container.innerHTML = html;
        return;
    }

    arr.sort((a,b) => (a.lessonNumber??0)-(b.lessonNumber??0));

    html += `<div class="lessons-list">`;
    arr.forEach(lesson => {
        html += `
      <div class="lesson-item">
        <div class="lesson-time">
          <div class="lesson-number">${lesson.lessonNumber ?? ''} урок</div>
          <div class="time-range">${formatTime(lesson.startTime)} - ${formatTime(lesson.endTime)}</div>
        </div>

        <div class="lesson-info">
          <div class="subject-name">${lesson.subjectName ?? '-'}</div>
          <div class="lesson-details">
            <span class="classroom"><i>🏫</i> ${lesson.classroom ?? '-'}</span>
            ${lesson.teacherName ? `<span class="teacher"><i>👤</i> ${lesson.teacherName}</span>` : ''}
          </div>
        </div>
      </div>
    `;
    });
    html += `</div>`;

    container.innerHTML = html;
}

function renderWeekSchedule(container, weekMap, weekStart) {
    // weekMap = { "2026-02-09": [LessonDTO...], ... }
    const days = [];
    for (let i=0;i<7;i++){
        const d = new Date(weekStart);
        d.setDate(weekStart.getDate()+i);
        const key = toYMD(d);
        days.push({ date:d, key, lessons: (weekMap && weekMap[key]) ? weekMap[key] : [] });
    }

    let html = `<div class="week-grid">`;

    days.forEach(day => {
        const title = day.date.toLocaleDateString('ru-RU', { weekday:'long', day:'numeric', month:'long' });
        html += `
      <div class="week-day">
        <div class="week-day-head">${title}</div>
        <div class="week-day-body">
    `;

        if (!day.lessons || day.lessons.length===0) {
            html += `<div class="week-empty">Нет уроков</div>`;
        } else {
            const arr = [...day.lessons].sort((a,b)=>(a.lessonNumber??0)-(b.lessonNumber??0));
            arr.forEach(ls => {
                html += `
          <div class="week-lesson">
            <div class="wl-time">${formatTime(ls.startTime)}-${formatTime(ls.endTime)}</div>
            <div class="wl-sub">${ls.subjectName ?? '-'}</div>
            <div class="wl-meta">${ls.className ?? ''} • ${ls.classroom ?? ''}</div>
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

function isCurrentLesson(lesson) {
    if (!lesson.startTime || !lesson.endTime) return false;

    const now = new Date();
    const current = now.getHours() * 60 + now.getMinutes();
    const start = timeToMinutes(lesson.startTime);
    const end = timeToMinutes(lesson.endTime);

    return current >= start && current <= end;
}

function timeToMinutes(timeString) {
    const s = String(timeString);
    const [h, m] = s.split(':');
    return (parseInt(h || '0', 10) * 60) + parseInt(m || '0', 10);
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
    const arr = Array.isArray(rows) ? rows : [];

    if (arr.length === 0) {
        tbody.innerHTML = `<tr><td colspan="3">Оценок пока нет</td></tr>`;
        return;
    }

    tbody.innerHTML = arr.map(r => `
    <tr>
      <td>${formatDateTime(r.date)}</td>
      <td>${r.subject ?? '-'}</td>
      <td>${renderGradeBadge(r.grade)}</td>
    </tr>
  `).join('');
}

function formatDateTime(dt) {
    if (!dt) return '-';
    const d = new Date(dt);
    if (isNaN(d.getTime())) return String(dt);
    return d.toLocaleString();
}

function renderGradeBadge(g) {
    if (g == null) return '-';
    return `<span class="grade-badge">${g}</span>`;
}

/* =========================
   4) Уведомления
========================= */
async function loadNotifications() {
    const box = document.getElementById('notificationsList');
    const items = await ApiService.get('/parent/notifications');
    const arr = Array.isArray(items) ? items : [];

    if (arr.length === 0) {
        box.innerHTML = `<p>Нет новых уведомлений.</p>`;
        return;
    }

    box.innerHTML = arr.map(n => `
    <div class="notif">
      <div class="title">${n.title ?? 'Уведомление'}</div>
      <div class="text">${n.text ?? ''}</div>
      <div class="meta">${n.date ? formatDateTime(n.date) : ''}</div>
    </div>
  `).join('');
}



// ===============================
// ADD CHILD MODAL (Parent Dashboard)
// ===============================

async function loadSchools(selectId) {
    const schools = await ApiService.get('/schools');
    const select = document.getElementById(selectId);
    if (!select) return;

    select.innerHTML =
        `<option value="">Выберите школу</option>` +
        schools.map(s => `<option value="${s.id}">${s.name}</option>`).join('');
}

async function loadClasses(schoolId, selectId) {
    const select = document.getElementById(selectId);
    if (!select) return;

    if (!schoolId) {
        select.innerHTML = `<option value="">Сначала выберите школу</option>`;
        return;
    }

    const classes = await ApiService.get(`/schools/${schoolId}/classes`);
    select.innerHTML =
        `<option value="">Выберите класс</option>` +
        classes.map(c => `<option value="${c.id}">${c.name}</option>`).join('');
}

async function loadStudentsByClass(classId, selectId) {
    const select = document.getElementById(selectId);
    if (!select) return;

    if (!classId) {
        select.innerHTML = `<option value="">Сначала выберите класс</option>`;
        return;
    }

    const students = await ApiService.get(`/students/classes/${classId}/students`);
    console.log("students =", students);

    if (!students || students.length === 0) {
        select.innerHTML = `<option value="">В этом классе нет учеников</option>`;
        return;
    }

    select.innerHTML = students.map(s => {
        const fio = `${s.lastName} ${s.firstName}${s.patronymic ? " " + s.patronymic : ""}`;
        return `<option value="${s.id}">${fio}</option>`;
    }).join("");
}

function openAddChildModal() {
    const modal = document.getElementById('addChildModal');
    if (modal) modal.style.display = 'flex';
}

function closeAddChildModal() {
    const modal = document.getElementById('addChildModal');
    if (modal) modal.style.display = 'none';

    // сброс UI
    document.getElementById('parentSchoolSelect').innerHTML = '';
    document.getElementById('parentClassSelect').innerHTML = '';
    document.getElementById('parentStudentsSelect').innerHTML = '';

    document.getElementById('parentClassSelect').disabled = true;
    document.getElementById('parentStudentsSelect').disabled = true;
    document.getElementById('linkChildBtn').disabled = true;

    const msg = document.getElementById('linkChildMsg');
    if (msg) msg.textContent = '';
}

async function initAddChildModalLogic() {
    const openBtn = document.getElementById('openAddChildModalBtn');
    const closeBtn = document.getElementById('closeAddChildModalBtn');

    const schoolSel = document.getElementById('parentSchoolSelect');
    const classSel = document.getElementById('parentClassSelect');
    const studentSel = document.getElementById('parentStudentsSelect');
    const linkBtn = document.getElementById('linkChildBtn');

    if (!openBtn || !closeBtn || !schoolSel || !classSel || !studentSel || !linkBtn) {
        console.warn('AddChild modal elements not found');
        return;
    }

    openBtn.addEventListener('click', async () => {
        openAddChildModal();

        // стартовое состояние
        classSel.disabled = true;
        studentSel.disabled = true;
        linkBtn.disabled = true;

        // 1) школы
        await loadSchools('parentSchoolSelect');

        // Если хочешь — авто-выбор первой школы:
        // if (!schoolSel.value && schoolSel.options.length > 1) schoolSel.selectedIndex = 1;
        // и сразу загрузить классы
    });

    closeBtn.addEventListener('click', () => closeAddChildModal());

    // 2) выбрали школу → грузим классы
    schoolSel.addEventListener('change', async () => {
        classSel.disabled = false;
        await loadClasses(schoolSel.value, 'parentClassSelect');

        // сброс ниже
        studentSel.innerHTML = '';
        studentSel.disabled = true;
        linkBtn.disabled = true;
    });

    // 3) выбрали класс → грузим учеников
    classSel.addEventListener('change', async () => {
        studentSel.disabled = false;
        await loadStudentsByClass(classSel.value, 'parentStudentsSelect');

        linkBtn.disabled = false;
    });

    // 4) привязать ребёнка
    linkBtn.addEventListener('click', async () => {
        const studentId = studentSel.value;
        const msg = document.getElementById('linkChildMsg');

        if (!studentId) {
            if (msg) msg.textContent = 'Выберите ученика';
            return;
        }

        try {
            await ApiService.post('/parent/children/link', { studentId: Number(studentId) });
            if (msg) msg.textContent = '✅ Ребёнок привязан';
            await loadChildren();                 // обновить список детей
            activeChildId = Number(studentId);    // сделать нового активным
            document.getElementById('activeChildSelect').value = String(studentId);
            await refreshAllBlocks();             // обновить расписание/оценки/уведомления
            closeAddChildModal();                 // закрыть модалку


            // тут можно: перезагрузить список детей и селект activeChildSelect
            // await loadParentChildren();  (если у тебя такая функция есть)
        } catch (e) {
            console.error(e);
            if (msg) msg.textContent = '❌ Ошибка: ' + (e.message || e);
        }
    });
}

// Запуск
document.addEventListener('DOMContentLoaded', () => {
    initAddChildModalLogic();
});
