class NotificationManager {
    constructor() {
        this.notificationContainer = null;
        this.pollingInterval = null;
        this.isPolling = false;
        this.init();
    }

    init() {
        this.createNotificationContainer();
        this.startPolling();
        this.updateNotificationBadge();
    }

    createNotificationContainer() {
        this.notificationContainer = document.createElement('div');
        this.notificationContainer.id = 'notification-container';
        this.notificationContainer.style.cssText = `
            position: fixed;
            top: 20px;
            right: 20px;
            z-index: 10000;
            max-width: 350px;
        `;
        document.body.appendChild(this.notificationContainer);
    }

    startPolling() {
        if (this.isPolling) return;

        this.isPolling = true;
        this.checkNewNotifications();

        this.pollingInterval = setInterval(() => {
            this.checkNewNotifications();
        }, 10000);
    }

    stopPolling() {
        this.isPolling = false;
        if (this.pollingInterval) {
            clearInterval(this.pollingInterval);
        }
    }

    async updateNotificationBadge() {
        try {
            const response = await ApiService.get('/notifications/unread-count');
            const count = response.count;

            const badge = document.getElementById('notification-badge');
            if (badge) {
                if (count > 0) {
                    badge.textContent = count > 99 ? '99+' : count;
                    badge.style.display = 'inline-block';
                } else {
                    badge.style.display = 'none';
                }
            }
        } catch (error) {
            console.error('Error updating notification badge:', error);
        }
    }

    async checkNewNotifications() {
        try {
            const unreadNotifications = await ApiService.get('/notifications/unread');

            const twoMinutesAgo = new Date(Date.now() - 2 * 60 * 1000);
            const newNotifications = unreadNotifications.filter(notification =>
                new Date(notification.createdAt) > twoMinutesAgo
            );

            newNotifications.forEach(notification => {
                this.showNotification(notification);
                this.markAsRead(notification.id);
            });

            this.updateNotificationBadge();

        } catch (error) {
            console.error('Error checking notifications:', error);
        }
    }

    showNotification(notification) {
        const notificationElement = document.createElement('div');
        notificationElement.className = 'notification';
        notificationElement.style.cssText = `
            background: white;
            padding: 15px;
            margin-bottom: 10px;
            border-radius: 8px;
            box-shadow: 0 4px 12px rgba(0,0,0,0.1);
            animation: slideIn 0.3s ease-out;
            position: relative;
            max-width: 350px;
            border-left: 4px solid ${this.getNotificationColor(notification.type)};
        `;

        const icon = this.getNotificationIcon(notification.type);
        const title = this.getNotificationTitle(notification.type);

        notificationElement.innerHTML = `
            <button class="notification-close" 
                    onclick="this.parentElement.remove()"
                    style="position: absolute; top: 5px; right: 5px; background: none; border: none; font-size: 16px; cursor: pointer; color: #666;">
                ×
            </button>
            <div style="font-weight: bold; margin-bottom: 5px;">${icon} ${title}</div>
            <div style="font-size: 14px; color: #333;">${notification.message}</div>
            <div style="font-size: 12px; color: #666; margin-top: 5px;">
                ${new Date(notification.createdAt).toLocaleTimeString('ru-RU')}
            </div>
            ${this.getNotificationActions(notification)}
        `;

        this.notificationContainer.appendChild(notificationElement);

        setTimeout(() => {
            if (notificationElement.parentElement) {
                notificationElement.remove();
            }
        }, 8000);
    }

    getNotificationIcon(type) {
        const icons = {
            'grade': '🎓',
            'new_assignment': '📝',
            'friend_request': '👋',
            'friend_request_accepted': '✅',
            'friend_request_rejected': '❌',
            'comment': '💬'
        };
        return icons[type] || '📢';
    }

    getNotificationColor(type) {
        const colors = {
            'grade': '#28a745',
            'new_assignment': '#007bff',
            'friend_request': '#ffc107',
            'friend_request_accepted': '#28a745',
            'friend_request_rejected': '#dc3545',
            'comment': '#6f42c1'
        };
        return colors[type] || '#667eea';
    }

    getNotificationTitle(type) {
        const titles = {
            'grade': 'Новая оценка',
            'new_assignment': 'Новое задание',
            'friend_request': 'Запрос на дружбу',
            'friend_request_accepted': 'Запрос принят',
            'friend_request_rejected': 'Запрос отклонен',
            'comment': 'Новый комментарий'
        };
        return titles[type] || 'Новое уведомление';
    }

    getNotificationActions(notification) {
        if (notification.type === 'friend_request' && notification.relatedId) {
            return `
                <div style="margin-top: 10px; display: flex; gap: 5px;">
                    <button class="btn-notification-accept" 
                            onclick="notificationManager.handleFriendRequest(${notification.id}, ${notification.relatedId}, 'accept')"
                            style="flex: 1; padding: 5px 10px; background: #28a745; color: white; border: none; border-radius: 4px; cursor: pointer; font-size: 12px;">
                        Принять
                    </button>
                    <button class="btn-notification-reject" 
                            onclick="notificationManager.handleFriendRequest(${notification.id}, ${notification.relatedId}, 'reject')"
                            style="flex: 1; padding: 5px 10px; background: #dc3545; color: white; border: none; border-radius: 4px; cursor: pointer; font-size: 12px;">
                        Отклонить
                    </button>
                </div>
            `;
        }

        if (notification.relatedId && (notification.type === 'friend_request_accepted' || notification.type === 'friend_request_rejected')) {
            return `
                <div style="margin-top: 10px;">
                    <button class="btn-notification-view" 
                            onclick="notificationManager.viewUserProfile(${notification.relatedId})"
                            style="width: 100%; padding: 5px 10px; background: #6c757d; color: white; border: none; border-radius: 4px; cursor: pointer; font-size: 12px;">
                        Посмотреть профиль
                    </button>
                </div>
            `;
        }

        if (notification.relatedId && notification.type === 'new_assignment') {
            return `
                <div style="margin-top: 10px;">
                    <button class="btn-notification-view" 
                            onclick="notificationManager.viewAssignment(${notification.relatedId})"
                            style="width: 100%; padding: 5px 10px; background: #007bff; color: white; border: none; border-radius: 4px; cursor: pointer; font-size: 12px;">
                        Посмотреть задание
                    </button>
                </div>
            `;
        }

        return '';
    }

    async handleFriendRequest(notificationId, requesterId, action) {
        try {
            if (action === 'accept') {
                // Находим friendshipId через pending запросы
                const pendingRequests = await ApiService.get('/friends/pending');
                const friendship = pendingRequests.find(req => req.requesterId === requesterId);

                if (friendship) {
                    await ApiService.post(`/friends/accept/${friendship.id}`);
                    this.showTempMessage('Запрос на дружбу принят!', 'success');
                }
            } else if (action === 'reject') {
                const pendingRequests = await ApiService.get('/friends/pending');
                const friendship = pendingRequests.find(req => req.requesterId === requesterId);

                if (friendship) {
                    await ApiService.post(`/friends/reject/${friendship.id}`);
                    this.showTempMessage('Запрос на дружбу отклонен', 'info');
                }
            }

            // Помечаем уведомление как прочитанное
            await this.markAsRead(notificationId);

            // Обновляем бейдж
            this.updateNotificationBadge();

        } catch (error) {
            console.error('Error handling friend request:', error);
            this.showTempMessage('Ошибка обработки запроса: ' + error.message, 'error');
        }
    }

    viewUserProfile(userId) {
        window.location.href = `/user-profile.html?id=${userId}`;
    }

    viewAssignment(assignmentId) {
        // Редирект на страницу задания
        if (window.location.pathname.includes('student-dashboard')) {
            // Для студента - показать задание
            window.location.href = `/student-dashboard.html#assignment-${assignmentId}`;
        } else if (window.location.pathname.includes('teacher-dashboard')) {
            // Для учителя - показать сдачи
            window.location.href = `/teacher-dashboard.html#submissions-${assignmentId}`;
        }
    }

    showTempMessage(message, type) {
        const messageDiv = document.createElement('div');
        messageDiv.style.cssText = `
            position: fixed;
            top: 50%;
            left: 50%;
            transform: translate(-50%, -50%);
            padding: 15px 20px;
            border-radius: 8px;
            color: white;
            font-weight: bold;
            z-index: 10001;
            background: ${type === 'success' ? '#28a745' : type === 'error' ? '#dc3545' : '#ffc107'};
            box-shadow: 0 4px 12px rgba(0,0,0,0.3);
        `;
        messageDiv.textContent = message;

        document.body.appendChild(messageDiv);

        setTimeout(() => {
            if (messageDiv.parentElement) {
                messageDiv.remove();
            }
        }, 3000);
    }

    async markAsRead(notificationId) {
        try {
            await ApiService.post(`/notifications/${notificationId}/mark-read`);
            this.updateNotificationBadge();
        } catch (error) {
            console.error('Error marking notification as read:', error);
        }
    }

    async manualCheck() {
        await this.checkNewNotifications();
    }

    async markAllAsRead() {
        try {
            await ApiService.post('/notifications/mark-all-read');
            this.updateNotificationBadge();
        } catch (error) {
            console.error('Error marking all notifications as read:', error);
        }
    }

    // Новый метод для проверки запросов в друзья
    async checkFriendRequests() {
        try {
            const pendingRequests = await ApiService.get('/friends/pending');
            return pendingRequests.length;
        } catch (error) {
            console.error('Error checking friend requests:', error);
            return 0;
        }
    }

    // Метод для отправки уведомления о друзьях (для использования из других модулей)
    static showFriendNotification(message, type = 'info') {
        if (notificationManager) {
            const tempNotification = {
                id: Date.now(),
                message: message,
                type: 'friend_request',
                createdAt: new Date().toISOString()
            };
            notificationManager.showNotification(tempNotification);
        }
    }
}

const style = document.createElement('style');
style.textContent = `
    @keyframes slideIn {
        from {
            transform: translateX(100%);
            opacity: 0;
        }
        to {
            transform: translateX(0);
            opacity: 1;
        }
    }
    
    .notification {
        animation: slideIn 0.3s ease-out;
    }

    /* Стили для бейджа уведомлений */
    #notification-badge {
        transition: all 0.3s ease;
    }

    #notification-badge:hover {
        transform: scale(1.1);
    }

    /* Стили для кнопок уведомлений */
    .btn-notification-accept:hover {
        background: #218838 !important;
    }

    .btn-notification-reject:hover {
        background: #c82333 !important;
    }

    .btn-notification-view:hover {
        background: #5a6268 !important;
    }

    /* Адаптивность для мобильных */
    @media (max-width: 768px) {
        #notification-container {
            right: 10px;
            left: 10px;
            max-width: none;
        }
        
        .notification {
            max-width: none;
        }
    }

    /* Анимация исчезновения */
    @keyframes slideOut {
        from {
            transform: translateX(0);
            opacity: 1;
        }
        to {
            transform: translateX(100%);
            opacity: 0;
        }
    }

    .notification.hiding {
        animation: slideOut 0.3s ease-in;
    }
`;
document.head.appendChild(style);

let notificationManager;

document.addEventListener('DOMContentLoaded', function() {
    notificationManager = new NotificationManager();

    // Добавляем обработчик для друзей, если на странице есть менеджер друзей
    if (typeof friendsManager !== 'undefined') {
        // Обновляем статистику друзей при получении уведомлений
        notificationManager.updateNotificationBadge = async function() {
            try {
                const response = await ApiService.get('/notifications/unread-count');
                const count = response.count;

                const badge = document.getElementById('notification-badge');
                if (badge) {
                    if (count > 0) {
                        badge.textContent = count > 99 ? '99+' : count;
                        badge.style.display = 'inline-block';
                    } else {
                        badge.style.display = 'none';
                    }
                }

                // Также обновляем статистику друзей
                if (friendsManager && typeof friendsManager.loadFriendsStats === 'function') {
                    friendsManager.loadFriendsStats();
                }
            } catch (error) {
                console.error('Error updating notification badge:', error);
            }
        };
    }
});

window.NotificationManager = {
    checkNotifications: function() {
        if (notificationManager) {
            notificationManager.manualCheck();
        }
    },
    markAllAsRead: function() {
        if (notificationManager) {
            notificationManager.markAllAsRead();
        }
    },
    getUnreadCount: async function() {
        try {
            const response = await ApiService.get('/notifications/unread-count');
            return response.count;
        } catch (error) {
            console.error('Error getting unread count:', error);
            return 0;
        }
    },
    showFriendNotification: function(message, type) {
        if (notificationManager) {
            notificationManager.showTempMessage(message, type);
        }
    },
    checkFriendRequests: async function() {
        if (notificationManager) {
            return await notificationManager.checkFriendRequests();
        }
        return 0;
    }
};

// Глобальные функции для использования в HTML
window.handleFriendRequestAccept = function(requesterId) {
    if (notificationManager) {
        notificationManager.handleFriendRequest(null, requesterId, 'accept');
    }
};

window.handleFriendRequestReject = function(requesterId) {
    if (notificationManager) {
        notificationManager.handleFriendRequest(null, requesterId, 'reject');
    }
};

window.viewUserProfile = function(userId) {
    if (notificationManager) {
        notificationManager.viewUserProfile(userId);
    }
};

// Добавить в auth.js или notifications.js

// Функция для обновления бейджа непрочитанных сообщений
async function updateChatBadge() {
    try {
        const token = localStorage.getItem('token');
        if (!token) return;

        const response = await ApiService.get('/chat/unread-count');
        const badge = document.getElementById('chat-badge');

        if (badge && response.unreadCount > 0) {
            badge.textContent = response.unreadCount > 99 ? '99+' : response.unreadCount;
            badge.style.display = 'inline-block';
        } else if (badge) {
            badge.style.display = 'none';
        }
    } catch (error) {
        console.error('Error updating chat badge:', error);
        const badge = document.getElementById('chat-badge');
        if (badge) {
            badge.style.display = 'none';
        }
    }
}

// Запуск обновления бейджа
function startChatBadgePolling() {
    updateChatBadge();
    setInterval(updateChatBadge, 30000); // Обновлять каждые 30 секунд
}

// Вызывать при загрузке страницы
document.addEventListener('DOMContentLoaded', function() {
    if (document.getElementById('chat-badge')) {
        startChatBadgePolling();
    }
});