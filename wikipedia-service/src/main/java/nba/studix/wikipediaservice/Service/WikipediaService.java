package nba.studix.wikipediaservice.Service;

import nba.studix.wikipediaservice.DTO.WikipediaDTO;
import nba.studix.wikipediaservice.DTO.SearchResponseDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class WikipediaService {
    private static final Logger logger = LoggerFactory.getLogger(WikipediaService.class);

    private final RestTemplate restTemplate;

    // Простой in-memory кэш (опционально)
    private final Map<String, CacheEntry> memoryCache = new ConcurrentHashMap<>();

    @Value("${wikipedia.api.url:https://ru.wikipedia.org/w/api.php}")
    private String wikipediaApiUrl;

    public WikipediaService() {
        this.restTemplate = new RestTemplate();
    }

    public List<WikipediaDTO> search(String query) {
        long startTime = System.currentTimeMillis();
        logger.info("🔍 Searching Wikipedia for: {}", query);

        try {
            String url = buildSearchUrl(query);
            logger.info("📡 API URL: {}", url);

            HttpHeaders headers = new HttpHeaders();
            headers.set("User-Agent", "EducationSystem/1.0 (https://github.com/nba-studix; admin@school.kz)");
            headers.setAccept(List.of(MediaType.APPLICATION_JSON));

            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<Map> response = restTemplate.exchange(
                    url, HttpMethod.GET, entity, Map.class);

            logger.info("📥 Response status: {}", response.getStatusCode());

            Map<String, Object> responseBody = response.getBody();
            List<WikipediaDTO> results = parseSearchResults(responseBody);

            long responseTime = System.currentTimeMillis() - startTime;
            logger.info("✅ Search completed, found {} results, time: {}ms",
                    results.size(), responseTime);

            return results;

        } catch (Exception e) {
            logger.error("💥 Error searching Wikipedia: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    public SearchResponseDTO searchWithMetadata(String query) {
        long startTime = System.currentTimeMillis();

        List<WikipediaDTO> results = search(query);
        long responseTime = System.currentTimeMillis() - startTime;

        return new SearchResponseDTO(query, results, false, responseTime);
    }

    // Остальные методы (buildSearchUrl, parseSearchResults, etc.) остаются без изменений
    private String buildSearchUrl(String query) {
        return UriComponentsBuilder
                .fromHttpUrl(wikipediaApiUrl)
                .queryParam("action", "query")
                .queryParam("format", "json")
                .queryParam("list", "search")
                .queryParam("srsearch", query)
                .queryParam("srlimit", "10")
                .queryParam("srprop", "snippet")
                .queryParam("utf8", "1")
                .build()
                .toUriString();
    }

    @SuppressWarnings("unchecked")
    private List<WikipediaDTO> parseSearchResults(Map<String, Object> responseBody) {
        if (responseBody == null || !responseBody.containsKey("query")) {
            logger.warn("❌ No query in response or response is null");
            return new ArrayList<>();
        }

        try {
            Map<String, Object> queryData = (Map<String, Object>) responseBody.get("query");
            List<Map<String, Object>> searchResults = (List<Map<String, Object>>) queryData.get("search");

            if (searchResults == null) {
                return new ArrayList<>();
            }

            return searchResults.stream()
                    .map(this::convertToWikipediaDTO)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            logger.error("Error parsing search results: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    private WikipediaDTO convertToWikipediaDTO(Map<String, Object> result) {
        try {
            String title = (String) result.get("title");
            String snippet = (String) result.get("snippet");

            // Очищаем HTML теги из сниппета
            String cleanSnippet = cleanHtml(snippet);

            // Формируем URL статьи
            String articleUrl = "https://ru.wikipedia.org/wiki/" + title.replace(" ", "_");

            return new WikipediaDTO(title, cleanSnippet, articleUrl);

        } catch (Exception e) {
            logger.error("Error converting result to DTO: {}", e.getMessage());
            return null;
        }
    }

    private String cleanHtml(String html) {
        if (html == null) return "";
        return html.replaceAll("<[^>]+>", "").trim();
    }

    // Простой in-memory кэш (опционально)
    private static class CacheEntry {
        List<WikipediaDTO> results;
        long timestamp;

        CacheEntry(List<WikipediaDTO> results) {
            this.results = results;
            this.timestamp = System.currentTimeMillis();
        }

        boolean isExpired() {
            return System.currentTimeMillis() - timestamp > 3600000; // 1 час
        }
    }
}