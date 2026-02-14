package ru.practicum.shareit.booking.mapper;

import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingShortDto;
import ru.practicum.shareit.booking.model.Booking;

public final class BookingMapper {

    private BookingMapper() {
    }

    public static BookingDto toDto(Booking booking) {
        if (booking == null) {
            return null;
        }
        return BookingDto.builder()
                .id(booking.getId())
                .start(booking.getStart())
                .end(booking.getEnd())
                .status(booking.getStatus())
                .booker(BookingDto.BookerDto.builder()
                        .id(booking.getBooker() != null ? booking.getBooker().getId() : null)
                        .build())
                .item(BookingDto.ItemInfoDto.builder()
                        .id(booking.getItem() != null ? booking.getItem().getId() : null)
                        .name(booking.getItem() != null ? booking.getItem().getName() : null)
                        .build())
                .build();
    }

    public static BookingShortDto toShortDto(Booking booking) {
        if (booking == null) {
            return null;
        }
        return BookingShortDto.builder()
                .id(booking.getId())
                .bookerId(booking.getBooker() != null ? booking.getBooker().getId() : null)
                .build();
    }
}