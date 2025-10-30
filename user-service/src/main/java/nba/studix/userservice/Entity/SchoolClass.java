package nba.studix.userservice.Entity;

import jakarta.persistence.*;

@Entity
@Table(name = "school_classes")
public class SchoolClass {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(name = "academic_year", nullable = false)
    private String academicYear;

    @Column(name = "school_id", nullable = false)
    private Long schoolId;

    // Конструкторы
    public SchoolClass() {}

    public SchoolClass(String name, String academicYear, Long schoolId) {
        this.name = name;
        this.academicYear = academicYear;
        this.schoolId = schoolId;
    }

    // Геттеры и сеттеры
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getAcademicYear() { return academicYear; }
    public void setAcademicYear(String academicYear) { this.academicYear = academicYear; }

    public Long getSchoolId() { return schoolId; }
    public void setSchoolId(Long schoolId) { this.schoolId = schoolId; }
}
