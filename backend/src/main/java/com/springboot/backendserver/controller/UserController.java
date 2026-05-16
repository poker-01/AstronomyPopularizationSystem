package com.springboot.backendserver.controller;

import com.springboot.backendserver.common.ApiResponse;
import com.springboot.backendserver.dto.ChangePasswordRequest;
import com.springboot.backendserver.dto.UpdateProfileRequest;
import com.springboot.backendserver.dto.UserProfileDto;
import com.springboot.backendserver.service.UserService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public ApiResponse<UserProfileDto> me() {
        return ApiResponse.ok(userService.getCurrentProfile());
    }

    @PutMapping("/me")
    public ApiResponse<UserProfileDto> updateMe(@RequestBody UpdateProfileRequest request) {
        return ApiResponse.ok(userService.updateProfile(request));
    }

    @PutMapping("/me/password")
    public ApiResponse<Void> changePassword(@RequestBody ChangePasswordRequest request) {
        userService.changePassword(request);
        return ApiResponse.ok("密码已更新", null);
    }
}
