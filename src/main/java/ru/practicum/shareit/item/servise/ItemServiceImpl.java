package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.storage.BookingRepository;
import ru.practicum.shareit.common.ForbiddenException;
import ru.practicum.shareit.common.NotFoundException;
import ru.practicum.shareit.common.ValidationException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.CommentMapper;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.CommentRepository;
import ru.practicum.shareit.item.storage.ItemRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.storage.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;

    @Override
    @Transactional
    public ItemDto create(long ownerId, ItemDto itemDto) {
        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new NotFoundException("User not found: " + ownerId));
        validateCreate(itemDto);

        Item item = ItemMapper.toModel(itemDto);
        item.setOwner(owner);

        Item saved = itemRepository.save(item);
        log.debug("Created item id={}, ownerId={}", saved.getId(), ownerId);
        return ItemMapper.toDto(saved);
    }

    @Override
    @Transactional
    public ItemDto update(long ownerId, long itemId, ItemDto updateDto) {
        ensureUserExists(ownerId);

        Item existing = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Item not found: " + itemId));

        if (existing.getOwner() == null || !Objects.equals(existing.getOwner().getId(), ownerId)) {
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

        Item updated = itemRepository.save(existing);
        log.debug("Updated item id={}, ownerId={}", updated.getId(), ownerId);
        return ItemMapper.toDto(updated);
    }

    @Override
    public ItemDto getById(long requesterId, long itemId) {
        ensureUserExists(requesterId);
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Item not found: " + itemId));

        ItemDto dto = ItemMapper.toDto(item);
        dto.setComments(commentRepository.findAllByItemId(itemId).stream()
                .map(CommentMapper::toDto)
                .toList());

        if (item.getOwner() != null && Objects.equals(item.getOwner().getId(), requesterId)) {
            enrichWithBookings(dto, itemId);
        }

        return dto;
    }

    @Override
    public List<ItemDto> getAllByOwner(long ownerId) {
        ensureUserExists(ownerId);
        return itemRepository.findAllByOwnerId(ownerId).stream()
                .map(ItemMapper::toDto)
                .peek(dto -> {
                    enrichWithBookings(dto, dto.getId());
                    dto.setComments(commentRepository.findAllByItemId(dto.getId()).stream()
                            .map(CommentMapper::toDto)
                            .toList());
                })
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

    @Override
    @Transactional
    public CommentDto addComment(long userId, long itemId, CommentDto commentDto) {
        if (commentDto == null || commentDto.getText() == null || commentDto.getText().isBlank()) {
            throw new ValidationException("text must not be blank");
        }

        User author = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found: " + userId));
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Item not found: " + itemId));

        boolean hasBooking = bookingRepository.existsByItem_IdAndBooker_IdAndStatusAndEndBefore(
                itemId, userId, BookingStatus.APPROVED, LocalDateTime.now());
        if (!hasBooking) {
            throw new ValidationException("User has not completed booking for item: " + itemId);
        }

        Comment comment = Comment.builder()
                .text(commentDto.getText())
                .item(item)
                .author(author)
                .created(LocalDateTime.now())
                .build();

        return CommentMapper.toDto(commentRepository.save(comment));
    }

    private void ensureUserExists(long userId) {
        if (!userRepository.existsById(userId)) {
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

    private void enrichWithBookings(ItemDto dto, long itemId) {
        LocalDateTime now = LocalDateTime.now();

        Booking last = bookingRepository.findLastApproved(itemId, now).stream().findFirst().orElse(null);
        Booking next = bookingRepository.findNextApproved(itemId, now).stream().findFirst().orElse(null);

        dto.setLastBooking(BookingMapper.toShortDto(last));
        dto.setNextBooking(BookingMapper.toShortDto(next));
    }
}