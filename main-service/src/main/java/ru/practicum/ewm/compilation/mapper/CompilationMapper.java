package ru.practicum.ewm.compilation.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.practicum.ewm.compilation.dto.CompilationDto;
import ru.practicum.ewm.compilation.dto.NewCompilationDto;
import ru.practicum.ewm.compilation.dto.UpdateCompilationRequest;
import ru.practicum.ewm.compilation.model.Compilation;
import ru.practicum.ewm.event.dto.EventShortDto;

import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CompilationMapper {

    public static Compilation mapToCompilationNew (NewCompilationDto newCompilationDto) {

        return Compilation.builder()
                .pinned(newCompilationDto.getPinned() != null && newCompilationDto.getPinned())
                .title(newCompilationDto.getTitle())
                .build();
    }

    public static Compilation mapToCompilationUpdate (UpdateCompilationRequest updateCompilation,
                                                      Compilation compilation) {

        return Compilation.builder()
                .id(compilation.getId())
                .pinned(updateCompilation.getPinned() != null ? updateCompilation.getPinned() : compilation.getPinned())
                .title(updateCompilation.getTitle() !=null ? updateCompilation.getTitle() : compilation.getTitle())
                .build();
    }


    public static CompilationDto mapToCompilationDto (Compilation compilation, List<EventShortDto> eventShorstDto) {

        return CompilationDto.builder()
                .id(compilation.getId())
                .events(eventShorstDto)
                .pinned(compilation.getPinned())
                .title(compilation.getTitle())
                .build();
    }
}
