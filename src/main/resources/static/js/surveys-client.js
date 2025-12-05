let availableSurveys = [];
let currentSurveyId = null;

document.addEventListener('DOMContentLoaded', () => {
    const counter = document.getElementById('surveys-counter');
    if (!counter) return; // на этой странице нет блока

    loadAvailableSurveys();

    const btn = document.getElementById('open-survey-btn');
    if (btn) {
        btn.addEventListener('click', openFillSurveyModal);
    }

    const form = document.getElementById('fillSurveyForm');
    if (form) {
        form.addEventListener('submit', submitSurveyAnswers);
    }
});

async function loadAvailableSurveys() {
    const message = document.getElementById('surveys-message');
    const counter = document.getElementById('surveys-counter');
    const btn = document.getElementById('open-survey-btn');

    message.textContent = 'Проверяем доступные опросы...';
    btn.style.display = 'none';

    try {
        availableSurveys = await ApiService.get('/surveys/available');
        const count = availableSurveys.length;

        if (count === 0) {
            counter.textContent = '0 опросов';
            message.textContent = 'Сейчас для вас нет активных опросов.';
            return;
        }

        counter.textContent = `Надо пройти ${count} опрос(ов)`;
        message.textContent = `Надо пройти ${count} опрос(ов). Нажмите, чтобы ответить.`;
        btn.style.display = 'inline-flex';
    } catch (e) {
        console.error(e);
        counter.textContent = '—';
        message.textContent = 'Ошибка загрузки опросов.';
    }
}

async function openFillSurveyModal() {
    if (!availableSurveys || availableSurveys.length === 0) return;

    // пока берём первый доступный опрос
    const survey = availableSurveys[0];
    currentSurveyId = survey.id;

    try {
        const fullSurvey = await ApiService.get(`/surveys/${survey.id}`);
        fillSurveyModal(fullSurvey);
    } catch (e) {
        console.error(e);
        alert('Ошибка загрузки опроса');
    }
}

function fillSurveyModal(data) {
    const titleEl = document.getElementById('fillSurveyTitle');
    const descEl = document.getElementById('fillSurveyDescription');
    const qContainer = document.getElementById('fillSurveyQuestions');

    titleEl.textContent = data.title;
    descEl.textContent = data.description || '';
    qContainer.innerHTML = '';

    data.questions.forEach((q, idx) => {
        const block = document.createElement('div');
        block.className = 'survey-question';
        block.dataset.questionId = q.id;

        const label = document.createElement('div');
        label.style.fontWeight = '600';
        label.style.marginBottom = '.25rem';
        label.textContent = (idx + 1) + '. ' + q.text;
        block.appendChild(label);

        if (q.type === 'MULTIPLE_CHOICE') {
            q.options.forEach(o => {
                const row = document.createElement('label');
                row.style.display = 'block';
                row.style.marginBottom = '.25rem';

                const input = document.createElement('input');
                input.type = 'radio';
                input.name = `q_${q.id}`;
                input.value = o.id;
                input.style.marginRight = '0.35rem';

                row.appendChild(input);
                row.appendChild(document.createTextNode(o.text));
                block.appendChild(row);
            });
        } else if (q.type === 'TEXT') {
            const textarea = document.createElement('textarea');
            textarea.rows = 2;
            textarea.className = 'survey-text-answer';
            textarea.placeholder = 'Ваш ответ';
            block.appendChild(textarea);
        }

        qContainer.appendChild(block);
    });

    document.getElementById('fillSurveyModal').style.display = 'flex';
}

function closeFillSurveyModal() {
    document.getElementById('fillSurveyModal').style.display = 'none';
    currentSurveyId = null;
}

async function submitSurveyAnswers(e) {
    e.preventDefault();
    if (!currentSurveyId) return;

    const answers = [];

    document.querySelectorAll('#fillSurveyQuestions .survey-question').forEach(qEl => {
        const questionId = parseInt(qEl.dataset.questionId, 10);
        const radios = qEl.querySelectorAll('input[type="radio"]');
        if (radios.length > 0) {
            const checked = Array.from(radios).find(r => r.checked);
            if (checked) {
                answers.push({
                    questionId,
                    optionId: parseInt(checked.value, 10),
                    textAnswer: null
                });
            }
        } else {
            const textarea = qEl.querySelector('.survey-text-answer');
            const text = textarea ? textarea.value.trim() : '';
            if (text) {
                answers.push({
                    questionId,
                    optionId: null,
                    textAnswer: text
                });
            }
        }
    });

    try {
        await ApiService.post('/surveys/answer', {
            surveyId: currentSurveyId,
            answers
        });

        alert('Спасибо! Ваши ответы сохранены.');
        closeFillSurveyModal();
        await loadAvailableSurveys();
    } catch (e) {
        console.error(e);
        alert('Ошибка отправки ответов');
    }
}
