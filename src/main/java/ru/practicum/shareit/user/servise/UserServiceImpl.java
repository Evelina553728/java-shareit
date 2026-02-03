package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.common.ConflictException;
import ru.practicum.shareit.common.NotFoundException;
import ru.practicum.shareit.common.ValidationException;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.storage.UserRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public UserDto create(UserDto userDto) {
        validateCreate(userDto);

        checkEmailUnique(userDto.getEmail(), null);

        User user = UserMapper.toModel(userDto);
        return UserMapper.toDto(userRepository.save(user));
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

            if (!updateDto.getEmail().equalsIgnoreCase(existing.getEmail())) {
                checkEmailUnique(updateDto.getEmail(), userId);
            }
            existing.setEmail(updateDto.getEmail());
        }

        return UserMapper.toDto(userRepository.update(existing));
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
        for (User user : userRepository.findAll()) {
            boolean sameEmail = user.getEmail() != null && user.getEmail().equalsIgnoreCase(email);
            boolean otherUser = currentUserId == null || user.getId() != currentUserId;

            if (sameEmail && otherUser) {
                throw new ConflictException("Email already exists: " + email);
            }
        }
    }
}