package com.springboot.backendserver.dto;

import com.springboot.backendserver.entity.UserStatus;
import lombok.Data;

@Data
public class AdminCreateUserRequest {

    private String username;
    private String password;
    private String role;
    private String email;
    private String nickname;
    private String avatar;
    private UserStatus status;
}
