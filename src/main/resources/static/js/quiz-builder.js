const quizBuilderState = {
    token: localStorage.getItem('token'),
    quizzes: [],
    assignments: [],
    classes: [],
    subjects: [],
    selectedQuizId: null,
    draftQuestions: []
};

const builderEls = {};

document.addEventListener('DOMContentLoaded', async () => {
    if (!quizBuilderState.token) {
        window.location.href = '/login.html';
        return;
    }

    cacheBuilderElements();
    bindBuilderEvents();
    setDefaultAssignmentWindow();

    await Promise.all([
        loadSubjects(),
        loadTeacherClasses(),
        loadMyQuizzes(),
        loadMyAssignments()
    ]);
});

function cacheBuilderElements() {
    builderEls.message = document.getElementById('pageMessage');
    builderEls.quizTitle = document.getElementById('quizTitle');
    builderEls.quizDescription = document.getElementById('quizDescription');
    builderEls.quizSubject = document.getElementById('quizSubject');
    builderEls.questionsWrap = document.getElementById('questionsWrap');
    builderEls.quizList = document.getElementById('quizList');
    builderEls.assignmentList = document.getElementById('assignmentList');
    builderEls.currentQuizBadge = document.getElementById('currentQuizBadge');

    builderEls.assignQuizSelect = document.getElementById('assignQuizSelect');
    builderEls.assignClassSelect = document.getElementById('assignClassSelect');
    builderEls.assignStartTime = document.getElementById('assignStartTime');
    builderEls.assignEndTime = document.getElementById('assignEndTime');
    builderEls.assignTimeLimit = document.getElementById('assignTimeLimit');

    document.getElementById('createQuizBtn').addEventListener('click', createQuizTemplate);
    document.getElementById('refreshQuizzesBtn').addEventListener('click', loadMyQuizzes);
    document.getElementById('refreshAssignmentsBtn').addEventListener('click', loadMyAssignments);
    document.getElementById('assignQuizBtn').addEventListener('click', assignQuiz);

    document.getElementById('addSingleBtn').addEventListener('click', () => addQuestionCard('SINGLE_CHOICE'));
    document.getElementById('addMultipleBtn').addEventListener('click', () => addQuestionCard('MULTIPLE_CHOICE'));
    document.getElementById('addTextBtn').addEventListener('click', () => addQuestionCard('TEXT_ANSWER'));
    document.getElementById('saveQuestionsBtn').addEventListener('click', saveAllDraftQuestions);
}

function bindBuilderEvents() {
    builderEls.questionsWrap.addEventListener('click', (e) => {
        const addOptionBtn = e.target.closest('[data-action="add-option"]');
        const removeQuestionBtn = e.target.closest('[data-action="remove-question"]');
        const removeOptionBtn = e.target.closest('[data-action="remove-option"]');

        if (addOptionBtn) {
            addOptionRow(addOptionBtn.dataset.qid);
        }

        if (removeQuestionBtn) {
            removeDraftQuestion(removeQuestionBtn.dataset.qid);
        }

        if (removeOptionBtn) {
            const qid = removeOptionBtn.dataset.qid;
            const oid = removeOptionBtn.dataset.oid;
            removeOptionRow(qid, oid);
        }
    });
}

function api(path, options = {}) {
    const headers = {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${quizBuilderState.token}`,
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

function showMessage(text, type = 'success') {
    builderEls.message.className = `message ${type}`;
    builderEls.message.textContent = text;
    builderEls.message.style.display = 'block';

    setTimeout(() => {
        builderEls.message.style.display = 'none';
    }, 5000);
}

function setDefaultAssignmentWindow() {
    const now = new Date();
    const start = new Date(now.getTime() + 10 * 60000);
    const end = new Date(now.getTime() + 6 * 60 * 60000);

    builderEls.assignStartTime.value = toDateTimeLocal(start);
    builderEls.assignEndTime.value = toDateTimeLocal(end);
}

function toDateTimeLocal(date) {
    const pad = (n) => String(n).padStart(2, '0');
    return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}T${pad(date.getHours())}:${pad(date.getMinutes())}`;
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

async function loadSubjects() {
    try {
        const subjects = await api('/subjects');
        quizBuilderState.subjects = Array.isArray(subjects) ? subjects : [];
        builderEls.quizSubject.innerHTML = '<option value="">Без предмета</option>';

        quizBuilderState.subjects.forEach(subject => {
            const option = document.createElement('option');
            option.value = subject.id;
            option.textContent = subject.name;
            builderEls.quizSubject.appendChild(option);
        });
    } catch (e) {
        console.error(e);
    }
}

async function loadTeacherClasses() {
    try {
        const classes = await api('/statistics/teacher/classes');
        quizBuilderState.classes = Array.isArray(classes) ? classes : [];
        builderEls.assignClassSelect.innerHTML = '<option value="">Выбери класс</option>';

        quizBuilderState.classes.forEach(cls => {
            const option = document.createElement('option');
            option.value = cls.id;
            option.textContent = cls.name;
            builderEls.assignClassSelect.appendChild(option);
        });
    } catch (e) {
        console.error(e);
    }
}

async function createQuizTemplate() {
    const title = builderEls.quizTitle.value.trim();
    const description = builderEls.quizDescription.value.trim();
    const subjectIdRaw = builderEls.quizSubject.value;

    if (!title) {
        showMessage('Введи название квиза', 'error');
        return;
    }

    try {
        const createdQuiz = await api('/teacher/quiz/create', {
            method: 'POST',
            body: JSON.stringify({
                title,
                description,
                subjectId: subjectIdRaw ? Number(subjectIdRaw) : null
            })
        });

        quizBuilderState.selectedQuizId = createdQuiz.id;
        builderEls.quizTitle.value = '';
        builderEls.quizDescription.value = '';
        builderEls.quizSubject.value = '';

        showMessage('Шаблон создан. Теперь добавь вопросы.');
        await loadMyQuizzes();
        renderDraftQuestions();
    } catch (e) {
        showMessage(e.message, 'error');
    }
}

function addQuestionCard(type) {
    if (!quizBuilderState.selectedQuizId) {
        showMessage('Сначала создай или выбери шаблон квиза', 'error');
        return;
    }

    const draft = {
        localId: `q_${Date.now()}_${Math.floor(Math.random() * 10000)}`,
        type,
        questionText: '',
        points: 1,
        options: type === 'TEXT_ANSWER'
            ? []
            : [
                { localId: `o_${Date.now()}_1`, optionText: '', isCorrect: false },
                { localId: `o_${Date.now()}_2`, optionText: '', isCorrect: false }
            ]
    };

    quizBuilderState.draftQuestions.push(draft);
    renderDraftQuestions();
}

function removeDraftQuestion(localId) {
    quizBuilderState.draftQuestions = quizBuilderState.draftQuestions.filter(q => q.localId !== localId);
    renderDraftQuestions();
}

function addOptionRow(questionLocalId) {
    const question = quizBuilderState.draftQuestions.find(q => q.localId === questionLocalId);
    if (!question || question.type === 'TEXT_ANSWER') return;

    question.options.push({
        localId: `o_${Date.now()}_${Math.floor(Math.random() * 10000)}`,
        optionText: '',
        isCorrect: false
    });

    renderDraftQuestions();
}

function removeOptionRow(questionLocalId, optionLocalId) {
    const question = quizBuilderState.draftQuestions.find(q => q.localId === questionLocalId);
    if (!question) return;

    question.options = question.options.filter(o => o.localId !== optionLocalId);
    renderDraftQuestions();
}

function renderDraftQuestions() {
    if (!quizBuilderState.draftQuestions.length) {
        builderEls.questionsWrap.innerHTML = '<div class="empty">Добавь первый вопрос кнопками ниже</div>';
        return;
    }

    builderEls.questionsWrap.innerHTML = quizBuilderState.draftQuestions.map((q, index) => `
        <div class="question-card">
            <div class="question-top">
                <div class="question-title">Вопрос #${index + 1}</div>
                <button class="btn btn-danger btn-small" data-action="remove-question" data-qid="${q.localId}">Удалить</button>
            </div>

            <div class="row">
                <div class="field">
                    <label>Тип</label>
                    <select class="question-type" data-qid="${q.localId}">
                        <option value="SINGLE_CHOICE" ${q.type === 'SINGLE_CHOICE' ? 'selected' : ''}>Single Choice</option>
                        <option value="MULTIPLE_CHOICE" ${q.type === 'MULTIPLE_CHOICE' ? 'selected' : ''}>Multiple Choice</option>
                        <option value="TEXT_ANSWER" ${q.type === 'TEXT_ANSWER' ? 'selected' : ''}>Text Answer</option>
                    </select>
                </div>

                <div class="field">
                    <label>Баллы</label>
                    <input class="question-points" data-qid="${q.localId}" type="number" min="1" value="${q.points}">
                </div>
            </div>

            <div class="field">
                <label>Текст вопроса</label>
                <textarea class="question-text" data-qid="${q.localId}" placeholder="Введи текст вопроса">${escapeHtml(q.questionText)}</textarea>
            </div>

            ${q.type !== 'TEXT_ANSWER' ? `
                <div class="field">
                    <label>Варианты ответа</label>
                    <div class="option-list">
                        ${q.options.map((o, idx) => `
                            <div class="option-item">
                                <input class="option-text" data-qid="${q.localId}" data-oid="${o.localId}" type="text" placeholder="Вариант ${idx + 1}" value="${escapeHtml(o.optionText)}">
                                <label>
                                    <input class="option-correct" data-qid="${q.localId}" data-oid="${o.localId}" type="${q.type === 'SINGLE_CHOICE' ? 'radio' : 'checkbox'}" name="correct_${q.localId}" ${o.isCorrect ? 'checked' : ''}>
                                    correct
                                </label>
                                <button type="button" class="btn btn-danger btn-small" data-action="remove-option" data-qid="${q.localId}" data-oid="${o.localId}">×</button>
                            </div>
                        `).join('')}
                    </div>
                </div>

                <div class="toolbar">
                    <button class="btn btn-secondary btn-small" data-action="add-option" data-qid="${q.localId}">+ Вариант</button>
                </div>
            ` : `
                <div class="field">
                    <div class="muted">Свободный ответ будет проверяться учителем вручную.</div>
                </div>
            `}
        </div>
    `).join('');

    builderEls.questionsWrap.querySelectorAll('.question-type').forEach(el => {
        el.addEventListener('change', handleQuestionTypeChange);
    });

    builderEls.questionsWrap.querySelectorAll('.question-text').forEach(el => {
        el.addEventListener('input', handleQuestionTextChange);
    });

    builderEls.questionsWrap.querySelectorAll('.question-points').forEach(el => {
        el.addEventListener('input', handleQuestionPointsChange);
    });

    builderEls.questionsWrap.querySelectorAll('.option-text').forEach(el => {
        el.addEventListener('input', handleOptionTextChange);
    });

    builderEls.questionsWrap.querySelectorAll('.option-correct').forEach(el => {
        el.addEventListener('change', handleOptionCorrectChange);
    });
}

function handleQuestionTypeChange(e) {
    const q = quizBuilderState.draftQuestions.find(x => x.localId === e.target.dataset.qid);
    if (!q) return;

    q.type = e.target.value;

    if (q.type === 'TEXT_ANSWER') {
        q.options = [];
    } else if (!q.options || q.options.length < 2) {
        q.options = [
            { localId: `o_${Date.now()}_1`, optionText: '', isCorrect: false },
            { localId: `o_${Date.now()}_2`, optionText: '', isCorrect: false }
        ];
    }

    renderDraftQuestions();
}

function handleQuestionTextChange(e) {
    const q = quizBuilderState.draftQuestions.find(x => x.localId === e.target.dataset.qid);
    if (q) q.questionText = e.target.value;
}

function handleQuestionPointsChange(e) {
    const q = quizBuilderState.draftQuestions.find(x => x.localId === e.target.dataset.qid);
    if (q) q.points = Number(e.target.value || 1);
}

function handleOptionTextChange(e) {
    const q = quizBuilderState.draftQuestions.find(x => x.localId === e.target.dataset.qid);
    if (!q) return;
    const opt = q.options.find(o => o.localId === e.target.dataset.oid);
    if (opt) opt.optionText = e.target.value;
}

function handleOptionCorrectChange(e) {
    const q = quizBuilderState.draftQuestions.find(x => x.localId === e.target.dataset.qid);
    if (!q) return;

    const opt = q.options.find(o => o.localId === e.target.dataset.oid);
    if (!opt) return;

    if (q.type === 'SINGLE_CHOICE') {
        q.options.forEach(o => o.isCorrect = false);
        opt.isCorrect = true;
    } else {
        opt.isCorrect = e.target.checked;
    }

    renderDraftQuestions();
}

async function saveAllDraftQuestions() {
    if (!quizBuilderState.selectedQuizId) {
        showMessage('Сначала выбери шаблон', 'error');
        return;
    }

    if (!quizBuilderState.draftQuestions.length) {
        showMessage('Нет новых вопросов', 'error');
        return;
    }

    try {
        for (let i = 0; i < quizBuilderState.draftQuestions.length; i++) {
            const q = quizBuilderState.draftQuestions[i];

            if (!q.questionText.trim()) {
                throw new Error(`У вопроса #${i + 1} пустой текст`);
            }

            if (q.type !== 'TEXT_ANSWER') {
                const filledOptions = q.options.filter(o => o.optionText.trim());
                if (filledOptions.length < 2) {
                    throw new Error(`У вопроса #${i + 1} минимум 2 варианта`);
                }
                const correctCount = filledOptions.filter(o => o.isCorrect).length;
                if (correctCount === 0) {
                    throw new Error(`У вопроса #${i + 1} нужно выбрать правильный ответ`);
                }
            }

            const payload = {
                questionText: q.questionText.trim(),
                questionType: q.type,
                points: Number(q.points || 1),
                orderIndex: i + 1,
                options: q.type === 'TEXT_ANSWER'
                    ? []
                    : q.options
                        .filter(o => o.optionText.trim())
                        .map((o, idx) => ({
                            optionText: o.optionText.trim(),
                            isCorrect: !!o.isCorrect,
                            orderIndex: idx + 1
                        }))
            };

            await api(`/teacher/quiz/${quizBuilderState.selectedQuizId}/question`, {
                method: 'POST',
                body: JSON.stringify(payload)
            });
        }

        quizBuilderState.draftQuestions = [];
        renderDraftQuestions();
        showMessage('Вопросы сохранены');
        await loadMyQuizzes();
    } catch (e) {
        showMessage(e.message, 'error');
    }
}

async function loadMyQuizzes() {
    try {
        const quizzes = await api('/teacher/quiz/my');
        quizBuilderState.quizzes = Array.isArray(quizzes) ? quizzes : [];

        renderQuizList();
        renderQuizSelect();
    } catch (e) {
        console.error(e);
    }
}

function renderQuizList() {
    const el = builderEls.quizList;

    if (!quizBuilderState.quizzes.length) {
        el.innerHTML = '<div class="empty">Пока нет созданных квизов</div>';
        return;
    }

    el.innerHTML = quizBuilderState.quizzes.map(q => `
        <div class="template-item ${quizBuilderState.selectedQuizId === q.id ? 'active' : ''}">
            <div class="template-item-head">
                <div>
                    <h3>${escapeHtml(q.title)}</h3>
                    <div class="muted">${escapeHtml(q.description || 'Без описания')}</div>
                </div>
                <button class="btn btn-primary btn-small" onclick="selectQuiz(${q.id})">Выбрать</button>
            </div>
        </div>
    `).join('');
}

function renderQuizSelect() {
    builderEls.assignQuizSelect.innerHTML = '<option value="">Выбери шаблон</option>';
    quizBuilderState.quizzes.forEach(q => {
        const opt = document.createElement('option');
        opt.value = q.id;
        opt.textContent = q.title;
        builderEls.assignQuizSelect.appendChild(opt);
    });
}

window.selectQuiz = function(id) {
    quizBuilderState.selectedQuizId = id;
    builderEls.currentQuizBadge.textContent = `Выбран: ${quizBuilderState.quizzes.find(q => q.id === id)?.title || id}`;
    renderQuizList();
};

async function assignQuiz() {
    const quizId = Number(builderEls.assignQuizSelect.value || quizBuilderState.selectedQuizId || 0);
    const classId = Number(builderEls.assignClassSelect.value || 0);
    const startTime = builderEls.assignStartTime.value;
    const endTime = builderEls.assignEndTime.value;
    const timeLimitMinutes = builderEls.assignTimeLimit.value ? Number(builderEls.assignTimeLimit.value) : null;

    if (!quizId) {
        showMessage('Выбери квиз', 'error');
        return;
    }

    if (!classId) {
        showMessage('Выбери класс', 'error');
        return;
    }

    try {
        await api('/teacher/quiz/assign', {
            method: 'POST',
            body: JSON.stringify({
                quizId,
                classId,
                studentIds: [],
                startTime,
                endTime,
                timeLimitMinutes
            })
        });

        showMessage('Квиз назначен');
        await loadMyAssignments();
    } catch (e) {
        showMessage(e.message, 'error');
    }
}

async function loadMyAssignments() {
    try {
        const assignments = await api('/teacher/quiz/assignments');
        quizBuilderState.assignments = Array.isArray(assignments) ? assignments : [];
        renderAssignments();
    } catch (e) {
        console.error(e);
    }
}

function renderAssignments() {
    const el = builderEls.assignmentList;

    if (!quizBuilderState.assignments.length) {
        el.innerHTML = '<div class="empty">Пока нет назначений</div>';
        return;
    }

    el.innerHTML = quizBuilderState.assignments.map(a => `
        <div class="assignment-item">
            <h4>${escapeHtml(a.quiz?.title || 'Без названия')}</h4>
            <div class="muted">Класс: ${escapeHtml(a.schoolClass?.name || '—')}</div>
            <div class="muted">Открытие: ${a.startTime || '—'}</div>
            <div class="muted">Закрытие: ${a.endTime || '—'}</div>
            <div class="muted">Лимит: ${a.timeLimitMinutes ? a.timeLimitMinutes + ' мин' : 'без лимита'}</div>
            <div style="margin-top:10px;">
                <button class="btn btn-secondary btn-small" onclick="openResults(${a.id})">
    Результаты
</button>
            </div>
        </div>
    `).join('');
}

function logout() {
    localStorage.clear();
    window.location.href = '/login.html';
}
window.logout = logout;
window.openResults = function(assignmentId) {
    window.location.href = `/quiz-results.html?assignmentId=${assignmentId}`;
};