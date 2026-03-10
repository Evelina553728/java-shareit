package ru.practicum.shareit.booking;

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

import java.util.List;

@RestController
@RequestMapping("/bookings")
@RequiredArgsConstructor
public class BookingController {
    private static final String HEADER_USER_ID = "X-Sharer-User-Id";
    private final BookingService bookingService;

    @PostMapping
    public BookingDto create(@RequestHeader(HEADER_USER_ID) long userId, @RequestBody BookingCreateDto dto) {
        return bookingService.create(userId, dto);
    }

    @PatchMapping("/{bookingId}")
    public BookingDto approve(@RequestHeader(HEADER_USER_ID) long ownerId, @PathVariable long bookingId, @RequestParam boolean approved) {
        return bookingService.approve(ownerId, bookingId, approved);
    }

    @GetMapping("/{bookingId}")
    public BookingDto getById(@RequestHeader(HEADER_USER_ID) long userId, @PathVariable long bookingId) {
        return bookingService.getById(userId, bookingId);
    }

    @GetMapping
    public List<BookingDto> getAllByBooker(@RequestHeader(HEADER_USER_ID) long userId,
                                           @RequestParam(required = false, defaultValue = "ALL") String state,
                                           @RequestParam(required = false) Integer from,
                                           @RequestParam(required = false) Integer size) {
        return bookingService.getAllByBooker(userId, BookingState.from(state));
    }

    @GetMapping("/owner")
    public List<BookingDto> getAllByOwner(@RequestHeader(HEADER_USER_ID) long ownerId,
                                          @RequestParam(required = false, defaultValue = "ALL") String state,
                                          @RequestParam(required = false) Integer from,
                                          @RequestParam(required = false) Integer size) {
        return bookingService.getAllByOwner(ownerId, BookingState.from(state));
    }
}