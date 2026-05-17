package com.springboot.backendserver.dto;

import com.springboot.backendserver.entity.Quiz;
import lombok.Data;

@Data
public class QuizSummaryDto {
    private Long id;
    private String name;
    private String description;
    private int questionCount;

    public static QuizSummaryDto from(Quiz quiz, int questionCount) {
        QuizSummaryDto dto = new QuizSummaryDto();
        dto.setId(quiz.getId());
        dto.setName(quiz.getName());
        dto.setDescription(quiz.getDescription());
        dto.setQuestionCount(questionCount);
        return dto;
    }
}
