package ru.practicum.shareit.booking.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.client.BaseClient;

import java.util.Map;

@Component
public class BookingClient extends BaseClient {

    public BookingClient(@Value("${shareit-server.url}") String serverUrl, RestTemplateBuilder builder) {
        super(builder, serverUrl);
    }

    public ResponseEntity<Object> create(long userId, Object dto) {
        return post("/bookings", userId, dto);
    }

    public ResponseEntity<Object> approve(long ownerId, long bookingId, boolean approved) {
        return patch("/bookings/{bookingId}?approved={approved}",
                ownerId,
                null,
                Map.of("bookingId", bookingId, "approved", approved));
    }

    public ResponseEntity<Object> getById(long userId, long bookingId) {
        return get("/bookings/{bookingId}", userId, Map.of("bookingId", bookingId));
    }

    public ResponseEntity<Object> getAllByBooker(long userId, String state) {
        return get("/bookings?state={state}", userId, Map.of("state", state));
    }

    public ResponseEntity<Object> getAllByOwner(long ownerId, String state) {
        return get("/bookings/owner?state={state}", ownerId, Map.of("state", state));
    }
}