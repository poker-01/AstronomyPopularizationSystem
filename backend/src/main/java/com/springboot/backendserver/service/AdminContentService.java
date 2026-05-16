package com.springboot.backendserver.service;

import com.springboot.backendserver.common.BusinessException;
import com.springboot.backendserver.common.PageResult;
import com.springboot.backendserver.dto.*;
import com.springboot.backendserver.entity.Article;
import com.springboot.backendserver.entity.ArticleStatus;
import com.springboot.backendserver.entity.Category;
import com.springboot.backendserver.repository.ArticleRepository;
import com.springboot.backendserver.repository.CategoryRepository;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AdminContentService {

    private final CategoryRepository categoryRepository;
    private final ArticleRepository articleRepository;

    public AdminContentService(CategoryRepository categoryRepository, ArticleRepository articleRepository) {
        this.categoryRepository = categoryRepository;
        this.articleRepository = articleRepository;
    }

    public List<CategoryDto> listCategories() {
        return categoryRepository.findAllByOrderBySortOrderAsc().stream().map(CategoryDto::from).toList();
    }

    public PageResult<ArticleSummaryDto> listArticles(int page, int size, String keyword, ArticleStatus status, Long categoryId) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), 50);

        Specification<Article> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (StringUtils.hasText(keyword)) {
                String pattern = "%" + keyword.trim().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("title")), pattern),
                        cb.like(cb.lower(root.get("summary")), pattern)
                ));
            }
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            if (categoryId != null) {
                predicates.add(cb.equal(root.get("categoryId"), categoryId));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<Article> result = articleRepository.findAll(
                spec,
                PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.DESC, "updatedAt"))
        );

        Map<Long, String> names = categoryNameMap();
        List<ArticleSummaryDto> content = result.getContent().stream()
                .map(a -> ArticleSummaryDto.from(a, names.getOrDefault(a.getCategoryId(), "")))
                .toList();
        return PageResult.of(content, result.getTotalElements(), result.getTotalPages(), safePage, safeSize);
    }

    public ArticleDetailDto getArticle(Long id) {
        Article article = articleRepository.findById(id).orElseThrow(() -> BusinessException.notFound("文章不存在"));
        String categoryName = categoryRepository.findById(article.getCategoryId()).map(Category::getName).orElse("");
        return ArticleDetailDto.from(article, categoryName);
    }

    @Transactional
    public ArticleDetailDto createArticle(ArticleSaveRequest request) {
        validateSaveRequest(request, null);
        Article article = new Article();
        applySaveRequest(article, request);
        if (article.getStatus() == ArticleStatus.PUBLISHED && article.getPublishedAt() == null) {
            article.setPublishedAt(LocalDateTime.now());
        }
        articleRepository.save(article);
        return getArticle(article.getId());
    }

    @Transactional
    public ArticleDetailDto updateArticle(Long id, ArticleSaveRequest request) {
        Article article = articleRepository.findById(id).orElseThrow(() -> BusinessException.notFound("文章不存在"));
        validateSaveRequest(request, id);
        ArticleStatus oldStatus = article.getStatus();
        applySaveRequest(article, request);
        if (oldStatus != ArticleStatus.PUBLISHED && article.getStatus() == ArticleStatus.PUBLISHED) {
            article.setPublishedAt(LocalDateTime.now());
        }
        articleRepository.save(article);
        return getArticle(id);
    }

    @Transactional
    public void deleteArticle(Long id) {
        if (!articleRepository.existsById(id)) {
            throw BusinessException.notFound("文章不存在");
        }
        articleRepository.deleteById(id);
    }

    private void validateSaveRequest(ArticleSaveRequest request, Long excludeId) {
        if (request == null || !StringUtils.hasText(request.getTitle()) || !StringUtils.hasText(request.getContent())) {
            throw BusinessException.badRequest("标题和内容不能为空");
        }
        if (request.getCategoryId() == null || !categoryRepository.existsById(request.getCategoryId())) {
            throw BusinessException.badRequest("分类不存在");
        }
        String slug = resolveSlug(request);
        articleRepository.findBySlug(slug).ifPresent(existing -> {
            if (excludeId == null || !existing.getId().equals(excludeId)) {
                throw BusinessException.badRequest("文章 slug 已存在");
            }
        });
    }

    private void applySaveRequest(Article article, ArticleSaveRequest request) {
        article.setCategoryId(request.getCategoryId());
        article.setTitle(request.getTitle().trim());
        article.setSlug(resolveSlug(request));
        article.setSummary(request.getSummary());
        article.setCoverUrl(request.getCoverUrl());
        article.setContent(request.getContent());
        article.setStatus(request.getStatus() != null ? request.getStatus() : ArticleStatus.DRAFT);
    }

    private String resolveSlug(ArticleSaveRequest request) {
        if (StringUtils.hasText(request.getSlug())) {
            return request.getSlug().trim().toLowerCase().replaceAll("\\s+", "-");
        }
        return request.getTitle().trim().toLowerCase()
                .replaceAll("[^a-z0-9\\u4e00-\\u9fa5]+", "-")
                .replaceAll("^-|-$", "");
    }

    private Map<Long, String> categoryNameMap() {
        return categoryRepository.findAll().stream()
                .collect(Collectors.toMap(Category::getId, Category::getName));
    }
}
