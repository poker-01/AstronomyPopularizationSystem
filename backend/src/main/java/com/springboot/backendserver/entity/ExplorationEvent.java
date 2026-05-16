package com.springboot.backendserver.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "exploration_events")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExplorationEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Integer year;

    @Column(length = 32)
    private String month;

    @Column(nullable = false, length = 256)
    private String title;

    @Column(nullable = false, length = 64)
    private String category;

    @Lob
    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(length = 1024)
    private String imageUrl;

    @Column(nullable = false)
    private Integer sortOrder = 0;
}
