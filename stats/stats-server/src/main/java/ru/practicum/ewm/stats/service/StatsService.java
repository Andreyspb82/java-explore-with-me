package ru.practicum.ewm.stats.service;

import ru.practicum.ewm.dto.stats.EndpointHitDto;
import ru.practicum.ewm.dto.stats.ViewStats;

import java.time.LocalDateTime;
import java.util.List;


public interface StatsService {

    public EndpointHitDto createEndpointHit(EndpointHitDto endpointHitDto);

    public List<ViewStats> getViewStats(LocalDateTime start, LocalDateTime end, List<String> uris, Boolean unique);
}
