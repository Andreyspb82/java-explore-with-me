package ru.practicum.ewm.stats.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.practicum.ewm.dto.stats.EndpointHitDto;
import ru.practicum.ewm.stats.model.EndpointHit;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class StatsMapper {

    private final static DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static EndpointHit mapToEndpointHit(EndpointHitDto endpointHitDto) {
        return EndpointHit.builder()
                .app(endpointHitDto.getApp())
                .uri(endpointHitDto.getUri())
                .ip(endpointHitDto.getIp())
                .created(LocalDateTime.parse(endpointHitDto.getTimestamp(), FORMAT))
                .build();
    }

    public static EndpointHitDto mapToEndpointHitDto (EndpointHit endpointHit) {
        return EndpointHitDto.builder()
                .id(endpointHit.getId())
                .app(endpointHit.getApp())
                .uri(endpointHit.getUri())
                .ip(endpointHit.getIp())
                .timestamp(endpointHit.getCreated().format(FORMAT))
                .build();
    }
}
