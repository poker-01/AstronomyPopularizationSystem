package com.springboot.backendserver.controller;

import com.springboot.backendserver.entity.User;
import com.springboot.backendserver.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class HomeController {

    private final AuthService authService;

    public HomeController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping("/home")
    public ResponseEntity<?> home(@RequestHeader(value = "Authorization", required = false) String authorization,
                                  @RequestHeader(value = "X-Auth-Token", required = false) String tokenHeader) {
        String token = null;
        if (authorization != null && authorization.startsWith("Bearer ")) {
            token = authorization.substring(7);
        } else if (tokenHeader != null) {
            token = tokenHeader;
        }
        User user = authService.findByToken(token).orElse(null);
        if (user == null) {
            return ResponseEntity.ok(Map.of("message", "Welcome guest! Please login to see personalized content."));
        }
        return ResponseEntity.ok(Map.of(
                "message", "Welcome to Astronomy Popularization Home",
                "username", user.getUsername(),
                "role", user.getRole()
        ));
    }

}
