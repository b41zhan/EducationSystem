const token = localStorage.getItem("token");

function getAssignmentId(){
    const params = new URLSearchParams(window.location.search);
    return params.get("assignmentId");
}

async function loadResults(){

    const assignmentId = getAssignmentId();

    if(!assignmentId){
        alert("assignmentId не найден");
        return;
    }

    const response = await fetch(`/api/teacher/quiz/results/${assignmentId}`, {
        headers:{
            "Authorization": "Bearer " + token
        }
    });

    if(!response.ok){
        alert("Ошибка загрузки результатов");
        return;
    }

    const data = await response.json();

    const container = document.getElementById("list");
    container.innerHTML = "";

    data.forEach(item => {

        const div = document.createElement("div");
        div.className = "item";

        div.innerHTML = `
            <div>
                <b>${item.studentName}</b><br>
                Балл: ${item.score ?? "—"}<br>
                Статус: ${item.status}
            </div>

            <button onclick="openAttempt(${item.attemptId})">
                Открыть
            </button>
        `;

        container.appendChild(div);
    });
}

function openAttempt(id){
    window.location.href = `/quiz-result.html?attemptId=${id}`;
}

document.addEventListener("DOMContentLoaded", loadResults);