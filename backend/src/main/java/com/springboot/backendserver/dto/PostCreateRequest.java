package com.springboot.backendserver.dto;

import lombok.Data;

@Data
public class PostCreateRequest {
    private String title;
    private String content;
}
