package com.springboot.backendserver.dto;

import com.springboot.backendserver.entity.Category;
import lombok.Data;

@Data
public class CategoryDto {

    private Long id;
    private String slug;
    private String name;
    private String description;
    private Integer sortOrder;

    public static CategoryDto from(Category c) {
        CategoryDto dto = new CategoryDto();
        dto.setId(c.getId());
        dto.setSlug(c.getSlug());
        dto.setName(c.getName());
        dto.setDescription(c.getDescription());
        dto.setSortOrder(c.getSortOrder());
        return dto;
    }
}
