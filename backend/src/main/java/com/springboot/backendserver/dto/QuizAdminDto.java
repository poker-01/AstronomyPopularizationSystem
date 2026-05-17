package com.springboot.backendserver.dto;

import com.springboot.backendserver.entity.Quiz;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class QuizAdminDto {
    private Long id;
    private String name;
    private String description;
    private Boolean enabled;
    private List<Long> questionIds;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static QuizAdminDto from(Quiz quiz, List<Long> questionIds) {
        QuizAdminDto dto = new QuizAdminDto();
        dto.setId(quiz.getId());
        dto.setName(quiz.getName());
        dto.setDescription(quiz.getDescription());
        dto.setEnabled(quiz.getEnabled());
        dto.setQuestionIds(questionIds);
        dto.setCreatedAt(quiz.getCreatedAt());
        dto.setUpdatedAt(quiz.getUpdatedAt());
        return dto;
    }
}
