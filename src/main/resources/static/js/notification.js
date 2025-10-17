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
            border-left: 4px solid #667eea;
            padding: 15px;
            margin-bottom: 10px;
            border-radius: 8px;
            box-shadow: 0 4px 12px rgba(0,0,0,0.1);
            animation: slideIn 0.3s ease-out;
            position: relative;
            max-width: 350px;
        `;

        let icon = '📚';
        if (notification.type === 'grade') {
            icon = '🎓';
        } else if (notification.type === 'new_assignment') {
            icon = '📝';
        }

        notificationElement.innerHTML = `
            <button class="notification-close" 
                    onclick="this.parentElement.remove()"
                    style="position: absolute; top: 5px; right: 5px; background: none; border: none; font-size: 16px; cursor: pointer; color: #666;">
                ×
            </button>
            <div style="font-weight: bold; margin-bottom: 5px;">${icon} Новое уведомление</div>
            <div style="font-size: 14px; color: #333;">${notification.message}</div>
            <div style="font-size: 12px; color: #666; margin-top: 5px;">
                ${new Date(notification.createdAt).toLocaleTimeString('ru-RU')}
            </div>
        `;

        this.notificationContainer.appendChild(notificationElement);

        setTimeout(() => {
            if (notificationElement.parentElement) {
                notificationElement.remove();
            }
        }, 8000);
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

    /* ДОБАВЛЕНО: Стили для бейджа уведомлений */
    #notification-badge {
        transition: all 0.3s ease;
    }

    #notification-badge:hover {
        transform: scale(1.1);
    }
`;
document.head.appendChild(style);

let notificationManager;

document.addEventListener('DOMContentLoaded', function() {
    notificationManager = new NotificationManager();
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
    }
};