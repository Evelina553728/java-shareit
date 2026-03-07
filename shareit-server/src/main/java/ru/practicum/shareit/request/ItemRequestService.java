package ru.practicum.shareit.request;

import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;

import java.util.List;

public interface ItemRequestService {
    ru.practicum.shareit.request.dto.ItemRequestDto create(long userId, ItemRequestCreateDto dto);

    List<ru.practicum.shareit.request.dto.ItemRequestDto> getOwn(long userId);

    List<ru.practicum.shareit.request.dto.ItemRequestDto> getOthers(long userId, int from, int size);

    ItemRequestDto getById(long userId, long requestId);
}