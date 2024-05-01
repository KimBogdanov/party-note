package ru.kim.partynote.category.service;

import ru.kim.partynote.category.dto.CategoryDto;
import ru.kim.partynote.category.dto.CategoryShortDto;
import ru.kim.partynote.category.dto.CategoryUpdateDto;

import javax.validation.Valid;
import java.util.List;

public interface CategoryService {
    CategoryDto saveCategory(CategoryShortDto categoryShortDto);

    CategoryDto patchCategory(Long categoryId, @Valid CategoryUpdateDto categoryShortDto);

    void deleteCategory(Long categoryId);

    CategoryDto getCategoryById(Long categoryId);

    List<CategoryDto> getAllCategories(Integer from, Integer size);
}
