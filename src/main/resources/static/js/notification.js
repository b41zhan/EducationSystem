
class NotificationManager {
    constructor() {
        this.unreadCount = 0;
        this.isDropdownOpen = false;
        this.pollingInterval = null;

        // Определяем роль пользователя
        this.userRole = localStorage.getItem('userRole');

        this.init();
    }

    init() {
        this.cacheElements();
        this.bindEvents();
        this.updateNotificationBadge();
        this.startPolling();
    }

    cacheElements() {
        this.bellBtn = document.getElementById('notification-bell-btn');
        this.badge = document.getElementById('notification-badge');
        this.dropdown = document.getElementById('notification-dropdown');
        this.listContainer = document.getElementById('notification-list');
        this.markAllReadBtn = document.getElementById('mark-all-read-btn');
    }

    bindEvents() {
        if (this.bellBtn) {
            this.bellBtn.addEventListener('click', (e) => {
                e.stopPropagation();
                this.toggleDropdown();
            });
        }

        if (this.markAllReadBtn) {
            this.markAllReadBtn.addEventListener('click', (e) => {
                e.stopPropagation();
                this.markAllAsRead();
            });
        }

        // Закрываем дропдаун при клике вне его
        document.addEventListener('click', (e) => {
            if (this.isDropdownOpen && !this.bellBtn.contains(e.target) && !this.dropdown.contains(e.target)) {
                this.closeDropdown();
            }
        });
    }

    async toggleDropdown() {
        if (this.isDropdownOpen) {
            this.closeDropdown();
        } else {
            await this.openDropdown();
        }
    }

    async openDropdown() {
        this.dropdown.style.display = 'flex';
        this.isDropdownOpen = true;
        await this.loadNotifications();
    }

    closeDropdown() {
        this.dropdown.style.display = 'none';
        this.isDropdownOpen = false;
    }

    async loadNotifications() {
        try {
            this.listContainer.innerHTML = '<div class="notification-loading">Загрузка уведомлений...</div>';
            const notifications = await ApiService.get('/notifications');
            this.renderNotifications(notifications);
        } catch (error) {
            console.error('Error loading notifications:', error);
            this.listContainer.innerHTML = '<div class="notification-empty">Ошибка загрузки уведомлений</div>';
        }
    }

    renderNotifications(notifications) {
        if (!notifications || notifications.length === 0) {
            this.listContainer.innerHTML = '<div class="notification-empty">У вас нет уведомлений</div>';
            return;
        }

        let html = '';
        notifications.forEach(notification => {
            const time = this.formatTime(notification.createdAt);
            const unreadClass = notification.read ? '' : 'unread';
            const icon = this.getNotificationIcon(notification.type);

            // Определяем, активно ли еще уведомление (для submission_graded)
            let isActive = true;
            let customMessage = notification.message;

            if (notification.type === 'submission_graded' && notification.relatedEntityStatus === 'graded') {
                isActive = false;
                // Пытаемся извлечь название работы из сообщения
                const match = notification.message.match(/"([^"]*)"/);
                if (match && match[1]) {
                    customMessage = `Работа "${match[1]}" проверена`;
                } else {
                    customMessage = 'Работа проверена';
                }
            }

            html += `
                <div class="notification-item ${unreadClass}" data-id="${notification.id}">
                    <div class="notification-icon">${icon}</div>
                    <div class="notification-content">
                        <div class="notification-message">${this.escapeHtml(customMessage)}</div>
                        <div class="notification-time">${time}</div>
                        <div class="notification-actions">
                            ${isActive ? this.getActionButton(notification) : ''}
                        </div>
                    </div>
                    <button class="notification-delete-btn" data-id="${notification.id}" title="Скрыть">✕</button>
                </div>
            `;
        });

        this.listContainer.innerHTML = html;

        // Добавляем обработчики событий для элементов списка
        this.listContainer.querySelectorAll('.notification-item').forEach(item => {
            const id = item.dataset.id;
            const notification = notifications.find(n => n.id == id);

            // Клик по элементу (кроме кнопок) - отметить как прочитанное
            item.addEventListener('click', (e) => {
                if (!e.target.closest('button') && !e.target.closest('.notification-action-btn')) {
                    this.handleNotificationClick(notification);
                }
            });

            // Кнопка "Скрыть"
            const deleteBtn = item.querySelector('.notification-delete-btn');
            if (deleteBtn) {
                deleteBtn.addEventListener('click', (e) => {
                    e.stopPropagation();
                    this.hideNotification(id);
                });
            }

            // Кнопки действий
            const actionBtn = item.querySelector('.notification-action-btn');
            if (actionBtn) {
                actionBtn.addEventListener('click', (e) => {
                    e.stopPropagation();
                    this.handleActionClick(notification);
                });
            }
        });
    }

    async handleNotificationClick(notification) {
        if (!notification.read) {
            await this.markAsRead(notification.id);
            // Обновляем UI элемента, чтобы он стал "прочитанным"
            const item = this.listContainer.querySelector(`.notification-item[data-id="${notification.id}"]`);
            if (item) {
                item.classList.remove('unread');
            }
            // Обновляем счетчик
            this.updateNotificationBadge();
        }

        // Навигация в зависимости от типа уведомления
        if (notification.relatedId) {
            this.navigateToRelatedContent(notification);
        }
    }

    handleActionClick(notification) {
        this.navigateToRelatedContent(notification);
    }

    navigateToRelatedContent(notification) {
        // Определяем переход в зависимости от роли и типа уведомления
        if (this.userRole === 'teacher') {
            switch (notification.type) {
                case 'submission_graded': // Студент сдал работу (для учителя это "submission_graded", но по сути - новая сдача)
                    // Открываем страницу учителя и пытаемся открыть модалку с оценкой
                    window.location.href = `/teacher-dashboard.html?submissionId=${notification.relatedId}`;
                    break;
                case 'quiz_submitted':
                    // Переход к проверке квиза
                    window.location.href = `/quiz-results.html`;
                    break;
                default:
                    window.location.href = '/teacher-dashboard.html';
                    break;
            }
        } else {
            // Логика для студента
            switch (notification.type) {
                case 'new_assignment':
                case 'grade': // assignment_graded
                    window.location.href = '/student-dashboard.html';
                    break;
                case 'quiz_graded':
                    window.location.href = `/quiz-result.html?attemptId=${notification.relatedId}`;
                    break;
                case 'friend_request':
                case 'friend_request_accepted':
                    window.location.href = '/profile.html';
                    break;
                default:
                    break;
            }
        }
    }

    async hideNotification(notificationId) {
        try {
            await ApiService.post(`/notifications/${notificationId}/hide`);
            const item = this.listContainer.querySelector(`.notification-item[data-id="${notificationId}"]`);
            if (item) item.remove();
            if (this.listContainer.children.length === 0) {
                this.listContainer.innerHTML = '<div class="notification-empty">У вас нет уведомлений</div>';
            }
            this.updateNotificationBadge();
        } catch (error) {
            console.error('Error hiding notification:', error);
        }
    }

    async markAsRead(notificationId) {
        try {
            await ApiService.post(`/notifications/${notificationId}/mark-read`);
        } catch (error) {
            console.error('Error marking notification as read:', error);
        }
    }

    async markAllAsRead() {
        try {
            await ApiService.post('/notifications/mark-all-read');
            this.listContainer.querySelectorAll('.notification-item.unread').forEach(item => {
                item.classList.remove('unread');
            });
            this.updateNotificationBadge();
        } catch (error) {
            console.error('Error marking all as read:', error);
        }
    }

    async updateNotificationBadge() {
        try {
            const response = await ApiService.get('/notifications/unread-count');
            this.unreadCount = response.count;

            if (this.badge) {
                if (this.unreadCount > 0) {
                    this.badge.textContent = this.unreadCount > 99 ? '99+' : this.unreadCount;
                    this.badge.style.display = 'inline-block';
                } else {
                    this.badge.style.display = 'none';
                }
            }
        } catch (error) {
            console.error('Error updating notification badge:', error);
        }
    }

    // ========== Вспомогательные методы ==========
    getNotificationIcon(type) {
        const icons = {
            'grade': '📊',
            'new_assignment': '📝',
            'friend_request': '👋',
            'friend_request_accepted': '✅',
            'friend_request_rejected': '❌',
            'comment': '💬',
            'quiz_graded': '🧠',
            'submission_graded': '📋', // Для учителя это новая сдача, для студента - оценка за сдачу
            'achievement': '🏆'
        };
        return icons[type] || '📢';
    }

    getActionButton(notification) {
        // Логика кнопок в зависимости от роли и типа
        if (this.userRole === 'teacher') {
            if (notification.type === 'submission_graded') {
                // Проверяем статус. Если relatedEntityStatus === 'graded', то кнопка не нужна.
                if (notification.relatedEntityStatus === 'graded') {
                    return ''; // Кнопка не показывается
                }
                return `<button class="notification-action-btn">Проверить работу</button>`;
            }
            if (notification.type === 'quiz_submitted') {
                return `<button class="notification-action-btn">Проверить квиз</button>`;
            }
        } else {
            // Логика для студента
            if (notification.type === 'grade') {
                return `<button class="notification-action-btn">Посмотреть оценку</button>`;
            }
            if (notification.type === 'quiz_graded') {
                return `<button class="notification-action-btn">Посмотреть результат</button>`;
            }
        }
        return '';
    }

    formatTime(dateString) {
        const date = new Date(dateString);
        const now = new Date();
        const diff = now - date;
        if (diff < 3600000) return `${Math.floor(diff / 60000)} мин. назад`;
        if (diff < 86400000) return `${Math.floor(diff / 3600000)} ч. назад`;
        return date.toLocaleDateString('ru-RU');
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

    // ========== Поллинг ==========
    // ========== Поллинг ==========
    startPolling() {
        console.log('🔔 Notification polling started');
        this.pollingInterval = setInterval(() => {
            console.log('🔄 Polling cycle running...');
            this.updateNotificationBadge();
            if (this.isDropdownOpen) {
                this.loadNotifications();
            }

            // Проверяем, существует ли глобальная функция
            // if (typeof window.updateChatBadge === 'function') {
            //     console.log('💬 Calling updateChatBadge');
            //     window.updateChatBadge();
            // } else {
            //     console.error('❌ window.updateChatBadge is not defined!');
            // }
        }, 10000);
    }

    stopPolling() {
        if (this.pollingInterval) {
            clearInterval(this.pollingInterval);
        }
    }
}

// Инициализация
let notificationManager;
document.addEventListener('DOMContentLoaded', () => {
    notificationManager = new NotificationManager();
});
