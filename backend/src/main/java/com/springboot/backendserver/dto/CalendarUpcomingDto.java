package com.springboot.backendserver.dto;

import lombok.Data;

import java.util.List;

@Data
public class CalendarUpcomingDto {
    private List<AstroEventDto> today;
    private List<AstroEventDto> thisWeek;
    private List<EventReminderDto> subscribedUpcoming;
}
