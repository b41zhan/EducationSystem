package nba.studix.userservice.Entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "parents")
public class Parent {
    @Id
    private Long id;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public Parent() {
        this.createdAt = LocalDateTime.now();
    }

    public Parent(Long id) {
        this();
        this.id = id;
    }

    // Геттеры и сеттеры
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}