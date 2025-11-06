package nba.studix.userservice.Entity;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "school_classes")
public class SchoolClass {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name; // "9А", "10Б"

    @Column(name = "academic_year", nullable = false)
    private String academicYear; // "2024-2025"

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "school_id", nullable = false)
    private School school;

    @OneToMany(mappedBy = "schoolClass")
    private List<Student> students = new ArrayList<>();

    // Предметы класса
    @ManyToMany
    @JoinTable(
            name = "class_subjects",
            joinColumns = @JoinColumn(name = "class_id"),
            inverseJoinColumns = @JoinColumn(name = "subject_id")
    )
    private Set<Subject> subjects = new HashSet<>();

    // Конструкторы, геттеры, сеттеры...
    public SchoolClass() {}

    public SchoolClass(String name, String academicYear, School school) {
        this.name = name;
        this.academicYear = academicYear;
        this.school = school;
    }
    public SchoolClass(String name, String academicYear) {
        this.name = name;
        this.academicYear = academicYear;
    }
    // Геттеры и сеттеры...
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getAcademicYear() { return academicYear; }
    public void setAcademicYear(String academicYear) { this.academicYear = academicYear; }

    public School getSchool() { return school; }
    public void setSchool(School school) { this.school = school; }

    public List<Student> getStudents() { return students; }
    public void setStudents(List<Student> students) { this.students = students; }
    public Set<Subject> getSubjects() { return subjects; }
    public void setSubjects(Set<Subject> subjects) { this.subjects = subjects; }
}