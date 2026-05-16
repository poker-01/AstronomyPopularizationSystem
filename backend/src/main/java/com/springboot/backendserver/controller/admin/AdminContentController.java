package com.springboot.backendserver.controller.admin;

import com.springboot.backendserver.common.ApiResponse;
import com.springboot.backendserver.common.PageResult;
import com.springboot.backendserver.dto.*;
import com.springboot.backendserver.entity.ArticleStatus;
import com.springboot.backendserver.service.AdminContentService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/content")
public class AdminContentController {

    private final AdminContentService adminContentService;

    public AdminContentController(AdminContentService adminContentService) {
        this.adminContentService = adminContentService;
    }

    @GetMapping("/categories")
    public ApiResponse<List<CategoryDto>> categories() {
        return ApiResponse.ok(adminContentService.listCategories());
    }

    @GetMapping("/articles")
    public ApiResponse<PageResult<ArticleSummaryDto>> articles(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) ArticleStatus status,
            @RequestParam(required = false) Long categoryId) {
        return ApiResponse.ok(adminContentService.listArticles(page, size, keyword, status, categoryId));
    }

    @GetMapping("/articles/{id}")
    public ApiResponse<ArticleDetailDto> get(@PathVariable Long id) {
        return ApiResponse.ok(adminContentService.getArticle(id));
    }

    @PostMapping("/articles")
    public ApiResponse<ArticleDetailDto> create(@RequestBody ArticleSaveRequest request) {
        return ApiResponse.ok(adminContentService.createArticle(request));
    }

    @PutMapping("/articles/{id}")
    public ApiResponse<ArticleDetailDto> update(@PathVariable Long id, @RequestBody ArticleSaveRequest request) {
        return ApiResponse.ok(adminContentService.updateArticle(id, request));
    }

    @DeleteMapping("/articles/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        adminContentService.deleteArticle(id);
        return ApiResponse.ok("已删除", null);
    }
}
