package com.springboot.backendserver.controller;

import com.springboot.backendserver.common.ApiResponse;
import com.springboot.backendserver.context.AuthContext;
import com.springboot.backendserver.dto.UserBadgeDto;
import com.springboot.backendserver.service.BadgeService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/badges")
public class BadgeController {

    private final BadgeService badgeService;

    public BadgeController(BadgeService badgeService) {
        this.badgeService = badgeService;
    }

    @GetMapping("/mine")
    public ApiResponse<List<UserBadgeDto>> mine() {
        return ApiResponse.ok(badgeService.listMine(AuthContext.require().getId()));
    }
}
