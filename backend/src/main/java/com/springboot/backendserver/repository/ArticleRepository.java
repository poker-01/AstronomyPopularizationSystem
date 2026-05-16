package com.springboot.backendserver.repository;

import com.springboot.backendserver.entity.Article;
import com.springboot.backendserver.entity.ArticleStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface ArticleRepository extends JpaRepository<Article, Long>, JpaSpecificationExecutor<Article> {

    Optional<Article> findBySlug(String slug);

    Page<Article> findByStatusOrderByPublishedAtDesc(ArticleStatus status, Pageable pageable);

    Page<Article> findByStatusAndCategoryIdOrderByPublishedAtDesc(ArticleStatus status, Long categoryId, Pageable pageable);

    boolean existsBySlug(String slug);
}
