const studentJournalState = {
    token: localStorage.getItem('token'),
    quarter: 1,
    subjects: []
};

document.addEventListener('DOMContentLoaded', async () => {
    if (!studentJournalState.token) {
        window.location.href = '/login.html';
        return;
    }

    document.getElementById('quarterSelect').addEventListener('change', (e) => {
        studentJournalState.quarter = Number(e.target.value);
    });

    document.getElementById('loadJournalBtn').addEventListener('click', loadStudentJournal);
});

function showMessage(text, type = 'error') {
    const el = document.getElementById('message');
    el.className = `message ${type}`;
    el.textContent = text;
    el.style.display = 'block';
}

function clearMessage() {
    const el = document.getElementById('message');
    el.style.display = 'none';
    el.textContent = '';
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

async function studentJournalApi(path) {
    const res = await fetch(`/api${path}`, {
        headers: {
            'Authorization': `Bearer ${studentJournalState.token}`
        }
    });

    const contentType = res.headers.get('content-type') || '';
    const isJson = contentType.includes('application/json');
    const data = isJson ? await res.json() : await res.text();

    if (!res.ok) {
        throw new Error(data?.error || data?.message || data || 'Ошибка запроса');
    }

    return data;
}

async function loadStudentJournal() {
    clearMessage();
    const container = document.getElementById('subjectsContainer');
    container.innerHTML = '<div class="empty">Загрузка журнала...</div>';

    try {
        const subjects = await studentJournalApi(`/journal/student/my?quarter=${studentJournalState.quarter}`);
        studentJournalState.subjects = Array.isArray(subjects) ? subjects : [];
        renderSubjects();
    } catch (e) {
        container.innerHTML = '';
        showMessage(e.message, 'error');
    }
}

function renderSubjects() {
    const container = document.getElementById('subjectsContainer');

    if (!studentJournalState.subjects.length) {
        container.innerHTML = '<div class="empty">Нет данных по журналу за эту четверть</div>';
        return;
    }

    let html = '';

    studentJournalState.subjects.forEach(subject => {
        html += `
            <div class="subject-card">
                <div class="subject-title">${escapeHtml(subject.subjectName)}</div>
                <div class="table-wrap">
                    <table class="journal-table">
                        <thead>
                            <tr>
                                ${subject.dates.map(d => `<th>${formatDate(d)}</th>`).join('')}
                                <th>Итог четверти</th>
                                ${studentJournalState.quarter === 4 ? '<th>Итог года</th>' : ''}
                            </tr>
                        </thead>
                        <tbody>
                            <tr>
                                ${subject.cells.map(cell => `<td>${renderCell(cell)}</td>`).join('')}
                                <td>${renderQuarterFinal(subject.finalGrade)}</td>
                                ${studentJournalState.quarter === 4 ? `<td>${renderYearFinal(subject.finalGrade)}</td>` : ''}
                            </tr>
                        </tbody>
                    </table>
                </div>
            </div>
        `;
    });

    container.innerHTML = html;
}

function renderCell(cell) {
    const attendanceBadge = cell.attendanceCode
        ? `<div class="att-code ${attendanceClass(cell.attendanceColor)}">${cell.attendanceCode}</div>`
        : '';

    const gradeChips = (cell.entries || [])
        .map(e => `<div class="grade-chip" title="${escapeHtml(e.label)}">${escapeHtml(e.displayValue || '')}</div>`)
        .join('');

    return `
        <div class="cell-box">
            ${attendanceBadge}
            ${gradeChips}
        </div>
    `;
}

function renderQuarterFinal(finalGrade) {
    const fg = finalGrade || {};
    return `
        <div class="final-box">
            <div class="final-value">${fg.quarterGrade ?? '-'}</div>
            <div class="small-note">расчёт: ${fg.calculatedQuarterGrade ?? '-'}</div>
        </div>
    `;
}

function renderYearFinal(finalGrade) {
    const fg = finalGrade || {};
    return `
        <div class="final-box">
            <div class="final-value">${fg.yearGrade ?? '-'}</div>
            <div class="small-note">расчёт: ${fg.calculatedYearGrade ?? '-'}</div>
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