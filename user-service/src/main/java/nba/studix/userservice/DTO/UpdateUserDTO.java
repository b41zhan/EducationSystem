package nba.studix.userservice.DTO;

public class UpdateUserDTO {
    private String email;
    private String firstName;
    private String lastName;
    private String patronymic;
    private String password;
    private String role;
    private Long schoolId;
    private String bio;

    // Конструкторы
    public UpdateUserDTO() {}

    // Геттеры и сеттеры
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getPatronymic() { return patronymic; }
    public void setPatronymic(String patronymic) { this.patronymic = patronymic; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public Long getSchoolId() { return schoolId; }
    public void setSchoolId(Long schoolId) { this.schoolId = schoolId; }

    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }
}