package com.springboot.backendserver.controller.admin;

import com.springboot.backendserver.common.ApiResponse;
import com.springboot.backendserver.common.PageResult;
import com.springboot.backendserver.dto.ModerationItemDto;
import com.springboot.backendserver.dto.ModerationRejectRequest;
import com.springboot.backendserver.entity.ModerationStatus;
import com.springboot.backendserver.service.AdminModerationService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/moderation")
public class AdminModerationController {

    private final AdminModerationService adminModerationService;

    public AdminModerationController(AdminModerationService adminModerationService) {
        this.adminModerationService = adminModerationService;
    }

    @GetMapping("/posts")
    public ApiResponse<PageResult<ModerationItemDto>> listPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) ModerationStatus status) {
        return ApiResponse.ok(adminModerationService.listPosts(page, size, status));
    }

    @PutMapping("/posts/{id}/approve")
    public ApiResponse<Void> approvePost(@PathVariable Long id) {
        adminModerationService.approvePost(id);
        return ApiResponse.ok("已通过", null);
    }

    @PutMapping("/posts/{id}/reject")
    public ApiResponse<Void> rejectPost(@PathVariable Long id, @RequestBody(required = false) ModerationRejectRequest request) {
        adminModerationService.rejectPost(id, request);
        return ApiResponse.ok("已驳回", null);
    }

    @GetMapping("/comments")
    public ApiResponse<PageResult<ModerationItemDto>> listComments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) ModerationStatus status) {
        return ApiResponse.ok(adminModerationService.listComments(page, size, status));
    }

    @PutMapping("/comments/{id}/approve")
    public ApiResponse<Void> approveComment(@PathVariable Long id) {
        adminModerationService.approveComment(id);
        return ApiResponse.ok("已通过", null);
    }

    @PutMapping("/comments/{id}/reject")
    public ApiResponse<Void> rejectComment(@PathVariable Long id, @RequestBody(required = false) ModerationRejectRequest request) {
        adminModerationService.rejectComment(id, request);
        return ApiResponse.ok("已驳回", null);
    }
}
