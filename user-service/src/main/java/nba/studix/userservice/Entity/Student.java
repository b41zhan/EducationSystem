package nba.studix.userservice.Entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "students")
public class Student {
    @Id
    private Long id; // Ссылка на User

    @OneToOne
    @MapsId
    @JoinColumn(name = "id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "class_id")
    private SchoolClass schoolClass;

    @Column(name = "student_id", unique = true)
    private String studentId; // Номер студенческого билета

    @Column(name = "enrollment_year")
    private Integer enrollmentYear; // Год поступления

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // Конструкторы
    public Student() {
        this.createdAt = LocalDateTime.now();
    }

    public Student(User user) {
        this();
        this.user = user;
    }

    public Student(User user, SchoolClass schoolClass) {
        this();
        this.user = user;
        this.schoolClass = schoolClass;
    }

    // Геттеры и сеттеры
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public SchoolClass getSchoolClass() { return schoolClass; }
    public void setSchoolClass(SchoolClass schoolClass) { this.schoolClass = schoolClass; }

    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }

    public Integer getEnrollmentYear() { return enrollmentYear; }
    public void setEnrollmentYear(Integer enrollmentYear) { this.enrollmentYear = enrollmentYear; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}