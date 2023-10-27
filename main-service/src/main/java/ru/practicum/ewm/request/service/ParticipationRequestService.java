package ru.practicum.ewm.request.service;

import ru.practicum.ewm.request.dto.ParticipationRequestDto;

import java.util.List;

public interface ParticipationRequestService {

    public ParticipationRequestDto createRequest(long userId, long eventId);

    public ParticipationRequestDto cancelRequestByOwner(long requestId, long userId);

    public List<ParticipationRequestDto> getRequestsByUserId(long userId);
}
