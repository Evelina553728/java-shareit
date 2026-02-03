package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.common.NotFoundException;
import ru.practicum.shareit.common.ValidationException;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.storage.UserRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements ru.practicum.shareit.user.service.UserService {

    private final UserRepository userRepository;

    @Override
    public UserDto create(UserDto userDto) {
        validateCreate(userDto);
        User user = UserMapper.toModel(userDto);
        return UserMapper.toDto(userRepository.save(user));
    }

    @Override
    public UserDto update(long userId, UserDto updateDto) {
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
    }
}