(function () {
    function escapeValue(value) {
        if (typeof escapeHtml === 'function') {
            return escapeHtml(value);
        }
        if (value === null || value === undefined) return '';
        return String(value)
            .replace(/&/g, '&amp;')
            .replace(/</g, '&lt;')
            .replace(/>/g, '&gt;')
            .replace(/"/g, '&quot;')
            .replace(/'/g, '&#039;');
    }

    function ensureState() {
        if (!Array.isArray(AdminState.availableTeachers)) {
            AdminState.availableTeachers = [];
        }
    }

    async function fetchTeachersBySchool(schoolId) {
        if (!schoolId) {
            AdminState.availableTeachers = [];
            fillHomeroomTeacherSelect('');
            return [];
        }

        const teachers = await ApiService.get(`/admin/structure/schools/${schoolId}/teachers`);
        AdminState.availableTeachers = Array.isArray(teachers) ? teachers : [];
        return AdminState.availableTeachers;
    }

    function fillHomeroomTeacherSelect(selectedValue = '') {
        const select = document.getElementById('classHomeroomTeacherSelect');
        if (!select) return;

        select.innerHTML =
            `<option value="">Без классного руководителя</option>` +
            (AdminState.availableTeachers || []).map(teacher => `
                <option value="${teacher.teacherId}">
                    ${escapeValue(teacher.fullName || 'Без имени')}${teacher.email ? ` — ${escapeValue(teacher.email)}` : ''}
                </option>
            `).join('');

        select.value = selectedValue ? String(selectedValue) : '';
    }

    async function prepareHomeroomTeachersForSchool(schoolId, selectedTeacherId = '') {
        if (!schoolId) {
            AdminState.availableTeachers = [];
            fillHomeroomTeacherSelect('');
            return;
        }

        await fetchTeachersBySchool(Number(schoolId));
        fillHomeroomTeacherSelect(selectedTeacherId);
    }

    function renderHomeroomTeacherText(item) {
        if (!item || !item.homeroomTeacherFullName) {
            return 'Не назначен';
        }
        return item.homeroomTeacherEmail
            ? `${item.homeroomTeacherFullName} — ${item.homeroomTeacherEmail}`
            : item.homeroomTeacherFullName;
    }

    function rebindClassForm() {
        const oldForm = document.getElementById('classForm');
        if (!oldForm || !oldForm.parentNode) return;

        const newForm = oldForm.cloneNode(true);
        oldForm.parentNode.replaceChild(newForm, oldForm);

        newForm.addEventListener('submit', handleSaveClass);

        const schoolSelect = document.getElementById('classSchoolSelect');
        if (schoolSelect) {
            schoolSelect.addEventListener('change', async function () {
                const schoolId = this.value ? Number(this.value) : null;
                await prepareHomeroomTeachersForSchool(schoolId);
            });
        }
    }

    window.renderEmptyOrganizationState = function () {
        AdminState.classDetails = null;

        const classesManagementList = document.getElementById('classesManagementList');
        const classDetailsContainer = document.getElementById('classDetailsContainer');
        const parentChildLinksList = document.getElementById('parentChildLinksList');

        if (classesManagementList) {
            classesManagementList.innerHTML = `<div class="empty-state">Выберите школу, чтобы загрузить классы</div>`;
        }

        if (classDetailsContainer) {
            classDetailsContainer.innerHTML = `Выберите класс, чтобы увидеть его состав`;
        }

        if (parentChildLinksList) {
            parentChildLinksList.innerHTML = `<div class="empty-state">Выберите школу, чтобы загрузить связи</div>`;
        }
    };

    window.refreshStructureData = async function () {
        ensureState();

        if (!AdminState.selectedSchoolId) {
            renderEmptyOrganizationState();
            return;
        }

        if (typeof hideStructureMessages === 'function') {
            hideStructureMessages();
        }

        try {
            await Promise.all([
                loadManagedClasses(),
                loadSchoolStudents(),
                loadUnassignedStudents(),
                loadAvailableParents(),
                loadParentChildLinks(),
                fetchTeachersBySchool(AdminState.selectedSchoolId)
            ]);

            const classDetailsSelect = document.getElementById('classDetailsSelect');
            if (classDetailsSelect && classDetailsSelect.value) {
                await loadSelectedClassDetails();
            }

            if (typeof showStructureInfo === 'function') {
                showStructureInfo('Данные структуры школы обновлены');
            }
        } catch (e) {
            console.error('refreshStructureData (homeroom) error:', e);
            if (typeof showStructureError === 'function') {
                showStructureError('Ошибка загрузки структуры школы: ' + (e.message || e));
            }
        }
    };

    window.loadManagedClasses = async function () {
        const classes = await ApiService.get(`/admin/structure/schools/${AdminState.selectedSchoolId}/classes`);
        AdminState.classes = Array.isArray(classes) ? classes : [];

        renderClassesManagementList();
        fillManagedClassSelects();
    };

    window.renderClassesManagementList = function () {
        const container = document.getElementById('classesManagementList');

        if (!container) return;

        if (!AdminState.classes.length) {
            container.innerHTML = `<div class="empty-state">Для этой школы пока нет классов</div>`;
            return;
        }

        container.innerHTML = AdminState.classes.map(item => `
            <div class="management-item">
                <div class="management-item-head">
                    <div>
                        <div class="management-item-title">
                            ${escapeValue(item.name)}
                            ${item.academicYear ? `<span class="status-pill">${escapeValue(item.academicYear)}</span>` : ''}
                        </div>
                        <div class="management-item-meta">
                            <span>Школа: ${escapeValue(item.schoolName || '—')}</span>
                            <span>Учеников: ${item.studentsCount ?? 0}</span>
                            <span>Классный руководитель: ${escapeValue(item.homeroomTeacherFullName || 'Не назначен')}</span>
                            ${item.active ? '<span class="status-pill">Активный</span>' : '<span class="status-pill inactive">Архивный</span>'}
                        </div>
                    </div>

                    <div class="management-item-actions">
                        <button class="btn-action btn-secondary btn-small" onclick="selectClassDetails(${item.id})">
                            <i class="fas fa-users"></i> Состав
                        </button>
                        <button class="btn-action btn-secondary btn-small" onclick="openEditClassModal(${item.id})">
                            <i class="fas fa-edit"></i> Редактировать
                        </button>
                        ${item.active ? `
                        <button class="btn-action btn-secondary btn-small" onclick="archiveClass(${item.id})">
                            <i class="fas fa-box-archive"></i> Архивировать
                        </button>` : ''}
                    </div>
                </div>
            </div>
        `).join('');
    };

    window.fillManagedClassSelects = function () {
        const selects = [
            document.getElementById('classDetailsSelect'),
            document.getElementById('assignTargetClassSelect'),
            document.getElementById('transferTargetClassSelect')
        ];

        const options =
            `<option value="">Выберите класс</option>` +
            AdminState.classes.map(item => `
                <option value="${item.id}">
                    ${escapeValue(item.name)}${item.academicYear ? ` (${escapeValue(item.academicYear)})` : ''}
                </option>
            `).join('');

        selects.forEach(select => {
            if (!select) return;
            const oldValue = select.value;
            select.innerHTML = options;

            if (oldValue && AdminState.classes.some(c => String(c.id) === String(oldValue))) {
                select.value = oldValue;
            }
        });
    };

    window.selectClassDetails = async function (classId) {
        const select = document.getElementById('classDetailsSelect');
        if (select) {
            select.value = String(classId);
        }
        await loadSelectedClassDetails();
    };

    window.loadSelectedClassDetails = async function () {
        const select = document.getElementById('classDetailsSelect');
        const classId = select ? select.value : '';

        if (!classId) {
            AdminState.classDetails = null;
            const container = document.getElementById('classDetailsContainer');
            if (container) {
                container.innerHTML = `Выберите класс, чтобы увидеть его состав`;
            }
            return;
        }

        try {
            const details = await ApiService.get(`/admin/structure/classes/${classId}`);
            AdminState.classDetails = details;
            renderClassDetails();
        } catch (e) {
            console.error('loadSelectedClassDetails error:', e);
            if (typeof showStructureError === 'function') {
                showStructureError('Ошибка загрузки деталей класса: ' + (e.message || e));
            }
        }
    };

    window.renderClassDetails = function () {
        const container = document.getElementById('classDetailsContainer');
        const details = AdminState.classDetails;

        if (!container) return;

        if (!details || !details.schoolClass) {
            container.innerHTML = `Выберите класс, чтобы увидеть его состав`;
            return;
        }

        const schoolClass = details.schoolClass;
        const students = Array.isArray(details.students) ? details.students : [];

        container.innerHTML = `
            <div style="display:grid; gap:12px;">
                <div>
                    <div class="management-item-title">${escapeValue(schoolClass.name || '—')}</div>
                    <div class="management-item-meta" style="margin-top:8px;">
                        <span>Учебный год: ${escapeValue(schoolClass.academicYear || '—')}</span>
                        <span>Статус: ${schoolClass.active ? 'Активный' : 'Архивный'}</span>
                        <span>Классный руководитель: ${escapeValue(renderHomeroomTeacherText(schoolClass))}</span>
                    </div>
                </div>

                ${students.length ? `
                <div class="data-table-wrap">
                    <table class="data-table">
                        <thead>
                            <tr>
                                <th>Ученик</th>
                                <th>Email</th>
                                <th>Класс</th>
                            </tr>
                        </thead>
                        <tbody>
                            ${students.map(student => `
                                <tr>
                                    <td>${escapeValue(student.fullName || 'Без имени')}</td>
                                    <td>${escapeValue(student.email || '—')}</td>
                                    <td>${escapeValue(student.className || '—')}</td>
                                </tr>
                            `).join('')}
                        </tbody>
                    </table>
                </div>
                ` : `
                <div class="empty-state">В этом классе пока нет учеников</div>
                `}
            </div>
        `;
    };

    window.openCreateClassModal = async function () {
        const classModalTitle = document.getElementById('classModalTitle');
        const classEditId = document.getElementById('classEditId');
        const classForm = document.getElementById('classForm');
        const classActiveGroup = document.getElementById('classActiveGroup');
        const classSchoolSelect = document.getElementById('classSchoolSelect');

        if (classModalTitle) {
            classModalTitle.innerHTML = '<i class="fas fa-layer-group"></i> Новый класс';
        }

        if (classEditId) {
            classEditId.value = '';
        }

        if (classForm) {
            classForm.reset();
        }

        if (classActiveGroup) {
            classActiveGroup.style.display = 'none';
        }

        if (typeof fillSchoolSelect === 'function') {
            fillSchoolSelect('classSchoolSelect', AdminState.schools, 'Выберите школу');
        }

        const schoolId = AdminState.selectedSchoolId ? Number(AdminState.selectedSchoolId) : null;
        if (classSchoolSelect && schoolId) {
            classSchoolSelect.value = String(schoolId);
        }

        await prepareHomeroomTeachersForSchool(schoolId);

        const modal = document.getElementById('classModal');
        if (modal) {
            modal.style.display = 'block';
        }
    };

    window.openEditClassModal = async function (classId) {
        const item = AdminState.classes.find(c => c.id === classId);
        if (!item) {
            if (typeof showStructureError === 'function') {
                showStructureError('Класс не найден');
            }
            return;
        }

        document.getElementById('classModalTitle').innerHTML = '<i class="fas fa-edit"></i> Редактирование класса';
        document.getElementById('classEditId').value = item.id;
        document.getElementById('classNameInput').value = item.name || '';
        document.getElementById('classAcademicYearInput').value = item.academicYear || '';
        document.getElementById('classActiveGroup').style.display = 'block';
        document.getElementById('classActiveSelect').value = String(item.active !== false);

        if (typeof fillSchoolSelect === 'function') {
            fillSchoolSelect('classSchoolSelect', AdminState.schools, 'Выберите школу');
        }

        const schoolId = item.schoolId || AdminState.selectedSchoolId || '';
        document.getElementById('classSchoolSelect').value = String(schoolId);

        await prepareHomeroomTeachersForSchool(
            schoolId ? Number(schoolId) : null,
            item.homeroomTeacherId ? String(item.homeroomTeacherId) : ''
        );

        document.getElementById('classModal').style.display = 'block';
    };

    window.handleSaveClass = async function (e) {
        e.preventDefault();

        const editId = document.getElementById('classEditId').value;
        const schoolId = document.getElementById('classSchoolSelect').value;
        const name = document.getElementById('classNameInput').value.trim();
        const academicYear = document.getElementById('classAcademicYearInput').value.trim();
        const homeroomTeacherValue = document.getElementById('classHomeroomTeacherSelect').value;
        const homeroomTeacherId = homeroomTeacherValue ? Number(homeroomTeacherValue) : null;

        if (!schoolId || !name || !academicYear) {
            if (typeof showStructureError === 'function') {
                showStructureError('Заполните школу, название класса и учебный год');
            }
            return;
        }

        try {
            if (editId) {
                await ApiService.put(`/admin/structure/classes/${editId}`, {
                    name,
                    academicYear,
                    active: document.getElementById('classActiveSelect').value === 'true',
                    homeroomTeacherId
                });

                if (typeof showStructureSuccess === 'function') {
                    showStructureSuccess('Класс успешно обновлён');
                }
            } else {
                await ApiService.post('/admin/structure/classes', {
                    schoolId: Number(schoolId),
                    name,
                    academicYear,
                    homeroomTeacherId
                });

                if (typeof showStructureSuccess === 'function') {
                    showStructureSuccess('Класс успешно создан');
                }
            }

            if (typeof closeClassModal === 'function') {
                closeClassModal();
            }

            await refreshStructureData();
        } catch (e2) {
            console.error('handleSaveClass (homeroom) error:', e2);
            if (typeof showStructureError === 'function') {
                showStructureError('Ошибка сохранения класса: ' + (e2.message || e2));
            }
        }
    };

    document.addEventListener('DOMContentLoaded', async function () {
        ensureState();
        rebindClassForm();

        const classSchoolSelect = document.getElementById('classSchoolSelect');
        if (classSchoolSelect) {
            classSchoolSelect.addEventListener('change', async function () {
                const schoolId = this.value ? Number(this.value) : null;
                await prepareHomeroomTeachersForSchool(schoolId);
            });
        }

        if (AdminState.selectedSchoolId) {
            try {
                await fetchTeachersBySchool(AdminState.selectedSchoolId);
            } catch (e) {
                console.error('Initial teachers load error:', e);
            }
        }

        await refreshStructureData();
    });
})();