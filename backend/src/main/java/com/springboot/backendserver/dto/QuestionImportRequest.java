package com.springboot.backendserver.dto;

import lombok.Data;

import java.util.List;

@Data
public class QuestionImportRequest {
    private List<QuestionSaveRequest> questions;
}
