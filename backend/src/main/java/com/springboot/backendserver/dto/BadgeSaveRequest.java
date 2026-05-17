package com.springboot.backendserver.dto;

import lombok.Data;

@Data
public class BadgeSaveRequest {
    private String name;
    private String iconUrl;
    private String description;
    private String ruleType;
    private Integer ruleValue;
    private Boolean enabled;
}
