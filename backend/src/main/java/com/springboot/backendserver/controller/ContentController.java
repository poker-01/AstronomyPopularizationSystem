package com.springboot.backendserver.controller;

import com.springboot.backendserver.common.ApiResponse;
import com.springboot.backendserver.common.PageResult;
import com.springboot.backendserver.dto.ArticleDetailDto;
import com.springboot.backendserver.dto.ArticleSummaryDto;
import com.springboot.backendserver.dto.CategoryDto;
import com.springboot.backendserver.service.ContentService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/content")
public class ContentController {

    private final ContentService contentService;

    public ContentController(ContentService contentService) {
        this.contentService = contentService;
    }

    @GetMapping("/categories")
    public ApiResponse<List<CategoryDto>> categories() {
        return ApiResponse.ok(contentService.listCategories());
    }

    @GetMapping("/articles")
    public ApiResponse<PageResult<ArticleSummaryDto>> articles(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "9") int size,
            @RequestParam(required = false) Long categoryId) {
        return ApiResponse.ok(contentService.listPublishedArticles(page, size, categoryId));
    }

    @GetMapping("/articles/{slug}")
    public ApiResponse<ArticleDetailDto> article(@PathVariable String slug) {
        return ApiResponse.ok(contentService.getPublishedArticle(slug));
    }
}
