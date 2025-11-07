class FriendsManager {
    constructor() {
        this.currentUserId = localStorage.getItem('userId');
        this.init();
    }

    init() {
        console.log('FriendsManager initialized for user:', this.currentUserId);
        this.loadFriendsStats();
        this.setupEventListeners();

        // Автоматически загружаем друзей при инициализации
        this.loadFriendsList();
    }

    async loadFriendsStats() {
        try {
            const stats = await ApiService.get('/friends/stats');
            this.displayFriendsStats(stats);
        } catch (error) {
            console.error('Error loading friends stats:', error);
            this.displayFriendsStats({ friendsCount: 0, pendingRequestsCount: 0 });
        }
    }

    displayFriendsStats(stats) {
        const friendsCount = document.getElementById('friends-count');
        const pendingCount = document.getElementById('pending-count');

        if (friendsCount) friendsCount.textContent = stats.friendsCount || 0;
        if (pendingCount) pendingCount.textContent = stats.pendingRequestsCount || 0;
    }

    setupEventListeners() {
        const searchInput = document.getElementById('friend-search');
        if (searchInput) {
            searchInput.addEventListener('input', this.debounce(this.searchUsers.bind(this), 300));
        }
    }

    async searchUsers(event) {
        const query = event.target.value.trim();
        const resultsContainer = document.getElementById('search-results');

        if (query.length < 2) {
            resultsContainer.innerHTML = '';
            resultsContainer.style.display = 'none';
            return;
        }

        try {
            const users = await ApiService.get(`/friends/search?query=${encodeURIComponent(query)}`);
            this.displaySearchResults(users);
        } catch (error) {
            console.error('Error searching users:', error);
            this.showMessage('Ошибка поиска: ' + error.message, 'error');
        }
    }

    displaySearchResults(users) {
        const resultsContainer = document.getElementById('search-results');

        if (!users || users.length === 0) {
            resultsContainer.innerHTML = '<div class="no-results">Пользователи не найдены</div>';
            resultsContainer.style.display = 'block';
            return;
        }

        let html = '';
        users.forEach(user => {
            const fullName = `${user.firstName} ${user.lastName}`;
            const avatarUrl = user.profilePhotoPath ? `/uploads/${user.profilePhotoPath}` : '';
            const initials = (user.firstName?.[0] || '') + (user.lastName?.[0] || '');

            html += `
                <div class="user-result-item" data-user-id="${user.id}">
                    <div class="user-avatar">
                        ${avatarUrl ?
                `<img src="${avatarUrl}" alt="${fullName}" onerror="this.style.display='none'; this.nextElementSibling.style.display='flex';"> 
                             <div class="avatar-placeholder-small" style="display: none;">${initials}</div>` :
                `<div class="avatar-placeholder-small">${initials}</div>`
            }
                    </div>
                    <div class="user-info">
                        <div class="user-name">${this.escapeHtml(fullName)}</div>
                        <div class="user-email">${this.escapeHtml(user.email)}</div>
                    </div>
                    <div class="friendship-status">
                        ${this.getFriendshipButton(user)}
                    </div>
                </div>
            `;
        });

        resultsContainer.innerHTML = html;
        resultsContainer.style.display = 'block';

        // Добавляем обработчики для кнопок
        this.attachSearchResultHandlers();
    }

    getFriendshipButton(user) {
        const status = user.friendshipStatus || 'NONE';

        switch(status) {
            case 'NONE':
                return `<button class="btn-primary btn-small" onclick="friendsManager.sendFriendRequest(${user.id})">Добавить в друзья</button>`;
            case 'PENDING':
                return `<span class="status-pending">Запрос отправлен</span>`;
            case 'ACCEPTED':
                return `<span class="status-accepted">Друг</span>`;
            case 'REJECTED':
                return `<button class="btn-primary btn-small" onclick="friendsManager.sendFriendRequest(${user.id})">Добавить в друзья</button>`;
            case 'SELF':
                return `<span class="status-self">Это вы</span>`;
            default:
                return `<button class="btn-primary btn-small" onclick="friendsManager.sendFriendRequest(${user.id})">Добавить в друзья</button>`;
        }
    }

    attachSearchResultHandlers() {
        // Обработчики уже добавлены через onclick в HTML
    }

    async sendFriendRequest(userId) {
        const button = event?.target;
        if (button) {
            button.disabled = true;
            button.textContent = 'Отправка...';
        }

        try {
            await ApiService.post(`/friends/request/${userId}`);

            if (button) {
                button.textContent = 'Запрос отправлен';
                button.className = 'btn-secondary btn-small';
                button.disabled = true;
            }

            this.showMessage('Запрос на дружбу отправлен!', 'success');
            this.loadFriendsStats();

            // Обновляем результаты поиска
            const searchInput = document.getElementById('friend-search');
            if (searchInput && searchInput.value.trim().length >= 2) {
                this.searchUsers({ target: searchInput });
            }

        } catch (error) {
            console.error('Error sending friend request:', error);
            if (button) {
                button.disabled = false;
                button.textContent = 'Добавить в друзья';
            }
            this.showMessage('Ошибка отправки запроса: ' + error.message, 'error');
        }
    }

    async loadFriendsList() {
        try {
            const friends = await ApiService.get('/friends/my');
            this.displayFriendsList(friends);

            // Показываем список друзей и скрываем запросы
            document.getElementById('friends-list').style.display = 'block';
            document.getElementById('pending-requests').style.display = 'none';

        } catch (error) {
            console.error('Error loading friends list:', error);
            this.showMessage('Ошибка загрузки списка друзей: ' + error.message, 'error');
        }
    }

    displayFriendsList(friends) {
        const container = document.getElementById('friends-list');

        if (!friends || friends.length === 0) {
            container.innerHTML = '<div class="no-friends">У вас пока нет друзей</div>';
            return;
        }

        let html = '';
        friends.forEach(friend => {
            const fullName = `${friend.firstName} ${friend.lastName}`;
            const avatarUrl = friend.profilePhotoPath ? `/uploads/${friend.profilePhotoPath}` : '';
            const initials = (friend.firstName?.[0] || '') + (friend.lastName?.[0] || '');

            html += `
                <div class="friend-item">
                    <div class="friend-avatar">
                        ${avatarUrl ?
                `<img src="${avatarUrl}" alt="${fullName}" onerror="this.style.display='none'; this.nextElementSibling.style.display='flex';"> 
                             <div class="avatar-placeholder-small" style="display: none;">${initials}</div>` :
                `<div class="avatar-placeholder-small">${initials}</div>`
            }
                    </div>
                    <div class="friend-info">
                        <div class="friend-name">${this.escapeHtml(fullName)}</div>
                        <div class="friend-email">${this.escapeHtml(friend.email)}</div>
                    </div>
                    <button class="btn-secondary btn-small" onclick="friendsManager.removeFriend(${friend.id})">
                        Удалить
                    </button>
                </div>
            `;
        });

        container.innerHTML = html;
    }

    async loadPendingRequests() {
        try {
            const requests = await ApiService.get('/friends/pending');
            this.displayPendingRequests(requests);

            // Показываем запросы и скрываем список друзей
            document.getElementById('pending-requests').style.display = 'block';
            document.getElementById('friends-list').style.display = 'none';

        } catch (error) {
            console.error('Error loading pending requests:', error);
            this.showMessage('Ошибка загрузки запросов: ' + error.message, 'error');
        }
    }

    displayPendingRequests(requests) {
        const container = document.getElementById('pending-requests');

        if (!requests || requests.length === 0) {
            container.innerHTML = '<div class="no-results">Нет входящих запросов в друзья</div>';
            return;
        }

        let html = '';
        requests.forEach(request => {
            const fullName = `${request.requesterName}`;
            const email = request.requesterEmail;
            const initials = fullName.split(' ').map(n => n[0]).join('').toUpperCase();

            html += `
                <div class="pending-item">
                    <div class="pending-avatar">
                        <div class="avatar-placeholder-small">${initials}</div>
                    </div>
                    <div class="pending-info">
                        <div class="pending-name">${this.escapeHtml(fullName)}</div>
                        <div class="pending-email">${this.escapeHtml(email)}</div>
                    </div>
                    <div class="pending-actions">
                        <button class="btn-primary btn-tiny" onclick="friendsManager.acceptFriendRequest(${request.id})">
                            Принять
                        </button>
                        <button class="btn-secondary btn-tiny" onclick="friendsManager.rejectFriendRequest(${request.id})">
                            Отклонить
                        </button>
                    </div>
                </div>
            `;
        });

        container.innerHTML = html;
    }

    async acceptFriendRequest(friendshipId) {
        try {
            await ApiService.post(`/friends/accept/${friendshipId}`);
            this.showMessage('Запрос на дружбу принят!', 'success');
            this.loadPendingRequests();
            this.loadFriendsStats();
        } catch (error) {
            console.error('Error accepting friend request:', error);
            this.showMessage('Ошибка принятия запроса: ' + error.message, 'error');
        }
    }

    async rejectFriendRequest(friendshipId) {
        try {
            await ApiService.post(`/friends/reject/${friendshipId}`);
            this.showMessage('Запрос на дружбу отклонен', 'info');
            this.loadPendingRequests();
            this.loadFriendsStats();
        } catch (error) {
            console.error('Error rejecting friend request:', error);
            this.showMessage('Ошибка отклонения запроса: ' + error.message, 'error');
        }
    }

    async removeFriend(friendId) {
        if (!confirm('Удалить пользователя из друзей?')) {
            return;
        }

        try {
            await ApiService.delete(`/friends/remove/${friendId}`);
            this.showMessage('Пользователь удален из друзей', 'success');
            this.loadFriendsList();
            this.loadFriendsStats();
        } catch (error) {
            console.error('Error removing friend:', error);
            this.showMessage('Ошибка удаления друга: ' + error.message, 'error');
        }
    }

    showMessage(message, type) {
        // Используем существующую систему сообщений или создаем простую
        const messageDiv = document.createElement('div');
        messageDiv.className = type === 'error' ? 'error-message' : 'success-message';
        messageDiv.textContent = message;
        messageDiv.style.marginTop = '10px';
        messageDiv.style.marginBottom = '10px';

        const container = document.getElementById('message-container') || document.body;
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
}

// Автоматическая инициализация при загрузке страницы
document.addEventListener('DOMContentLoaded', function() {
    // Ждем немного чтобы все скрипты загрузились
    setTimeout(() => {
        if (typeof ApiService !== 'undefined') {
            window.friendsManager = new FriendsManager();
            console.log('FriendsManager successfully initialized');
        } else {
            console.error('ApiService not found - FriendsManager cannot initialize');
        }
    }, 100);
});