package nba.studix.userservice.Entity;

import jakarta.persistence.*;

@Entity
@Table(name = "user_roles")
public class UserRole {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    // Конструкторы
    public UserRole() {}
    public UserRole(User user, Role role) {
        this.user = user;
        this.role = role;
    }

    // Геттеры и сеттеры
    public Long getId() {return id;}
    public void setId(Long id) {this.id = id;}
    public User getUser() {return user;}
    public void setUser(User user) {this.user = user;}
    public Role getRole() {return role;}
    public void setRole(Role role) {this.role = role;}
}