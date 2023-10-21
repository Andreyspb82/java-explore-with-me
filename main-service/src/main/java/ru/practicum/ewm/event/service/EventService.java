package ru.practicum.ewm.event.service;

import org.springframework.data.domain.PageRequest;
import ru.practicum.ewm.event.dto.EventFullDto;
import ru.practicum.ewm.event.dto.SearchFilterAdmin;
import ru.practicum.ewm.event.dto.SearchFilterPublic;
import ru.practicum.ewm.request.dto.EventRequestStatusUpdateRequest;
import ru.practicum.ewm.request.dto.EventRequestStatusUpdateResult;
import ru.practicum.ewm.event.dto.EventShortDto;
import ru.practicum.ewm.event.dto.NewEventDto;
import ru.practicum.ewm.event.dto.UpdateEventAdminRequest;
import ru.practicum.ewm.event.dto.UpdateEventUserRequest;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.request.dto.ParticipationRequestDto;

import java.time.LocalDateTime;
import java.util.List;

public interface EventService {

    //private
    public EventFullDto createEvent(long userId, NewEventDto newEvent);

    public EventFullDto updateEventByUserId(long userId, long eventId, UpdateEventUserRequest updateEventUserRequest);

    public EventRequestStatusUpdateResult updateStatusRequestForEvent(long userId, long eventId, EventRequestStatusUpdateRequest eventRequestStatusUpdateRequest);


    public List<EventShortDto> getEventsByUserId(long userId, PageRequest page);

    public EventFullDto getEventByOwner(long userId, long eventId);

    public List<ParticipationRequestDto> getRequestsByOwner(long userId, long eventId);


    //admin
    public EventFullDto updateEventByAdmin(long eventId, UpdateEventAdminRequest updateEventAdminRequest);


    public List<EventFullDto> getEventsByAdmin(SearchFilterAdmin filterAdmin, PageRequest page);


    //public

    public List<EventShortDto> getEventsPublic(SearchFilterPublic filterPublic, PageRequest page);

    public EventFullDto getEventByIdPublic(long id);


    //для сервиса запросов
    public Event getEventByIdForService(long eventId);


    //test


    public EventFullDto getListTest();

}
