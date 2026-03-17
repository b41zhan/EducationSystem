const token = localStorage.getItem("token");

let attemptId = null;
let assignmentId = null;
let quizData = null;
let assignmentData = null;

let timerInterval = null;

function getAssignmentId() {
    const params = new URLSearchParams(window.location.search);
    return params.get("assignmentId");
}

async function startAttempt() {
    assignmentId = getAssignmentId();

    if (!assignmentId) {
        alert("Assignment not found");
        return;
    }

    const response = await fetch(`/api/student/quiz/start`, {
        method: "POST",
        headers: {
            "Content-Type": "application/json",
            "Authorization": "Bearer " + token
        },
        body: JSON.stringify({
            assignmentId: Number(assignmentId)
        })
    });

    if (!response.ok) {
        const err = await response.text();
        console.error(err);
        alert("Не удалось начать квиз");
        return;
    }

    const data = await response.json();
    attemptId = data.id || data.attemptId;

    if (!attemptId) {
        console.error("start response:", data);
        alert("Attempt ID not returned from backend");
        return;
    }

    await loadQuiz(data);
}

async function loadQuiz(startData) {
    const response = await fetch(`/api/student/quiz/assignment/${assignmentId}`, {
        headers: {
            "Authorization": "Bearer " + token
        }
    });

    if (!response.ok) {
        const err = await response.text();
        console.error(err);
        alert("Не удалось загрузить квиз");
        return;
    }

    assignmentData = await response.json();
    quizData = assignmentData.quiz;

    document.getElementById("quiz-title").innerText = quizData.title || "Quiz";
    document.getElementById("quiz-description").innerText = quizData.description || "";

    // 🔥 ЗАПУСК ТАЙМЕРА
    startTimer(
        assignmentData.timeLimitMinutes,
        startData.startTime || startData.startedAt
    );

    const container = document.getElementById("questions-container");
    container.innerHTML = "";

    const questions = [...(quizData.questions || [])]
        .sort((a, b) => (a.orderIndex || 0) - (b.orderIndex || 0));

    questions.forEach((q, index) => {
        const questionDiv = document.createElement("div");
        questionDiv.className = "question";

        const title = document.createElement("h3");
        title.innerText = `${index + 1}. ${q.questionText}`;
        questionDiv.appendChild(title);

        if (q.questionType === "TEXT_ANSWER") {
            const input = document.createElement("textarea");
            input.placeholder = "Введите ваш ответ";
            input.id = `text_${q.id}`;
            input.rows = 4;
            questionDiv.appendChild(input);
        }

        if (q.questionType === "SINGLE_CHOICE") {
            q.options
                .sort((a, b) => (a.orderIndex || 0) - (b.orderIndex || 0))
                .forEach(opt => {
                    const label = document.createElement("label");
                    label.style.display = "block";

                    const input = document.createElement("input");
                    input.type = "radio";
                    input.name = `question_${q.id}`;
                    input.value = opt.id;

                    label.appendChild(input);
                    label.appendChild(document.createTextNode(" " + opt.optionText));
                    questionDiv.appendChild(label);
                });
        }

        if (q.questionType === "MULTIPLE_CHOICE") {
            q.options
                .sort((a, b) => (a.orderIndex || 0) - (b.orderIndex || 0))
                .forEach(opt => {
                    const label = document.createElement("label");
                    label.style.display = "block";

                    const input = document.createElement("input");
                    input.type = "checkbox";
                    input.name = `question_${q.id}`;
                    input.value = opt.id;

                    label.appendChild(input);
                    label.appendChild(document.createTextNode(" " + opt.optionText));
                    questionDiv.appendChild(label);
                });
        }

        container.appendChild(questionDiv);
    });
}

function startTimer(timeLimitMinutes, startTime) {
    if (!timeLimitMinutes || !startTime) return;

    const timerEl = document.getElementById("timer");

    const start = new Date(startTime).getTime();
    const end = start + timeLimitMinutes * 60 * 1000;

    function updateTimer() {
        const now = Date.now();
        const diff = end - now;

        if (diff <= 0) {
            timerEl.textContent = "00:00";

            clearInterval(timerInterval);
            alert("Время вышло!");
            submitQuiz();
            return;
        }

        const seconds = Math.floor(diff / 1000);
        const mins = Math.floor(seconds / 60);
        const secs = seconds % 60;

        timerEl.textContent =
            `${String(mins).padStart(2, '0')}:${String(secs).padStart(2, '0')}`;
    }

    updateTimer();
    timerInterval = setInterval(updateTimer, 1000);
}

async function submitQuiz() {
    if (!attemptId) {
        alert("Attempt not initialized");
        return;
    }

    const questions = quizData.questions || [];

    for (const q of questions) {
        const payload = {
            attemptId: attemptId,
            questionId: q.id,
            answerText: null,
            selectedOptionIdsJson: null
        };

        if (q.questionType === "TEXT_ANSWER") {
            const text = document.getElementById(`text_${q.id}`);
            payload.answerText = text ? text.value : null;
        }

        if (q.questionType === "SINGLE_CHOICE") {
            const radio = document.querySelector(`input[name="question_${q.id}"]:checked`);
            payload.selectedOptionIdsJson = radio ? JSON.stringify([Number(radio.value)]) : null;
        }

        if (q.questionType === "MULTIPLE_CHOICE") {
            const checked = document.querySelectorAll(`input[name="question_${q.id}"]:checked`);
            const values = Array.from(checked).map(cb => Number(cb.value));
            payload.selectedOptionIdsJson = values.length ? JSON.stringify(values) : null;
        }

        const saveResponse = await fetch(`/api/student/quiz/answer`, {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
                "Authorization": "Bearer " + token
            },
            body: JSON.stringify(payload)
        });

        if (!saveResponse.ok) {
            alert("Ошибка при сохранении ответа");
            return;
        }
    }

    const finishResponse = await fetch(`/api/student/quiz/finish`, {
        method: "POST",
        headers: {
            "Content-Type": "application/json",
            "Authorization": "Bearer " + token
        },
        body: JSON.stringify({
            attemptId: attemptId
        })
    });

    if (!finishResponse.ok) {
        alert("Ошибка при завершении квиза");
        return;
    }

    // 🔥 ПРАВИЛЬНЫЙ ПЕРЕХОД
    window.location.href = `/quiz-result.html?attemptId=${attemptId}`;
}

document.addEventListener("DOMContentLoaded", startAttempt);
window.submitQuiz = submitQuiz;