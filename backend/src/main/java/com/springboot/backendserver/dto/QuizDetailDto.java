package com.springboot.backendserver.dto;

import com.springboot.backendserver.entity.Quiz;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class QuizDetailDto {
    private Long id;
    private String name;
    private String description;
    private Boolean enabled;
    private List<Long> questionIds;
    private List<QuestionDto> questions;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static QuizDetailDto from(Quiz quiz, List<Long> questionIds, List<QuestionDto> questions) {
        QuizDetailDto dto = new QuizDetailDto();
        dto.setId(quiz.getId());
        dto.setName(quiz.getName());
        dto.setDescription(quiz.getDescription());
        dto.setEnabled(quiz.getEnabled());
        dto.setQuestionIds(questionIds);
        dto.setQuestions(questions);
        dto.setCreatedAt(quiz.getCreatedAt());
        dto.setUpdatedAt(quiz.getUpdatedAt());
        return dto;
    }
}
