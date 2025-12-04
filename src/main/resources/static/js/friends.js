class FriendsManager {
    constructor() {
        this.currentUserId = localStorage.getItem('userId');
        this.init();
    }

    /* ===============================
       INIT
    =============================== */
    init() {
        console.log('FriendsManager initialized for user:', this.currentUserId);

        this.loadFriendsStats();
        this.setupEventListeners();

        // Автоматическая загрузка друзей
        this.loadFriendsList();
    }

    /* ===============================
       ЗАГРУЗКА СТАТИСТИКИ
    =============================== */
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

    /* ===============================
       ПОИСК ПОЛЬЗОВАТЕЛЕЙ
    =============================== */
    setupEventListeners() {
        const input = document.getElementById('friend-search');

        if (input) {
            input.addEventListener(
                'input',
                this.debounce((e) => this.searchUsers(e), 300)
            );
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
            const initials = this.getInitials(fullName);

            html += `
                <div class="user-result-item" data-user-id="${user.id}">
                    <div class="user-avatar">
                        ${avatarUrl ?
                `<img src="${avatarUrl}" alt="${fullName}" 
                                onerror="this.style.display='none'; this.nextElementSibling.style.display='flex';"> 
                            <div class="avatar-placeholder-small" style="display:none">${initials}</div>`
                :
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
    }

    /* ===============================
       КНОПКИ ДЛЯ ДРУЖБЫ
    =============================== */
    getFriendshipButton(user) {
        const status = user.friendshipStatus || 'NONE';

        switch(status) {
            case 'NONE':
            case 'REJECTED':
                return `<button class="btn-primary btn-small" onclick="friendsManager.sendFriendRequest(${user.id})">Добавить</button>`;
            case 'PENDING':
                return `<span class="status-pending">Ожидание</span>`;
            case 'ACCEPTED':
                return `<span class="status-accepted">Друг</span>`;
            case 'SELF':
                return `<span class="status-self">Это вы</span>`;
            default:
                return `<button class="btn-primary btn-small" onclick="friendsManager.sendFriendRequest(${user.id})">Добавить</button>`;
        }
    }

    /* ===============================
       ОТПРАВКА ЗАПРОСА В ДРУЗЬЯ
    =============================== */
    async sendFriendRequest(userId) {
        const button = event?.target;
        if (button) {
            button.disabled = true;
            button.textContent = '...';
        }

        try {
            await ApiService.post(`/friends/request/${userId}`);

            if (button) {
                button.textContent = 'Отправлено';
                button.className = 'btn-secondary btn-small';
                button.disabled = true;
            }

            this.showMessage('Запрос отправлен', 'success');
            this.loadFriendsStats();

            // обновляем результаты поиска
            const input = document.getElementById('friend-search');
            if (input && input.value.trim().length >= 2) {
                this.searchUsers({ target: input });
            }

        } catch (error) {
            console.error('Error sending request:', error);
            if (button) {
                button.disabled = false;
                button.textContent = 'Добавить';
                button.className = 'btn-primary btn-small';
            }
            this.showMessage('Ошибка: ' + error.message, 'error');
        }
    }

    /* ===============================
       СПИСОК ДРУЗЕЙ
    =============================== */
    async loadFriendsList() {
        try {
            const friends = await ApiService.get('/friends/my');
            this.displayFriendsList(friends);

            document.getElementById('friends-list').style.display = 'block';
            document.getElementById('pending-requests').style.display = 'none';

        } catch (error) {
            console.error('Error loading friends:', error);
            this.showMessage('Ошибка загрузки друзей: ' + error.message, 'error');
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
            const initials = this.getInitials(fullName);

            html += `
                <div class="friend-item">
                    <div class="friend-avatar">
                        ${avatarUrl ?
                `<img src="${avatarUrl}" alt="${fullName}" 
                                onerror="this.style.display='none'; this.nextElementSibling.style.display='flex';">
                            <div class="avatar-placeholder-small" style="display:none">${initials}</div>`
                :
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

    /* ===============================
       ВХОДЯЩИЕ ЗАПРОСЫ
    =============================== */
    async loadPendingRequests() {
        try {
            const requests = await ApiService.get('/friends/pending');
            this.displayPendingRequests(requests);

            document.getElementById('pending-requests').style.display = 'block';
            document.getElementById('friends-list').style.display = 'none';

        } catch (error) {
            console.error('Error loading pending:', error);
            this.showMessage('Ошибка загрузки запросов: ' + error.message, 'error');
        }
    }

    displayPendingRequests(requests) {
        const container = document.getElementById('pending-requests');

        if (!requests || requests.length === 0) {
            container.innerHTML = '<div class="no-results">Нет входящих запросов</div>';
            return;
        }

        let html = '';

        requests.forEach(req => {
            const fullName = req.requesterName;
            const email = req.requesterEmail;
            const initials = this.getInitials(fullName);

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
                        <button class="btn-primary btn-tiny" onclick="friendsManager.acceptFriendRequest(${req.id})">Принять</button>
                        <button class="btn-secondary btn-tiny" onclick="friendsManager.rejectFriendRequest(${req.id})">Отклонить</button>
                    </div>
                </div>
            `;
        });

        container.innerHTML = html;
    }

    async acceptFriendRequest(id) {
        try {
            await ApiService.post(`/friends/accept/${id}`);
            this.showMessage('Добавлен в друзья', 'success');
            this.loadPendingRequests();
            this.loadFriendsStats();
        } catch (error) {
            this.showMessage('Ошибка: ' + error.message, 'error');
        }
    }

    async rejectFriendRequest(id) {
        try {
            await ApiService.post(`/friends/reject/${id}`);
            this.showMessage('Запрос отклонён', 'info');
            this.loadPendingRequests();
            this.loadFriendsStats();
        } catch (error) {
            this.showMessage('Ошибка: ' + error.message, 'error');
        }
    }

    /* ===============================
       УДАЛЕНИЕ ДРУГА
    =============================== */
    async removeFriend(friendId) {
        if (!confirm('Удалить из друзей?')) return;

        try {
            await ApiService.delete(`/friends/remove/${friendId}`);
            this.showMessage('Пользователь удалён', 'success');
            this.loadFriendsList();
            this.loadFriendsStats();
        } catch (error) {
            this.showMessage('Ошибка: ' + error.message, 'error');
        }
    }

    /* ===============================
       HELPERS
    =============================== */
    showMessage(message, type) {
        const container = document.getElementById('message-container');
        const div = document.createElement('div');

        div.className = type === 'error'
            ? 'error-message'
            : 'success-message';

        div.textContent = message;
        div.style.marginTop = '10px';

        container.appendChild(div);

        setTimeout(() => div.remove(), 5000);
    }

    getInitials(fullName) {
        return fullName
            .split(' ')
            .map(n => n[0])
            .join('')
            .toUpperCase();
    }

    escapeHtml(str) {
        if (!str) return '';
        return str
            .replace(/&/g, "&amp;")
            .replace(/</g, "&lt;")
            .replace(/>/g, "&gt;")
            .replace(/"/g, "&quot;")
            .replace(/'/g, "&#039;");
    }

    debounce(func, wait) {
        let timeout;
        return (...args) => {
            clearTimeout(timeout);
            timeout = setTimeout(() => func(...args), wait);
        };
    }
}

/* ===============================
   AUTO INIT
=============================== */
document.addEventListener('DOMContentLoaded', () => {
    setTimeout(() => {
        if (typeof ApiService !== 'undefined') {
            window.friendsManager = new FriendsManager();
        } else {
            console.error('ApiService not found — FriendsManager cannot start');
        }
    }, 100);
});
