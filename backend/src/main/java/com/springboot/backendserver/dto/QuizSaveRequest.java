package com.springboot.backendserver.dto;

import lombok.Data;

import java.util.List;

@Data
public class QuizSaveRequest {
    private String name;
    private String description;
    private Boolean enabled;
    private List<Long> questionIds;
}
