package ru.practicum.shareit.booking.service;

import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.model.BookingState;

import java.util.List;

public interface BookingService {
    BookingDto create(long userId, BookingCreateDto dto);

    BookingDto approve(long ownerId, long bookingId, boolean approved);

    BookingDto getById(long userId, long bookingId);

    List<BookingDto> getAllByBooker(long userId, BookingState state);

    List<BookingDto> getAllByOwner(long ownerId, BookingState state);
}