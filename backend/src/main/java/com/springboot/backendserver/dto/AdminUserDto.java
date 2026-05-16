package com.springboot.backendserver.dto;

import com.springboot.backendserver.entity.User;
import com.springboot.backendserver.entity.UserStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AdminUserDto {

    private Long id;
    private String username;
    private String role;
    private String email;
    private String nickname;
    private String avatar;
    private UserStatus status;
    private LocalDateTime createdAt;

    public static AdminUserDto from(User user) {
        AdminUserDto dto = new AdminUserDto();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setRole(user.getRole());
        dto.setEmail(user.getEmail());
        dto.setNickname(user.getNickname());
        dto.setAvatar(user.getAvatar());
        dto.setStatus(user.getStatus());
        dto.setCreatedAt(user.getCreatedAt());
        return dto;
    }
}
