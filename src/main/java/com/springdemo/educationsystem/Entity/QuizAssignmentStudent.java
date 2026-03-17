package com.springdemo.educationsystem.Entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

@Entity
@Table(name = "quiz_assignment_student")
public class QuizAssignmentStudent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quiz_assignment_id", nullable = false)
    @JsonIgnore
    private QuizAssignment quizAssignment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    @JsonIgnore
    private Student student;

    public QuizAssignmentStudent() {
    }

    public Long getId() {
        return id;
    }

    public QuizAssignment getQuizAssignment() {
        return quizAssignment;
    }

    public Student getStudent() {
        return student;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setQuizAssignment(QuizAssignment quizAssignment) {
        this.quizAssignment = quizAssignment;
    }

    public void setStudent(Student student) {
        this.student = student;
    }
}