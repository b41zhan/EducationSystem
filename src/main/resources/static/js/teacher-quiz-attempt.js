const token = localStorage.getItem("token");
let attemptData = null;

function getAttemptId() {
    const params = new URLSearchParams(window.location.search);
    return params.get("attemptId");
}

function showMessage(text, type = "success") {
    const el = document.getElementById("pageMessage");
    el.className = `message ${type}`;
    el.textContent = text;
    el.style.display = "block";

    setTimeout(() => {
        el.style.display = "none";
    }, 5000);
}

function escapeHtml(str) {
    if (str === null || str === undefined) return "";
    return String(str)
        .replace(/&/g, "&amp;")
        .replace(/</g, "&lt;")
        .replace(/>/g, "&gt;")
        .replace(/"/g, "&quot;")
        .replace(/'/g, "&#039;");
}

function logout() {
    localStorage.clear();
    window.location.href = "/login.html";
}
window.logout = logout;

function parseSelectedIds(json) {
    if (!json) return [];
    try {
        const parsed = JSON.parse(json);
        return Array.isArray(parsed) ? parsed.map(Number) : [];
    } catch (e) {
        return [];
    }
}

async function loadAttemptDetails() {
    const attemptId = getAttemptId();

    if (!attemptId) {
        showMessage("attemptId не найден", "error");
        return;
    }

    const response = await fetch(`/api/teacher/quiz/attempt/${attemptId}`, {
        headers: {
            "Authorization": "Bearer " + token
        }
    });

    if (!response.ok) {
        let message = "Ошибка загрузки попытки";
        try {
            const err = await response.json();
            message = err.error || err.message || message;
        } catch (_) {}
        showMessage(message, "error");
        return;
    }

    attemptData = await response.json();
    renderAttempt();
}

function renderAttempt() {
    document.getElementById("quizTitle").textContent =
        `${attemptData.quizTitle} — ${attemptData.studentName}`;

    const backLink = document.getElementById("backLink");
    backLink.href = `/quiz-results.html?assignmentId=${attemptData.assignmentId}`;

    const meta = document.getElementById("attemptMeta");
    meta.innerHTML = `
        <span class="chip">Attempt ID: ${attemptData.attemptId}</span>
        <span class="chip">Student: ${escapeHtml(attemptData.studentName)}</span>
        <span class="chip">Score: ${attemptData.score ?? 0}</span>
        <span class="chip">Status: ${escapeHtml(attemptData.status)}</span>
        <span class="chip">Start: ${escapeHtml(attemptData.startTime || "—")}</span>
        <span class="chip">End: ${escapeHtml(attemptData.endTime || "—")}</span>
    `;

    const container = document.getElementById("questionsContainer");
    container.innerHTML = "";

    (attemptData.questions || []).forEach((q, index) => {
        const selectedIds = parseSelectedIds(q.studentSelectedOptionIdsJson);

        const block = document.createElement("div");
        block.className = "question";

        let correctnessHtml = "";
        if (q.questionType !== "TEXT_ANSWER") {
            if (q.isCorrect === true) {
                correctnessHtml = `<div class="status-ok">Правильно</div>`;
            } else if (q.isCorrect === false) {
                correctnessHtml = `<div class="status-bad">Неправильно</div>`;
            } else {
                correctnessHtml = `<div class="status-wait">Не проверено</div>`;
            }
        }

        let optionsHtml = "";
        if (Array.isArray(q.options) && q.options.length > 0) {
            optionsHtml = q.options.map(opt => {
                const isSelected = selectedIds.includes(Number(opt.id));
                const classes = ["option"];
                if (opt.correct) classes.push("correct");

                return `
                    <div class="${classes.join(" ")}">
                        ${opt.correct ? "✅" : "▫️"} ${escapeHtml(opt.optionText)}
                        ${isSelected ? `<div class="muted" style="margin-top:6px;">Выбрано студентом</div>` : ""}
                    </div>
                `;
            }).join("");
        }

        let studentAnswerHtml = "";
        if (q.questionType === "TEXT_ANSWER") {
            studentAnswerHtml = `
                <div class="student-answer">
                    ${q.studentAnswerText ? escapeHtml(q.studentAnswerText) : "<span class='muted'>Студент не дал текстовый ответ</span>"}
                </div>
            `;
        }

        let manualGradingHtml = "";
        if (q.manuallyGradable) {
            manualGradingHtml = `
                <div class="grading-box">
                    <div class="row">
                        <label for="grade_${q.questionId}"><b>Баллы за текстовый ответ</b></label>
                        <input
                            id="grade_${q.questionId}"
                            type="number"
                            min="0"
                            max="${q.points}"
                            value="${q.pointsAwarded ?? 0}"
                        />
                        <span class="muted">из ${q.points}</span>
                    </div>
                </div>
            `;
        }

        block.innerHTML = `
            <div class="question-title">
                ${index + 1}. ${escapeHtml(q.questionText)}
            </div>

            <div class="meta">
                <span class="chip">Тип: ${escapeHtml(q.questionType)}</span>
                <span class="chip">Макс. балл: ${q.points}</span>
                <span class="chip">Начислено: ${q.pointsAwarded ?? 0}</span>
            </div>

            <div style="margin-top:12px;">
                ${correctnessHtml}
            </div>

            ${optionsHtml}
            ${studentAnswerHtml}
            ${manualGradingHtml}
        `;

        container.appendChild(block);
    });
}

async function saveTextGrades() {
    if (!attemptData || !Array.isArray(attemptData.questions)) {
        showMessage("Нет данных попытки", "error");
        return;
    }

    const grades = attemptData.questions
        .filter(q => q.manuallyGradable)
        .map(q => {
            const input = document.getElementById(`grade_${q.questionId}`);
            let value = Number(input?.value ?? 0);

            if (Number.isNaN(value)) value = 0;
            if (value < 0) value = 0;
            if (value > q.points) value = q.points;

            return {
                questionId: q.questionId,
                pointsAwarded: value
            };
        });

    const response = await fetch(`/api/teacher/quiz/attempt/grade-text`, {
        method: "POST",
        headers: {
            "Content-Type": "application/json",
            "Authorization": "Bearer " + token
        },
        body: JSON.stringify({
            attemptId: attemptData.attemptId,
            grades: grades
        })
    });

    if (!response.ok) {
        let message = "Ошибка сохранения проверки";
        try {
            const err = await response.json();
            message = err.error || err.message || message;
        } catch (_) {}
        showMessage(message, "error");
        return;
    }

    attemptData = await response.json();
    renderAttempt();
    showMessage("Проверка текстовых ответов сохранена");
}

document.addEventListener("DOMContentLoaded", () => {
    if (!token) {
        window.location.href = "/login.html";
        return;
    }

    document.getElementById("saveGradesBtn")
        .addEventListener("click", saveTextGrades);

    loadAttemptDetails();
});