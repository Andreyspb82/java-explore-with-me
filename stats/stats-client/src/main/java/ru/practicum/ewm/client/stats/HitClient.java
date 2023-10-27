package ru.practicum.ewm.client.stats;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.ewm.dto.stats.EndpointHitDto;

@Service
public class HitClient {

    private static final String API_PREFIX = "/hit";
    private final RestTemplate rest;

    public HitClient(@Value("${stats-server.url}") String serverUrl, RestTemplateBuilder builder) {
        this.rest = builder
                .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl + API_PREFIX))
                .requestFactory(HttpComponentsClientHttpRequestFactory::new)
                .build();
    }

    public ResponseEntity<EndpointHitDto> createEndpointHit(EndpointHitDto endpointHitDto) {
        ResponseEntity<EndpointHitDto> response = rest.postForEntity(
                "", endpointHitDto, EndpointHitDto.class);

        return response;
    }
}
