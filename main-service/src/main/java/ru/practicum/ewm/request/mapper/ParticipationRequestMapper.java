package ru.practicum.ewm.request.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.practicum.ewm.request.dto.ParticipationRequestDto;
import ru.practicum.ewm.request.model.ParticipationRequest;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ParticipationRequestMapper {

    private static final DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static ParticipationRequestDto mapToParticipationRequestDto(ParticipationRequest request) {

        return ParticipationRequestDto.builder()
                .id(request.getId())
                .created(request.getCreated().format(FORMAT))
                .event(request.getEvent().getId())
                .requester(request.getRequester().getId())
                .status(String.valueOf(request.getStatus()))
                .build();
    }

    public static List<ParticipationRequestDto> mapToParticipationRequestsDto(List<ParticipationRequest> requests) {

        return requests.stream()
                .map(ParticipationRequestMapper::mapToParticipationRequestDto)
                .collect(Collectors.toList());
    }
}
