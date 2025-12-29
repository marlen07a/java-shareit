package ru.practicum.shareit.user;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.NotFoundException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class UserServiceImplIntegrationTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Test
    void createUser_shouldCreateSuccessfully() {
        // Given
        UserDto userDto = new UserDto(null, "John Doe", "john@example.com");

        // When
        UserDto result = userService.createUser(userDto);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isNotNull();
        assertThat(result.getName()).isEqualTo("John Doe");
        assertThat(result.getEmail()).isEqualTo("john@example.com");
    }

    @Test
    void getAllUsers_shouldReturnAllUsers() {
        // Given
        userService.createUser(new UserDto(null, "User 1", "user1@test.com"));
        userService.createUser(new UserDto(null, "User 2", "user2@test.com"));

        // When
        List<UserDto> result = userService.getAllUsers();

        // Then
        assertThat(result).hasSize(2);
    }

    @Test
    void getUserById_shouldReturnUser() {
        // Given
        UserDto created = userService.createUser(new UserDto(null, "John", "john@test.com"));

        // When
        UserDto result = userService.getUserById(created.getId());

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(created.getId());
        assertThat(result.getName()).isEqualTo("John");
    }

    @Test
    void getUserById_shouldThrowExceptionWhenNotFound() {
        // When & Then
        assertThatThrownBy(() -> userService.getUserById(999L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    void updateUser_shouldUpdateSuccessfully() {
        // Given
        UserDto created = userService.createUser(new UserDto(null, "John", "john@test.com"));
        UserDto updateDto = new UserDto(null, "John Updated", "johnupdated@test.com");

        // When
        UserDto result = userService.updateUser(created.getId(), updateDto);

        // Then
        assertThat(result.getName()).isEqualTo("John Updated");
        assertThat(result.getEmail()).isEqualTo("johnupdated@test.com");
    }

    @Test
    void deleteUser_shouldDeleteSuccessfully() {
        // Given
        UserDto created = userService.createUser(new UserDto(null, "John", "john@test.com"));

        // When
        userService.deleteUser(created.getId());

        // Then
        assertThat(userRepository.findById(created.getId())).isEmpty();
    }
}