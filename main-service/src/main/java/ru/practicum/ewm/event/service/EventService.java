package ru.practicum.ewm.event.service;

import org.springframework.data.domain.PageRequest;
import ru.practicum.ewm.event.dto.EventFullDto;
import ru.practicum.ewm.event.dto.EventShortDto;
import ru.practicum.ewm.event.dto.NewEventDto;
import ru.practicum.ewm.event.dto.SearchFilterAdmin;
import ru.practicum.ewm.event.dto.SearchFilterPublic;
import ru.practicum.ewm.event.dto.UpdateEventAdminRequest;
import ru.practicum.ewm.event.dto.UpdateEventUserRequest;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.request.dto.EventRequestStatusUpdateRequest;
import ru.practicum.ewm.request.dto.EventRequestStatusUpdateResult;
import ru.practicum.ewm.request.dto.ParticipationRequestDto;

import java.util.List;

public interface EventService {

    public EventFullDto createEvent(long userId, NewEventDto newEvent);

    public EventFullDto updateEventByOwnerId(long userId, long eventId, UpdateEventUserRequest updateEventUserRequest);

    public EventRequestStatusUpdateResult updateStatusRequestByOwnerId(long userId,
                                                                       long eventId, EventRequestStatusUpdateRequest eventRequestStatusUpdateRequest);

    public List<EventShortDto> getEventsByOwnerId(long userId, PageRequest page);

    public EventFullDto getEventByOwnerId(long userId, long eventId);

    public List<ParticipationRequestDto> getRequestsByOwnerId(long userId, long eventId);

    public EventFullDto updateEventByAdmin(long eventId, UpdateEventAdminRequest updateEventAdminRequest);

    public List<EventFullDto> getEventsByAdmin(SearchFilterAdmin filterAdmin, PageRequest page);

    public List<EventShortDto> getEventsPublic(SearchFilterPublic filterPublic);

    public EventFullDto getEventByIdPublic(long id);

    public Event getEventByIdForService(long eventId);
}
