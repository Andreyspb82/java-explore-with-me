package ru.practicum.ewm.request.controller;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.ewm.request.dto.ParticipationRequestDto;
import ru.practicum.ewm.request.service.ParticipationRequestService;

import java.util.List;

@Validated
@RestController
@Slf4j
@RequestMapping(path = "/users/{userId}/requests")
@AllArgsConstructor
public class RequestControllerPrivate {

    public final ParticipationRequestService requestService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ParticipationRequestDto createRequest(@PathVariable long userId,
                                                 @RequestParam long eventId) {

        log.info("Creating ParticipationRequest with userId={}, eventId={}", userId, eventId);
        return requestService.createRequest(userId, eventId);
    }

    @PatchMapping("/{requestId}/cancel")
    @ResponseStatus(HttpStatus.OK)
    public ParticipationRequestDto cancelRequestByOwner(@PathVariable long userId,
                                                        @PathVariable long requestId) {
        log.info("Cancel ParticipationRequest with userId={}, requestId={}", userId, requestId);
        return requestService.cancelRequestByOwner(requestId, userId);
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<ParticipationRequestDto> getRequestsByUserId(@PathVariable long userId) {

        log.info("Get List<ParticipationRequestDto> with userId={}", userId);
        return requestService.getRequestsByUserId(userId);
    }
}
