package ru.kim.partynote.category.dto;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class CategoryDto {
    private final Long id;
    private final String name;
}
