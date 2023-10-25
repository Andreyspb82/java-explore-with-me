package ru.practicum.ewm.event.service;


import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.category.model.Category;
import ru.practicum.ewm.category.service.CategoryService;
import ru.practicum.ewm.client.stats.StatsClient;
import ru.practicum.ewm.dto.stats.ViewStats;
import ru.practicum.ewm.event.dto.EventFullDto;
import ru.practicum.ewm.event.dto.EventShortDto;
import ru.practicum.ewm.event.dto.NewEventDto;
import ru.practicum.ewm.event.dto.SearchFilterAdmin;
import ru.practicum.ewm.event.dto.SearchFilterPublic;
import ru.practicum.ewm.event.dto.UpdateEventAdminRequest;
import ru.practicum.ewm.event.dto.UpdateEventUserRequest;
import ru.practicum.ewm.event.enums.SortEvents;
import ru.practicum.ewm.event.enums.State;
import ru.practicum.ewm.event.mapper.EventMapper;
import ru.practicum.ewm.event.mapper.LocationMapper;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.event.model.LocationModel;
import ru.practicum.ewm.event.repository.EventRepository;
import ru.practicum.ewm.event.repository.LocationRepository;
import ru.practicum.ewm.exception.BadRequestException;
import ru.practicum.ewm.exception.ConflictException;
import ru.practicum.ewm.exception.NotFoundException;
import ru.practicum.ewm.request.dto.EventRequestStatusUpdateRequest;
import ru.practicum.ewm.request.dto.EventRequestStatusUpdateResult;
import ru.practicum.ewm.request.dto.ParticipationRequestDto;
import ru.practicum.ewm.request.enums.Status;
import ru.practicum.ewm.request.mapper.ParticipationRequestMapper;
import ru.practicum.ewm.request.model.ParticipationRequest;
import ru.practicum.ewm.request.repository.ParticipationRequestRepository;
import ru.practicum.ewm.user.model.User;
import ru.practicum.ewm.user.service.UserService;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
@Data
public class EventServiceImpl implements EventService {

    public final EventRepository eventRepository;

    public final CategoryService categoryService;

    public final UserService userService;

    public final LocationRepository locationRepository;

    public final ParticipationRequestRepository participationRequestRepository;

    public final StatsClient statsClient;

    private static final DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public EventFullDto createEvent(long userId, NewEventDto newEvent) {

        eventDateValidation(LocalDateTime.parse(newEvent.getEventDate(), FORMAT), 2);

        LocationModel locationModel = locationRepository.save(LocationMapper.mapToLocationModel(newEvent.getLocation()));
        Category category = categoryService.getCategoryByIdForService(newEvent.getCategory());
        User user = userService.getUserByIdForService(userId);

        Event event = EventMapper.mapToEventNew(newEvent, category, user);
        event.setLocationModel(locationModel);
        event.setState(State.PENDING);

        return EventMapper.mapToEventFullDto(eventRepository.save(event));
    }

    @Override
    public EventFullDto updateEventByOwnerId(long userId, long eventId, UpdateEventUserRequest updateEventUserRequest) {

        if (updateEventUserRequest.getEventDate() != null) {
            eventDateValidation(LocalDateTime.parse(updateEventUserRequest.getEventDate(), FORMAT), 2);
        }
        Event eventOld = getEventByIdForService(eventId);
        if (userId != eventOld.getInitiator().getId()) {
            throw new ConflictException("Event not created by this user");
        }
        if (eventOld.getState().equals(State.PUBLISHED)) {
            throw new ConflictException("Event published");
        }
        Event eventUpdate = eventUpdateByUserAndByAdmin(eventOld, updateEventUserRequest);

        return EventMapper.mapToEventFullDto(eventRepository.save(eventUpdate));
    }

    @Override
    public EventRequestStatusUpdateResult updateStatusRequestByOwnerId(long userId, long eventId, EventRequestStatusUpdateRequest updateRequest) {
        Event event = getEventByIdForService(eventId);

        if (userId != event.getInitiator().getId()) {
            throw new ConflictException("Event not created by this user");
        }
        long participantLimit = event.getParticipantLimit();
        long confirmedRequest = event.getConfirmedRequests();

        if (participantLimit > 0 && confirmedRequest == participantLimit) {
            throw new ConflictException("Event participant limit reached");
        }
        if (updateRequest.getStatus().equals(String.valueOf(Status.REJECTED))) {
            updateStatusByIds(eventId, updateRequest.getRequestIds(), Status.REJECTED);
        }
        if (participantLimit == 0) {
            updateStatusByIds(eventId, updateRequest.getRequestIds(), Status.CONFIRMED);
        }
        if (participantLimit > 0) {
            List<ParticipationRequest> requestsPendingForUpdate = participationRequestRepository.findByEventIdAndStatusAndId(eventId,
                    String.valueOf(Status.PENDING), updateRequest.getRequestIds());
            List<ParticipationRequest> requestForRejected = new ArrayList<>();

            for (ParticipationRequest request : requestsPendingForUpdate) {
                if (confirmedRequest < participantLimit) {
                    participationRequestRepository.save(updateStatus(request, Status.CONFIRMED));
                    requestForRejected.remove(request);
                    confirmedRequest++;
                }
            }
            if (confirmedRequest == participantLimit) {
                for (ParticipationRequest request : requestForRejected) {
                    participationRequestRepository.save(updateStatus(request, Status.REJECTED));
                }
            }
        }
        List<ParticipationRequest> confirmRequests = participationRequestRepository.findByEventIdAndStatusAndId(eventId,
                String.valueOf(Status.CONFIRMED), updateRequest.getRequestIds());
        List<ParticipationRequest> rejectedRequests = participationRequestRepository.findByEventIdAndStatusAndId(eventId,
                String.valueOf(Status.REJECTED), updateRequest.getRequestIds());

        event.setConfirmedRequests(confirmedRequest);
        eventRepository.save(event);

        return EventRequestStatusUpdateResult.builder()
                .confirmedRequests(ParticipationRequestMapper.mapToParticipationRequestsDto(confirmRequests))
                .rejectedRequests(ParticipationRequestMapper.mapToParticipationRequestsDto(rejectedRequests))
                .build();
    }

    private void updateStatusByIds(long eventId, List<Long> ids, Status status) {
        List<ParticipationRequest> requestsPendingForUpdate = participationRequestRepository.findByEventIdAndStatusAndId(eventId,
                String.valueOf(Status.PENDING), ids);
        for (ParticipationRequest request : requestsPendingForUpdate) {
            participationRequestRepository.save(updateStatus(request, status));
        }
    }

    private ParticipationRequest updateStatus(ParticipationRequest request, Status status) {
        request.setStatus(status);
        return request;
    }

    @Override
    public List<EventShortDto> getEventsByOwnerId(long userId, PageRequest page) {

        List<Event> events = eventRepository.findByInitiatorId(userId, page);
        return EventMapper.mapToEventsShortDto(events);
    }

    @Override
    public EventFullDto getEventByOwnerId(long userId, long eventId) {

        Event event = eventRepository.findByInitiatorIdAndId(userId, eventId);
        return EventMapper.mapToEventFullDto(event);
    }

    @Override
    public List<ParticipationRequestDto> getRequestsByOwnerId(long userId, long eventId) {

        Event event = getEventByIdForService(eventId);
        if (userId != event.getInitiator().getId()) {
            throw new BadRequestException("Event not created by this user");
        }
        List<ParticipationRequest> requests = participationRequestRepository.findByEventId(eventId);
        return ParticipationRequestMapper.mapToParticipationRequestsDto(requests);
    }

    @Override
    public EventFullDto updateEventByAdmin(long eventId, UpdateEventAdminRequest updateEventAdminRequest) {

        if (updateEventAdminRequest.getEventDate() != null) {
            eventDateValidation(LocalDateTime.parse(updateEventAdminRequest.getEventDate(), FORMAT), 1);
        }
        Event eventOld = getEventByIdForService(eventId);

        if (!eventOld.getState().equals(State.PENDING)) {
            throw new ConflictException("Event not in pending status");
        }
        Event eventUpdate = eventUpdateByUserAndByAdmin(eventOld, updateEventAdminRequest);

        if (updateEventAdminRequest.getEventDate() == null) {
            eventDateValidation(eventUpdate.getEventDate(), 1);
        }
        return EventMapper.mapToEventFullDto(eventRepository.save(eventUpdate));
    }

    @Override
    public List<EventFullDto> getEventsByAdmin(SearchFilterAdmin filterAdmin, PageRequest page) {

        System.out.println("filterAdmin = " + filterAdmin);
        List<Specification<Event>> specifications = searchFilterAdminToSpecification(filterAdmin);

        if (specifications.isEmpty()) {
            Page<Event> events = eventRepository.findAll(page);
            return EventMapper.mapToEventsFullDto(events.getContent());
        } else {
            Page<Event> events = eventRepository.findAll(specifications.stream().reduce(Specification::and).orElseThrow(() -> new BadRequestException("Bad request")), page);
            return EventMapper.mapToEventsFullDto(events.getContent());
        }
    }

    @Override
    public List<EventShortDto> getEventsPublic(SearchFilterPublic filterPublic) {

        List<Specification<Event>> specifications = searchFilterPublicToSpecification(filterPublic);
        List<Event> result = new ArrayList<>();
        int from = filterPublic.getFrom();
        int size = filterPublic.getSize();

        if (filterPublic.getSort().isEmpty()) {
            PageRequest page = PageRequest.of(from / size, size);
            Page<Event> eventsPage = eventRepository.findAll((queryAnnotation(filterPublic.getText()))
                    .or(queryDescription(filterPublic.getText()))
                    .or(specifications.stream().reduce(Specification::and).get()), page);
            result = eventsPage.getContent();
        } else {
            String sorts = (filterPublic.getSort().equals(String.valueOf(SortEvents.EVENT_DATE)) ? "eventDate" : "views");
            Pageable sorted = PageRequest.of(from / size, size, Sort.by(sorts));

            Page<Event> eventsPage = eventRepository.findAll((queryAnnotation(filterPublic.getText()))
                    .or(queryDescription(filterPublic.getText()))
                    .or(specifications.stream().reduce(Specification::and).get()), sorted);
            result = eventsPage.getContent();
        }
        if (result.isEmpty()) {
            throw new BadRequestException("The request was formed incorrectly");
        } else {
            return EventMapper.mapToEventsShortDto(result);
        }
    }

    @Override
    public EventFullDto getEventByIdPublic(long eventId) {

        Optional<Event> event = Optional.ofNullable(eventRepository.findByIdAndState(eventId, String.valueOf(State.PUBLISHED)));

        if (event.isEmpty()) {
            throw new NotFoundException("Event with Id =" + eventId + " not found or not available");
        }
        Map<String, Object> parameters = Map.of(
                "start", LocalDateTime.now().minusYears(100).format(FORMAT),
                "end", LocalDateTime.now().plusHours(1).format(FORMAT),
                "uris", "/events/" + eventId,
                "unique", true);
        Optional<List<ViewStats>> viewStats = Optional.ofNullable(statsClient.getViewStats(parameters));

        if (viewStats.isPresent()) {
            long hits = viewStats.get().get(0).getHits();
            event.get().setViews(hits);
        }
        return EventMapper.mapToEventFullDto(eventRepository.save(event.get()));
    }

    @Override
    public Event getEventByIdForService(long eventId) {
        if (!eventRepository.existsById(eventId)) {
            throw new NotFoundException("Event with Id =" + eventId + " does not exist");
        }
        return eventRepository.findById(eventId).get();
    }

    private void eventDateValidation(LocalDateTime eventDate, int laterHours) {
        LocalDateTime nowDate = LocalDateTime.now();
        Duration duration = Duration.between(nowDate, eventDate);

        if (nowDate.isAfter(eventDate)) {
            throw new BadRequestException("Event starts in the past");
        }
        if (duration.toHours() < laterHours) {
            throw new ConflictException("Event starts earlier than " + laterHours + " hours");
        }
    }

    private Event eventUpdateByUserAndByAdmin(Event eventOld, UpdateEventUserRequest updateEvent) {

        if (updateEvent.getCategory() != null) {
            Category categoryUpdate = categoryService.getCategoryByIdForService(updateEvent.getCategory());
            eventOld.setCategory(categoryUpdate);
        }
        if (updateEvent.getLocation() != null) {
            LocationModel locationModelUpdate = locationRepository.save(LocationMapper.mapToLocationModel(updateEvent.getLocation()));
            eventOld.setLocationModel(locationModelUpdate);
        }
        return EventMapper.mapToEventUpdate(eventOld, updateEvent);
    }

    private List<Specification<Event>> searchFilterAdminToSpecification(SearchFilterAdmin filterAdmin) {

        List<Specification<Event>> specifications = new ArrayList<>();
        specifications.add(filterAdmin.getUsers().isEmpty() ? null : initiatorId(filterAdmin.getUsers()));
        specifications.add(filterAdmin.getStates().isEmpty() ? null : states(filterAdmin.getStates()));
        specifications.add(filterAdmin.getCategories().isEmpty() ? null : categories(filterAdmin.getCategories()));
        specifications.add((filterAdmin.getRangeStart() == null || filterAdmin.getRangeEnd() == null) ? null
                : eventDate(filterAdmin.getRangeStart(), filterAdmin.getRangeEnd()));
        return specifications.stream().filter(Objects::nonNull).collect(Collectors.toList());
    }

    private List<Specification<Event>> searchFilterPublicToSpecification(SearchFilterPublic filterPublic) {

        List<Specification<Event>> specifications = new ArrayList<>();
        specifications.add(states(Collections.singletonList(String.valueOf(State.PUBLISHED))));
        specifications.add(filterPublic.getCategories().isEmpty() ? null : categories(filterPublic.getCategories()));
        specifications.add(filterPublic.getPaid() == null ? null : paid(filterPublic.getPaid()));
        specifications.add((filterPublic.getRangeStart() == null || filterPublic.getRangeEnd() == null)
                ? eventDate(LocalDateTime.now(), LocalDateTime.now().plusYears(1000))
                : eventDate(filterPublic.getRangeStart(), filterPublic.getRangeEnd()));
        specifications.add(!filterPublic.getOnlyAvailable() ? null : onlyAvailable());
        return specifications.stream().filter(Objects::nonNull).collect(Collectors.toList());
    }

    private Specification<Event> initiatorId(List<Long> usersId) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.in(root.get("initiator").get("id")).value(usersId);
    }

    private Specification<Event> states(List<String> states) {
        List<State> result = states.stream()
                .map(this::mapToState)
                .collect(Collectors.toList());
        return (root, query, criteriaBuilder) -> criteriaBuilder.in(root.get("state")).value(result);
    }

    private State mapToState(String string) {
        return State.valueOf(string);
    }

    private Specification<Event> categories(List<Long> categoryId) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.in(root.get("category").get("id")).value(categoryId);
    }

    private Specification<Event> eventDate(LocalDateTime rangeStart, LocalDateTime rangeEnd) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.between(root.get("eventDate"), rangeStart, rangeEnd);
    }

    private Specification<Event> paid(Boolean paid) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("paid"), paid);
    }

    private Specification<Event> onlyAvailable() {
        return (root, query, criteriaBuilder) -> criteriaBuilder.or(criteriaBuilder.equal(root.get("participantLimit"), 0),
                criteriaBuilder.greaterThan(root.get("participantLimit"), root.get("confirmedRequests")));
    }

    private Specification<Event> queryAnnotation(String text) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.like(criteriaBuilder.lower(root.get("annotation")),
                "%" + text.toLowerCase() + "%");
    }

    private Specification<Event> queryDescription(String text) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.like(criteriaBuilder.lower(root.get("description")),
                "%" + text.toLowerCase() + "%");
    }
}
