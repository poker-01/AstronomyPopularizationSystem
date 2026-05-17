package com.springboot.backendserver.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class QuizSubmitResultDto {
    private int score;
    private int total;
    private int percent;
    private Long attemptId;
    private Map<Long, QuestionResultItem> results;
    private List<UserBadgeDto> newBadges;

    @Data
    public static class QuestionResultItem {
        private boolean correct;
        private List<String> correctAnswer;
        private String explanation;
    }
}
