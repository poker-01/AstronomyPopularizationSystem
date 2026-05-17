package com.springboot.backendserver.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AstroEventSaveRequest {
    private String title;
    private String eventType;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String description;
    private Integer reminderOffsetMinutes;
}
