package com.springboot.backendserver.repository;

import com.springboot.backendserver.entity.UserBadge;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserBadgeRepository extends JpaRepository<UserBadge, Long> {

    List<UserBadge> findByUserIdOrderByEarnedAtDesc(Long userId);

    boolean existsByUserIdAndBadgeId(Long userId, Long badgeId);

    Optional<UserBadge> findByUserIdAndBadgeId(Long userId, Long badgeId);
}
