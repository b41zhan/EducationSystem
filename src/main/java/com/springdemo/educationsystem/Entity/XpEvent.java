package com.springdemo.educationsystem.Entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class XpEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "student_id")
    private Student student;

    private Integer xpChange; // +25, +10, +5
    private Integer totalXpAfter; // total XP after this award

    private String source;  // "assignment", "perfect", "streak", "achievement", "daily_login"
    private Long sourceId;  // id задания или ачивки (nullable)

    private LocalDateTime createdAt = LocalDateTime.now();
}
