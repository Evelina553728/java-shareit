package ru.practicum.shareit.request.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.client.BaseClient;

import java.util.Map;

@Component
public class ItemRequestClient extends BaseClient {

    public ItemRequestClient(@Value("${shareit-server.url}") String serverUrl, RestTemplateBuilder builder) {
        super(builder, serverUrl);
    }

    public ResponseEntity<Object> create(long userId, Object dto) {
        return post("/requests", userId, dto);
    }

    public ResponseEntity<Object> getOwn(long userId) {
        return get("/requests", userId);
    }

    public ResponseEntity<Object> getAllOther(long userId, int from, int size) {
        return get("/requests/all?from={from}&size={size}", userId, Map.of("from", from, "size", size));
    }

    public ResponseEntity<Object> getById(long userId, long requestId) {
        return get("/requests/{requestId}", userId, Map.of("requestId", requestId));
    }
}