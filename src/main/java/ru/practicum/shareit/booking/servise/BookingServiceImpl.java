package ru.practicum.shareit.booking.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.storage.BookingRepository;
import ru.practicum.shareit.common.ForbiddenException;
import ru.practicum.shareit.common.NotFoundException;
import ru.practicum.shareit.common.ValidationException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.ItemRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.storage.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public BookingDto create(long userId, BookingCreateDto dto) {
        validateCreate(dto);

        User booker = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found: " + userId));

        Item item = itemRepository.findById(dto.getItemId())
                .orElseThrow(() -> new NotFoundException("Item not found: " + dto.getItemId()));

        if (item.getOwner() != null && item.getOwner().getId() != null
                && item.getOwner().getId().equals(userId)) {
            throw new NotFoundException("Owner cannot book own item: " + item.getId());
        }

        if (!Boolean.TRUE.equals(item.getAvailable())) {
            throw new ValidationException("Item is not available for booking: " + item.getId());
        }

        Booking booking = Booking.builder()
                .start(dto.getStart())
                .end(dto.getEnd())
                .item(item)
                .booker(booker)
                .status(BookingStatus.WAITING)
                .build();

        Booking saved = bookingRepository.save(booking);
        log.debug("Created booking id={}, itemId={}, bookerId={}", saved.getId(), item.getId(), userId);
        return BookingMapper.toDto(saved);
    }

    @Override
    @Transactional
    public BookingDto approve(long ownerId, long bookingId, boolean approved) {
        userRepository.findById(ownerId)
                .orElseThrow(() -> new NotFoundException("User not found: " + ownerId));

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Booking not found: " + bookingId));

        long realOwnerId = booking.getItem().getOwner().getId();
        if (realOwnerId != ownerId) {
            throw new ForbiddenException("Only owner can approve booking: " + bookingId);
        }

        if (booking.getStatus() != BookingStatus.WAITING) {
            throw new ValidationException("Booking already processed: " + bookingId);
        }

        booking.setStatus(approved ? BookingStatus.APPROVED : BookingStatus.REJECTED);
        Booking saved = bookingRepository.save(booking);
        log.debug("Booking {} set to {} by owner {}", bookingId, saved.getStatus(), ownerId);
        return BookingMapper.toDto(saved);
    }

    @Override
    public BookingDto getById(long userId, long bookingId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found: " + userId));

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Booking not found: " + bookingId));

        boolean isBooker = booking.getBooker().getId().equals(userId);
        boolean isOwner = booking.getItem().getOwner().getId().equals(userId);
        if (!isBooker && !isOwner) {
            throw new ForbiddenException("Access denied to booking: " + bookingId);
        }

        return BookingMapper.toDto(booking);
    }

    @Override
    public List<BookingDto> getAllByBooker(long userId, BookingState state) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found: " + userId));

        LocalDateTime now = LocalDateTime.now();
        List<Booking> bookings = switch (state) {
            case ALL -> bookingRepository.findAllByBooker(userId);
            case CURRENT -> bookingRepository.findCurrentByBooker(userId, now);
            case PAST -> bookingRepository.findPastByBooker(userId, now);
            case FUTURE -> bookingRepository.findFutureByBooker(userId, now);
            case WAITING -> bookingRepository.findByBookerAndStatus(userId, BookingStatus.WAITING);
            case REJECTED -> bookingRepository.findByBookerAndStatus(userId, BookingStatus.REJECTED);
        };

        return bookings.stream().map(BookingMapper::toDto).toList();
    }

    @Override
    public List<BookingDto> getAllByOwner(long ownerId, BookingState state) {
        userRepository.findById(ownerId)
                .orElseThrow(() -> new NotFoundException("User not found: " + ownerId));

        LocalDateTime now = LocalDateTime.now();
        List<Booking> bookings = switch (state) {
            case ALL -> bookingRepository.findAllByOwner(ownerId);
            case CURRENT -> bookingRepository.findCurrentByOwner(ownerId, now);
            case PAST -> bookingRepository.findPastByOwner(ownerId, now);
            case FUTURE -> bookingRepository.findFutureByOwner(ownerId, now);
            case WAITING -> bookingRepository.findByOwnerAndStatus(ownerId, BookingStatus.WAITING);
            case REJECTED -> bookingRepository.findByOwnerAndStatus(ownerId, BookingStatus.REJECTED);
        };

        return bookings.stream().map(BookingMapper::toDto).toList();
    }

    private void validateCreate(BookingCreateDto dto) {
        if (dto == null) {
            throw new ValidationException("booking must not be null");
        }
        if (dto.getItemId() == null) {
            throw new ValidationException("itemId must not be null");
        }
        if (dto.getStart() == null || dto.getEnd() == null) {
            throw new ValidationException("start/end must not be null");
        }
        if (!dto.getEnd().isAfter(dto.getStart())) {
            throw new ValidationException("end must be after start");
        }
    }
}