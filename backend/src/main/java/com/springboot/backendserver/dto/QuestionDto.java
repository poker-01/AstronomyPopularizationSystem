package com.springboot.backendserver.dto;

import com.springboot.backendserver.entity.Question;
import com.springboot.backendserver.util.JsonAnswerUtils;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
public class QuestionDto {
    private Long id;
    private String stem;
    private String type;
    private List<Map<String, String>> options;
    private List<String> correctAnswer;
    private String difficulty;
    private String explanation;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static QuestionDto from(Question q) {
        QuestionDto dto = new QuestionDto();
        dto.setId(q.getId());
        dto.setStem(q.getStem());
        dto.setType(q.getType().name());
        dto.setOptions(JsonAnswerUtils.parseOptions(q.getOptionsJson()));
        dto.setCorrectAnswer(JsonAnswerUtils.parseAnswerKeys(q.getCorrectAnswerJson()).stream().toList());
        dto.setDifficulty(q.getDifficulty().name());
        dto.setExplanation(q.getExplanation());
        dto.setCreatedAt(q.getCreatedAt());
        dto.setUpdatedAt(q.getUpdatedAt());
        return dto;
    }
}
