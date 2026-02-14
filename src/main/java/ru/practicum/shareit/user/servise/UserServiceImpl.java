package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.common.ConflictException;
import ru.practicum.shareit.common.NotFoundException;
import ru.practicum.shareit.common.ValidationException;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.storage.UserRepository;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public UserDto create(UserDto userDto) {
        validateCreate(userDto);
        checkEmailUnique(userDto.getEmail(), null);

        User user = UserMapper.toModel(userDto);
        User saved = userRepository.save(user);

        log.debug("Created user id={}, email={}", saved.getId(), saved.getEmail());
        return UserMapper.toDto(saved);
    }

    @Override
    public UserDto update(long userId, UserDto updateDto) {
        if (updateDto == null) {
            throw new ValidationException("user must not be null");
        }

        User existing = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found: " + userId));

        if (updateDto.getName() != null) {
            if (updateDto.getName().isBlank()) {
                throw new ValidationException("name must not be blank");
            }
            existing.setName(updateDto.getName());
        }

        if (updateDto.getEmail() != null) {
            if (updateDto.getEmail().isBlank()) {
                throw new ValidationException("email must not be blank");
            }
            validateEmailFormat(updateDto.getEmail());

            if (existing.getEmail() == null || !updateDto.getEmail().equalsIgnoreCase(existing.getEmail())) {
                checkEmailUnique(updateDto.getEmail(), userId);
            }
            existing.setEmail(updateDto.getEmail());
        }

        User updated = userRepository.save(existing);

        log.debug("Updated user id={}, email={}", updated.getId(), updated.getEmail());
        return UserMapper.toDto(updated);
    }

    @Override
    public UserDto getById(long userId) {
        return userRepository.findById(userId)
                .map(UserMapper::toDto)
                .orElseThrow(() -> new NotFoundException("User not found: " + userId));
    }

    @Override
    public List<UserDto> getAll() {
        return userRepository.findAll().stream()
                .map(UserMapper::toDto)
                .toList();
    }

    @Override
    public void delete(long userId) {
        if (userRepository.findById(userId).isEmpty()) {
            throw new NotFoundException("User not found: " + userId);
        }
        userRepository.deleteById(userId);
        log.debug("Deleted user id={}", userId);
    }

    private void validateCreate(UserDto dto) {
        if (dto == null) {
            throw new ValidationException("user must not be null");
        }
        if (dto.getName() == null || dto.getName().isBlank()) {
            throw new ValidationException("name must not be blank");
        }
        if (dto.getEmail() == null || dto.getEmail().isBlank()) {
            throw new ValidationException("email must not be blank");
        }
        validateEmailFormat(dto.getEmail());
    }

    private void validateEmailFormat(String email) {
        int at = email.indexOf('@');
        if (at <= 0 || at != email.lastIndexOf('@') || at == email.length() - 1) {
            throw new ValidationException("email must be valid");
        }
        int dotAfterAt = email.indexOf('.', at);
        if (dotAfterAt == -1 || dotAfterAt == at + 1 || dotAfterAt == email.length() - 1) {
            throw new ValidationException("email must be valid");
        }
    }

    private void checkEmailUnique(String email, Long currentUserId) {
        userRepository.findByEmailIgnoreCase(email)
                .filter(u -> currentUserId == null || !u.getId().equals(currentUserId))
                .ifPresent(u -> {
                    throw new ConflictException("Email already exists: " + email);
                });
    }
}