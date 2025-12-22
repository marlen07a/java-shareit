package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.NotFoundException;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository repository;

    @Override
    public List<UserDto> getAllUsers() {
        return repository.findAll().stream()
                .map(UserMapper::toUserDto)
                .collect(Collectors.toList());
    }

    @Override
    public UserDto getUserById(Long id) {
        User user = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found"));
        return UserMapper.toUserDto(user);
    }

    @Override
    public UserDto createUser(UserDto userDto) {
        if (!repository.isEmailUnique(userDto.getEmail(), null)) {
            throw new RuntimeException("Email already exists");
        }
        User user = UserMapper.toUser(userDto);
        return UserMapper.toUserDto(repository.save(user));
    }

    public UserDto updateUser(Long id, UserDto userDto) {
        User user = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found"));

        if (userDto.getEmail() != null && !repository.isEmailUnique(userDto.getEmail(), id)) {
            throw new RuntimeException("Email already exists");
        }

        if (userDto.getName() != null) user.setName(userDto.getName());
        if (userDto.getEmail() != null) user.setEmail(userDto.getEmail());

        return UserMapper.toUserDto(repository.save(user));
    }

    @Override
    public void deleteUser(Long id) {
        repository.deleteById(id);
    }
}
