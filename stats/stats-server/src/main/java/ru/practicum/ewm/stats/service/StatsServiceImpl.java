package ru.practicum.ewm.stats.service;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.dto.stats.EndpointHitDto;
import ru.practicum.ewm.dto.stats.ViewStats;
import ru.practicum.ewm.stats.exception.BadRequestException;
import ru.practicum.ewm.stats.mapper.StatsMapper;
import ru.practicum.ewm.stats.repository.StatsRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
@Data
public class StatsServiceImpl implements StatsService {

    private final StatsRepository statsRepository;

    @Override
    public EndpointHitDto createEndpointHit(EndpointHitDto endpointHitDto) {
        return StatsMapper.mapToEndpointHitDto(statsRepository.save(StatsMapper.mapToEndpointHit(endpointHitDto)));
    }

    @Override
    public List<ViewStats> getViewStats(LocalDateTime start, LocalDateTime end, List<String> uris, Boolean unique) {

        if (start.isAfter(end)) {
            throw new BadRequestException("Searching the start date is after the end date");
        }
        if (unique && !uris.isEmpty()) {
            return statsRepository.findByDateAndUrisAndUnique(start, end, uris);
        }
        if (!unique && !uris.isEmpty()) {
            return statsRepository.findByDateAndUris(start, end, uris);
        }
        if (unique) {
            return statsRepository.findByDateAndUnique(start, end);
        } else {
            return statsRepository.findByDate(start, end);
        }
    }
}
