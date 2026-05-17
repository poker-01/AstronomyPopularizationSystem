package com.springboot.backendserver.dto;

import com.springboot.backendserver.entity.Quiz;
import lombok.Data;

import java.util.List;

@Data
public class QuizPublicDto {
    private Long id;
    private String name;
    private String description;
    private List<QuestionPublicDto> questions;

    public static QuizPublicDto from(Quiz quiz, List<QuestionPublicDto> questions) {
        QuizPublicDto dto = new QuizPublicDto();
        dto.setId(quiz.getId());
        dto.setName(quiz.getName());
        dto.setDescription(quiz.getDescription());
        dto.setQuestions(questions);
        return dto;
    }
}
