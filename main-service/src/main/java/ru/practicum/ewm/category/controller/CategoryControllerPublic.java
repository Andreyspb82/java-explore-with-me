package ru.practicum.ewm.category.controller;

import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.ewm.category.dto.CategoryDto;
import ru.practicum.ewm.category.service.CategoryService;

import javax.validation.constraints.Min;
import java.util.List;

@Validated
@RestController
@RequestMapping(path = "/categories")
@AllArgsConstructor
public class CategoryControllerPublic {

    public final CategoryService categoryService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<CategoryDto> getCategories (@Min(0) @RequestParam(defaultValue = "0") int from,
                                            @Min(0) @RequestParam(defaultValue = "10") int size) {

        PageRequest page = PageRequest.of(from / size, size);
        return categoryService.getCategories(page);
    }

    @GetMapping ("/{catId}")
    @ResponseStatus(HttpStatus.OK)
    public CategoryDto getCategoryById (@PathVariable long catId) {
        return categoryService.getCategoryDtoById(catId);


    }
}