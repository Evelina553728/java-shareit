package ru.practicum.shareit.booking.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingCreateDto {
    @NotNull(message = "itemId must not be null")
    private Long itemId;

    @NotNull(message = "start must not be null")
    @Future(message = "start must be in the future")
    private LocalDateTime start;

    @NotNull(message = "end must not be null")
    @Future(message = "end must be in the future")
    private LocalDateTime end;
}