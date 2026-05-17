package com.springboot.backendserver.controller.admin;

import com.springboot.backendserver.common.ApiResponse;
import com.springboot.backendserver.dto.AstroEventDto;
import com.springboot.backendserver.dto.AstroEventSaveRequest;
import com.springboot.backendserver.dto.CalendarImportResultDto;
import com.springboot.backendserver.service.AdminCalendarService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/calendar")
public class AdminCalendarController {

    private final AdminCalendarService adminCalendarService;

    public AdminCalendarController(AdminCalendarService adminCalendarService) {
        this.adminCalendarService = adminCalendarService;
    }

    @GetMapping("/events")
    public ApiResponse<List<AstroEventDto>> list() {
        return ApiResponse.ok(adminCalendarService.list());
    }

    @GetMapping("/events/{id}")
    public ApiResponse<AstroEventDto> get(@PathVariable Long id) {
        return ApiResponse.ok(adminCalendarService.get(id));
    }

    @PostMapping("/events")
    public ApiResponse<AstroEventDto> create(@RequestBody AstroEventSaveRequest request) {
        return ApiResponse.ok(adminCalendarService.create(request));
    }

    @PutMapping("/events/{id}")
    public ApiResponse<AstroEventDto> update(@PathVariable Long id, @RequestBody AstroEventSaveRequest request) {
        return ApiResponse.ok(adminCalendarService.update(id, request));
    }

    @DeleteMapping("/events/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        adminCalendarService.delete(id);
        return ApiResponse.ok("已删除", null);
    }

    @PostMapping("/import")
    public ApiResponse<CalendarImportResultDto> importData(@RequestParam(required = false) Integer year) {
        return ApiResponse.ok(adminCalendarService.importPublicData(year));
    }
}
