package nba.studix.wikipediaservice.DTO;

public class SearchRequestDTO {
    private String query;
    private Integer limit = 10;

    // Конструкторы
    public SearchRequestDTO() {}

    public SearchRequestDTO(String query) {
        this.query = query;
    }

    // Геттеры и сеттеры
    public String getQuery() { return query; }
    public void setQuery(String query) { this.query = query; }

    public Integer getLimit() { return limit; }
    public void setLimit(Integer limit) { this.limit = limit; }
}