package ru.practicum.ewm.compilation.service;


import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.compilation.dto.CompilationDto;
import ru.practicum.ewm.compilation.dto.NewCompilationDto;
import ru.practicum.ewm.compilation.dto.UpdateCompilationRequest;
import ru.practicum.ewm.compilation.mapper.CompilationMapper;
import ru.practicum.ewm.compilation.model.Compilation;
import ru.practicum.ewm.compilation.repository.CompilationRepository;
import ru.practicum.ewm.event.dto.EventShortDto;
import ru.practicum.ewm.event.mapper.EventMapper;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.event.repository.EventRepository;
import ru.practicum.ewm.exception.NotFoundException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@Data
public class CompilationServiceImpl implements CompilationService {

    public final CompilationRepository compilationRepository;

    public final EventRepository eventRepository;

    @Override
    public CompilationDto createCompilation (NewCompilationDto newCompilationDto){

        List<Event> events = eventRepository.findAllById(newCompilationDto.getEvents());

        Compilation compilation = CompilationMapper.mapToCompilationNew(newCompilationDto);
        compilation.setEvents(events);

        Compilation compilationSave = compilationRepository.save(compilation);
        List<EventShortDto> eventShort = EventMapper.mapToEventsShortDto(compilationSave.getEvents());

        return CompilationMapper.mapToCompilationDto(compilationSave, eventShort);
    }

    @Override
    public CompilationDto updateCompilation(long compId, UpdateCompilationRequest updateCompilation){

        if(!compilationRepository.existsById(compId)) {
            throw new NotFoundException("Compilation with Id =" + compId + " does not exist");
        }

        Compilation compilation = compilationRepository.findById(compId).get();

        List<Event> events = eventRepository.findAllById(updateCompilation.getEvents());

        Compilation compilationUpdate = CompilationMapper.mapToCompilationUpdate(updateCompilation, compilation);
        compilationUpdate.setEvents(events);

        Compilation compilationSaveUpdate = compilationRepository.save(compilationUpdate);
        List<EventShortDto> eventShort = EventMapper.mapToEventsShortDto(compilationSaveUpdate.getEvents());

        return CompilationMapper.mapToCompilationDto(compilationSaveUpdate, eventShort);
    }

    @Override
    public void removeCompilationById(long compId){
        if(!compilationRepository.existsById(compId)) {
            throw new NotFoundException("Compilation with Id =" + compId + " does not exist");
        }
        compilationRepository.deleteById(compId);
    }

    @Override
    public List<CompilationDto> getCompilations(Boolean pinned, PageRequest page){
        List<Compilation> compilations = compilationRepository.findByPinned(pinned, page);

      //  List<Event> events = new ArrayList<>();
        List<CompilationDto> compilationDtos = new ArrayList<>();
        for(Compilation compilation : compilations) {
            List<EventShortDto> eventShortDtos = EventMapper.mapToEventsShortDto(compilation.getEvents());
            CompilationDto compilationDto = CompilationMapper.mapToCompilationDto(compilation, eventShortDtos);
            compilationDtos.add(compilationDto);
        }
        return compilationDtos;
    }

    @Override
    public CompilationDto getCompilationById (long compId){
//
        if(!compilationRepository.existsById(compId)) {
            throw new NotFoundException("Compilation with Id =" + compId + " does not exist");
        }

        Compilation compilation = compilationRepository.findById(compId).get();
        List<EventShortDto> eventShort = EventMapper.mapToEventsShortDto(compilation.getEvents());

        return CompilationMapper.mapToCompilationDto(compilation, eventShort);
    }

}
