package ru.kim.partynote.category.mapper;

import org.mapstruct.Mapper;
import ru.kim.partynote.category.dto.CategoryDto;
import ru.kim.partynote.category.model.Category;

@Mapper(componentModel = "spring")
public interface CategoryDtoMapper {
    CategoryDto toCategory(Category category);
}
