package com.springboot.backendserver.dto;

import com.springboot.backendserver.entity.ExplorationEvent;
import lombok.Data;

@Data
public class ExplorationEventDto {

    private Long id;
    private Integer year;
    private String month;
    private String title;
    private String category;
    private String description;
    private String imageUrl;
    private Integer sortOrder;

    public static ExplorationEventDto from(ExplorationEvent event) {
        ExplorationEventDto dto = new ExplorationEventDto();
        dto.setId(event.getId());
        dto.setYear(event.getYear());
        dto.setMonth(event.getMonth());
        dto.setTitle(event.getTitle());
        dto.setCategory(event.getCategory());
        dto.setDescription(event.getDescription());
        dto.setImageUrl(event.getImageUrl());
        dto.setSortOrder(event.getSortOrder());
        return dto;
    }
}
