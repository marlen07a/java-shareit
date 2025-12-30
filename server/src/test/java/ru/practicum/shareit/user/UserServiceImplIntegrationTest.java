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
        UserDto userDto = new UserDto(null, "John Doe", "john@example.com");

        UserDto result = userService.createUser(userDto);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isNotNull();
        assertThat(result.getName()).isEqualTo("John Doe");
        assertThat(result.getEmail()).isEqualTo("john@example.com");
    }

    @Test
    void getAllUsers_shouldReturnAllUsers() {
        userService.createUser(new UserDto(null, "User 1", "user1@test.com"));
        userService.createUser(new UserDto(null, "User 2", "user2@test.com"));

        List<UserDto> result = userService.getAllUsers();

        assertThat(result).hasSize(2);
    }

    @Test
    void getUserById_shouldReturnUser() {
        UserDto created = userService.createUser(new UserDto(null, "John", "john@test.com"));

        UserDto result = userService.getUserById(created.getId());

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(created.getId());
        assertThat(result.getName()).isEqualTo("John");
    }

    @Test
    void getUserById_shouldThrowExceptionWhenNotFound() {
        assertThatThrownBy(() -> userService.getUserById(999L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    void updateUser_shouldUpdateSuccessfully() {
        UserDto created = userService.createUser(new UserDto(null, "John", "john@test.com"));
        UserDto updateDto = new UserDto(null, "John Updated", "johnupdated@test.com");

        UserDto result = userService.updateUser(created.getId(), updateDto);

        assertThat(result.getName()).isEqualTo("John Updated");
        assertThat(result.getEmail()).isEqualTo("johnupdated@test.com");
    }

    @Test
    void deleteUser_shouldDeleteSuccessfully() {
        UserDto created = userService.createUser(new UserDto(null, "John", "john@test.com"));

        userService.deleteUser(created.getId());

        assertThat(userRepository.findById(created.getId())).isEmpty();
    }
}