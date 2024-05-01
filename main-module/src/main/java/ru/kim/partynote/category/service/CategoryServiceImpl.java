package ru.kim.partynote.category.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.kim.partynote.category.dto.CategoryDto;
import ru.kim.partynote.category.dto.CategoryShortDto;
import ru.kim.partynote.category.dto.CategoryUpdateDto;
import ru.kim.partynote.category.mapper.CategoryDtoMapper;
import ru.kim.partynote.category.mapper.CategoryShortDtoMapper;
import ru.kim.partynote.category.model.Category;
import ru.kim.partynote.category.repository.CategoryRepository;
import ru.kim.partynote.exception.NotFoundException;
import ru.kim.partynote.util.PageRequestFrom;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository categoryRepository;
    private final CategoryShortDtoMapper categoryShortDtoMapper;
    private final CategoryDtoMapper categoryDtoMapper;

    @Override
    @Transactional
    public CategoryDto saveCategory(CategoryShortDto categoryShortDto) {
        return Optional.of(categoryShortDto)
                .map(categoryShortDtoMapper::toCategory)
                .map(categoryRepository::save)
                .map(categoryDtoMapper::toCategory)
                .get();
    }

    @Override
    @Transactional
    public CategoryDto patchCategory(Long categoryId, CategoryUpdateDto categoryUpdateDto) {
        Category category = getCategoryOrThrowException(categoryId);
        if (categoryUpdateDto.getName() != null) {
            category.setName(categoryUpdateDto.getName());
        }
        return categoryDtoMapper.toCategory(category);
    }

    private Category getCategoryOrThrowException(Long categoryId) {
        return categoryRepository.findById(categoryId).orElseThrow(() ->
                new NotFoundException(String.format("Category with id=%d was not found", categoryId))
        );
    }

    @Override
    @Transactional
    public void deleteCategory(Long categoryId) {
        if (!categoryRepository.existsById(categoryId)) {
            throw new NotFoundException(String.format("Category with id=%d was not found", categoryId));
        }
        categoryRepository.deleteById(categoryId);
    }

    @Override
    public CategoryDto getCategoryById(Long categoryId) {
        return categoryRepository.findById(categoryId)
                .map(categoryDtoMapper::toCategory)
                .orElseThrow(() -> new NotFoundException(String.format("Category with id=%d was not found", categoryId)));
    }

    @Override
    public List<CategoryDto> getAllCategories(Integer from, Integer size) {
        return categoryRepository.findAll(new PageRequestFrom(from, size, null))
                .getContent()
                .stream()
                .map(categoryDtoMapper::toCategory)
                .collect(Collectors.toList());
    }
}
