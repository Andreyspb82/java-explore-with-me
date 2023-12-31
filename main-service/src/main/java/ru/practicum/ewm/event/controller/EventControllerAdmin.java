package ru.practicum.ewm.event.controller;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.ewm.event.dto.EventFullDto;
import ru.practicum.ewm.event.dto.SearchFilterAdmin;
import ru.practicum.ewm.event.dto.UpdateEventAdminRequest;
import ru.practicum.ewm.event.service.EventService;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import java.time.LocalDateTime;
import java.util.List;

@Validated
@RestController
@Slf4j
@RequestMapping(path = "/admin/events")
@AllArgsConstructor
public class EventControllerAdmin {

    private static final String FORMAT_TO_DATE = ("yyyy-MM-dd HH:mm:ss");

    public final EventService eventService;

    @PatchMapping("/{eventId}")
    public EventFullDto updateEventByAdmin(@PathVariable long eventId,
                                           @Valid @RequestBody UpdateEventAdminRequest adminRequest) {

        log.info("Update Event by admin with eventId={}, UpdateEventAdminRequest={}", eventId, adminRequest);
        return eventService.updateEventByAdmin(eventId, adminRequest);
    }

    @GetMapping
    public List<EventFullDto> getEventsByAdmin(@RequestParam(defaultValue = "") List<Long> users,
                                               @RequestParam(defaultValue = "") List<String> states,
                                               @RequestParam(defaultValue = "") List<Long> categories,
                                               @RequestParam(defaultValue = "") @DateTimeFormat(pattern = FORMAT_TO_DATE) LocalDateTime rangeStart,
                                               @RequestParam(defaultValue = "") @DateTimeFormat(pattern = FORMAT_TO_DATE) LocalDateTime rangeEnd,
                                               @Min(0) @RequestParam(defaultValue = "0") int from,
                                               @Min(0) @RequestParam(defaultValue = "10") int size) {

        PageRequest page = PageRequest.of(from / size, size);

        SearchFilterAdmin filterAdmin = SearchFilterAdmin.builder()
                .users(users)
                .states(states)
                .categories(categories)
                .rangeStart(rangeStart)
                .rangeEnd(rangeEnd)
                .build();
        log.info("Get List<EventFullDto> by admin with {}, from={}, size={}", filterAdmin, from, size);
        return eventService.getEventsByAdmin(filterAdmin, page);
    }
}
