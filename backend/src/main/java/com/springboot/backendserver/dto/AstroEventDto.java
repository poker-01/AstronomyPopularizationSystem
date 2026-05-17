package com.springboot.backendserver.dto;

import com.springboot.backendserver.entity.AstroEvent;
import com.springboot.backendserver.entity.AstroEventType;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AstroEventDto {
    private Long id;
    private String title;
    private String eventType;
    private String eventTypeLabel;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String description;
    private Integer reminderOffsetMinutes;
    private String source;
    private Boolean subscribed;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static AstroEventDto from(AstroEvent event) {
        return from(event, null);
    }

    public static AstroEventDto from(AstroEvent event, Boolean subscribed) {
        AstroEventDto dto = new AstroEventDto();
        dto.setId(event.getId());
        dto.setTitle(event.getTitle());
        dto.setEventType(event.getEventType().name());
        dto.setEventTypeLabel(labelOf(event.getEventType()));
        dto.setStartTime(event.getStartTime());
        dto.setEndTime(event.getEndTime());
        dto.setDescription(event.getDescription());
        dto.setReminderOffsetMinutes(event.getReminderOffsetMinutes());
        dto.setSource(event.getSource());
        dto.setSubscribed(subscribed);
        dto.setCreatedAt(event.getCreatedAt());
        dto.setUpdatedAt(event.getUpdatedAt());
        return dto;
    }

    public static String labelOf(AstroEventType type) {
        return switch (type) {
            case METEOR_SHOWER -> "流星雨";
            case LUNAR_ECLIPSE -> "月食";
            case SOLAR_ECLIPSE -> "日食";
            case PLANETARY_CONJUNCTION -> "行星合";
            case OTHER -> "其他";
        };
    }
}
