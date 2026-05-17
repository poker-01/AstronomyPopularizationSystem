package com.springboot.backendserver.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class QuestionSaveRequest {
    private String stem;
    private String type;
    private List<Map<String, String>> options;
    private List<String> correctAnswer;
    private String difficulty;
    private String explanation;
}
