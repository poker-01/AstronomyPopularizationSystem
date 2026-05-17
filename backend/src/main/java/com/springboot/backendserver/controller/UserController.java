package com.springboot.backendserver.controller;

import com.springboot.backendserver.common.ApiResponse;
import com.springboot.backendserver.dto.ChangePasswordRequest;
import com.springboot.backendserver.dto.UpdateProfileRequest;
import com.springboot.backendserver.dto.UserProfileDto;
import com.springboot.backendserver.dto.UserPublicProfileDto;
import com.springboot.backendserver.service.FollowService;
import com.springboot.backendserver.service.UserService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final FollowService followService;

    public UserController(UserService userService, FollowService followService) {
        this.userService = userService;
        this.followService = followService;
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

    @GetMapping("/{id}")
    public ApiResponse<UserPublicProfileDto> publicProfile(@PathVariable Long id) {
        return ApiResponse.ok(followService.getPublicProfile(id));
    }

    @PostMapping("/{id}/follow")
    public ApiResponse<Void> follow(@PathVariable Long id) {
        followService.follow(id);
        return ApiResponse.ok("已关注", null);
    }

    @DeleteMapping("/{id}/unfollow")
    public ApiResponse<Void> unfollow(@PathVariable Long id) {
        followService.unfollow(id);
        return ApiResponse.ok("已取消关注", null);
    }
}
