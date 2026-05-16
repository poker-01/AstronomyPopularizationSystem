package com.springboot.backendserver.controller;

import com.springboot.backendserver.common.ApiResponse;
import com.springboot.backendserver.dto.UserProfileDto;
import com.springboot.backendserver.entity.User;
import com.springboot.backendserver.service.AuthService;
import com.springboot.backendserver.util.TokenUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class HomeController {

    private final AuthService authService;

    public HomeController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping("/home")
    public ApiResponse<Map<String, Object>> home(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestHeader(value = "X-Auth-Token", required = false) String tokenHeader) {
        String token = TokenUtils.extractBearer(authorization);
        if (token == null) {
            token = tokenHeader;
        }

        Map<String, Object> data = new LinkedHashMap<>();
        return authService.findByToken(token)
                .map(user -> {
                    data.put("message", "欢迎回来，" + displayName(user) + "！");
                    data.put("username", user.getUsername());
                    data.put("nickname", displayName(user));
                    data.put("role", user.getRole());
                    data.put("profile", UserProfileDto.from(user));
                    return ApiResponse.ok(data);
                })
                .orElseGet(() -> {
                    data.put("message", "一个关于太阳系的科普网站");
                    return ApiResponse.ok(data);
                });
    }

    private String displayName(User user) {
        if (user.getNickname() != null && !user.getNickname().isBlank()) {
            return user.getNickname();
        }
        return user.getUsername();
    }
}
