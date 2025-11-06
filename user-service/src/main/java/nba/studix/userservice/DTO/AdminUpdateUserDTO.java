package nba.studix.userservice.DTO;

import nba.studix.userservice.Entity.ProfileVisibility;
import nba.studix.userservice.Entity.UserStatus;

public class AdminUpdateUserDTO {
    private String email;
    private String firstName;
    private String lastName;
    private String patronymic;
    private UserStatus status;
    private ProfileVisibility profileVisibility;
    private String bio;
    private Long schoolId;
    private String password; // Админ может сбросить пароль

    // Конструкторы
    public AdminUpdateUserDTO() {}

    // Геттеры и сеттеры
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getPatronymic() { return patronymic; }
    public void setPatronymic(String patronymic) { this.patronymic = patronymic; }

    public UserStatus getStatus() { return status; }
    public void setStatus(UserStatus status) { this.status = status; }

    public ProfileVisibility getProfileVisibility() { return profileVisibility; }
    public void setProfileVisibility(ProfileVisibility profileVisibility) { this.profileVisibility = profileVisibility; }

    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }

    public Long getSchoolId() { return schoolId; }
    public void setSchoolId(Long schoolId) { this.schoolId = schoolId; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}