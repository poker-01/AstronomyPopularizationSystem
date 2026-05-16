package com.springboot.backendserver.dto;

import lombok.Data;

@Data
public class UpdateProfileRequest {

    private String email;
    private String nickname;
    private String avatar;
}
