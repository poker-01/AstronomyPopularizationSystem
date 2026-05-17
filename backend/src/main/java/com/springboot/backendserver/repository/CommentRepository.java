package com.springboot.backendserver.repository;

import com.springboot.backendserver.entity.Comment;
import com.springboot.backendserver.entity.ModerationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long>, JpaSpecificationExecutor<Comment> {

    List<Comment> findByPostIdAndStatusOrderByCreatedAtAsc(Long postId, ModerationStatus status);

    Page<Comment> findByStatusOrderByCreatedAtDesc(ModerationStatus status, Pageable pageable);

    long countByPostIdAndStatus(Long postId, ModerationStatus status);
}
