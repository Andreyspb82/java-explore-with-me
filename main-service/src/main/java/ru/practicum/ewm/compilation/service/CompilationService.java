package ru.practicum.ewm.compilation.service;

import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.PathVariable;
import ru.practicum.ewm.compilation.dto.CompilationDto;
import ru.practicum.ewm.compilation.dto.NewCompilationDto;
import ru.practicum.ewm.compilation.dto.UpdateCompilationRequest;

import java.util.List;

public interface CompilationService {

    public CompilationDto createCompilation(NewCompilationDto newCompilationDto);

    public CompilationDto updateCompilation(long compId, UpdateCompilationRequest updateCompilation);

    public void removeCompilationById(long compId);

    public List<CompilationDto> getCompilations(Boolean pinned, PageRequest page);

    public CompilationDto getCompilationById (long compId);

}
