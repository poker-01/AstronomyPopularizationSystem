package com.springboot.backendserver.dto;

import lombok.Data;

@Data
public class CalendarImportResultDto {
    private int imported;
    private int skipped;
    private String source;
    private String message;
}
