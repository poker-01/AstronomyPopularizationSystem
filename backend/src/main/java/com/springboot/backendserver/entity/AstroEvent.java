package com.springboot.backendserver.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "astro_events")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AstroEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 256)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private AstroEventType eventType = AstroEventType.OTHER;

    @Column(nullable = false)
    private LocalDateTime startTime;

    @Column(nullable = false)
    private LocalDateTime endTime;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String description;

    /** Minutes before startTime to remind subscribers */
    @Column(nullable = false)
    private Integer reminderOffsetMinutes = 60;

    @Column(length = 64)
    private String source;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
        if (reminderOffsetMinutes == null) {
            reminderOffsetMinutes = 60;
        }
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
