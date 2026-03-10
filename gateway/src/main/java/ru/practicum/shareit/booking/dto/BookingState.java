package ru.practicum.shareit.booking.dto;

import java.util.Optional;

public enum BookingState {
    ALL,
    CURRENT,
    PAST,
    FUTURE,
    WAITING,
    REJECTED;

    public static Optional<BookingState> from(String value) {
        if (value == null || value.isBlank()) {
            return Optional.of(ALL);
        }
        try {
            return Optional.of(BookingState.valueOf(value.trim().toUpperCase()));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }
}
