package ru.practicum.shareit.user.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;

import java.io.IOException;

@Component
public class UserClient {

    private final RestTemplate rest;

    public UserClient(@Value("${shareit-server.url}") String serverUrl,
                      RestTemplateBuilder builder) {

        DefaultUriBuilderFactory factory = new DefaultUriBuilderFactory(serverUrl);

        this.rest = builder
                .uriTemplateHandler(factory)
                .requestFactory(() -> new HttpComponentsClientHttpRequestFactory())
                .errorHandler(new DefaultResponseErrorHandler() {
                    @Override
                    public void handleError(ClientHttpResponse response) throws IOException {
                    }
                })
                .build();
    }

    public ResponseEntity<Object> create(Object dto) {
        return rest.postForEntity("/users", dto, Object.class);
    }

    public ResponseEntity<Object> update(long userId, Object dto) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Object> entity = new HttpEntity<>(dto, headers);
        return rest.exchange("/users/{id}", HttpMethod.PATCH, entity, Object.class, userId);
    }

    public ResponseEntity<Object> getById(long userId) {
        return rest.getForEntity("/users/{id}", Object.class, userId);
    }

    public ResponseEntity<Object> getAll() {
        return rest.getForEntity("/users", Object.class);
    }

    public ResponseEntity<Object> delete(long userId) {
        return rest.exchange("/users/{id}", HttpMethod.DELETE, HttpEntity.EMPTY, Object.class, userId);
    }
}