package com.springboot.backendserver.repository;

import com.springboot.backendserver.entity.UserQuizAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserQuizAttemptRepository extends JpaRepository<UserQuizAttempt, Long> {

    long countByUserId(Long userId);

    @Query("SELECT MAX(a.score) FROM UserQuizAttempt a WHERE a.userId = :userId")
    Integer findMaxScoreByUserId(@Param("userId") Long userId);

    List<UserQuizAttempt> findByUserIdOrderByFinishedAtDesc(Long userId);
}
