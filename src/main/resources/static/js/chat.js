class ChatManager {
    constructor() {
        this.currentUserId = localStorage.getItem('userId');
        this.currentConversation = null;
        this.pollingInterval = null;
        this.init();
    }

    init() {
        this.loadConversations();
        this.setupEventListeners();
        this.startPolling();
    }


    async loadConversations() {
        try {
            const conversations = await ApiService.get('/chat/conversations');
            this.displayConversations(conversations);
        } catch (error) {
            console.error('Error loading conversations:', error);
            this.showMessage('Ошибка загрузки чатов', 'error');
        }
    }

    displayConversations(conversations) {
        const container = document.getElementById('conversations-list');

        if (!conversations || conversations.length === 0) {
            container.innerHTML = '<div class="no-conversations">Нет диалогов</div>';
            return;
        }

        let html = '';
        conversations.forEach(conversation => {
            const initials = this.getInitials(conversation.otherUserName);
            const preview = conversation.lastMessage.length > 50 ?
                conversation.lastMessage.substring(0, 50) + '...' : conversation.lastMessage;
            const time = this.formatTime(conversation.lastMessageTime);

            html += `
                <div class="conversation-item" data-conversation-id="${conversation.conversationId}" 
                     data-user-id="${conversation.otherUserId}">
                    <div class="user-avatar">
                        ${conversation.otherUserAvatar ?
                `<img src="/uploads/${conversation.otherUserAvatar}" alt="${conversation.otherUserName}">` :
                initials}
                    </div>
                    <div class="conversation-info">
                        <div class="conversation-name">${conversation.otherUserName}</div>
                        <div class="conversation-preview">${preview}</div>
                        <div class="conversation-meta">
                            <div class="conversation-time">${time}</div>
                            ${conversation.unreadCount > 0 ?
                `<div class="unread-badge">${conversation.unreadCount}</div>` : ''}
                        </div>
                    </div>
                </div>
            `;
        });

        container.innerHTML = html;

        // Добавляем обработчики клика
        container.querySelectorAll('.conversation-item').forEach(item => {
            item.addEventListener('click', () => {
                const userId = item.getAttribute('data-user-id');
                this.openConversation(userId);
            });
        });
    }

    async openConversation(otherUserId) {
        console.log('Opening conversation with user:', otherUserId); // Отладочная информация

        this.currentConversation = otherUserId;

        // Обновляем активный элемент в списке
        document.querySelectorAll('.conversation-item').forEach(item => {
            item.classList.remove('active');
        });

        const activeItem = document.querySelector(`[data-user-id="${otherUserId}"]`);
        if (activeItem) {
            activeItem.classList.add('active');
        }

        // Показываем окно чата
        document.getElementById('no-chat-selected').style.display = 'none';
        const chatWindow = document.getElementById('chat-window');
        chatWindow.style.display = 'flex';

        // Фокусируемся на поле ввода
        setTimeout(() => {
            document.getElementById('message-input').focus();
        }, 100);

        await this.loadMessages(otherUserId);
        await this.updatePartnerInfo(otherUserId);
    }

    async loadMessages(otherUserId) {
        try {
            const messages = await ApiService.get(`/chat/conversation/${otherUserId}`);
            this.displayMessages(messages);
        } catch (error) {
            console.error('Error loading messages:', error);
            this.showMessage('Ошибка загрузки сообщений', 'error');
        }
    }

    displayMessages(messages) {
        const container = document.getElementById('messages-container');
        const wasAtBottom = this.isScrolledToBottom(container);

        container.innerHTML = '';

        console.log('Messages to display:', messages);

        if (!messages || messages.length === 0) {
            container.innerHTML = `
            <div class="no-messages">
                <div class="no-messages-icon">💬</div>
                <div class="no-messages-text">Нет сообщений</div>
                <div class="no-messages-hint">Напишите первое сообщение!</div>
            </div>
        `;
            return;
        }

        messages.forEach(message => {
            const messageElement = this.createMessageElement(message);
            container.appendChild(messageElement);
        });

        // Прокручиваем вниз только если пользователь уже был внизу
        if (wasAtBottom) {
            container.scrollTop = container.scrollHeight;
        }
    }

// Проверяем, прокручен ли контейнер до конца
    isScrolledToBottom(container) {
        return container.scrollTop + container.clientHeight >= container.scrollHeight - 50;
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

        console.log('Sending message:', content, 'to user:', this.currentConversation); // Отладочная информация

        if (!content) {
            this.showMessage('Введите сообщение', 'error');
            return;
        }

        if (!this.currentConversation) {
            this.showMessage('Выберите пользователя для чата', 'error');
            return;
        }

        const sendBtn = document.getElementById('send-message-btn');
        const originalText = sendBtn.textContent;

        try {
            sendBtn.disabled = true;
            sendBtn.textContent = 'Отправка...';

            const response = await ApiService.post('/chat/send', {
                receiverId: this.currentConversation,
                content: content
            });

            console.log('Message sent successfully:', response); // Отладочная информация

            input.value = '';
            await this.loadMessages(this.currentConversation);
            await this.loadConversations();

            // Прокручиваем к последнему сообщению
            const container = document.getElementById('messages-container');
            container.scrollTop = container.scrollHeight;

            this.showMessage('Сообщение отправлено!', 'success');

        } catch (error) {
            console.error('Error sending message:', error);
            this.showMessage('Ошибка отправки сообщения: ' + error.message, 'error');
        } finally {
            sendBtn.disabled = false;
            sendBtn.textContent = originalText;
        }
    }

    async searchUsers(event) {
        const query = event.target.value.trim();
        const resultsContainer = document.getElementById('search-results');

        if (query.length < 2) {
            resultsContainer.style.display = 'none';
            return;
        }

        try {
            // Используем правильный endpoint
            const users = await ApiService.get(`/friends/search-users?query=${encodeURIComponent(query)}`);
            console.log('Found users:', users); // Для отладки
            this.displaySearchResults(users);
        } catch (error) {
            console.error('Error searching users:', error);
            // Пробуем альтернативный endpoint
            try {
                const users = await ApiService.get(`/api/friends/search?query=${encodeURIComponent(query)}`);
                this.displaySearchResults(users);
            } catch (error2) {
                console.error('Alternative search also failed:', error2);
                this.showMessage('Ошибка поиска пользователей', 'error');
            }
        }
    }

    displaySearchResults(users) {
        const container = document.getElementById('search-results');

        if (!users || users.length === 0) {
            container.innerHTML = '<div class="no-results">Пользователи не найдены</div>';
            container.style.display = 'block';
            return;
        }

        let html = '';
        users.forEach(user => {
            const fullName = `${user.firstName} ${user.lastName}`;
            const initials = this.getInitials(fullName);

            html += `
            <div class="search-result-item" data-user-id="${user.id}">
                <div class="user-avatar-small">
                    ${user.profilePhotoPath ?
                `<img src="/uploads/${user.profilePhotoPath}" alt="${fullName}" 
                              onerror="this.style.display='none'; this.nextElementSibling.style.display='flex';">` :
                ''}
                    <div class="avatar-placeholder-small" ${user.profilePhotoPath ? 'style="display: none;"' : ''}>
                        ${initials}
                    </div>
                </div>
                <div class="user-search-info">
                    <div class="user-name">${this.escapeHtml(fullName)}</div>
                    <div class="user-email">${this.escapeHtml(user.email)}</div>
                </div>
            </div>
        `;
        });

        container.innerHTML = html;
        container.style.display = 'block';

        // Добавляем обработчики
        container.querySelectorAll('.search-result-item').forEach(item => {
            item.addEventListener('click', () => {
                const userId = item.getAttribute('data-user-id');
                this.startNewChat(userId);
                container.style.display = 'none';
                document.getElementById('user-search').value = '';
            });
        });
    }

    startNewChat(userId) {
        this.openConversation(userId);
    }

    async updatePartnerInfo(userId) {
        try {
            // Загружаем информацию о пользователе
            const user = await ApiService.get(`/users/${userId}`);
            const partnerInfo = document.getElementById('chat-partner-info');
            const fullName = `${user.firstName} ${user.lastName}`;
            const initials = this.getInitials(fullName);

            partnerInfo.innerHTML = `
            <div class="user-avatar">
                ${user.profilePhotoPath ?
                `<img src="/uploads/${user.profilePhotoPath}" alt="${fullName}" 
                          onerror="this.style.display='none'; this.nextElementSibling.style.display='flex';">` :
                ''}
                <div class="avatar-placeholder" ${user.profilePhotoPath ? 'style="display: none;"' : ''}>
                    ${initials}
                </div>
            </div>
            <div class="partner-info">
                <div class="partner-name">${this.escapeHtml(fullName)}</div>
                <div class="partner-email">${this.escapeHtml(user.email)}</div>
            </div>
        `;

        } catch (error) {
            console.error('Error loading user info:', error);
            // Fallback если не удалось загрузить информацию
            const partnerInfo = document.getElementById('chat-partner-info');
            partnerInfo.innerHTML = `
            <div class="user-avatar">
                <div class="avatar-placeholder">U</div>
            </div>
            <div class="partner-info">
                <div class="partner-name">Пользователь ${userId}</div>
            </div>
        `;
        }
    }

    startPolling() {
        this.pollingInterval = setInterval(async () => {
            if (this.currentConversation) {
                await this.loadMessages(this.currentConversation);
            }
            await this.loadConversations();
        }, 5000); // Обновление каждые 5 секунд
    }

    stopPolling() {
        if (this.pollingInterval) {
            clearInterval(this.pollingInterval);
        }
    }

    setupEventListeners() {
        // Поиск пользователей
        const searchInput = document.getElementById('user-search');
        if (searchInput) {
            searchInput.addEventListener('input',
                this.debounce(this.searchUsers.bind(this), 300));
        }

        // Новая кнопка чата
        const newChatBtn = document.getElementById('new-chat-btn');
        if (newChatBtn) {
            newChatBtn.addEventListener('click', () => {
                document.getElementById('user-search').focus();
            });
        }

        // Кнопка назад
        const backBtn = document.getElementById('back-btn');
        if (backBtn) {
            backBtn.addEventListener('click', () => {
                this.goBack();
            });
        }

        // Отправка сообщения
        const sendBtn = document.getElementById('send-message-btn');
        if (sendBtn) {
            sendBtn.addEventListener('click', () => {
                this.sendMessage();
            });
        }

        // Enter для отправки сообщения
        const messageInput = document.getElementById('message-input');
        if (messageInput) {
            messageInput.addEventListener('input', (e) => {
                this.autoResizeTextarea(e.target);
                this.updateMessageCounter(e.target.value.length);
            });

            messageInput.addEventListener('keypress', (e) => {
                if (e.key === 'Enter' && !e.shiftKey) {
                    e.preventDefault();
                    this.sendMessage();
                }
            });
        }
    }

    autoResizeTextarea(textarea) {
        textarea.style.height = 'auto';
        textarea.style.height = Math.min(textarea.scrollHeight, 150) + 'px'; // Максимум 150px
    }

    updateMessageCounter(length) {
        let counter = document.getElementById('message-counter');
        if (!counter) {
            counter = document.createElement('div');
            counter.id = 'message-counter';
            counter.className = 'message-counter';
            document.querySelector('.message-input-container').appendChild(counter);
        }

        counter.textContent = `${length}/1000`;

        if (length > 800) {
            counter.style.color = '#ff6b6b';
        } else if (length > 500) {
            counter.style.color = '#ffa94d';
        } else {
            counter.style.color = '#666';
        }
    }

    goBack() {
        // Возвращаемся на предыдущую страницу или на dashboard
        if (document.referrer && document.referrer.includes(window.location.hostname)) {
            window.history.back();
        } else {
            window.location.href = '/student-dashboard.html';
        }
    }

    // Вспомогательные методы
    getInitials(name) {
        return name.split(' ').map(n => n[0]).join('').toUpperCase();
    }

    formatTime(dateString) {
        const date = new Date(dateString);
        const now = new Date();
        const diff = now - date;

        if (diff < 24 * 60 * 60 * 1000) {
            return date.toLocaleTimeString('ru-RU', { hour: '2-digit', minute: '2-digit' });
        } else {
            return date.toLocaleDateString('ru-RU');
        }
    }

    escapeHtml(unsafe) {
        return unsafe
            .replace(/&/g, "&amp;")
            .replace(/</g, "&lt;")
            .replace(/>/g, "&gt;")
            .replace(/"/g, "&quot;")
            .replace(/'/g, "&#039;");
    }

    debounce(func, wait) {
        let timeout;
        return function executedFunction(...args) {
            const later = () => {
                clearTimeout(timeout);
                func(...args);
            };
            clearTimeout(timeout);
            timeout = setTimeout(later, wait);
        };
    }

    showMessage(message, type) {
        // Используем существующую систему сообщений
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

// Инициализация при загрузке страницы
document.addEventListener('DOMContentLoaded', function() {
    window.chatManager = new ChatManager();
});