package ru.practicum.ewm.event.controller;

import lombok.AllArgsConstructor;
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
@RequestMapping(path = "/users/{userId}/events")
@AllArgsConstructor
@Valid
public class EventControllerPrivate {

    public final EventService eventService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EventFullDto createEvent(@PathVariable long userId,
                                    @Valid @RequestBody NewEventDto newEvent) {
        return eventService.createEvent(userId, newEvent);
    }

    @PatchMapping("/{eventId}")
    @ResponseStatus(HttpStatus.OK)
    public EventFullDto updateEventByUserId(@PathVariable long userId,
                                            @PathVariable long eventId,
                                            @Valid @RequestBody UpdateEventUserRequest updateEventUserRequest) {


        return eventService.updateEventByUserId(userId, eventId, updateEventUserRequest);
    }

    @PatchMapping("/{eventId}/requests")
    @ResponseStatus(HttpStatus.OK)
    public EventRequestStatusUpdateResult updateStatusRequestForEvent(@PathVariable long userId,
                                                                      @PathVariable long eventId,
                                                                      @Valid @RequestBody EventRequestStatusUpdateRequest
                                                                              eventRequestStatusUpdateRequest) {


        System.out.println("userId1 контр " + userId);
        System.out.println("eventId контр" + eventId);
        System.out.println("eventRequestStatusUpdateRequest контр " + eventRequestStatusUpdateRequest);

        return eventService.updateStatusRequestForEvent(userId, eventId, eventRequestStatusUpdateRequest);
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<EventShortDto> getEventsByUserId(@PathVariable long userId,
                                                 @Min(0) @RequestParam(defaultValue = "0") int from,
                                                 @Min(0) @RequestParam(defaultValue = "10") int size) {

        PageRequest page = PageRequest.of(from / size, size);

        return eventService.getEventsByUserId(userId, page);
    }

    @GetMapping("/{eventId}")
    @ResponseStatus(HttpStatus.OK)
    public EventFullDto getEventByOwner(@PathVariable long userId,
                                        @PathVariable long eventId) {

        return eventService.getEventByOwner(userId, eventId);
    }

    @GetMapping("/{eventId}/requests")
    @ResponseStatus(HttpStatus.OK)
    public List<ParticipationRequestDto> getRequestsByOwner(@PathVariable long userId,
                                                            @PathVariable long eventId) {

        return eventService.getRequestsByOwner(userId, eventId);
    }


}
