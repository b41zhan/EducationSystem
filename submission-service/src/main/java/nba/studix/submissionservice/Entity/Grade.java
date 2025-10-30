package nba.studix.submissionservice.Entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "grades")
public class Grade {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "submission_id", nullable = false, unique = true)
    private Long submissionId;

    @Column(name = "grade_value")
    private Integer gradeValue;

    @Column(columnDefinition = "TEXT")
    private String comment;

    @Column(name = "graded_at")
    private LocalDateTime gradedAt;

    @Column(name = "teacher_id", nullable = false)
    private Long teacherId;

    // Конструкторы
    public Grade() {
        this.gradedAt = LocalDateTime.now();
    }

    public Grade(Long submissionId, Integer gradeValue, String comment, Long teacherId) {
        this();
        this.submissionId = submissionId;
        this.gradeValue = gradeValue;
        this.comment = comment;
        this.teacherId = teacherId;
    }

    // Геттеры и сеттеры
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getSubmissionId() { return submissionId; }
    public void setSubmissionId(Long submissionId) { this.submissionId = submissionId; }

    public Integer getGradeValue() { return gradeValue; }
    public void setGradeValue(Integer gradeValue) { this.gradeValue = gradeValue; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    public LocalDateTime getGradedAt() { return gradedAt; }
    public void setGradedAt(LocalDateTime gradedAt) { this.gradedAt = gradedAt; }

    public Long getTeacherId() { return teacherId; }
    public void setTeacherId(Long teacherId) { this.teacherId = teacherId; }
}