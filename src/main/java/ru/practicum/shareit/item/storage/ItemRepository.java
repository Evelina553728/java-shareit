package ru.practicum.shareit.item.storage;

import ru.practicum.shareit.item.model.Item;

import java.util.List;
import java.util.Optional;

public interface ItemRepository {
    Item save(Item item);

    Item update(Item item);

    Optional<Item> findById(long itemId);

    List<Item> findAllByOwnerId(long ownerId);

    List<Item> searchByText(String text);
}