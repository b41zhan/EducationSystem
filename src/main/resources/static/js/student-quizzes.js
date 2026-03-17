const studentQuizState = {
    token: localStorage.getItem('token'),
    assignments: []
};

document.addEventListener('DOMContentLoaded', async () => {
    if (!studentQuizState.token) {
        window.location.href = '/login.html';
        return;
    }

    document.getElementById('refreshAvailableBtn').addEventListener('click', loadAvailableQuizzes);
    await loadAvailableQuizzes();
});

function studentApi(path, options = {}) {
    const headers = {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${studentQuizState.token}`,
        ...(options.headers || {})
    };

    return fetch(`/api${path}`, { ...options, headers })
        .then(async (res) => {
            const contentType = res.headers.get('content-type') || '';
            const isJson = contentType.includes('application/json');
            const data = isJson ? await res.json() : await res.text();

            if (!res.ok) {
                const msg = data?.error || data?.message || data || 'Ошибка запроса';
                throw new Error(msg);
            }
            return data;
        });
}

function showStudentMessage(text, type = 'success') {
    const el = document.getElementById('pageMessage');
    el.className = `message ${type}`;
    el.textContent = text;
    el.style.display = 'block';
    setTimeout(() => {
        el.style.display = 'none';
    }, 5000);
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

async function loadAvailableQuizzes() {
    const container = document.getElementById('availableList');
    container.innerHTML = '<div class="empty">Загрузка...</div>';

    try {
        const assignments = await studentApi('/student/quiz/available', { method: 'GET' });
        studentQuizState.assignments = Array.isArray(assignments) ? assignments : [];
        renderAvailableAssignments();
    } catch (e) {
        container.innerHTML = `<div class="empty">Ошибка: ${escapeHtml(e.message)}</div>`;
    }
}

function renderAvailableAssignments() {
    const container = document.getElementById('availableList');

    if (!studentQuizState.assignments.length) {
        container.innerHTML = '<div class="empty">Сейчас нет доступных квизов</div>';
        return;
    }

    container.innerHTML = studentQuizState.assignments.map(a => {
        const quizTitle = a.quiz?.title || `Quiz #${a.quiz?.id || ''}`;
        const description = a.quiz?.description || 'Без описания';
        const questionsCount = Array.isArray(a.quiz?.questions) ? a.quiz.questions.length : 0;
        return `
            <div class="card item">
                <div class="head">
                    <div>
                        <h3>${escapeHtml(quizTitle)}</h3>
                        <div>${escapeHtml(description)}</div>
                        <div class="meta">
                            <span class="chip">Assignment ID: ${a.id}</span>
                            <span class="chip">Вопросов: ${questionsCount}</span>
                            <span class="chip">Лимит: ${a.timeLimitMinutes ? `${a.timeLimitMinutes} мин` : 'без лимита'}</span>
                            <span class="chip">До: ${formatDateTime(a.endTime)}</span>
                        </div>
                    </div>
                    <div>
                        <button class="btn btn-primary" onclick="openQuiz(${a.id})">Открыть</button>
                    </div>
                </div>
            </div>
        `;
    }).join('');
}

window.openQuiz = function(assignmentId) {
    window.location.href = `/quiz-session.html?assignmentId=${assignmentId}`;
};

function formatDateTime(value) {
    if (!value) return '—';
    const d = new Date(value);
    if (Number.isNaN(d.getTime())) return value;
    return d.toLocaleString('ru-RU');
}

function logout() {
    localStorage.clear();
    window.location.href = '/login.html';
}
window.logout = logout;