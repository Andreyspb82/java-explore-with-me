package ru.practicum.ewm.category.service;

import org.springframework.data.domain.PageRequest;
import ru.practicum.ewm.category.dto.CategoryDto;
import ru.practicum.ewm.category.dto.NewCategoryDto;
import ru.practicum.ewm.category.model.Category;

import java.util.List;

public interface CategoryService {

    public CategoryDto createCategory(NewCategoryDto newCategory);

    public void removeCategoryById(long catId);

    public CategoryDto updateCategory(long catId, CategoryDto categoryDto);

    public List<CategoryDto> getCategories (PageRequest page);

    public CategoryDto getCategoryDtoById(long catId);

    public Category getCategoryById (long catId);

}
