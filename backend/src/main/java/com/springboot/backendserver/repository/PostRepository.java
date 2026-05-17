package com.springboot.backendserver.repository;

import com.springboot.backendserver.entity.ModerationStatus;
import com.springboot.backendserver.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long>, JpaSpecificationExecutor<Post> {

    Page<Post> findByStatusOrderByCreatedAtDesc(ModerationStatus status, Pageable pageable);

    Page<Post> findByUserIdAndStatusOrderByCreatedAtDesc(Long userId, ModerationStatus status, Pageable pageable);

    Page<Post> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    Page<Post> findByUserIdAndStatusInOrderByCreatedAtDesc(Long userId, Collection<ModerationStatus> statuses, Pageable pageable);

    @Query("SELECT p FROM Post p WHERE p.status = :status AND p.userId IN :userIds ORDER BY p.createdAt DESC")
    Page<Post> findApprovedByUserIds(@Param("status") ModerationStatus status,
                                     @Param("userIds") List<Long> userIds,
                                     Pageable pageable);

    long countByUserIdAndStatus(Long userId, ModerationStatus status);
}
