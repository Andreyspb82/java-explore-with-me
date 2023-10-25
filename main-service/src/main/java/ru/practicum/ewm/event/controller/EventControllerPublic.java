package ru.practicum.ewm.event.controller;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.ewm.client.stats.HitClient;
import ru.practicum.ewm.dto.stats.EndpointHitDto;
import ru.practicum.ewm.event.dto.EventFullDto;
import ru.practicum.ewm.event.dto.EventShortDto;
import ru.practicum.ewm.event.dto.SearchFilterPublic;
import ru.practicum.ewm.event.service.EventService;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.validation.constraints.Min;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Validated
@RestController
@Slf4j
@RequestMapping(path = "/events")
@AllArgsConstructor
@Valid
public class EventControllerPublic {

    public final EventService eventService;

    public final HitClient hitClient;

    private static final String FORMAT_TO_DATE = ("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<EventShortDto> getEventsPublic(@RequestParam(defaultValue = "") String text,
                                               @RequestParam(defaultValue = "") List<Long> categories,
                                               @RequestParam(defaultValue = "") Boolean paid,
                                               @RequestParam(defaultValue = "") @DateTimeFormat(pattern = FORMAT_TO_DATE) LocalDateTime rangeStart,
                                               @RequestParam(defaultValue = "") @DateTimeFormat(pattern = FORMAT_TO_DATE) LocalDateTime rangeEnd,
                                               @RequestParam(defaultValue = "false") Boolean onlyAvailable,
                                               @RequestParam(defaultValue = "") String sort,
                                               @Min(0) @RequestParam(defaultValue = "0") int from,
                                               @Min(0) @RequestParam(defaultValue = "10") int size,
                                               HttpServletRequest request) {

        SearchFilterPublic filterPublic = SearchFilterPublic.builder()
                .text(text)
                .categories(categories)
                .paid(paid)
                .rangeStart(rangeStart)
                .rangeEnd(rangeEnd)
                .onlyAvailable(onlyAvailable)
                .sort(sort)
                .from(from)
                .size(size)
                .build();

        EndpointHitDto endpointHitDto = EndpointHitDto.builder()
                .app("ewm-main-service")
                .ip(request.getRemoteAddr())
                .uri(request.getRequestURI())
                .timestamp(LocalDateTime.now().format(FORMAT))
                .build();

        log.info("Get List<EventShortDto> with {}, from={}, size={}", filterPublic, from, size);
        hitClient.createEndpointHit(endpointHitDto);
        return eventService.getEventsPublic(filterPublic);
    }

    @GetMapping("{id}")
    @ResponseStatus(HttpStatus.OK)
    public EventFullDto getEventByIdPublic(@PathVariable long id,
                                           HttpServletRequest request) {

        EndpointHitDto endpointHitDto = EndpointHitDto.builder()
                .app("ewm-main-service")
                .ip(request.getRemoteAddr())
                .uri(request.getRequestURI())
                .timestamp(LocalDateTime.now().format(FORMAT))
                .build();

        log.info("Get Event with id={}", id);
        hitClient.createEndpointHit(endpointHitDto);
        return eventService.getEventByIdPublic(id);
    }
}
