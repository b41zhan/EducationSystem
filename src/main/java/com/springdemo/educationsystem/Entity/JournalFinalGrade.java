package com.springdemo.educationsystem.Entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "journal_final_grades",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"teacher_id", "student_id", "class_id", "subject_id", "quarter"})
        }
)
public class JournalFinalGrade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "teacher_id")
    private Teacher teacher;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id")
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "class_id")
    private SchoolClass schoolClass;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "subject_id")
    private Subject subject;

    @Column(nullable = false)
    private Integer quarter;

    @Column(name = "quarter_grade")
    private Double quarterGrade;

    @Column(name = "calculated_quarter_grade")
    private Double calculatedQuarterGrade;

    @Column(name = "year_grade")
    private Double yearGrade;

    @Column(name = "calculated_year_grade")
    private Double calculatedYearGrade;

    @Column(name = "is_quarter_manual")
    private boolean quarterManual = false;

    @Column(name = "is_year_manual")
    private boolean yearManual = false;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public JournalFinalGrade() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Teacher getTeacher() { return teacher; }
    public void setTeacher(Teacher teacher) { this.teacher = teacher; }

    public Student getStudent() { return student; }
    public void setStudent(Student student) { this.student = student; }

    public SchoolClass getSchoolClass() { return schoolClass; }
    public void setSchoolClass(SchoolClass schoolClass) { this.schoolClass = schoolClass; }

    public Subject getSubject() { return subject; }
    public void setSubject(Subject subject) { this.subject = subject; }

    public Integer getQuarter() { return quarter; }
    public void setQuarter(Integer quarter) { this.quarter = quarter; }

    public Double getQuarterGrade() { return quarterGrade; }
    public void setQuarterGrade(Double quarterGrade) { this.quarterGrade = quarterGrade; }

    public Double getCalculatedQuarterGrade() { return calculatedQuarterGrade; }
    public void setCalculatedQuarterGrade(Double calculatedQuarterGrade) { this.calculatedQuarterGrade = calculatedQuarterGrade; }

    public Double getYearGrade() { return yearGrade; }
    public void setYearGrade(Double yearGrade) { this.yearGrade = yearGrade; }

    public Double getCalculatedYearGrade() { return calculatedYearGrade; }
    public void setCalculatedYearGrade(Double calculatedYearGrade) { this.calculatedYearGrade = calculatedYearGrade; }

    public boolean isQuarterManual() { return quarterManual; }
    public void setQuarterManual(boolean quarterManual) { this.quarterManual = quarterManual; }

    public boolean isYearManual() { return yearManual; }
    public void setYearManual(boolean yearManual) { this.yearManual = yearManual; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}