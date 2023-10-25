package ru.practicum.ewm.category.service;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.category.dto.CategoryDto;
import ru.practicum.ewm.category.dto.NewCategoryDto;
import ru.practicum.ewm.category.mapper.CategoryMapper;
import ru.practicum.ewm.category.model.Category;
import ru.practicum.ewm.category.repository.CategoryRepository;
import ru.practicum.ewm.exception.NotFoundException;

import java.util.List;

@Service
@Slf4j
@Data
public class CategoryServiceImpl implements CategoryService {

    public final CategoryRepository categoryRepository;

    @Override
    public CategoryDto createCategory(NewCategoryDto newCategory) {
        return CategoryMapper.mapToCategoryDto(categoryRepository.save(CategoryMapper.mapToCategory(newCategory)));
    }

    @Override
    public void removeCategoryById(long catId) {
        categoryRepository.deleteById(catId);
    }

    @Override
    public CategoryDto updateCategory(long catId, CategoryDto categoryDto) {

        if (!categoryRepository.existsById(catId)) {
            throw new NotFoundException("Category with Id =" + catId + " does not exist");
        }

        Category oldCat = categoryRepository.findById(catId).get();
        oldCat.setName(categoryDto.getName());
        return CategoryMapper.mapToCategoryDto(categoryRepository.save(oldCat));
    }

    @Override
    public List<CategoryDto> getCategories(PageRequest page) {
        return CategoryMapper.mapToCategoriesDto(categoryRepository.findAll(page));
    }

    @Override
    public CategoryDto getCategoryDtoById(long catId) {

        if (!categoryRepository.existsById(catId)) {
            throw new NotFoundException("Category with Id =" + catId + " does not exist");
        }
        return CategoryMapper.mapToCategoryDto(categoryRepository.findById(catId).get());
    }

    @Override
    public Category getCategoryByIdForService(long catId) {

        if (!categoryRepository.existsById(catId)) {
            throw new NotFoundException("Category with Id =" + catId + " does not exist");
        }
        return categoryRepository.findById(catId).get();
    }
}
