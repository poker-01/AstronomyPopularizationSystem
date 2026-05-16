package com.springboot.backendserver.controller;

import com.springboot.backendserver.common.ApiResponse;
import com.springboot.backendserver.common.BusinessException;
import com.springboot.backendserver.dto.UserProfileDto;
import com.springboot.backendserver.entity.User;
import com.springboot.backendserver.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserProfileDto>> register(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");
        try {
            User user = authService.register(username, password);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.ok(UserProfileDto.from(user)));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(ApiResponse.fail(400, ex.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<Map<String, Object>>> login(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");
        try {
            return authService.login(username, password)
                    .map(user -> {
                        Map<String, Object> payload = Map.of(
                                "token", user.getToken(),
                                "username", user.getUsername(),
                                "role", user.getRole(),
                                "nickname", user.getNickname() != null ? user.getNickname() : user.getUsername()
                        );
                        return ResponseEntity.ok(ApiResponse.ok(payload));
                    })
                    .orElseGet(() -> ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                            .body(ApiResponse.fail(401, "用户名或密码错误")));
        } catch (BusinessException ex) {
            return ResponseEntity.badRequest().body(ApiResponse.fail(ex.getCode(), ex.getMessage()));
        }
    }
}
