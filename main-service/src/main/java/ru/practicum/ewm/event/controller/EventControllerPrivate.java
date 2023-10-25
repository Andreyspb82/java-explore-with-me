package ru.practicum.ewm.event.controller;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.ewm.event.dto.EventFullDto;
import ru.practicum.ewm.event.dto.EventShortDto;
import ru.practicum.ewm.event.dto.NewEventDto;
import ru.practicum.ewm.event.dto.UpdateEventUserRequest;
import ru.practicum.ewm.event.service.EventService;
import ru.practicum.ewm.request.dto.EventRequestStatusUpdateRequest;
import ru.practicum.ewm.request.dto.EventRequestStatusUpdateResult;
import ru.practicum.ewm.request.dto.ParticipationRequestDto;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import java.util.List;

@Validated
@RestController
@Slf4j
@RequestMapping(path = "/users/{userId}/events")
@AllArgsConstructor
@Valid
public class EventControllerPrivate {

    public final EventService eventService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EventFullDto createEvent(@PathVariable long userId,
                                    @Valid @RequestBody NewEventDto newEvent) {

        log.info("Creating Event with {}, userId={}", newEvent, userId);
        return eventService.createEvent(userId, newEvent);
    }

    @PatchMapping("/{eventId}")
    @ResponseStatus(HttpStatus.OK)
    public EventFullDto updateEventByOwnerId(@PathVariable long userId,
                                             @PathVariable long eventId,
                                             @Valid @RequestBody UpdateEventUserRequest updateEventUserRequest) {

        log.info("Update Event by Owner with eventId={}, ownerId={}, UpdateEventUserRequest={}", eventId, userId, updateEventUserRequest);
        return eventService.updateEventByOwnerId(userId, eventId, updateEventUserRequest);
    }

    @PatchMapping("/{eventId}/requests")
    @ResponseStatus(HttpStatus.OK)
    public EventRequestStatusUpdateResult updateStatusRequestByOwnerId(@PathVariable long userId,
                                                                       @PathVariable long eventId,
                                                                       @Valid @RequestBody EventRequestStatusUpdateRequest
                                                                               eventRequestStatusUpdateRequest) {
        log.info("Update Event Request Status by Owner with eventId={}, ownerId={}, requestIds={}, status={}", eventId,
                userId, eventRequestStatusUpdateRequest.getRequestIds(), eventRequestStatusUpdateRequest.getStatus());
        return eventService.updateStatusRequestByOwnerId(userId, eventId, eventRequestStatusUpdateRequest);
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<EventShortDto> getEventsByOwnerId(@PathVariable long userId,
                                                  @Min(0) @RequestParam(defaultValue = "0") int from,
                                                  @Min(0) @RequestParam(defaultValue = "10") int size) {

        PageRequest page = PageRequest.of(from / size, size);
        log.info("Get List<EventShortDto> by Owner with id={}, from={}, size={}", userId, from, size);
        return eventService.getEventsByOwnerId(userId, page);
    }

    @GetMapping("/{eventId}")
    @ResponseStatus(HttpStatus.OK)
    public EventFullDto getEventByOwnerId(@PathVariable long userId,
                                          @PathVariable long eventId) {

        log.info("Get Event by Owner with ownerId={}, eventId={}", userId, eventId);
        return eventService.getEventByOwnerId(userId, eventId);
    }

    @GetMapping("/{eventId}/requests")
    @ResponseStatus(HttpStatus.OK)
    public List<ParticipationRequestDto> getRequestsByOwnerId(@PathVariable long userId,
                                                              @PathVariable long eventId) {

        log.info("Get List<ParticipationRequestDto> by Owner with ownerId={}, eventId={}", userId, eventId);
        return eventService.getRequestsByOwnerId(userId, eventId);
    }
}
