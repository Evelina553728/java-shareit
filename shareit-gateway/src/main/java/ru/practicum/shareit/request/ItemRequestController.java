package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.client.ItemRequestClient;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/requests")
public class ItemRequestController {

    private static final String USER_HEADER = "X-Sharer-User-Id";

    private final ItemRequestClient requestClient;

    @PostMapping
    public ResponseEntity<Object> create(@RequestHeader(USER_HEADER) long userId,
                                         @RequestBody ItemRequestCreateDto dto) {
        return requestClient.create(userId, dto);
    }

    @GetMapping
    public ResponseEntity<Object> getOwn(@RequestHeader(USER_HEADER) long userId) {
        return requestClient.getOwn(userId);
    }

    @GetMapping("/all")
    public ResponseEntity<Object> getAllOther(@RequestHeader(USER_HEADER) long userId,
                                              @RequestParam(defaultValue = "0") int from,
                                              @RequestParam(defaultValue = "10") int size) {
        return requestClient.getAllOther(userId, from, size);
    }

    @GetMapping("/{requestId}")
    public ResponseEntity<Object> getById(@RequestHeader(USER_HEADER) long userId,
                                          @PathVariable long requestId) {
        return requestClient.getById(userId, requestId);
    }
}