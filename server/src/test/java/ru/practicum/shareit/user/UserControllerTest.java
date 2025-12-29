package ru.practicum.shareit.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @Test
    void getAllUsers_shouldReturn200AndUserList() throws Exception {
        UserDto user1 = new UserDto(1L, "User 1", "user1@test.com");
        UserDto user2 = new UserDto(2L, "User 2", "user2@test.com");

        when(userService.getAllUsers())
                .thenReturn(List.of(user1, user2));

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].name").value("User 1"))
                .andExpect(jsonPath("$[0].email").value("user1@test.com"))
                .andExpect(jsonPath("$[1].id").value(2L));
    }

    @Test
    void getUserById_shouldReturn200AndUser() throws Exception {
        UserDto userDto = new UserDto(1L, "John Doe", "john@test.com");

        when(userService.getUserById(anyLong()))
                .thenReturn(userDto);

        mockMvc.perform(get("/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("John Doe"))
                .andExpect(jsonPath("$.email").value("john@test.com"));
    }

    @Test
    void createUser_shouldReturn200AndCreatedUser() throws Exception {
        UserDto inputDto = new UserDto(null, "John Doe", "john@test.com");
        UserDto responseDto = new UserDto(1L, "John Doe", "john@test.com");

        when(userService.createUser(any(UserDto.class)))
                .thenReturn(responseDto);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("John Doe"))
                .andExpect(jsonPath("$.email").value("john@test.com"));
    }

    @Test
    void updateUser_shouldReturn200AndUpdatedUser() throws Exception {
        UserDto updateDto = new UserDto(null, "John Updated", "johnupdated@test.com");
        UserDto responseDto = new UserDto(1L, "John Updated", "johnupdated@test.com");

        when(userService.updateUser(anyLong(), any(UserDto.class)))
                .thenReturn(responseDto);

        mockMvc.perform(patch("/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("John Updated"))
                .andExpect(jsonPath("$.email").value("johnupdated@test.com"));
    }

    @Test
    void updateUser_shouldUpdateOnlyName() throws Exception {
        UserDto updateDto = new UserDto(null, "John Updated", null);
        UserDto responseDto = new UserDto(1L, "John Updated", "john@test.com");

        when(userService.updateUser(anyLong(), any(UserDto.class)))
                .thenReturn(responseDto);

        mockMvc.perform(patch("/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("John Updated"))
                .andExpect(jsonPath("$.email").value("john@test.com"));
    }

    @Test
    void updateUser_shouldUpdateOnlyEmail() throws Exception {
        UserDto updateDto = new UserDto(null, null, "newemail@test.com");
        UserDto responseDto = new UserDto(1L, "John Doe", "newemail@test.com");

        when(userService.updateUser(anyLong(), any(UserDto.class)))
                .thenReturn(responseDto);

        mockMvc.perform(patch("/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("John Doe"))
                .andExpect(jsonPath("$.email").value("newemail@test.com"));
    }

    @Test
    void deleteUser_shouldReturn200() throws Exception {
        doNothing().when(userService).deleteUser(anyLong());

        mockMvc.perform(delete("/users/1"))
                .andExpect(status().isOk());

        verify(userService, times(1)).deleteUser(1L);
    }
}