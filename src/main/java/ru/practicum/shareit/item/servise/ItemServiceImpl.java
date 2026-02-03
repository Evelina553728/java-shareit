package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.common.ForbiddenException;
import ru.practicum.shareit.common.NotFoundException;
import ru.practicum.shareit.common.ValidationException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.ItemRepository;
import ru.practicum.shareit.user.storage.UserRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ru.practicum.shareit.item.service.ItemService {

    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    @Override
    public ItemDto create(long ownerId, ItemDto itemDto) {
        ensureUserExists(ownerId);
        validateCreate(itemDto);

        Item item = ItemMapper.toModel(itemDto);
        item.setOwnerId(ownerId);
        return ItemMapper.toDto(itemRepository.save(item));
    }

    @Override
    public ItemDto update(long ownerId, long itemId, ItemDto updateDto) {
        ensureUserExists(ownerId);
        Item existing = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Item not found: " + itemId));

        if (existing.getOwnerId() == null || existing.getOwnerId() != ownerId) {
            throw new ForbiddenException("Only owner can update item: " + itemId);
        }

        if (updateDto.getName() != null) {
            if (updateDto.getName().isBlank()) {
                throw new ValidationException("name must not be blank");
            }
            existing.setName(updateDto.getName());
        }

        if (updateDto.getDescription() != null) {
            if (updateDto.getDescription().isBlank()) {
                throw new ValidationException("description must not be blank");
            }
            existing.setDescription(updateDto.getDescription());
        }

        if (updateDto.getAvailable() != null) {
            existing.setAvailable(updateDto.getAvailable());
        }

        if (updateDto.getRequestId() != null) {
            existing.setRequestId(updateDto.getRequestId());
        }

        return ItemMapper.toDto(itemRepository.update(existing));
    }

    @Override
    public ItemDto getById(long requesterId, long itemId) {
        ensureUserExists(requesterId);
        return itemRepository.findById(itemId)
                .map(ItemMapper::toDto)
                .orElseThrow(() -> new NotFoundException("Item not found: " + itemId));
    }

    @Override
    public List<ItemDto> getAllByOwner(long ownerId) {
        ensureUserExists(ownerId);
        return itemRepository.findAllByOwnerId(ownerId).stream()
                .map(ItemMapper::toDto)
                .toList();
    }

    @Override
    public List<ItemDto> search(long requesterId, String text) {
        ensureUserExists(requesterId);
        if (text == null || text.isBlank()) {
            return List.of();
        }
        return itemRepository.searchAvailableByText(text).stream()
                .map(ItemMapper::toDto)
                .toList();
    }

    private void ensureUserExists(long userId) {
        if (userRepository.findById(userId).isEmpty()) {
            throw new NotFoundException("User not found: " + userId);
        }
    }

    private void validateCreate(ItemDto dto) {
        if (dto == null) {
            throw new ValidationException("item must not be null");
        }
        if (dto.getName() == null || dto.getName().isBlank()) {
            throw new ValidationException("name must not be blank");
        }
        if (dto.getDescription() == null || dto.getDescription().isBlank()) {
            throw new ValidationException("description must not be blank");
        }
        if (dto.getAvailable() == null) {
            throw new ValidationException("available must not be null");
        }
    }
}