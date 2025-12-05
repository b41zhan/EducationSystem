let surveyQuestionCounter = 0;
let surveyChartInstance = null;

document.addEventListener('DOMContentLoaded', () => {
    // ничего не делаем пока, модалка открывается по кнопке
});

function openSurveyModal() {
    const modal = document.getElementById('surveyModal');
    if (!modal) return;
    modal.style.display = 'flex';

    resetSurveyForm();
    loadAdminSurveysList();
}

function closeSurveyModal() {
    const modal = document.getElementById('surveyModal');
    if (!modal) return;
    modal.style.display = 'none';
}

function resetSurveyForm() {
    document.getElementById('surveyTitle').value = '';
    document.getElementById('surveyDescription').value = '';
    document.getElementById('surveyForStudents').checked = true;
    document.getElementById('surveyForTeachers').checked = false;
    document.getElementById('surveyQuestionsContainer').innerHTML = '';
    surveyQuestionCounter = 0;
    addSurveyQuestion();

    document.getElementById('surveyResultsContainer').innerHTML = '';
    const select = document.getElementById('surveyResultsSelect');
    select.innerHTML = '';
    clearSurveyChart();
}

function addSurveyQuestion() {
    const container = document.getElementById('surveyQuestionsContainer');
    const idx = surveyQuestionCounter++;

    const wrapper = document.createElement('div');
    wrapper.className = 'survey-question';
    wrapper.dataset.index = idx;

    wrapper.innerHTML = `
        <div class="survey-question-header">
            <input type="text" class="survey-question-text"
                   placeholder="Текст вопроса" required>

            <select class="survey-question-type" onchange="onSurveyQuestionTypeChange(this)">
                <option value="MULTIPLE_CHOICE">С вариантами ответа</option>
                <option value="TEXT">Открытый вопрос</option>
            </select>

            <button type="button" class="btn-secondary" onclick="removeSurveyQuestion(this)">
                ✖
            </button>
        </div>

        <div class="survey-question-options">
            <div class="options-container">
                <input type="text" class="survey-option-input" placeholder="Вариант 1">
                <input type="text" class="survey-option-input" placeholder="Вариант 2">
            </div>
            <button type="button" class="btn-secondary" onclick="addSurveyOption(this)">
                ➕ Добавить вариант
            </button>
        </div>
    `;

    container.appendChild(wrapper);
}

function removeSurveyQuestion(btn) {
    const question = btn.closest('.survey-question');
    if (question) question.remove();
}

function onSurveyQuestionTypeChange(select) {
    const question = select.closest('.survey-question');
    const optionsBlock = question.querySelector('.survey-question-options');
    if (select.value === 'TEXT') {
        optionsBlock.style.display = 'none';
    } else {
        optionsBlock.style.display = 'block';
    }
}

function addSurveyOption(btn) {
    const container = btn.closest('.survey-question-options')
        .querySelector('.options-container');
    const input = document.createElement('input');
    input.type = 'text';
    input.className = 'survey-option-input';
    input.placeholder = 'Новый вариант';
    container.appendChild(input);
}

// ====== Сохранение опроса ======

const surveyForm = document.getElementById('surveyForm');
if (surveyForm) {
    surveyForm.addEventListener('submit', async (e) => {
        e.preventDefault();
        try {
            const dto = buildSurveyDTO();
            await ApiService.post('/surveys', dto);
            alert('Опрос сохранён');
            resetSurveyForm();
            loadAdminSurveysList();
        } catch (err) {
            console.error(err);
            alert('Ошибка при сохранении опроса');
        }
    });
}

function buildSurveyDTO() {
    const title = document.getElementById('surveyTitle').value.trim();
    const description = document.getElementById('surveyDescription').value.trim();
    const forStudents = document.getElementById('surveyForStudents').checked;
    const forTeachers = document.getElementById('surveyForTeachers').checked;

    const questions = [];
    document.querySelectorAll('.survey-question').forEach(qEl => {
        const text = qEl.querySelector('.survey-question-text').value.trim();
        const type = qEl.querySelector('.survey-question-type').value;
        if (!text) return;

        const qDto = { text, type, options: [] };

        if (type === 'MULTIPLE_CHOICE') {
            qEl.querySelectorAll('.survey-option-input').forEach(optInput => {
                const val = optInput.value.trim();
                if (val) qDto.options.push(val);
            });
        }

        questions.push(qDto);
    });

    return { title, description, forStudents, forTeachers, questions };
}

// ====== Результаты для админа ======

async function loadAdminSurveysList() {
    try {
        const surveys = await ApiService.get('/surveys/admin');
        const select = document.getElementById('surveyResultsSelect');
        select.innerHTML = '';

        if (!surveys || surveys.length === 0) {
            select.innerHTML = '<option value="">Опросов нет</option>';
            return;
        }

        surveys.forEach(s => {
            const opt = document.createElement('option');
            opt.value = s.id;
            opt.textContent = `${s.title} (${s.questionsCount} вопросов)`;
            select.appendChild(opt);
        });
    } catch (e) {
        console.error(e);
    }
}

async function loadSurveyResults() {
    clearSurveyChart();
    const select = document.getElementById('surveyResultsSelect');
    const id = select.value;
    if (!id) return;

    try {
        const data = await ApiService.get(`/surveys/${id}/results`);
        renderSurveyResults(data);
    } catch (e) {
        console.error(e);
        alert('Ошибка загрузки результатов');
    }
}

function renderSurveyResults(data) {
    const container = document.getElementById('surveyResultsContainer');
    container.innerHTML = '';

    if (!data || !data.questions || data.questions.length === 0) {
        container.innerHTML = '<div>Нет данных по вопросам</div>';
        return;
    }

    const title = document.createElement('h4');
    title.textContent = data.title;
    container.appendChild(title);

    data.questions.forEach(q => {
        const block = document.createElement('div');
        block.style.marginTop = '0.5rem';

        const h = document.createElement('div');
        h.style.fontWeight = '600';
        h.textContent = q.text;
        block.appendChild(h);

        if (q.type === 'MULTIPLE_CHOICE') {
            const list = document.createElement('ul');
            q.options.forEach(o => {
                const li = document.createElement('li');
                li.textContent = `${o.text}: ${o.count}`;
                list.appendChild(li);
            });
            block.appendChild(list);
        } else if (q.type === 'TEXT' && q.textAnswers) {
            const small = document.createElement('div');
            small.style.fontSize = '0.9rem';
            small.style.opacity = '0.8';
            small.textContent = `Открытых ответов: ${q.textAnswers.length}`;
            block.appendChild(small);
        }

        container.appendChild(block);
    });

    // Диаграмма ТОЛЬКО по первому закрытому вопросу
    const mcQuestion = data.questions.find(q => q.type === 'MULTIPLE_CHOICE');
    if (mcQuestion && mcQuestion.options && mcQuestion.options.length > 0) {
        const labels = mcQuestion.options.map(o => o.text);
        const counts = mcQuestion.options.map(o => o.count);

        const ctx = document.getElementById('surveyChart').getContext('2d');
        surveyChartInstance = new Chart(ctx, {
            type: 'bar',
            data: {
                labels,
                datasets: [{
                    label: mcQuestion.text,
                    data: counts
                }]
            },
            options: {
                responsive: true,
                plugins: {
                    legend: { display: false }
                }
            }
        });
    }
}

function clearSurveyChart() {
    if (surveyChartInstance) {
        surveyChartInstance.destroy();
        surveyChartInstance = null;
    }
}
