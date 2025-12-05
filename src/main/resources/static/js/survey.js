
    const surveyModal = document.getElementById('survey-modal');
    const surveyModalBody = document.getElementById('survey-modal-body');
    const surveyOpenBtn = document.getElementById('open-survey-btn');
    const surveyCloseBtn = document.getElementById('survey-modal-close');

    const surveyStatusBadge = document.getElementById('survey-status-badge');
    const surveyStatusText = document.getElementById('survey-status-text');

    function openSurveyModal() {
    surveyModal.classList.add('visible');
    document.body.style.overflow = 'hidden'; // блокируем скролл фона
}

    function closeSurveyModal() {
    surveyModal.classList.remove('visible');
    document.body.style.overflow = '';
    surveyModalBody.innerHTML = ''; // чистим форму
}

    // закрытие по крестику
    surveyCloseBtn.addEventListener('click', closeSurveyModal);

    // закрытие по клику на фон
    surveyModal.addEventListener('click', function (e) {
    if (e.target === surveyModal) {
    closeSurveyModal();
}
});

    // клик по "Пройти опрос"
    if (surveyOpenBtn) {
    surveyOpenBtn.addEventListener('click', function () {
        loadSurveyAndOpen();
    });
}

    // === ЗАГРУЗКА ОПРОСА И РЕНДЕР В МОДАЛКЕ ===
    // === ЗАГРУЗКА ОПРОСА И РЕНДЕР В МОДАЛКЕ ===
    async function loadSurveyAndOpen() {
        try {
            // ИСПОЛЬЗУЕМ ApiService, чтобы подставился Authorization
            const list = await ApiService.get('/surveys/available');  // = /api/surveys/available с токеном
            const surveyShort = Array.isArray(list) ? list[0] : list;

            if (!surveyShort) {
                alert('Нет доступных опросов');
                updateSurveyStatusAsCompleted();
                return;
            }

            // тянем детали опроса
            const survey = await ApiService.get(`/surveys/${surveyShort.id}`); // /api/surveys/{id} с токеном

            renderSurveyFormInModal(survey);
            openSurveyModal();
        } catch (err) {
            console.error(err);
            alert('Не удалось загрузить опрос');
        }

    }

    // data = SurveyDetailsDTO из бекэнда
    function renderSurveyFormInModal(survey) {
    let html = '';

    html += `<h3>${survey.title}</h3>`;
    html += `<p class="survey-description">${survey.description || ''}</p>`;
    html += `<form id="survey-form">`;

    survey.questions.forEach((q, idx) => {
    html += `<div class="survey-question">`;
    html += `<div class="survey-question-title">${idx + 1}. ${q.text}</div>`;

    if (q.type === 'MULTIPLE_CHOICE') {
    q.options.forEach(opt => {
    const inputId = `q${q.id}_opt${opt.id}`;
    html += `
                        <div>
                            <label for="${inputId}">
                                <input type="radio"
                                       name="q_${q.id}"
                                       id="${inputId}"
                                       value="${opt.id}">
                                ${opt.text}
                            </label>
                        </div>
                    `;
});
} else { // TEXT
    html += `
                    <textarea name="q_${q.id}"
                              class="form-control"
                              rows="2"
                              placeholder="Ваш ответ..."></textarea>
                `;
}

    html += `</div>`;
});

    html += `<button type="submit" class="btn btn-primary btn-sm">Отправить ответы</button>`;
    html += `</form>`;

    surveyModalBody.innerHTML = html;

    // обработчик отправки
    const form = document.getElementById('survey-form');
        form.addEventListener('submit', function (e) {
            e.preventDefault();
            submitSurveyAnswersFromModal(survey.id, form, closeSurveyModal);
        });
}

    // === ОТПРАВКА ОТВЕТОВ НА БЕК ===
    // === ОТПРАВКА ОТВЕТОВ НА БЕК ИЗ МОДАЛКИ ===
    async function submitSurveyAnswersFromModal(surveyId, form, onSuccess) {
        const formData = new FormData(form);
        const answers = [];

        for (const [name, value] of formData.entries()) {
            if (!name.startsWith('q_')) continue;
            const questionId = parseInt(name.substring(2), 10);

            const answer = { questionId };

            if (!isNaN(parseInt(value, 10))) {
                // выбран вариант (radio)
                answer.optionId = parseInt(value, 10);
            } else {
                // текстовый ответ
                answer.textAnswer = value;
            }
            answers.push(answer);
        }

        const payload = {
            surveyId,
            answers
        };

        try {
            await ApiService.post('/surveys/answer', payload);
            alert('Спасибо! Ваши ответы сохранены.');
            updateSurveyStatusAsCompleted();
            onSuccess();
        } catch (err) {
            console.error(err);
            alert('Не удалось отправить ответы');
        }
    }


    // Обновление текста под карточкой после прохождения опроса
    function updateSurveyStatusAsCompleted() {
    if (surveyStatusBadge) {
    surveyStatusBadge.textContent = 'Опросов нет';
}
    if (surveyStatusText) {
    surveyStatusText.textContent = 'Все доступные опросы пройдены.';
}
    if (surveyOpenBtn) {
    surveyOpenBtn.disabled = true;
    surveyOpenBtn.textContent = 'Опросов нет';
}
}

