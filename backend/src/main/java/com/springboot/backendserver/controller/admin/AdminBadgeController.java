package com.springboot.backendserver.controller.admin;

import com.springboot.backendserver.common.ApiResponse;
import com.springboot.backendserver.dto.BadgeDto;
import com.springboot.backendserver.dto.BadgeSaveRequest;
import com.springboot.backendserver.service.AdminBadgeService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/badges")
public class AdminBadgeController {

    private final AdminBadgeService adminBadgeService;

    public AdminBadgeController(AdminBadgeService adminBadgeService) {
        this.adminBadgeService = adminBadgeService;
    }

    @GetMapping
    public ApiResponse<List<BadgeDto>> list() {
        return ApiResponse.ok(adminBadgeService.list());
    }

    @GetMapping("/{id}")
    public ApiResponse<BadgeDto> get(@PathVariable Long id) {
        return ApiResponse.ok(adminBadgeService.get(id));
    }

    @PostMapping
    public ApiResponse<BadgeDto> create(@RequestBody BadgeSaveRequest request) {
        return ApiResponse.ok(adminBadgeService.create(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<BadgeDto> update(@PathVariable Long id, @RequestBody BadgeSaveRequest request) {
        return ApiResponse.ok(adminBadgeService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        adminBadgeService.delete(id);
        return ApiResponse.ok("已删除", null);
    }
}
