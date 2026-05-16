package com.springboot.backendserver.controller.admin;

import com.springboot.backendserver.common.ApiResponse;
import com.springboot.backendserver.common.PageResult;
import com.springboot.backendserver.dto.AdminCreateUserRequest;
import com.springboot.backendserver.dto.AdminUpdateUserRequest;
import com.springboot.backendserver.dto.AdminUserDto;
import com.springboot.backendserver.entity.UserStatus;
import com.springboot.backendserver.service.AdminUserService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/users")
public class AdminUserController {

    private final AdminUserService adminUserService;

    public AdminUserController(AdminUserService adminUserService) {
        this.adminUserService = adminUserService;
    }

    @GetMapping
    public ApiResponse<PageResult<AdminUserDto>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) UserStatus status) {
        return ApiResponse.ok(adminUserService.listUsers(page, size, keyword, role, status));
    }

    @GetMapping("/{id}")
    public ApiResponse<AdminUserDto> get(@PathVariable Long id) {
        return ApiResponse.ok(adminUserService.getUser(id));
    }

    @PostMapping
    public ApiResponse<AdminUserDto> create(@RequestBody AdminCreateUserRequest request) {
        return ApiResponse.ok(adminUserService.createUser(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<AdminUserDto> update(@PathVariable Long id, @RequestBody AdminUpdateUserRequest request) {
        return ApiResponse.ok(adminUserService.updateUser(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        adminUserService.deleteUser(id);
        return ApiResponse.ok("用户已删除", null);
    }
}
