package ru.practicum.shareit.item.storage;

import org.springframework.stereotype.Repository;
import ru.practicum.shareit.item.model.Item;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class InMemoryItemRepository implements ItemRepository {

    private final Map<Long, Item> items = new HashMap<>();
    private final AtomicLong idSeq = new AtomicLong(0);

    @Override
    public Item save(Item item) {
        long id = idSeq.incrementAndGet();
        item.setId(id);
        items.put(id, item);
        return item;
    }

    @Override
    public Item update(Item item) {
        items.put(item.getId(), item);
        return item;
    }

    @Override
    public Optional<Item> findById(long itemId) {
        return Optional.ofNullable(items.get(itemId));
    }

    @Override
    public List<Item> findAllByOwnerId(long ownerId) {
        return items.values().stream()
                .filter(i -> Objects.equals(i.getOwnerId(), ownerId))
                .toList();
    }

    @Override
    public List<Item> searchByText(String text) {
        String q = text == null ? "" : text.toLowerCase();

        return items.values().stream()
                .filter(i -> containsIgnoreCase(i.getName(), q) || containsIgnoreCase(i.getDescription(), q))
                .toList();
    }

    private boolean containsIgnoreCase(String value, String textLower) {
        if (value == null || textLower.isBlank()) {
            return false;
        }
        return value.toLowerCase().contains(textLower);
    }
}