package com.springboot.backendserver.dto;

import com.springboot.backendserver.entity.Article;
import com.springboot.backendserver.entity.ArticleStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ArticleSummaryDto {

    private Long id;
    private Long categoryId;
    private String categoryName;
    private String title;
    private String slug;
    private String summary;
    private String coverUrl;
    private ArticleStatus status;
    private Long viewCount;
    private LocalDateTime publishedAt;

    public static ArticleSummaryDto from(Article article, String categoryName) {
        ArticleSummaryDto dto = new ArticleSummaryDto();
        dto.setId(article.getId());
        dto.setCategoryId(article.getCategoryId());
        dto.setCategoryName(categoryName);
        dto.setTitle(article.getTitle());
        dto.setSlug(article.getSlug());
        dto.setSummary(article.getSummary());
        dto.setCoverUrl(article.getCoverUrl());
        dto.setStatus(article.getStatus());
        dto.setViewCount(article.getViewCount());
        dto.setPublishedAt(article.getPublishedAt());
        return dto;
    }
}
