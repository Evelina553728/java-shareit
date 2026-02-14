package ru.practicum.shareit.booking;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.common.ValidationException;

import java.util.List;

@RestController
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
public class BookingController {

    private static final String USER_ID_HEADER = "X-Sharer-User-Id";

    private final BookingService bookingService;

    @PostMapping
    public BookingDto create(@RequestHeader(USER_ID_HEADER) long userId,
                             @Valid @RequestBody BookingCreateDto dto) {
        return bookingService.create(userId, dto);
    }

    @PatchMapping("/{bookingId}")
    public BookingDto approve(@RequestHeader(USER_ID_HEADER) long ownerId,
                              @PathVariable long bookingId,
                              @RequestParam boolean approved) {
        return bookingService.approve(ownerId, bookingId, approved);
    }

    @GetMapping("/{bookingId}")
    public BookingDto getById(@RequestHeader(USER_ID_HEADER) long userId,
                              @PathVariable long bookingId) {
        return bookingService.getById(userId, bookingId);
    }

    @GetMapping
    public List<BookingDto> getAllByBooker(@RequestHeader(USER_ID_HEADER) long userId,
                                           @RequestParam(name = "state", defaultValue = "ALL") String state) {
        return bookingService.getAllByBooker(userId, parseState(state));
    }

    @GetMapping("/owner")
    public List<BookingDto> getAllByOwner(@RequestHeader(USER_ID_HEADER) long ownerId,
                                          @RequestParam(name = "state", defaultValue = "ALL") String state) {
        return bookingService.getAllByOwner(ownerId, parseState(state));
    }

    private BookingState parseState(String state) {
        try {
            return BookingState.valueOf(state);
        } catch (IllegalArgumentException e) {
            throw new ValidationException("Unknown state: " + state);
        }
    }
}