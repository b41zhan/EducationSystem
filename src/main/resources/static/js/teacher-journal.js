const journalState = {
    token: localStorage.getItem('token'),
    selectedClassId: null,
    selectedSubjectId: null,
    quarter: 1,
    pairs: [],
    journal: null
};

document.addEventListener('DOMContentLoaded', async () => {
    if (!journalState.token) {
        window.location.href = '/login.html';
        return;
    }

    document.getElementById('quarterSelect').addEventListener('change', (e) => {
        journalState.quarter = Number(e.target.value);
    });

    document.getElementById('classSubjectSelect').addEventListener('change', (e) => {
        const val = e.target.value;
        if (!val) {
            journalState.selectedClassId = null;
            journalState.selectedSubjectId = null;
            return;
        }
        const [classId, subjectId] = val.split('_').map(Number);
        journalState.selectedClassId = classId;
        journalState.selectedSubjectId = subjectId;
    });

    document.getElementById('loadJournalBtn').addEventListener('click', loadJournal);
    document.getElementById('syncAssignmentsBtn').addEventListener('click', syncAssignments);
    document.getElementById('syncQuizzesBtn').addEventListener('click', syncQuizzes);

    await loadPairs();
});

function showMessage(text, type = 'success') {
    const el = document.getElementById('message');
    el.className = `message ${type}`;
    el.textContent = text;
    el.style.display = 'block';
    setTimeout(() => {
        el.style.display = 'none';
    }, 3500);
}

function escapeHtml(str) {
    if (!str) return '';
    return String(str)
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;')
        .replace(/'/g, '&#039;');
}

async function journalApi(path, options = {}) {
    const headers = {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${journalState.token}`,
        ...(options.headers || {})
    };

    const res = await fetch(`/api${path}`, { ...options, headers });
    const contentType = res.headers.get('content-type') || '';
    const isJson = contentType.includes('application/json');
    const data = isJson ? await res.json() : await res.text();

    if (!res.ok) {
        throw new Error(data?.error || data?.message || data || 'Ошибка запроса');
    }

    return data;
}

async function loadPairs() {
    try {
        const pairs = await journalApi('/journal/teacher/my-classes-subjects');
        journalState.pairs = Array.isArray(pairs) ? pairs : [];

        const select = document.getElementById('classSubjectSelect');
        select.innerHTML = '<option value="">Выберите класс и предмет</option>';

        journalState.pairs.forEach(item => {
            const option = document.createElement('option');
            option.value = `${item.classId}_${item.subjectId}`;
            option.textContent = `${item.className} — ${item.subjectName}`;
            select.appendChild(option);
        });
    } catch (e) {
        showMessage(e.message, 'error');
    }
}

async function loadJournal() {
    if (!journalState.selectedClassId || !journalState.selectedSubjectId) {
        showMessage('Выбери класс и предмет', 'error');
        return;
    }

    const container = document.getElementById('journalContainer');
    container.innerHTML = '<div class="empty">Загрузка журнала...</div>';

    try {
        const journal = await journalApi(
            `/journal/teacher?classId=${journalState.selectedClassId}&subjectId=${journalState.selectedSubjectId}&quarter=${journalState.quarter}`
        );
        journalState.journal = journal;
        renderJournal();
    } catch (e) {
        container.innerHTML = `<div class="empty">Ошибка: ${escapeHtml(e.message)}</div>`;
    }
}

function renderJournal() {
    const journal = journalState.journal;
    const container = document.getElementById('journalContainer');

    if (!journal || !journal.students || !journal.students.length) {
        container.innerHTML = '<div class="empty">Нет данных для журнала</div>';
        return;
    }

    let html = `
        <table class="journal-table">
            <thead>
                <tr>
                    <th class="sticky">Ученик</th>
                    ${journal.dates.map(d => `<th>${formatDate(d)}</th>`).join('')}
                    <th>Итог четверти</th>
                    ${journal.quarter === 4 ? '<th>Итог года</th>' : ''}
                </tr>
            </thead>
            <tbody>
    `;

    journal.students.forEach(student => {
        html += `<tr>`;
        html += `<td class="sticky">${escapeHtml(student.studentName)}</td>`;

        student.cells.forEach(cell => {
            html += `<td>${renderCell(student.studentId, cell)}</td>`;
        });

        html += `<td>${renderQuarterFinal(student)}</td>`;

        if (journal.quarter === 4) {
            html += `<td>${renderYearFinal(student)}</td>`;
        }

        html += `</tr>`;
    });

    html += `</tbody></table>`;
    container.innerHTML = html;

    bindDynamicEvents();
}

function renderCell(studentId, cell) {
    const attendanceBadge = cell.attendanceCode
        ? `<div class="att-code ${attendanceClass(cell.attendanceColor)}">${cell.attendanceCode}</div>`
        : '';

    const gradeChips = (cell.entries || [])
        .filter(e => e.type !== 'LESSON_GRADE')
        .map(e => `<div class="grade-chip" title="${escapeHtml(e.label)}">${escapeHtml(e.displayValue || '')}</div>`)
        .join('');

    const lessonGrade = (cell.entries || []).find(e => e.type === 'LESSON_GRADE');

    return `
        <div class="cell-box">
            ${attendanceBadge}
            <div class="attendance-controls">
                <button class="att-btn" data-action="attendance" data-status="EXCUSED" data-student="${studentId}" data-date="${cell.date}">П</button>
                <button class="att-btn" data-action="attendance" data-status="ABSENT" data-student="${studentId}" data-date="${cell.date}">Н</button>
                <button class="att-btn" data-action="attendance" data-status="SICK" data-student="${studentId}" data-date="${cell.date}">Б</button>
            </div>
            ${gradeChips}
            <div class="lesson-grade-wrap">
                <input class="lesson-grade-input"
                       type="number"
                       min="0"
                       max="10"
                       step="0.1"
                       value="${lessonGrade?.numericValue ?? ''}"
                       data-role="lesson-input"
                       data-student="${studentId}"
                       data-date="${cell.date}"
                       title="Оценка за урок">
                <button class="save-small"
                        data-action="save-lesson"
                        data-student="${studentId}"
                        data-date="${cell.date}">OK</button>
            </div>
        </div>
    `;
}

function renderQuarterFinal(student) {
    const fg = student.finalGrade || {};
    return `
        <div class="final-box">
            <input type="number"
                   min="0"
                   max="10"
                   step="0.1"
                   value="${fg.quarterGrade ?? ''}"
                   data-role="quarter-final-input"
                   data-student="${student.studentId}">
            <div class="small-note">расчёт: ${fg.calculatedQuarterGrade ?? '-'}</div>
            <button class="save-small" data-action="save-quarter-final" data-student="${student.studentId}">Сохранить</button>
            <button class="calc-small" data-action="calc-quarter-final" data-student="${student.studentId}">Рассчитать</button>
        </div>
    `;
}

function renderYearFinal(student) {
    const fg = student.finalGrade || {};
    return `
        <div class="final-box">
            <input type="number"
                   min="0"
                   max="10"
                   step="0.1"
                   value="${fg.yearGrade ?? ''}"
                   data-role="year-final-input"
                   data-student="${student.studentId}">
            <div class="small-note">расчёт: ${fg.calculatedYearGrade ?? '-'}</div>
            <button class="save-small" data-action="save-year-final" data-student="${student.studentId}">Сохранить</button>
            <button class="calc-small" data-action="calc-year-final" data-student="${student.studentId}">Рассчитать</button>
        </div>
    `;
}

function attendanceClass(color) {
    if (color === 'red') return 'att-red';
    if (color === 'yellow') return 'att-yellow';
    if (color === 'orange') return 'att-orange';
    return '';
}

function formatDate(dateStr) {
    const d = new Date(dateStr);
    return `${String(d.getDate()).padStart(2, '0')}.${String(d.getMonth() + 1).padStart(2, '0')}`;
}

function bindDynamicEvents() {
    document.querySelectorAll('[data-action="attendance"]').forEach(btn => {
        btn.addEventListener('click', async () => {
            try {
                await journalApi('/journal/teacher/attendance', {
                    method: 'PUT',
                    body: JSON.stringify({
                        classId: journalState.selectedClassId,
                        subjectId: journalState.selectedSubjectId,
                        studentId: Number(btn.dataset.student),
                        quarter: journalState.quarter,
                        lessonDate: btn.dataset.date,
                        status: btn.dataset.status
                    })
                });
                await loadJournal();
            } catch (e) {
                showMessage(e.message, 'error');
            }
        });
    });

    document.querySelectorAll('[data-action="save-lesson"]').forEach(btn => {
        btn.addEventListener('click', async () => {
            const studentId = Number(btn.dataset.student);
            const date = btn.dataset.date;

            const input = document.querySelector(`[data-role="lesson-input"][data-student="${studentId}"][data-date="${date}"]`);
            const value = input.value.trim();

            try {
                await journalApi('/journal/teacher/lesson-grade', {
                    method: 'PUT',
                    body: JSON.stringify({
                        classId: journalState.selectedClassId,
                        subjectId: journalState.selectedSubjectId,
                        studentId,
                        quarter: journalState.quarter,
                        lessonDate: date,
                        value: value ? Number(value) : null,
                        comment: null
                    })
                });
                await loadJournal();
            } catch (e) {
                showMessage(e.message, 'error');
            }
        });
    });

    document.querySelectorAll('[data-action="save-quarter-final"]').forEach(btn => {
        btn.addEventListener('click', async () => {
            const studentId = Number(btn.dataset.student);
            const input = document.querySelector(`[data-role="quarter-final-input"][data-student="${studentId}"]`);

            try {
                await journalApi('/journal/teacher/quarter-final', {
                    method: 'PUT',
                    body: JSON.stringify({
                        classId: journalState.selectedClassId,
                        subjectId: journalState.selectedSubjectId,
                        studentId,
                        quarter: journalState.quarter,
                        quarterGrade: input.value ? Number(input.value) : null
                    })
                });
                await loadJournal();
            } catch (e) {
                showMessage(e.message, 'error');
            }
        });
    });

    document.querySelectorAll('[data-action="calc-quarter-final"]').forEach(btn => {
        btn.addEventListener('click', async () => {
            const studentId = Number(btn.dataset.student);

            try {
                await journalApi(
                    `/journal/teacher/calculate-quarter-final?classId=${journalState.selectedClassId}&subjectId=${journalState.selectedSubjectId}&studentId=${studentId}&quarter=${journalState.quarter}`,
                    { method: 'POST' }
                );
                await loadJournal();
            } catch (e) {
                showMessage(e.message, 'error');
            }
        });
    });

    document.querySelectorAll('[data-action="save-year-final"]').forEach(btn => {
        btn.addEventListener('click', async () => {
            const studentId = Number(btn.dataset.student);
            const input = document.querySelector(`[data-role="year-final-input"][data-student="${studentId}"]`);

            try {
                await journalApi('/journal/teacher/year-final', {
                    method: 'PUT',
                    body: JSON.stringify({
                        classId: journalState.selectedClassId,
                        subjectId: journalState.selectedSubjectId,
                        studentId,
                        quarter: 4,
                        yearGrade: input.value ? Number(input.value) : null
                    })
                });
                await loadJournal();
            } catch (e) {
                showMessage(e.message, 'error');
            }
        });
    });

    document.querySelectorAll('[data-action="calc-year-final"]').forEach(btn => {
        btn.addEventListener('click', async () => {
            const studentId = Number(btn.dataset.student);

            try {
                await journalApi(
                    `/journal/teacher/calculate-year-final?classId=${journalState.selectedClassId}&subjectId=${journalState.selectedSubjectId}&studentId=${studentId}`,
                    { method: 'POST' }
                );
                await loadJournal();
            } catch (e) {
                showMessage(e.message, 'error');
            }
        });
    });
}

async function syncAssignments() {
    if (!journalState.selectedClassId || !journalState.selectedSubjectId) {
        showMessage('Сначала выбери класс и предмет', 'error');
        return;
    }

    try {
        await journalApi(
            `/journal/teacher/sync-assignments?classId=${journalState.selectedClassId}&subjectId=${journalState.selectedSubjectId}&quarter=${journalState.quarter}`,
            { method: 'POST' }
        );
        showMessage('Оценки за задания подтянуты');
        await loadJournal();
    } catch (e) {
        showMessage(e.message, 'error');
    }
}

async function syncQuizzes() {
    if (!journalState.selectedClassId || !journalState.selectedSubjectId) {
        showMessage('Сначала выбери класс и предмет', 'error');
        return;
    }

    try {
        await journalApi(
            `/journal/teacher/sync-quizzes?classId=${journalState.selectedClassId}&subjectId=${journalState.selectedSubjectId}&quarter=${journalState.quarter}`,
            { method: 'POST' }
        );
        showMessage('Оценки за квизы подтянуты');
        await loadJournal();
    } catch (e) {
        showMessage(e.message, 'error');
    }
}