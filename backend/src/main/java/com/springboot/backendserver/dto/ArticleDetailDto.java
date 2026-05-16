package com.springboot.backendserver.dto;

import com.springboot.backendserver.entity.Article;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ArticleDetailDto extends ArticleSummaryDto {

    private String content;

    public static ArticleDetailDto from(Article article, String categoryName) {
        ArticleDetailDto dto = new ArticleDetailDto();
        ArticleSummaryDto base = ArticleSummaryDto.from(article, categoryName);
        dto.setId(base.getId());
        dto.setCategoryId(base.getCategoryId());
        dto.setCategoryName(base.getCategoryName());
        dto.setTitle(base.getTitle());
        dto.setSlug(base.getSlug());
        dto.setSummary(base.getSummary());
        dto.setCoverUrl(base.getCoverUrl());
        dto.setStatus(base.getStatus());
        dto.setViewCount(base.getViewCount());
        dto.setPublishedAt(base.getPublishedAt());
        dto.setContent(article.getContent());
        return dto;
    }
}
