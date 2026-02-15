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
        validateCreate(itemDto);

        User owner = getUserOrThrow(ownerId);
        Item item = ItemMapper.toModel(itemDto);
        item.setOwner(owner);

        Item saved = itemRepository.save(item);
        log.debug("Item created: id={}, ownerId={}", saved.getId(), ownerId);

        return ItemMapper.toDto(saved);
    }

    @Override
    @Transactional
    public ItemDto update(long ownerId, long itemId, ItemDto updateDto) {
        getUserOrThrow(ownerId);
        Item existing = getItemOrThrow(itemId);

        validateOwner(existing, ownerId);
        applyItemUpdate(existing, updateDto);

        Item updated = itemRepository.save(existing);
        log.debug("Item updated: id={}, ownerId={}", updated.getId(), ownerId);

        return ItemMapper.toDto(updated);
    }

    @Override
    public ItemDto getById(long requesterId, long itemId) {
        getUserOrThrow(requesterId);
        Item item = getItemOrThrow(itemId);

        ItemDto dto = ItemMapper.toDto(item);
        dto.setComments(getItemComments(itemId));

        if (isOwner(item, requesterId)) {
            enrichWithBookings(dto, itemId);
        }

        log.debug("Item getById: itemId={}, requesterId={}, ownerView={}",
                itemId, requesterId, isOwner(item, requesterId));

        return dto;
    }

    @Override
    public List<ItemDto> getAllByOwner(long ownerId) {
        getUserOrThrow(ownerId);

        List<ItemDto> result = itemRepository.findAllByOwnerId(ownerId).stream()
                .map(ItemMapper::toDto)
                .map(dto -> enrichItemForOwnerView(dto, dto.getId()))
                .toList();

        log.debug("Items getAllByOwner: ownerId={}, count={}", ownerId, result.size());
        return result;
    }

    @Override
    public List<ItemDto> search(long requesterId, String text) {
        getUserOrThrow(requesterId);

        if (text == null || text.isBlank()) {
            return List.of();
        }

        List<ItemDto> result = itemRepository.searchAvailableByText(text).stream()
                .map(ItemMapper::toDto)
                .toList();

        log.debug("Items search: requesterId={}, text='{}', count={}", requesterId, text, result.size());
        return result;
    }

    @Override
    @Transactional
    public CommentDto addComment(long userId, long itemId, CommentDto commentDto) {
        validateComment(commentDto);

        User author = getUserOrThrow(userId);
        Item item = getItemOrThrow(itemId);

        ensureUserHasCompletedBooking(userId, itemId);

        Comment comment = buildComment(commentDto.getText(), item, author);
        Comment saved = commentRepository.save(comment);

        log.debug("Comment added: id={}, itemId={}, authorId={}", saved.getId(), itemId, userId);
        return CommentMapper.toDto(saved);
    }

    private User getUserOrThrow(long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found: " + userId));
    }

    private Item getItemOrThrow(long itemId) {
        return itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Item not found: " + itemId));
    }

    private void validateOwner(Item item, long ownerId) {
        if (item.getOwner() == null || !Objects.equals(item.getOwner().getId(), ownerId)) {
            throw new ForbiddenException("Only owner can update item: " + item.getId());
        }
    }

    private boolean isOwner(Item item, long userId) {
        return item.getOwner() != null && Objects.equals(item.getOwner().getId(), userId);
    }

    private void applyItemUpdate(Item existing, ItemDto updateDto) {
        if (updateDto == null) {
            return;
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
    }

    private List<CommentDto> getItemComments(long itemId) {
        return commentRepository.findAllByItemId(itemId).stream()
                .map(CommentMapper::toDto)
                .toList();
    }

    private ItemDto enrichItemForOwnerView(ItemDto dto, long itemId) {
        enrichWithBookings(dto, itemId);
        dto.setComments(getItemComments(itemId));
        return dto;
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

    private void validateComment(CommentDto commentDto) {
        if (commentDto == null || commentDto.getText() == null || commentDto.getText().isBlank()) {
            throw new ValidationException("text must not be blank");
        }
    }

    private void ensureUserHasCompletedBooking(long userId, long itemId) {
        boolean hasBooking = bookingRepository.existsByItem_IdAndBooker_IdAndStatusAndEndBefore(
                itemId, userId, BookingStatus.APPROVED, LocalDateTime.now()
        );
        if (!hasBooking) {
            throw new ValidationException("User has not completed booking for item: " + itemId);
        }
    }

    private Comment buildComment(String text, Item item, User author) {
        return Comment.builder()
                .text(text)
                .item(item)
                .author(author)
                .created(LocalDateTime.now())
                .build();
    }

    private void enrichWithBookings(ItemDto dto, long itemId) {
        LocalDateTime now = LocalDateTime.now();

        Booking last = bookingRepository.findLastApproved(itemId, now).stream().findFirst().orElse(null);
        Booking next = bookingRepository.findNextApproved(itemId, now).stream().findFirst().orElse(null);

        dto.setLastBooking(BookingMapper.toShortDto(last));
        dto.setNextBooking(BookingMapper.toShortDto(next));
    }
}