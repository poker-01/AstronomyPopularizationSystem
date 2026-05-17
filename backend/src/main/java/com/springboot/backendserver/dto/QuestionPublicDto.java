package com.springboot.backendserver.dto;

import com.springboot.backendserver.entity.Question;
import com.springboot.backendserver.util.JsonAnswerUtils;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class QuestionPublicDto {
    private Long id;
    private String stem;
    private String type;
    private List<Map<String, String>> options;
    private String difficulty;

    public static QuestionPublicDto from(Question q) {
        QuestionPublicDto dto = new QuestionPublicDto();
        dto.setId(q.getId());
        dto.setStem(q.getStem());
        dto.setType(q.getType().name());
        dto.setOptions(JsonAnswerUtils.parseOptions(q.getOptionsJson()));
        dto.setDifficulty(q.getDifficulty().name());
        return dto;
    }
}
