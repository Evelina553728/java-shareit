package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.common.NotFoundException;
import ru.practicum.shareit.common.ValidationException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.ItemRepository;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestItemDto;
import ru.practicum.shareit.user.storage.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemRequestServiceImpl implements ItemRequestService {

    private final ItemRequestRepository requestRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    @Override
    @Transactional
    public ru.practicum.shareit.request.dto.ItemRequestDto create(long userId, ItemRequestCreateDto dto) {
        ensureUserExists(userId);
        if (dto == null || dto.getDescription() == null || dto.getDescription().isBlank()) {
            throw new ValidationException("description must not be blank");
        }

        ItemRequest saved = requestRepository.save(ItemRequest.builder()
                .description(dto.getDescription())
                .requestorId(userId)
                .created(LocalDateTime.now())
                .build());
        log.debug("Created request id={}, userId={}", saved.getId(), userId);

        return ru.practicum.shareit.request.dto.ItemRequestDto.builder()
                .id(saved.getId())
                .description(saved.getDescription())
                .created(saved.getCreated())
                .items(List.of())
                .build();
    }

    @Override
    public List<ru.practicum.shareit.request.dto.ItemRequestDto> getOwn(long userId) {
        ensureUserExists(userId);
        return attachItems(requestRepository.findAllByRequestorIdOrderByCreatedDesc(userId));
    }

    @Override
    public List<ru.practicum.shareit.request.dto.ItemRequestDto> getOthers(long userId, int from, int size) {
        ensureUserExists(userId);
        if (from < 0 || size <= 0) {
            throw new ValidationException("from must be >= 0 and size must be > 0");
        }
        int page = from / size;
        return attachItems(requestRepository.findAllByRequestorIdNotOrderByCreatedDesc(userId, PageRequest.of(page, size)));
    }

    @Override
    public ru.practicum.shareit.request.dto.ItemRequestDto getById(long userId, long requestId) {
        ensureUserExists(userId);
        ItemRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Request not found: " + requestId));

        List<ru.practicum.shareit.request.dto.ItemRequestItemDto> items = itemRepository.findAllByRequestId(requestId).stream()
                .map(this::toRequestItemDto)
                .toList();

        return ru.practicum.shareit.request.dto.ItemRequestDto.builder()
                .id(request.getId())
                .description(request.getDescription())
                .created(request.getCreated())
                .items(items)
                .build();
    }

    private void ensureUserExists(long userId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("User not found: " + userId);
        }
    }

    private List<ru.practicum.shareit.request.dto.ItemRequestDto> attachItems(List<ItemRequest> requests) {
        if (requests.isEmpty()) {
            return List.of();
        }
        List<Long> requestIds = requests.stream().map(ItemRequest::getId).toList();
        Map<Long, List<ru.practicum.shareit.request.dto.ItemRequestItemDto>> itemsByRequest = itemRepository.findAllByRequestIdIn(requestIds).stream()
                .collect(Collectors.groupingBy(Item::getRequestId,
                        Collectors.mapping(this::toRequestItemDto, Collectors.toList())));

        return requests.stream()
                .map(r -> ItemRequestDto.builder()
                        .id(r.getId())
                        .description(r.getDescription())
                        .created(r.getCreated())
                        .items(itemsByRequest.getOrDefault(r.getId(), List.of()))
                        .build())
                .toList();
    }

    private ru.practicum.shareit.request.dto.ItemRequestItemDto toRequestItemDto(Item item) {
        return ItemRequestItemDto.builder()
                .id(item.getId())
                .name(item.getName())
                .ownerId(item.getOwner() != null ? item.getOwner().getId() : null)
                .build();
    }
}