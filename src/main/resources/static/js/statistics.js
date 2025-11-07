class StatisticsManager {
    constructor() {
        this.currentTeacherId = localStorage.getItem('userId');
        this.init();
    }

    init() {
        console.log('StatisticsManager initialized for teacher:', this.currentTeacherId);
        this.loadTeacherClasses();
    }

    async loadTeacherClasses() {
        try {
            const classes = await ApiService.get('/statistics/teacher/classes');
            this.populateClassSelect(classes);
        } catch (error) {
            console.error('Error loading teacher classes:', error);
            this.showMessage('Ошибка загрузки классов: ' + error.message, 'error');
        }
    }

    populateClassSelect(classes) {
        const select = document.getElementById('class-select');
        select.innerHTML = '<option value="">-- Выберите класс --</option>';

        if (!classes || classes.length === 0) {
            select.innerHTML += '<option value="" disabled>Нет доступных классов</option>';
            return;
        }

        classes.forEach(schoolClass => {
            const option = document.createElement('option');
            option.value = schoolClass.id;
            option.textContent = schoolClass.name;
            select.appendChild(option);
        });
    }

    async loadClassStatistics(classId) {
        if (!classId) {
            this.hideStatistics();
            return;
        }

        try {
            const classStats = await ApiService.get(`/statistics/class/${classId}`);
            this.displayClassStatistics(classStats);
        } catch (error) {
            console.error('Error loading class statistics:', error);
            this.showMessage('Ошибка загрузки статистики: ' + error.message, 'error');
        }
    }

    displayClassStatistics(classStats) {
        this.showStatistics();
        this.displayClassSummary(classStats);
        this.displayStudentsTable(classStats);
    }

    displayClassSummary(classStats) {
        const summaryContainer = document.getElementById('class-summary');

        const completionRate = classStats.totalAssignments > 0 ?
            Math.round((classStats.students.reduce((sum, student) => sum + student.completedAssignments, 0) /
                (classStats.students.length * classStats.totalAssignments)) * 100) : 0;

        summaryContainer.innerHTML = `
            <h3>Класс: ${classStats.className}</h3>
            <div class="summary-stats">
                <div class="summary-stat">
                    <div class="stat-value">${classStats.totalStudents}</div>
                    <div class="stat-label">Студентов</div>
                </div>
                <div class="summary-stat">
                    <div class="stat-value">${classStats.totalAssignments}</div>
                    <div class="stat-label">Заданий</div>
                </div>
                <div class="summary-stat">
                    <div class="stat-value">${classStats.classAverageGrade || 0}</div>
                    <div class="stat-label">Средний балл</div>
                </div>
                <div class="summary-stat">
                    <div class="stat-value">${completionRate}%</div>
                    <div class="stat-label">Выполнено</div>
                </div>
            </div>
        `;
    }

    displayStudentsTable(classStats) {
        const tableContainer = document.getElementById('students-stats-table');

        if (!classStats.students || classStats.students.length === 0) {
            tableContainer.innerHTML = '<p>Нет студентов в классе</p>';
            return;
        }

        let tableHTML = `
            <table class="students-table">
                <thead>
                    <tr>
                        <th>Студент</th>
                        ${this.generateAssignmentHeaders(classStats.totalAssignments)}
                        <th>Средний</th>
                        <th>Выполнено</th>
                    </tr>
                </thead>
                <tbody>
        `;

        classStats.students.forEach(student => {
            tableHTML += this.generateStudentRow(student, classStats.totalAssignments);
        });

        tableHTML += '</tbody></table>';
        tableContainer.innerHTML = tableHTML;
    }

    generateAssignmentHeaders(totalAssignments) {
        let headers = '';
        for (let i = 1; i <= totalAssignments; i++) {
            headers += `<th>#${i}</th>`;
        }
        return headers;
    }

    generateStudentRow(student, totalAssignments) {
        const completionRate = totalAssignments > 0 ?
            Math.round((student.completedAssignments / totalAssignments) * 100) : 0;

        let gradesHTML = '';
        for (let i = 0; i < totalAssignments; i++) {
            const grade = student.grades && i < student.grades.length ? student.grades[i] : 0;
            gradesHTML += `<td class="grade-cell ${this.getGradeClass(grade)}">${grade > 0 ? grade : '-'}</td>`;
        }

        return `
            <tr>
                <td class="student-name">${student.studentName}</td>
                ${gradesHTML}
                <td class="average-grade ${this.getGradeClass(student.averageGrade)}">
                    ${student.averageGrade || '-'}
                </td>
                <td>
                    ${student.completedAssignments}/${totalAssignments}
                    <div class="completion-rate">${completionRate}%</div>
                </td>
            </tr>
        `;
    }

    getGradeClass(grade) {
        if (!grade || grade === 0) return 'grade-missing';
        if (grade >= 90) return 'grade-excellent';
        if (grade >= 75) return 'grade-good';
        if (grade >= 60) return 'grade-average';
        if (grade >= 40) return 'grade-poor';
        return 'grade-fail';
    }

    showStatistics() {
        document.getElementById('class-statistics').style.display = 'block';
        document.getElementById('no-statistics').style.display = 'none';
    }

    hideStatistics() {
        document.getElementById('class-statistics').style.display = 'none';
        document.getElementById('no-statistics').style.display = 'block';
    }

    showMessage(message, type) {
        // Используем существующую систему сообщений
        const messageDiv = document.createElement('div');
        messageDiv.className = type === 'error' ? 'error-message' : 'success-message';
        messageDiv.textContent = message;
        messageDiv.style.marginTop = '10px';

        const statisticsCard = document.querySelector('.card h2').parentElement;
        statisticsCard.appendChild(messageDiv);

        setTimeout(() => {
            if (messageDiv.parentElement) {
                messageDiv.remove();
            }
        }, 5000);
    }
}

// Автоматическая инициализация
document.addEventListener('DOMContentLoaded', function() {
    setTimeout(() => {
        if (typeof ApiService !== 'undefined') {
            window.statisticsManager = new StatisticsManager();
            console.log('StatisticsManager successfully initialized');
        } else {
            console.error('ApiService not found - StatisticsManager cannot initialize');
        }
    }, 100);
});