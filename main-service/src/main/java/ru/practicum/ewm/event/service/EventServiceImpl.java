package ru.practicum.ewm.event.service;


import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.category.model.Category;
import ru.practicum.ewm.category.service.CategoryService;
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
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
@Data
public class EventServiceImpl implements EventService {

    public final EventRepository eventRepository;

    // public final CategoryRepository categoryRepository;

    public final CategoryService categoryService;

    public final UserService userService;

    public final LocationRepository locationRepository;

    public final ParticipationRequestRepository participationRequestRepository;

    private static final DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static final int LATER_HOURS = 2;

    // private
    @Override
    public EventFullDto createEvent(long userId, NewEventDto newEvent) {

        eventDateValidation(LocalDateTime.parse(newEvent.getEventDate(), FORMAT));

        LocationModel locationModel = locationRepository.save(LocationMapper.mapToLocationModel(newEvent.getLocation()));
        Category category = categoryService.getCategoryById(newEvent.getCategory());
        User user = userService.getUserByIdForService(userId);

        Event event = EventMapper.mapToEventNew(newEvent, category, user);
        event.setLocationModel(locationModel);
        event.setState(State.PENDING);

        return EventMapper.mapToEventFullDto(eventRepository.save(event));
    }


    @Override
    public EventFullDto updateEventByUserId(long userId, long eventId, UpdateEventUserRequest updateEventUserRequest) {

        if (updateEventUserRequest.getEventDate() != null) {
            eventDateValidation(LocalDateTime.parse(updateEventUserRequest.getEventDate(), FORMAT));
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
    public EventRequestStatusUpdateResult updateStatusRequestForEvent(long userId, long eventId, EventRequestStatusUpdateRequest updateRequest) {
        Event event = getEventByIdForService(eventId);
        if (userId != event.getInitiator().getId()) {
            throw new ConflictException("Event not created by this user");
        }
        long participantLimit = event.getParticipantLimit();
        long confirmedRequest = event.getConfirmedRequests();

        if (participantLimit == 0 || !event.getRequestModeration()) {
            List<ParticipationRequest> requestsPendingForUpdate = participationRequestRepository.findByEventIdAndStatusAndId(eventId,
                    String.valueOf(Status.PENDING), updateRequest.getRequestIds());
            for (ParticipationRequest request : requestsPendingForUpdate) {
                participationRequestRepository.save(updateStatus(request, Status.CONFIRMED));
            }
        } else {
            Optional<Long> count = Optional.ofNullable(participationRequestRepository.countRequestStatusConfirmedForEventId(eventId));
            if (count.isPresent()) {
                confirmedRequest = confirmedRequest + count.get();
            }
            if (confirmedRequest < participantLimit && updateRequest.getStatus().equals(String.valueOf(Status.CONFIRMED))) {

                List<ParticipationRequest> requestsPendingForUpdate = participationRequestRepository.findByEventIdAndStatusAndId(eventId,
                        String.valueOf(Status.PENDING), updateRequest.getRequestIds());
                if (requestsPendingForUpdate.isEmpty()) {
                    throw new ConflictException("requestsPending.isEmpty()");
                }
                List<ParticipationRequest> requestForCanceled = new ArrayList<>();
                if (confirmedRequest < participantLimit) {
                    for (ParticipationRequest request : requestsPendingForUpdate) {
                        requestForCanceled.remove(request);
                        participationRequestRepository.save(updateStatus(request, Status.CONFIRMED));
                        confirmedRequest++;
                        System.out.println("счетчик100 = " + confirmedRequest);

                        if (confirmedRequest == participantLimit) {
                            throw new ConflictException("confirmedRequest == participantLimit 11");
                        }
                    }
                }
                for (ParticipationRequest request : requestForCanceled) {
                    participationRequestRepository.save(updateStatus(request, Status.REJECTED));
                }
            }
            if (updateRequest.getStatus().equals(String.valueOf(Status.REJECTED))) {
                List<ParticipationRequest> requestsPendingForUpdate = participationRequestRepository.findByEventIdAndStatusAndId(eventId,
                        String.valueOf(Status.PENDING), updateRequest.getRequestIds());
                for (ParticipationRequest request : requestsPendingForUpdate) {
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

    private ParticipationRequest updateStatus(ParticipationRequest request, Status status) {
        return ParticipationRequest.builder()
                .id(request.getId())
                .created(request.getCreated())
                .event(request.getEvent())
                .requester(request.getRequester())
                .status(status)
                .build();
    }

    @Override
    public List<EventShortDto> getEventsByUserId(long userId, PageRequest page) {

        List<Event> events = eventRepository.findByInitiatorId(userId, page);

        return EventMapper.mapToEventsShortDto(events);
    }

    @Override
    public EventFullDto getEventByOwner(long userId, long eventId) {

        Event event = eventRepository.findByInitiatorIdAndId(userId, eventId);

        //  Event event = eventRepository.findByUserIdAndId(userId, eventId);

        return EventMapper.mapToEventFullDto(event);
    }


    @Override
    public List<ParticipationRequestDto> getRequestsByOwner(long userId, long eventId) {

        Event event = getEventByIdForService(eventId);

        if (userId != event.getInitiator().getId()) {
            throw new BadRequestException("Event not created by this user");
        }

        List<ParticipationRequest> requests = participationRequestRepository.findByEventId(eventId);

        return ParticipationRequestMapper.mapToParticipationRequestsDto(requests);
    }


    //admin
    @Override
    public EventFullDto updateEventByAdmin(long eventId, UpdateEventAdminRequest updateEventAdminRequest) {
        if (updateEventAdminRequest.getEventDate() != null) {
            eventDateValidation(LocalDateTime.parse(updateEventAdminRequest.getEventDate(), FORMAT));
        }

        Event eventOld = getEventByIdForService(eventId);

        if (!eventOld.getState().equals(State.PENDING)) {
            throw new ConflictException("Event not in pending status");
        }

        Event eventUpdate = eventUpdateByUserAndByAdmin(eventOld, updateEventAdminRequest);

        return EventMapper.mapToEventFullDto(eventRepository.save(eventUpdate));
    }

//    @Override
//    public List<EventFullDto> getEventsByAdmin(SearchFilterAdmin filterAdmin, PageRequest page) {
//
//        List<Event> events = eventRepository.findAll(new Specification<Event>() {
//            @Override
//            public Predicate toPredicate(Root<Event> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
//                //return criteriaBuilder.ge(root.get("event_date"), filterAdmin.getRangeStart());
//                return criteriaBuilder.between(root.get("event_date"), filterAdmin.getRangeStart(), filterAdmin.getRangeEnd());
//            }
//        });
//
//        return EventMapper.mapToEventsFullDto(events);
//    }


    @Override

    public List<EventFullDto> getEventsByAdmin(SearchFilterAdmin filterAdmin, PageRequest page) {
        List<Specification<Event>> specifications = searchFilterAdminToSpecification(filterAdmin);


        // List<Event> events = eventRepository.findAll(initiatorId(filterAdmin.getUsers()));

        List<Event> events = eventRepository.findAll(specifications.stream().reduce(Specification::and).orElseThrow(() -> new BadRequestException("Bad request")));

//        Page<Event> ev = eventRepository.findAll(specifications.stream().reduce(Specification::and).orElseThrow(() -> new BadRequestException("Bad request")), page);
//
//        List<Event> ev1 = (List<Event>) ev;

        return EventMapper.mapToEventsFullDto(events);

    }


    //public
    @Override
    public List<EventShortDto> getEventsPublic(SearchFilterPublic filterPublic, PageRequest page) {

        List<Specification<Event>> specifications = searchFilterPublicToSpecification(filterPublic);

        List<Event> events = new ArrayList<>();

        events = eventRepository.findAll((queryAnnotation(filterPublic.getText()))
                .or(queryDescription(filterPublic.getText()))
                .or(specifications.stream().reduce(Specification::and).get()), sortEvents(filterPublic.getSort()));

        return EventMapper.mapToEventsShortDto(events);

    }

    private Sort sortEvents(String sortEvents) {
        if (sortEvents.equals(String.valueOf(SortEvents.EVENT_DATE))) {
            return Sort.by(Sort.Direction.DESC, "eventDate");
        } else {
            return Sort.by(Sort.Direction.DESC, "views");
        }
    }

    @Override
    public EventFullDto getEventByIdPublic(long eventId) {

        Optional<Event> event = Optional.ofNullable(eventRepository.findByIdAndState(eventId, String.valueOf(State.PUBLISHED)));

        if (event.isEmpty()) {
            throw new NotFoundException("Event with Id =" + eventId + " not found or not available");
        } else {
            return EventMapper.mapToEventFullDto(event.get());
        }

        //   Event event = eventRepository.findByIdAndState(eventId, String.valueOf(State.PUBLISHED));

//        if (!eventRepository.existsById(eventId)) {
//            throw new NotFoundException("Event with Id =" + eventId + " does not exist");
//        }
//        return EventMapper.mapToEventFullDto( eventRepository.findById(eventId).get());

        // return EventMapper.mapToEventFullDto(event);
    }

    //для сервиса запросов
    @Override
    public Event getEventByIdForService(long eventId) {
        if (!eventRepository.existsById(eventId)) {
            throw new NotFoundException("Event with Id =" + eventId + " does not exist");
        }
        return eventRepository.findById(eventId).get();
    }


    //необходимые методы
    private void eventDateValidation(LocalDateTime eventDate) {
        LocalDateTime nowDate = LocalDateTime.now();
        Duration duration = Duration.between(nowDate, eventDate);
        System.out.println("hours " + duration.toHours()); // удалить

        if (duration.toHours() < LATER_HOURS) {
            throw new ConflictException("Event starts earlier than 2 hours");
        }
    }

    private Event eventUpdateByUserAndByAdmin(Event eventOld, UpdateEventUserRequest updateEvent) {

        if (updateEvent.getCategory() != null) {
            Category categoryUpdate = categoryService.getCategoryById(updateEvent.getCategory());
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


        //делать по лимиту запросов
        specifications.add(filterPublic.getOnlyAvailable() == null ? null : onlyAvailable(filterPublic.getOnlyAvailable()));


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

    // переделать
    private Specification<Event> onlyAvailable(Boolean onlyAvailable) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("paid"), onlyAvailable);
    }


    private Specification<Event> queryAnnotation(String text) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.like(criteriaBuilder.lower(root.get("annotation")), "%" + text.toLowerCase() + "%");
    }

    private Specification<Event> queryDescription(String text) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.like(criteriaBuilder.lower(root.get("description")), "%" + text.toLowerCase() + "%");
    }

//    private Specification<Event> queryDescriptionTest(String text) {
//        return (root, query, criteriaBuilder) -> criteriaBuilder.like(criteriaBuilder.lower(root.get("description")), "%" + text.toLowerCase() + "%");
//    }


// test

    @Override
    public EventFullDto getListTest() {


        return EventMapper.mapToEventFullDto(eventRepository.findById(1L).get());
    }


}
