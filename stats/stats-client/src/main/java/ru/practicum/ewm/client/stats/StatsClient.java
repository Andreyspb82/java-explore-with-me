package ru.practicum.ewm.client.stats;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.ewm.dto.stats.ViewStats;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Service
public class StatsClient {

    private static final String API_PREFIX = "/stats";

    public final RestTemplate rest;

    public StatsClient(@Value("${stats-server.url}") String serverUrl, RestTemplateBuilder builder) {
        this.rest = builder
                .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl + API_PREFIX))
                .requestFactory(HttpComponentsClientHttpRequestFactory::new)
                .build();
    }

    public List<ViewStats> getViewStats(Map<String, Object> parameters) {
        try {
            ResponseEntity<ViewStats[]> response = rest.exchange("?start={start}&end={end}&uris={uris}&unique={unique}",
                    HttpMethod.GET, null, ViewStats[].class, parameters);
            return Arrays.asList(response.getBody());
        } catch (HttpStatusCodeException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
}
