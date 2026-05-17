package com.springboot.backendserver.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class EventReminderDto {
    private Long subscriptionId;
    private Long eventId;
    private String title;
    private String eventType;
    private String eventTypeLabel;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String description;
    private String notifyChannel;
    private Boolean reminded;
    private long minutesUntilStart;
}
