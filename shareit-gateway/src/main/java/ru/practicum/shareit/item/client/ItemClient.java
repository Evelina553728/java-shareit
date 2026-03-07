package ru.practicum.shareit.item.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.client.BaseClient;

import java.util.Map;

@Component
public class ItemClient extends BaseClient {

    public ItemClient(@Value("${shareit-server.url}") String serverUrl, RestTemplateBuilder builder) {
        super(builder, serverUrl);
    }

    public ResponseEntity<Object> create(long userId, Object dto) {
        return post("/items", userId, dto);
    }

    public ResponseEntity<Object> update(long userId, long itemId, Object dto) {
        return patch("/items/{itemId}", userId, dto, Map.of("itemId", itemId));
    }

    public ResponseEntity<Object> getById(long userId, long itemId) {
        return get("/items/{itemId}", userId, Map.of("itemId", itemId));
    }

    public ResponseEntity<Object> getAllByOwner(long userId) {
        return get("/items", userId);
    }

    public ResponseEntity<Object> search(long userId, String text) {
        return get("/items/search?text={text}", userId, Map.of("text", text));
    }

    public ResponseEntity<Object> addComment(long userId, long itemId, Object dto) {
        return post("/items/{itemId}/comment", userId, dto, Map.of("itemId", itemId));
    }
}