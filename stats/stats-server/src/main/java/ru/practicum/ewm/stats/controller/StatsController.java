package ru.practicum.ewm.stats.controller;


import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.ewm.dto.stats.EndpointHitDto;
import ru.practicum.ewm.dto.stats.ViewStats;
import ru.practicum.ewm.stats.service.StatsService;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;

@Validated
@RestController
@Slf4j
@AllArgsConstructor
public class StatsController {

    private final StatsService statsService;

    private final static String FORMAT = ("yyyy-MM-dd HH:mm:ss");

    @PostMapping("/hit")
    @ResponseStatus(HttpStatus.OK)
    public EndpointHitDto createEndpointHit(@Valid @RequestBody EndpointHitDto endpointHitDto) {
        log.info("Creating EndpointHit with app={}, uri={}, ip={}, timestamp={}",
                endpointHitDto.getApp(), endpointHitDto.getUri(), endpointHitDto.getIp(), endpointHitDto.getTimestamp());
        return statsService.createEndpointHit(endpointHitDto);
    }

    @GetMapping("/stats")
    public List<ViewStats> getViewStats(@RequestParam @DateTimeFormat(pattern = FORMAT) LocalDateTime start,
                                        @RequestParam @DateTimeFormat(pattern = FORMAT) LocalDateTime end,
                                        @RequestParam(defaultValue = "") List<String> uris,
                                        @RequestParam(defaultValue = "false") Boolean unique) {

        log.info("Get stats with start={}, end={}, uris={}, unique={}", start, end, uris, unique);
        return statsService.getViewStats(start, end, uris, unique);
    }
}
