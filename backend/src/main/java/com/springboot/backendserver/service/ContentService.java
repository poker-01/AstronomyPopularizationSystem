package com.springboot.backendserver.service;

import com.springboot.backendserver.common.BusinessException;
import com.springboot.backendserver.common.PageResult;
import com.springboot.backendserver.dto.ArticleDetailDto;
import com.springboot.backendserver.dto.ArticleSummaryDto;
import com.springboot.backendserver.dto.CategoryDto;
import com.springboot.backendserver.entity.Article;
import com.springboot.backendserver.entity.ArticleStatus;
import com.springboot.backendserver.entity.Category;
import com.springboot.backendserver.repository.ArticleRepository;
import com.springboot.backendserver.repository.CategoryRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ContentService {

    private final CategoryRepository categoryRepository;
    private final ArticleRepository articleRepository;

    public ContentService(CategoryRepository categoryRepository, ArticleRepository articleRepository) {
        this.categoryRepository = categoryRepository;
        this.articleRepository = articleRepository;
    }

    public List<CategoryDto> listCategories() {
        return categoryRepository.findAllByOrderBySortOrderAsc().stream()
                .map(CategoryDto::from)
                .toList();
    }

    public PageResult<ArticleSummaryDto> listPublishedArticles(int page, int size, Long categoryId) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), 50);
        PageRequest pageable = PageRequest.of(safePage, safeSize);

        Page<Article> result = categoryId == null
                ? articleRepository.findByStatusOrderByPublishedAtDesc(ArticleStatus.PUBLISHED, pageable)
                : articleRepository.findByStatusAndCategoryIdOrderByPublishedAtDesc(ArticleStatus.PUBLISHED, categoryId, pageable);

        Map<Long, String> categoryNames = categoryNameMap();
        List<ArticleSummaryDto> content = result.getContent().stream()
                .map(a -> ArticleSummaryDto.from(a, categoryNames.getOrDefault(a.getCategoryId(), "")))
                .toList();

        return PageResult.of(content, result.getTotalElements(), result.getTotalPages(), safePage, safeSize);
    }

    @Transactional
    public ArticleDetailDto getPublishedArticle(String slug) {
        Article article = articleRepository.findBySlug(slug)
                .orElseThrow(() -> BusinessException.notFound("文章不存在"));
        if (article.getStatus() != ArticleStatus.PUBLISHED) {
            throw BusinessException.notFound("文章不存在");
        }
        article.setViewCount(article.getViewCount() + 1);
        articleRepository.save(article);
        String categoryName = categoryRepository.findById(article.getCategoryId())
                .map(Category::getName)
                .orElse("");
        return ArticleDetailDto.from(article, categoryName);
    }

    private Map<Long, String> categoryNameMap() {
        return categoryRepository.findAll().stream()
                .collect(Collectors.toMap(Category::getId, Category::getName));
    }
}
