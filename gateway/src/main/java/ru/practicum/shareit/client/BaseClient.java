package ru.practicum.shareit.client;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;

import java.io.IOException;
import java.util.Map;

public class BaseClient {

    protected final RestTemplate rest;

    protected BaseClient(RestTemplateBuilder builder, String serverUrl) {
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

    protected ResponseEntity<Object> get(String path, long userId, Map<String, ?> params) {
        HttpHeaders headers = defaultHeaders(userId);
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        return rest.exchange(path, HttpMethod.GET, entity, Object.class, params);
    }

    protected ResponseEntity<Object> get(String path, long userId) {
        HttpHeaders headers = defaultHeaders(userId);
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        return rest.exchange(path, HttpMethod.GET, entity, Object.class);
    }

    protected ResponseEntity<Object> post(String path, long userId, Object body) {
        HttpHeaders headers = defaultHeaders(userId);
        HttpEntity<Object> entity = new HttpEntity<>(body, headers);
        return rest.exchange(path, HttpMethod.POST, entity, Object.class);
    }

    protected ResponseEntity<Object> post(String path, long userId, Object body, Map<String, ?> params) {
        HttpHeaders headers = defaultHeaders(userId);
        HttpEntity<Object> entity = new HttpEntity<>(body, headers);
        return rest.exchange(path, HttpMethod.POST, entity, Object.class, params);
    }

    protected ResponseEntity<Object> patch(String path, long userId, Object body, Map<String, ?> params) {
        HttpHeaders headers = defaultHeaders(userId);
        HttpEntity<Object> entity = new HttpEntity<>(body, headers);
        return rest.exchange(path, HttpMethod.PATCH, entity, Object.class, params);
    }

    protected ResponseEntity<Object> patch(String path, long userId, Object body) {
        HttpHeaders headers = defaultHeaders(userId);
        HttpEntity<Object> entity = new HttpEntity<>(body, headers);
        return rest.exchange(path, HttpMethod.PATCH, entity, Object.class);
    }

    protected ResponseEntity<Object> delete(String path, long userId, Map<String, ?> params) {
        HttpHeaders headers = defaultHeaders(userId);
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        return rest.exchange(path, HttpMethod.DELETE, entity, Object.class, params);
    }

    protected ResponseEntity<Object> delete(String path, long userId) {
        HttpHeaders headers = defaultHeaders(userId);
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        return rest.exchange(path, HttpMethod.DELETE, entity, Object.class);
    }

    private HttpHeaders defaultHeaders(long userId) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("X-Sharer-User-Id", String.valueOf(userId));
        return headers;
    }
}