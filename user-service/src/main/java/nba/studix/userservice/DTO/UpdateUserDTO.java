package nba.studix.userservice.DTO;

import nba.studix.userservice.Entity.ProfileVisibility;

public class UpdateUserDTO {
    private String firstName;
    private String lastName;
    private String patronymic;
    private String bio;
    private ProfileVisibility profileVisibility;

    // Конструкторы
    public UpdateUserDTO() {}

    // Геттеры и сеттеры
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getPatronymic() { return patronymic; }
    public void setPatronymic(String patronymic) { this.patronymic = patronymic; }

    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }

    public ProfileVisibility getProfileVisibility() { return profileVisibility; }
    public void setProfileVisibility(ProfileVisibility profileVisibility) { this.profileVisibility = profileVisibility; }
}