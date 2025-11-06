package nba.studix.userservice.Entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;


@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    private String patronymic;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserStatus status = UserStatus.ACTIVE;

    @Enumerated(EnumType.STRING)
    @Column(name = "profile_visibility", nullable = false)
    private ProfileVisibility profileVisibility = ProfileVisibility.PRIVATE;

    @Column(name = "profile_photo_path")
    private String profilePhotoPath;

    private String bio;

    @Column(name = "school_id")
    private Long schoolId;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "last_modified_at")
    private LocalDateTime lastModifiedAt;

    @Column(name = "last_modified_by")
    private Long lastModifiedBy;

    //Конструкторы, геттеры и сеттеры
    public User(Long id, String email, String passwordHash, String firstName, String lastName, String patronymic, UserStatus status, ProfileVisibility profileVisibility, String profilePhotoPath, String bio, Long schoolId, LocalDateTime createdAt, LocalDateTime lastModifiedAt, Long lastModifiedBy) {
        this.id = id;
        this.email = email;
        this.passwordHash = passwordHash;
        this.firstName = firstName;
        this.lastName = lastName;
        this.patronymic = patronymic;
        this.status = status;
        this.profileVisibility = profileVisibility;
        this.profilePhotoPath = profilePhotoPath;
        this.bio = bio;
        this.schoolId = schoolId;
        this.createdAt = createdAt;
        this.lastModifiedAt = lastModifiedAt;
        this.lastModifiedBy = lastModifiedBy;
    }
    public User(String email, String passwordHash, String firstName, String lastName) {
        this();
        this.email = email;
        this.passwordHash = passwordHash;
        this.firstName = firstName;
        this.lastName = lastName;
    }
    public User() {
        this.createdAt = LocalDateTime.now();
        this.lastModifiedAt = LocalDateTime.now();
    }

    public Long getId() {return id;}
    public void setId(Long id) {this.id = id;}
    public LocalDateTime getLastModifiedAt() {return lastModifiedAt;}
    public void setLastModifiedAt(LocalDateTime lastModifiedAt) {this.lastModifiedAt = lastModifiedAt;}
    public LocalDateTime getCreatedAt() {return createdAt;}
    public void setCreatedAt(LocalDateTime createdAt) {this.createdAt = createdAt;}
    public Long getSchoolId() {return schoolId;}
    public void setSchoolId(Long schoolId) {this.schoolId = schoolId;}
    public String getBio() {return bio;}
    public void setBio(String bio) {this.bio = bio;}
    public String getProfilePhotoPath() {return profilePhotoPath;}
    public void setProfilePhotoPath(String profilePhotoPath) {this.profilePhotoPath = profilePhotoPath;}
    public ProfileVisibility getProfileVisibility() {return profileVisibility;}
    public void setProfileVisibility(ProfileVisibility profileVisibility) {this.profileVisibility = profileVisibility;}
    public UserStatus getStatus() {return status;}
    public void setStatus(UserStatus status) {this.status = status;}
    public String getPatronymic() {return patronymic;}
    public void setPatronymic(String patronymic) {this.patronymic = patronymic;}
    public String getLastName() {return lastName;}
    public void setLastName(String lastName) {this.lastName = lastName;}
    public String getFirstName() {return firstName;}
    public void setFirstName(String firstName) {this.firstName = firstName;}
    public String getPasswordHash() {return passwordHash;}
    public void setPasswordHash(String passwordHash) {this.passwordHash = passwordHash;}
    public String getEmail() {return email;}
    public void setEmail(String email) {this.email = email;}
    public Long getLastModifiedBy() {return lastModifiedBy;}
    public void setLastModifiedBy(Long lastModifiedBy) {this.lastModifiedBy = lastModifiedBy;}
}