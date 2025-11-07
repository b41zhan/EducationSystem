class TagsManager {
    constructor() {
        this.currentUserId = localStorage.getItem('userId');
        this.userRole = localStorage.getItem('userRole');
        this.selectedTags = new Set();
        this.maxTags = 3;
        this.init();
    }

    init() {
        // Показываем секцию тегов только для студентов
        if (this.userRole === 'student') {
            document.getElementById('tags-section').style.display = 'block';
            this.loadStudentTags();
        }
    }

    async loadStudentTags() {
        try {
            const tags = await ApiService.get('/tags/student/available');
            this.displayTags(tags);
            this.updateSelectedCount();
        } catch (error) {
            console.error('Error loading tags:', error);
            this.showMessage('Ошибка загрузки тегов: ' + error.message, 'error');
        }
    }

    displayTags(tags) {
        const container = document.getElementById('tags-container');

        if (!tags || tags.length === 0) {
            container.innerHTML = '<p>Теги не найдены</p>';
            return;
        }

        let html = '';
        tags.forEach(tag => {
            const isSelected = tag.selected;
            if (isSelected) {
                this.selectedTags.add(tag.id);
            }

            html += `
                <div class="tag-item ${isSelected ? 'selected' : ''} 
                             ${this.selectedTags.size >= this.maxTags && !isSelected ? 'disabled' : ''}" 
                     data-tag-id="${tag.id}"
                     onclick="tagsManager.toggleTag(${tag.id})">
                    <div class="tag-name">${this.escapeHtml(tag.name)}</div>
                    <div class="tag-description">${this.escapeHtml(tag.description)}</div>
                </div>
            `;
        });

        container.innerHTML = html;
    }

    toggleTag(tagId) {
        const tagElement = document.querySelector(`[data-tag-id="${tagId}"]`);

        if (tagElement.classList.contains('disabled')) {
            return;
        }

        if (this.selectedTags.has(tagId)) {
            this.selectedTags.delete(tagId);
            tagElement.classList.remove('selected');
        } else {
            if (this.selectedTags.size < this.maxTags) {
                this.selectedTags.add(tagId);
                tagElement.classList.add('selected');
            }
        }

        this.updateSelectedCount();
        this.updateTagsAvailability();
    }

    updateSelectedCount() {
        const counter = document.getElementById('selected-tags-count');
        if (counter) {
            counter.textContent = this.selectedTags.size;
        }
    }

    updateTagsAvailability() {
        const allTags = document.querySelectorAll('.tag-item:not(.selected)');
        const shouldDisable = this.selectedTags.size >= this.maxTags;

        allTags.forEach(tag => {
            if (shouldDisable) {
                tag.classList.add('disabled');
            } else {
                tag.classList.remove('disabled');
            }
        });
    }

    async saveTags() {
        if (this.selectedTags.size > this.maxTags) {
            this.showMessage(`Можно выбрать не более ${this.maxTags} тегов`, 'error');
            return;
        }

        try {
            await ApiService.put('/tags/student/update', {
                tagIds: Array.from(this.selectedTags)
            });

            this.showMessage('Теги успешно сохранены!', 'success');

            // Перезагружаем теги чтобы обновить состояние
            setTimeout(() => {
                this.loadStudentTags();
            }, 1000);

        } catch (error) {
            console.error('Error saving tags:', error);
            this.showMessage('Ошибка сохранения тегов: ' + error.message, 'error');
        }
    }

    cancelEditing() {
        // Просто перезагружаем теги чтобы сбросить изменения
        this.loadStudentTags();
        this.showMessage('Изменения отменены', 'info');
    }

    showMessage(message, type) {
        const container = document.getElementById('tags-message-container');
        const messageDiv = document.createElement('div');
        messageDiv.className = type === 'error' ? 'error-message' : 'success-message';
        messageDiv.textContent = message;

        container.appendChild(messageDiv);

        setTimeout(() => {
            if (messageDiv.parentElement) {
                messageDiv.remove();
            }
        }, 5000);
    }

    escapeHtml(unsafe) {
        if (!unsafe) return '';
        return unsafe
            .replace(/&/g, "&amp;")
            .replace(/</g, "&lt;")
            .replace(/>/g, "&gt;")
            .replace(/"/g, "&quot;")
            .replace(/'/g, "&#039;");
    }
}

// Автоматическая инициализация
document.addEventListener('DOMContentLoaded', function() {
    setTimeout(() => {
        if (typeof ApiService !== 'undefined') {
            window.tagsManager = new TagsManager();
            console.log('TagsManager successfully initialized');
        } else {
            console.error('ApiService not found - TagsManager cannot initialize');
        }
    }, 100);
});