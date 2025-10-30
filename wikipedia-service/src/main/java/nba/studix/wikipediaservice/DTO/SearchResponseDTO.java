package nba.studix.wikipediaservice.DTO;

import java.util.List;

public class SearchResponseDTO {
    private String query;
    private List<WikipediaDTO> results;
    private Integer totalResults;
    private Boolean fromCache;
    private Long responseTime;

    // Конструкторы
    public SearchResponseDTO() {}

    public SearchResponseDTO(String query, List<WikipediaDTO> results, Boolean fromCache, Long responseTime) {
        this.query = query;
        this.results = results;
        this.totalResults = results != null ? results.size() : 0;
        this.fromCache = fromCache;
        this.responseTime = responseTime;
    }

    // Геттеры и сеттеры
    public String getQuery() { return query; }
    public void setQuery(String query) { this.query = query; }

    public List<WikipediaDTO> getResults() { return results; }
    public void setResults(List<WikipediaDTO> results) { this.results = results; }

    public Integer getTotalResults() { return totalResults; }
    public void setTotalResults(Integer totalResults) { this.totalResults = totalResults; }

    public Boolean getFromCache() { return fromCache; }
    public void setFromCache(Boolean fromCache) { this.fromCache = fromCache; }

    public Long getResponseTime() { return responseTime; }
    public void setResponseTime(Long responseTime) { this.responseTime = responseTime; }
}