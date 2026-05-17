package com.springboot.backendserver.repository;

import com.springboot.backendserver.entity.Quiz;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuizRepository extends JpaRepository<Quiz, Long> {

    List<Quiz> findByEnabledTrueOrderByIdAsc();
}
