class SingleChatManager {
    constructor() {
        this.currentUserId = localStorage.getItem('userId');
        this.otherUserId = this.getOtherUserIdFromURL();
        this.pollingInterval = null;
        this.init();
    }

    init() {
        this.loadMessages();
        this.setupEventListeners();
        this.startPolling();
        this.updatePartnerInfo();
    }

    getOtherUserIdFromURL() {
        const urlParams = new URLSearchParams(window.location.search);
        return urlParams.get('userId');
    }

    setupEventListeners() {
        document.getElementById('back-btn').addEventListener('click', () => {
            window.history.back();
        });

        document.getElementById('send-message-btn').addEventListener('click',
            this.sendMessage.bind(this));

        document.getElementById('message-input').addEventListener('keypress',
            (e) => {
                if (e.key === 'Enter' && !e.shiftKey) {
                    e.preventDefault();
                    this.sendMessage();
                }
            });
    }

    async loadMessages() {
        try {
            const messages = await ApiService.get(`/chat/conversation/${this.otherUserId}`);
            this.displayMessages(messages);
        } catch (error) {
            console.error('Error loading messages:', error);
            this.showMessage('Ошибка загрузки сообщений', 'error');
        }
    }

    displayMessages(messages) {
        const container = document.getElementById('messages-container');
        container.innerHTML = '';

        if (!messages || messages.length === 0) {
            container.innerHTML = '<div class="no-messages">Нет сообщений</div>';
            return;
        }

        messages.forEach(message => {
            const messageElement = this.createMessageElement(message);
            container.appendChild(messageElement);
        });

        container.scrollTop = container.scrollHeight;
    }

    createMessageElement(message) {
        const messageDiv = document.createElement('div');
        const isSent = message.senderId == this.currentUserId;

        messageDiv.className = `message ${isSent ? 'sent' : 'received'}`;

        const time = new Date(message.createdAt).toLocaleTimeString('ru-RU', {
            hour: '2-digit',
            minute: '2-digit'
        });

        messageDiv.innerHTML = `
            <div class="message-bubble">
                <div class="message-content">${this.escapeHtml(message.content)}</div>
                <div class="message-time">${time}</div>
            </div>
        `;

        return messageDiv;
    }

    async sendMessage() {
        const input = document.getElementById('message-input');
        const content = input.value.trim();

        if (!content) return;

        try {
            await ApiService.post('/chat/send', {
                receiverId: this.otherUserId,
                content: content
            });

            input.value = '';
            await this.loadMessages();

        } catch (error) {
            console.error('Error sending message:', error);
            this.showMessage('Ошибка отправки сообщения', 'error');
        }
    }

    async updatePartnerInfo() {
        try {
            // Загружаем информацию о пользователе
            const user = await ApiService.get(`/users/${this.otherUserId}`);
            const partnerInfo = document.getElementById('chat-partner-info');
            const initials = this.getInitials(`${user.firstName} ${user.lastName}`);

            partnerInfo.innerHTML = `
                <div class="user-avatar">
                    ${user.profilePhotoPath ?
                `<img src="/uploads/${user.profilePhotoPath}" alt="${user.firstName} ${user.lastName}">` :
                initials}
                </div>
                <div class="partner-name">${user.firstName} ${user.lastName}</div>
            `;

            document.getElementById('chat-title').textContent = `Чат с ${user.firstName} ${user.lastName}`;

        } catch (error) {
            console.error('Error loading user info:', error);
        }
    }

    startPolling() {
        this.pollingInterval = setInterval(() => {
            this.loadMessages();
        }, 3000);
    }

    stopPolling() {
        if (this.pollingInterval) {
            clearInterval(this.pollingInterval);
        }
    }

    // Вспомогательные методы
    getInitials(name) {
        return name.split(' ').map(n => n[0]).join('').toUpperCase();
    }

    escapeHtml(unsafe) {
        return unsafe
            .replace(/&/g, "&amp;")
            .replace(/</g, "&lt;")
            .replace(/>/g, "&gt;")
            .replace(/"/g, "&quot;")
            .replace(/'/g, "&#039;");
    }

    showMessage(message, type) {
        const messageDiv = document.createElement('div');
        messageDiv.className = type === 'error' ? 'error-message' : 'success-message';
        messageDiv.textContent = message;
        messageDiv.style.margin = '10px';

        document.body.appendChild(messageDiv);

        setTimeout(() => {
            if (messageDiv.parentNode) {
                messageDiv.parentNode.removeChild(messageDiv);
            }
        }, 5000);
    }
}

// Инициализация
document.addEventListener('DOMContentLoaded', function() {
    window.singleChatManager = new SingleChatManager();
});