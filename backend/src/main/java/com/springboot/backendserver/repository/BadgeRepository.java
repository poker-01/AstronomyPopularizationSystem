package com.springboot.backendserver.repository;

import com.springboot.backendserver.entity.Badge;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BadgeRepository extends JpaRepository<Badge, Long> {

    List<Badge> findByEnabledTrueOrderByIdAsc();
}
