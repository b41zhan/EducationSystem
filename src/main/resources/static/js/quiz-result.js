const token = localStorage.getItem("token");

function getAttemptId() {
    const params = new URLSearchParams(window.location.search);
    return params.get("attemptId");
}

async function loadResult() {

    const attemptId = getAttemptId();

    if (!attemptId) {
        document.getElementById("score").innerText = "Attempt not found";
        return;
    }

    const response = await fetch(`/api/student/quiz/result/${attemptId}`, {
        headers: {
            "Authorization": "Bearer " + token
        }
    });

    if(!response.ok){
        document.getElementById("score").innerText = "Ошибка загрузки";
        return;
    }

    const data = await response.json();

    document.getElementById("score").innerText =
        data.score !== null ? data.score : "Ожидает проверки";

    document.getElementById("status").innerText = data.status;
    document.getElementById("startTime").innerText = data.startTime;
    document.getElementById("endTime").innerText = data.endTime;
}

function goBack(){
    window.location.href = "/student-dashboard.html";
}

document.addEventListener("DOMContentLoaded", loadResult);