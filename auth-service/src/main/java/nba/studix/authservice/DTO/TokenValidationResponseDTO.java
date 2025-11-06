package nba.studix.authservice.DTO;

public class TokenValidationResponseDTO {
    private boolean valid;
    private String role;
    private Long userId;
    private String error;

    public TokenValidationResponseDTO() {}

    public TokenValidationResponseDTO(boolean valid, String role, Long userId) {
        this.valid = valid;
        this.role = role;
        this.userId = userId;
    }

    public TokenValidationResponseDTO(String error) {
        this.valid = false;
        this.error = error;
    }

    // Геттеры и сеттеры
    public boolean isValid() { return valid; }
    public void setValid(boolean valid) { this.valid = valid; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getError() { return error; }
    public void setError(String error) { this.error = error; }
}