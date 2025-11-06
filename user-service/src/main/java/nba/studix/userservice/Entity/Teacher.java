package nba.studix.userservice.Entity;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "teachers")
public class Teacher {
    @Id
    private Long id; // Ссылка на User

    @OneToOne
    @MapsId
    @JoinColumn(name = "id")
    private User user;

    @ManyToMany
    @JoinTable(
            name = "teacher_subjects",
            joinColumns = @JoinColumn(name = "teacher_id"),
            inverseJoinColumns = @JoinColumn(name = "subject_id")
    )
    private Set<Subject> subjects = new HashSet<>();

    @Column(name = "is_class_teacher")
    private Boolean isClassTeacher = false;

    @ManyToMany
    @JoinTable(
            name = "class_teacher_classes",
            joinColumns = @JoinColumn(name = "teacher_id"),
            inverseJoinColumns = @JoinColumn(name = "class_id")
    )
    private Set<SchoolClass> classTeacherClasses = new HashSet<>();

    // Конструкторы
    public Teacher() {}

    public Teacher(User user) {
        this.user = user;
    }

    public Teacher(User user, Boolean isClassTeacher) {
        this.user = user;
        this.isClassTeacher = isClassTeacher;
    }

    // Геттеры и сеттеры
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Set<Subject> getSubjects() {
        return subjects;
    }

    public void setSubjects(Set<Subject> subjects) {
        this.subjects = subjects;
    }

    public Boolean getIsClassTeacher() {
        return isClassTeacher;
    }

    public void setIsClassTeacher(Boolean isClassTeacher) {
        this.isClassTeacher = isClassTeacher;
    }

    public Set<SchoolClass> getClassTeacherClasses() {
        return classTeacherClasses;
    }

    public void setClassTeacherClasses(Set<SchoolClass> classTeacherClasses) {
        this.classTeacherClasses = classTeacherClasses;
    }
}