package com.springboot.backendserver.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageResult<T> {

    private List<T> content;
    private long totalElements;
    private int totalPages;
    private int page;
    private int size;

    public static <T> PageResult<T> of(List<T> content, long totalElements, int totalPages, int page, int size) {
        return new PageResult<>(content, totalElements, totalPages, page, size);
    }
}
