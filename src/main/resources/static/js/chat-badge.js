
// ========== ОБЛЕГЧЕННАЯ ВЕРСИЯ ДЛЯ ДАШБОРДОВ ==========
// Только функция обновления бейджа чата и поллинг

console.log('💬 Chat Badge Module loaded');

// Глобальная функция для обновления бейджа чата
window.updateChatBadge = async function() {
    console.log('💬 updateChatBadge called');
    try {
        const token = localStorage.getItem('token');
        if (!token) {
            console.log('❌ No token found for chat badge');
            return;
        }

        const response = await fetch('/api/chat/unread-count', {
            headers: { 'Authorization': `Bearer ${token}` }
        });

        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }

        const data = await response.json();
        console.log('📊 Chat unread count response:', data);

        const badge = document.getElementById('chat-badge');

        if (badge) {
            const unreadCount = data.unreadCount || 0;
            console.log('🔴 Unread count:', unreadCount);

            if (unreadCount > 0) {
                badge.textContent = unreadCount > 99 ? '99+' : unreadCount;
                badge.style.display = 'inline-block';
                console.log('✅ Chat badge displayed with count:', unreadCount);
            } else {
                badge.style.display = 'none';
                console.log('✅ Chat badge hidden (no unread messages)');
            }
        } else {
            console.warn('⚠️ Chat badge element not found in DOM');
        }
    } catch (error) {
        console.error('❌ Error updating chat badge:', error);
        const badge = document.getElementById('chat-badge');
        if (badge) {
            badge.style.display = 'none';
        }
    }
};

// Запускаем поллинг для обновления бейджа чата
(function startChatBadgePolling() {
    console.log('🔄 Chat badge polling started');

    // Первый запуск через 1 секунду после загрузки
    setTimeout(() => {
        if (typeof window.updateChatBadge === 'function') {
            window.updateChatBadge();
        }
    }, 1000);

    // Затем каждые 10 секунд
    setInterval(() => {
        if (typeof window.updateChatBadge === 'function') {
            window.updateChatBadge();
        }
    }, 10000);
})();
