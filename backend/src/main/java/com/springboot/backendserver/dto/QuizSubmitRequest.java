package com.springboot.backendserver.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class QuizSubmitRequest {
    /** questionId -> selected option keys */
    private Map<String, List<String>> answers;
}
