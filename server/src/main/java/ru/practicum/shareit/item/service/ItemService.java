package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;

import java.util.List;

public interface ItemService {
    ItemDto create(long ownerId, ItemDto itemDto);

    ItemDto update(long ownerId, long itemId, ItemDto updateDto);

    ItemDto getById(long requesterId, long itemId);

    List<ItemDto> getAllByOwner(long ownerId);

    List<ItemDto> search(long requesterId, String text);

    CommentDto addComment(long userId, long itemId, CommentDto commentDto);
}