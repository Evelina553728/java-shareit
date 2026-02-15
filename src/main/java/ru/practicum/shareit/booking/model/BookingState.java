package ru.practicum.shareit.booking.model;

import ru.practicum.shareit.common.ValidationException;

public enum BookingState {
    ALL,
    CURRENT,
    PAST,
    FUTURE,
    WAITING,
    REJECTED;

    public static BookingState from(String value) {
        if (value == null || value.isBlank()) {
            return ALL;
        }
        try {
            return BookingState.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ValidationException("Unknown state: " + value);
        }
    }
}