package ru.practicum.ewm.request.service;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.event.enums.State;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.event.repository.EventRepository;
import ru.practicum.ewm.event.service.EventService;
import ru.practicum.ewm.exception.ConflictException;
import ru.practicum.ewm.exception.NotFoundException;
import ru.practicum.ewm.request.dto.ParticipationRequestDto;
import ru.practicum.ewm.request.enums.Status;
import ru.practicum.ewm.request.mapper.ParticipationRequestMapper;
import ru.practicum.ewm.request.model.ParticipationRequest;
import ru.practicum.ewm.request.repository.ParticipationRequestRepository;
import ru.practicum.ewm.user.model.User;
import ru.practicum.ewm.user.service.UserService;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@Data
public class ParticipationRequestServiceImpl implements ParticipationRequestService {

    public final ParticipationRequestRepository requestRepository;

    public final EventService eventService;

    public final UserService userService;

    public final EventRepository eventRepository;

    @Override
    public ParticipationRequestDto createRequest(long userId, long eventId) {

        Event event = eventService.getEventByIdForService(eventId);
        User user = userService.getUserByIdForService(userId);
        long confirmedRequests = event.getConfirmedRequests();

        if (event.getInitiator().getId().longValue() == user.getId()) {
            throw new ConflictException("The user is the initiator of the event");
        }
        if (!event.getState().equals(State.PUBLISHED)) {
            throw new ConflictException("Event not published");
        }
        if (event.getParticipantLimit() != 0 && event.getParticipantLimit() == event.getConfirmedRequests()) {
            throw new ConflictException("Event participant limit reached");
        }
        ParticipationRequest request = new ParticipationRequest();
        request.setRequester(user);
        request.setStatus(Status.PENDING);

        if (event.getParticipantLimit() == 0) {
            request.setStatus(Status.CONFIRMED);
        }
        if (!event.getRequestModeration() && (event.getConfirmedRequests() < event.getParticipantLimit())) {
            request.setStatus(Status.CONFIRMED);
            confirmedRequests++;
        }
        event.setConfirmedRequests(confirmedRequests);
        request.setEvent(event);
        eventRepository.save(event);
        return ParticipationRequestMapper.mapToParticipationRequestDto(requestRepository.save(request));
    }

    @Override
    public ParticipationRequestDto cancelRequestByOwner(long requestId, long userId) {

        Optional<ParticipationRequest> request = Optional.ofNullable(requestRepository.findByIdAndRequesterId(requestId, userId));

        if (request.isEmpty()) {
            throw new NotFoundException("Request with Id =" + requestId + " not found or not available");
        } else {
            request.get().setStatus(Status.CANCELED);
            return ParticipationRequestMapper.mapToParticipationRequestDto(requestRepository.save(request.get()));
        }
    }

    @Override
    public List<ParticipationRequestDto> getRequestsByUserId(long userId) {

        return ParticipationRequestMapper.mapToParticipationRequestsDto(requestRepository.findByUserIdForOtherUserEvents(userId));
    }
}
