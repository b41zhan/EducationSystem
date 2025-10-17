document.addEventListener('DOMContentLoaded', function() {
    console.log('🔧 Wikipedia search initializing...');
    initializeWikipediaSearch();
});

function initializeWikipediaSearch() {
    const searchInput = document.getElementById('wikipediaSearch');
    const resultsContainer = document.getElementById('wikipediaResults');

    if (!searchInput) {
        console.error('❌ Wikipedia search input not found');
        return;
    }

    if (!resultsContainer) {
        console.error('❌ Wikipedia results container not found');
        return;
    }

    console.log('✅ Wikipedia search initialized');

    let searchTimeout;

    searchInput.addEventListener('input', function(e) {
        const query = e.target.value.trim();
        console.log('⌨️ Input:', query);

        // Очищаем предыдущий таймаут
        clearTimeout(searchTimeout);

        // Скрываем результаты если запрос пустой
        if (query === '') {
            resultsContainer.style.display = 'none';
            searchInput.classList.remove('loading');
            return;
        }

        // Показываем индикатор загрузки
        searchInput.classList.add('loading');
        console.log('⏳ Starting search for:', query);

        // Устанавливаем новый таймаут для поиска (задержка 500ms)
        searchTimeout = setTimeout(() => {
            performSearch(query);
        }, 500);
    });

    // Скрываем результаты при клике вне области
    document.addEventListener('click', function(e) {
        if (!searchInput.contains(e.target) && !resultsContainer.contains(e.target)) {
            resultsContainer.style.display = 'none';
        }
    });

    // Обработка нажатия Enter
    searchInput.addEventListener('keypress', function(e) {
        if (e.key === 'Enter') {
            const query = e.target.value.trim();
            if (query !== '') {
                console.log('↵ Enter pressed, searching:', query);
                clearTimeout(searchTimeout);
                performSearch(query);
            }
        }
    });
}

async function performSearch(query) {
    const searchInput = document.getElementById('wikipediaSearch');
    const resultsContainer = document.getElementById('wikipediaResults');

    console.log('🚀 Performing search:', query);

    try {
        const response = await fetch(`/api/wikipedia/search?query=${encodeURIComponent(query)}`, {
            headers: {
                'Authorization': `Bearer ${localStorage.getItem('token')}`,
                'Content-Type': 'application/json'
            }
        });

        console.log('📡 Response status:', response.status);

        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }

        const results = await response.json();
        console.log('📊 Search results:', results);

        displayResults(results);

    } catch (error) {
        console.error('💥 Wikipedia search error:', error);
        displayError('Ошибка поиска: ' + error.message);
    } finally {
        searchInput.classList.remove('loading');
    }
}

function displayResults(results) {
    const resultsContainer = document.getElementById('wikipediaResults');

    console.log('🎨 Displaying results:', results);

    if (!results || results.length === 0) {
        resultsContainer.innerHTML = `
            <div class="wikipedia-no-results">
                По вашему запросу ничего не найдено
            </div>
        `;
        resultsContainer.style.display = 'block';
        return;
    }

    let html = '';

    results.forEach(result => {
        // Обрезаем описание если слишком длинное
        let description = result.description;
        if (description && description.length > 200) {
            description = description.substring(0, 200) + '...';
        }

        html += `
            <div class="wikipedia-result-item">
                <div class="wikipedia-result-title">${escapeHtml(result.title)}</div>
                <div class="wikipedia-result-description">${escapeHtml(description)}</div>
                <a href="${result.url}" target="_blank" class="wikipedia-result-link">
                    📖 Открыть полную статью
                </a>
            </div>
        `;
    });

    resultsContainer.innerHTML = html;
    resultsContainer.style.display = 'block';
}

function displayError(message) {
    const resultsContainer = document.getElementById('wikipediaResults');

    resultsContainer.innerHTML = `
        <div class="wikipedia-no-results">
            ${message}
        </div>
    `;
    resultsContainer.style.display = 'block';
}

function escapeHtml(unsafe) {
    if (!unsafe) return '';
    return unsafe
        .replace(/&/g, "&amp;")
        .replace(/</g, "&lt;")
        .replace(/>/g, "&gt;")
        .replace(/"/g, "&quot;")
        .replace(/'/g, "&#039;");
}