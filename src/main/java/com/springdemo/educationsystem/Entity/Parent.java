package com.springdemo.educationsystem.Entity;

import jakarta.persistence.Entity;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "parents")
public class Parent {
    @Id
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL)
    private List<ParentStudent> studentAssociations = new ArrayList<>();

    public Parent() {}

    // Геттеры и сеттеры
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public List<ParentStudent> getStudentAssociations() { return studentAssociations; }
    public void setStudentAssociations(List<ParentStudent> studentAssociations) { this.studentAssociations = studentAssociations; }
}