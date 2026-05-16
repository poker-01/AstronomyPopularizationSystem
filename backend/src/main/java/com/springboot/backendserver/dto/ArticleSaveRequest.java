package com.springboot.backendserver.dto;

import com.springboot.backendserver.entity.ArticleStatus;
import lombok.Data;

@Data
public class ArticleSaveRequest {

    private Long categoryId;
    private String title;
    private String slug;
    private String summary;
    private String coverUrl;
    private String content;
    private ArticleStatus status;
}
