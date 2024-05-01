package ru.kim.partynote.category.mapper;

import org.mapstruct.Mapper;
import ru.kim.partynote.category.dto.CategoryShortDto;
import ru.kim.partynote.category.model.Category;

@Mapper(componentModel = "spring")
public interface CategoryShortDtoMapper {
    Category toCategory(CategoryShortDto categoryShortDto);
}
