package com.springboot.backendserver.controller.admin;

import com.springboot.backendserver.common.ApiResponse;
import com.springboot.backendserver.service.AdminUserService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/stats")
public class AdminStatsController {

    private final AdminUserService adminUserService;

    public AdminStatsController(AdminUserService adminUserService) {
        this.adminUserService = adminUserService;
    }

    @GetMapping
    public ApiResponse<Map<String, Long>> stats() {
        return ApiResponse.ok(Map.of("userCount", adminUserService.countActiveUsers()));
    }
}
