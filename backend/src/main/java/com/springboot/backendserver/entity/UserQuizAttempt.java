package com.springboot.backendserver.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_quiz_attempts")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserQuizAttempt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "quiz_id", nullable = false)
    private Long quizId;

    @Column(nullable = false)
    private Integer score;

    @Column(name = "answers_json", nullable = false, columnDefinition = "TEXT")
    private String answersJson;

    @Column(name = "finished_at", nullable = false)
    private LocalDateTime finishedAt;

    @PrePersist
    protected void onCreate() {
        if (finishedAt == null) {
            finishedAt = LocalDateTime.now();
        }
    }
}
