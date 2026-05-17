package com.springboot.backendserver.controller;

import com.springboot.backendserver.common.ApiResponse;
import com.springboot.backendserver.dto.*;
import com.springboot.backendserver.service.CalendarService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/calendar")
public class CalendarController {

    private final CalendarService calendarService;

    public CalendarController(CalendarService calendarService) {
        this.calendarService = calendarService;
    }

    @GetMapping("/events")
    public ApiResponse<List<AstroEventDto>> listByMonth(
            @RequestParam int year,
            @RequestParam int month) {
        return ApiResponse.ok(calendarService.listByMonth(year, month));
    }

    @GetMapping("/events/{id}")
    public ApiResponse<AstroEventDto> get(@PathVariable Long id) {
        return ApiResponse.ok(calendarService.get(id));
    }

    @GetMapping("/upcoming")
    public ApiResponse<CalendarUpcomingDto> upcoming() {
        return ApiResponse.ok(calendarService.upcoming());
    }

    @GetMapping("/reminders/mine")
    public ApiResponse<List<EventReminderDto>> myReminders() {
        return ApiResponse.ok(calendarService.listMineReminders());
    }

    @PostMapping("/events/{id}/subscribe")
    public ApiResponse<Void> subscribe(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, String> body) {
        String channel = body != null ? body.get("notifyChannel") : null;
        calendarService.subscribe(id, channel);
        return ApiResponse.ok("订阅成功", null);
    }

    @DeleteMapping("/events/{id}/subscribe")
    public ApiResponse<Void> unsubscribe(@PathVariable Long id) {
        calendarService.unsubscribe(id);
        return ApiResponse.ok("已取消订阅", null);
    }
}
