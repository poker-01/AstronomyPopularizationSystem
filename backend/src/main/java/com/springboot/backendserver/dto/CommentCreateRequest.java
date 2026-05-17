package com.springboot.backendserver.dto;

import lombok.Data;

@Data
public class CommentCreateRequest {
    private String content;
    private Long parentId;
}
