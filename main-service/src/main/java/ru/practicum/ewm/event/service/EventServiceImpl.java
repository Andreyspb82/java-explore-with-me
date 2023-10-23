package ru.practicum.ewm.event.service;


import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
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

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
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

    public final StatsClient statsClient;

    private static final DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static final int LATER_HOURS = 2;

    // private
    @Override
    public EventFullDto createEvent(long userId, NewEventDto newEvent) {

        eventDateValidation(LocalDateTime.parse(newEvent.getEventDate(), FORMAT), 2);

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
    public EventRequestStatusUpdateResult updateStatusRequestForEvent(long userId, long eventId, EventRequestStatusUpdateRequest updateRequest) {
        Event event = getEventByIdForService(eventId);

        if (userId != event.getInitiator().getId()) {
            throw new ConflictException("Event not created by this user");
        }
        System.out.println("печать event = " + event);

        long participantLimit = event.getParticipantLimit();
        long confirmedRequest = event.getConfirmedRequests();


        //if (participantLimit == 0 || !event.getRequestModeration())

        if (participantLimit == 0 ) {
            List<ParticipationRequest> requestsPendingForUpdate = participationRequestRepository.findByEventIdAndStatusAndId(eventId,
                    String.valueOf(Status.PENDING), updateRequest.getRequestIds());
            for (ParticipationRequest request : requestsPendingForUpdate) {
                participationRequestRepository.save(updateStatus(request, Status.CONFIRMED));
            }
        }

        if (participantLimit > 0) {
            if (confirmedRequest == participantLimit) {
                throw new ConflictException("Event participant limit reached");
            }

            List<ParticipationRequest> requestsPendingForUpdate2 = participationRequestRepository.findByEventIdAndStatusAndId(eventId,
                    String.valueOf(Status.PENDING), updateRequest.getRequestIds());


            if (confirmedRequest < participantLimit && updateRequest.getStatus().equals(String.valueOf(Status.CONFIRMED))) {

                System.out.println("Добралось ли до сюда и разниц = " + (participantLimit - confirmedRequest));

                List<ParticipationRequest> requestsPendingForUpdate = participationRequestRepository.findByEventIdAndStatusAndId(eventId,
                        String.valueOf(Status.PENDING), updateRequest.getRequestIds());

                System.out.println("Печать списка для запросов  перед статусом  = " + requestsPendingForUpdate.size());


                List<ParticipationRequest> requestForCanceled = new ArrayList<>();

                for (ParticipationRequest request : requestsPendingForUpdate) {
                    if (confirmedRequest < participantLimit){
                        System.out.println("счетчик до = " + confirmedRequest);
                        participationRequestRepository.save(updateStatus(request, Status.CONFIRMED));
                        requestForCanceled.remove(request);
                        confirmedRequest++;
                        System.out.println("счетчик после = " + confirmedRequest);

                    }

                }

                if (confirmedRequest ==  participantLimit) {
                    for (ParticipationRequest request : requestForCanceled) {
                        participationRequestRepository.save(updateStatus(request, Status.REJECTED));
                    }
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

        System.out.println("счетчик перед сохранением " + confirmedRequest);
        event.setConfirmedRequests(confirmedRequest);
        System.out.println("счетчик после сохранения " + event.getConfirmedRequests());
        eventRepository.save(event);

        return EventRequestStatusUpdateResult.builder()
                .confirmedRequests(ParticipationRequestMapper.mapToParticipationRequestsDto(confirmRequests))
                .rejectedRequests(ParticipationRequestMapper.mapToParticipationRequestsDto(rejectedRequests))
                .build();
    }

    private ParticipationRequest updateStatus(ParticipationRequest request, Status status) {

        request.setStatus(status);

//        return ParticipationRequest.builder()
//                .id(request.getId())
//                .created(request.getCreated())
//                .event(request.getEvent())
//                .requester(request.getRequester())
//                .status(status)
//                .build();
        return request;
    }


//    @Override
//    public EventRequestStatusUpdateResult updateStatusRequestForEvent(long userId, long eventId, EventRequestStatusUpdateRequest updateRequest) {
//        Event event = getEventByIdForService(eventId);
//        if (userId != event.getInitiator().getId()) {
//            throw new ConflictException("Event not created by this user");
//        }
//        long participantLimit = event.getParticipantLimit();
//        long confirmedRequest = event.getConfirmedRequests();
//
//        if (participantLimit == 0 || !event.getRequestModeration()) {
//            List<ParticipationRequest> requestsPendingForUpdate = participationRequestRepository.findByEventIdAndStatusAndId(eventId,
//                    String.valueOf(Status.PENDING), updateRequest.getRequestIds());
//            for (ParticipationRequest request : requestsPendingForUpdate) {
//                participationRequestRepository.save(updateStatus(request, Status.CONFIRMED));
//            }
//        } else {
//            Optional<Long> count = Optional.ofNullable(participationRequestRepository.countRequestStatusConfirmedForEventId(eventId));
//            if (count.isPresent()) {
//                confirmedRequest = confirmedRequest + count.get();
//            }
//            if (confirmedRequest < participantLimit && updateRequest.getStatus().equals(String.valueOf(Status.CONFIRMED))) {
//
//                List<ParticipationRequest> requestsPendingForUpdate = participationRequestRepository.findByEventIdAndStatusAndId(eventId,
//                        String.valueOf(Status.PENDING), updateRequest.getRequestIds());
//                if (requestsPendingForUpdate.isEmpty()) {
//                    throw new ConflictException("requestsPending.isEmpty()");
//                }
//                List<ParticipationRequest> requestForCanceled = new ArrayList<>();
//                if (confirmedRequest < participantLimit) {
//                    for (ParticipationRequest request : requestsPendingForUpdate) {
//                        requestForCanceled.remove(request);
//                        participationRequestRepository.save(updateStatus(request, Status.CONFIRMED));
//                        confirmedRequest++;
//                        System.out.println("счетчик100 = " + confirmedRequest);
//
//                        if (confirmedRequest == participantLimit) {
//                            throw new ConflictException("confirmedRequest == participantLimit 11");
//                        }
//                    }
//                }
//                for (ParticipationRequest request : requestForCanceled) {
//                    participationRequestRepository.save(updateStatus(request, Status.REJECTED));
//                }
//            }
//            if (updateRequest.getStatus().equals(String.valueOf(Status.REJECTED))) {
//                List<ParticipationRequest> requestsPendingForUpdate = participationRequestRepository.findByEventIdAndStatusAndId(eventId,
//                        String.valueOf(Status.PENDING), updateRequest.getRequestIds());
//                for (ParticipationRequest request : requestsPendingForUpdate) {
//                    participationRequestRepository.save(updateStatus(request, Status.REJECTED));
//                }
//            }
//        }
//        List<ParticipationRequest> confirmRequests = participationRequestRepository.findByEventIdAndStatusAndId(eventId,
//                String.valueOf(Status.CONFIRMED), updateRequest.getRequestIds());
//        List<ParticipationRequest> rejectedRequests = participationRequestRepository.findByEventIdAndStatusAndId(eventId,
//                String.valueOf(Status.REJECTED), updateRequest.getRequestIds());
//
//        event.setConfirmedRequests(confirmedRequest);
//        eventRepository.save(event);
//
//        return EventRequestStatusUpdateResult.builder()
//                .confirmedRequests(ParticipationRequestMapper.mapToParticipationRequestsDto(confirmRequests))
//                .rejectedRequests(ParticipationRequestMapper.mapToParticipationRequestsDto(rejectedRequests))
//                .build();
//    }
//
//    private ParticipationRequest updateStatus(ParticipationRequest request, Status status) {
//        return ParticipationRequest.builder()
//                .id(request.getId())
//                .created(request.getCreated())
//                .event(request.getEvent())
//                .requester(request.getRequester())
//                .status(status)
//                .build();
//    }

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
            eventDateValidation(LocalDateTime.parse(updateEventAdminRequest.getEventDate(), FORMAT), 1);
        }


//        if (updateEventAdminRequest.getEventDate() != null && updateEventAdminRequest.getStateAction().equals(String.valueOf(State.PUBLISHED))) {
//            eventDateValidation(LocalDateTime.parse(updateEventAdminRequest.getEventDate(), FORMAT), 1);
//        }


        Event eventOld = getEventByIdForService(eventId);

        if (!eventOld.getState().equals(State.PENDING)) {
            throw new ConflictException("Event not in pending status");
        }

        Event eventUpdate = eventUpdateByUserAndByAdmin(eventOld, updateEventAdminRequest);

        if (updateEventAdminRequest.getEventDate() == null) {
            eventDateValidation(eventUpdate.getEventDate(), 1);
        }

//        Set<ConstraintViolation<Event>> violations = Validation.buildDefaultValidatorFactory().getValidator().validate(eventUpdate);
//        if (!violations.isEmpty()) {
//            throw new BadRequestException("Event data not validated");
//        }

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


    //public List<EventFullDto> getEventsForAdmin(SearchFilterAdmin filterAdmin, PageRequest page)
    @Override
    public List<EventFullDto> getEventsForAdmin(SearchFilterAdmin filterAdmin, PageRequest page) {

        System.out.println("filterAdmin = " + filterAdmin);
        List<Specification<Event>> specifications = searchFilterAdminToSpecification(filterAdmin);

        // List<Event> events = new ArrayList<>();


        if (specifications.isEmpty()) {
            Page<Event> events = eventRepository.findAll(page);
            return EventMapper.mapToEventsFullDto(events.getContent());
        } else {
            Page<Event> events = eventRepository.findAll(specifications.stream().reduce(Specification::and).orElseThrow(() -> new BadRequestException("Bad request")), page);
            return EventMapper.mapToEventsFullDto(events.getContent());
        }


        // List<Event> events = eventRepository.findAll(initiatorId(filterAdmin.getUsers()));


//        System.out.println("размер списка без страницы admin = " + events.size());
//        System.out.println("размер списка без страницы admin все = " + events);
//
//       // Page<Event> ev = eventRepository.findAll(specifications.stream().reduce(Specification::and).orElseThrow(() -> new BadRequestException("Bad request")), page);
//        Page<Event> ev = eventRepository.findAll(specifications.stream().reduce(Specification::and).get(), page);
//        System.out.println("размер списка без страницы admin со страницей = " + ev);
//        List<Event> events1 = ev.getContent();
//        System.out.println("размер списка без страницы admin со страницей переделав в лист = " + events1);
//
//        List<Event> ev1 = (List<Event>) ev;


    }


    //public


    @Override
    public List<EventShortDto> getEventsPublic(SearchFilterPublic filterPublic, PageRequest page) {

        List<Specification<Event>> specifications = searchFilterPublicToSpecification(filterPublic);

        List<Event> eventsTest = eventRepository.findAll((queryAnnotation(filterPublic.getText()))
                .or(queryDescription(filterPublic.getText()))
                .or(specifications.stream().reduce(Specification::and).get()));

        System.out.println("размер списка без страницы = " + eventsTest.size());


        Page<Event> eventsPage = eventRepository.findAll((queryAnnotation(filterPublic.getText()))
                .or(queryDescription(filterPublic.getText()))
                .or(specifications.stream().reduce(Specification::and).get()), page);

        System.out.println("содержимое public " + eventsPage);


        List<Event> result = eventsPage.getContent();


        if (result.isEmpty()) {
            throw new BadRequestException("The request was formed incorrectly");
        } else {
            return EventMapper.mapToEventsShortDto(result);
        }
    }

    // метод работает, но нет страниц
//    @Override
//    public List<EventShortDto> getEventsPublic(SearchFilterPublic filterPublic, PageRequest page) {
//
//        List<Specification<Event>> specifications = searchFilterPublicToSpecification(filterPublic);
//
//        List<Event> events = new ArrayList<>();
//
//        events = eventRepository.findAll((queryAnnotation(filterPublic.getText()))
//                .or(queryDescription(filterPublic.getText()))
//                .or(specifications.stream().reduce(Specification::and).get()), sortEvents(filterPublic.getSort()));
//
//        if (events.isEmpty()) {
//            throw new BadRequestException("The request was formed incorrectly");
//        } else {
//            return EventMapper.mapToEventsShortDto(events);
//        }
//    }
//
//    private Sort sortEvents(String sortEvents) {
//        if (sortEvents.equals(String.valueOf(SortEvents.EVENT_DATE))) {
//            return Sort.by(Sort.Direction.DESC, "eventDate");
//        } else {
//            return Sort.by(Sort.Direction.DESC, "views");
//        }
//    }

    @Override
    public EventFullDto getEventByIdPublic(long eventId) {

        Optional<Event> event = Optional.ofNullable(eventRepository.findByIdAndState(eventId, String.valueOf(State.PUBLISHED)));

        if (event.isEmpty()) {
            throw new NotFoundException("Event with Id =" + eventId + " not found or not available");
        } else {

            Map<String, Object> parameters = Map.of(
                    "start", "2000-05-05 00:00:00",
                    "end", "3020-05-05 00:00:00",
                    "uris", "/events/" + eventId,
                    "unique", true
            );

            Optional<List<ViewStats>> viewStats = Optional.ofNullable(statsClient.getViewStats(parameters));
            if (viewStats.isPresent()) {
                long hits = viewStats.get().get(0).getHits();
                event.get().setViews(hits);
            }

            return EventMapper.mapToEventFullDto(eventRepository.save(event.get()));

            //  return EventMapper.mapToEventFullDto(event.get());
        }


        //Optional<List<ViewStats>> viewStats = Optional.ofNullable(statsClient.getViewStats("2000-05-05 00:00:00", "3020-05-05 00:00:00", "/events/"+eventId, false));


        // Optional<List<ViewStats>> viewStats =Optional.ofNullable( statsClient.getViewStats("2000-05-05 00:00:00", "3020-05-05 00:00:00", "/events/"+eventId, false));

        //   List<ViewStats> viewStats1 = statsClient.getViewStats("2000-05-05 00:00:00", "3020-05-05 00:00:00", "/events/"+eventId, false);

        // if (viewStats.isPresent()) {
//            System.out.println("path " + List.of(String.valueOf("/events/" + eventId)));
//            System.out.println("Печать viewStats1 " + viewStats);
//        System.out.println("Печать viewStats2 " + viewStats.get());
//        System.out.println("Печать viewStats2 " + viewStats.get().get(0).getHits());
//            System.out.println("Печать viewStats2 " + viewStats.get());
//            System.out.println("Печать viewStats3 " +  viewStats.get().getBody());
//            System.out.println("Печать viewStats4 " +  viewStats.get().getBody().getClass());
//            Optional<List<ViewStats>> test1 = Optional.ofNullable((List<ViewStats>) viewStats.get().getBody());
//            Optional<String> test100 = Optional.ofNullable(viewStats.get().getBody().toString());
//            System.out.println("test100 = " + test100);

//            System.out.println("hits1 = " + test1.get());
//
//            List<ViewStats> test2 = test1.get();
//
//            System.out.println("hits2 = " + test2);
//            System.out.println("hits3 = " + test2.get(0).getClass());
//
//            System.out.println("hits5 = " + test2.get(0).getApp());
//
//            Long hits4 = Long.valueOf(test2.get(0).getHits());
//
//
//            ViewStats test3 =   test2.get(0);
//            System.out.println("hits4 = " + hits4);
//
//
//            Optional<ViewStats> viewStats1 = Optional.ofNullable(test2.get(0));
//            ViewStats viewStats1 = test2.get(0).getClass().newInstance();

//            System.out.println("hits4 = " + viewStats1.get());
//            System.out.println("hits5 = " + viewStats1.get().getHits());
//
//            List<ViewStats> test = (List<ViewStats>) viewStats.get().getBody();
//            System.out.println("hits = " + test.get(0).getHits());

        //   }
        // else {
        //  System.out.println("Печать viewStats null? ");
        // }
        //ResponseEntity<Object> viewStats = statsClient.getViewStats(null, null, List.of(String.valueOf(eventId)), true);


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
    private void eventDateValidation(LocalDateTime eventDate, int laterHours) {
        LocalDateTime nowDate = LocalDateTime.now();
        Duration duration = Duration.between(nowDate, eventDate);
        System.out.println("hours " + duration.toHours()); // удалить

        if (nowDate.isAfter(eventDate)) {
            throw new BadRequestException("Event starts in the past");
        }

        if (duration.toHours() < laterHours) {
            throw new ConflictException("Event starts earlier than " + laterHours + " hours");
        }


    }

//    private void eventDateValidation(LocalDateTime eventDate) {
//        LocalDateTime nowDate = LocalDateTime.now();
//        Duration duration = Duration.between(nowDate, eventDate);
//        System.out.println("hours " + duration.toHours()); // удалить
//
//        if (duration.toHours() < LATER_HOURS) {
//            throw new ConflictException("Event starts earlier than 2 hours");
//        }
//    }

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
        return (root, query, criteriaBuilder) -> criteriaBuilder.like(criteriaBuilder.lower(root.get("annotation")),
                "%" + text.toLowerCase() + "%");
    }

    private Specification<Event> queryDescription(String text) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.like(criteriaBuilder.lower(root.get("description")),
                "%" + text.toLowerCase() + "%");
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
