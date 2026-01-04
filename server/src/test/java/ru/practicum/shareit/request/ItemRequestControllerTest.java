package ru.practicum.shareit.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestDtoOut;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ItemRequestController.class)
class ItemRequestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ItemRequestService itemRequestService;

    private static final String USER_ID_HEADER = "X-Sharer-User-Id";

    @Test
    void create_shouldReturn200AndCreatedRequest() throws Exception {
        ItemRequestDto inputDto = new ItemRequestDto();
        inputDto.setDescription("Need a drill");

        ItemRequestDtoOut responseDto = ItemRequestDtoOut.builder()
                .id(1L)
                .description("Need a drill")
                .created(LocalDateTime.now())
                .items(Collections.emptyList())
                .build();

        when(itemRequestService.create(anyLong(), any(ItemRequestDto.class)))
                .thenReturn(responseDto);

        mockMvc.perform(post("/requests")
                        .header(USER_ID_HEADER, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.description").value("Need a drill"))
                .andExpect(jsonPath("$.created").exists())
                .andExpect(jsonPath("$.items").isArray());
    }

    @Test
    void getOwn_shouldReturn200AndOwnRequests() throws Exception {
        ItemRequestDtoOut request1 = ItemRequestDtoOut.builder()
                .id(1L)
                .description("Need a drill")
                .created(LocalDateTime.now())
                .items(Collections.emptyList())
                .build();

        ItemRequestDtoOut request2 = ItemRequestDtoOut.builder()
                .id(2L)
                .description("Need a ladder")
                .created(LocalDateTime.now())
                .items(Collections.emptyList())
                .build();

        when(itemRequestService.getOwn(anyLong()))
                .thenReturn(List.of(request1, request2));

        mockMvc.perform(get("/requests")
                        .header(USER_ID_HEADER, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].description").value("Need a drill"))
                .andExpect(jsonPath("$[1].id").value(2L));
    }

    @Test
    void getOwn_shouldReturnEmptyListWhenNoRequests() throws Exception {
        when(itemRequestService.getOwn(anyLong()))
                .thenReturn(Collections.emptyList());

        mockMvc.perform(get("/requests")
                        .header(USER_ID_HEADER, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void getAll_shouldReturn200AndOtherUsersRequests() throws Exception {
        ItemRequestDtoOut.ItemDtoForRequest item = ItemRequestDtoOut.ItemDtoForRequest.builder()
                .id(1L)
                .name("Power Drill")
                .ownerId(2L)
                .build();

        ItemRequestDtoOut request = ItemRequestDtoOut.builder()
                .id(1L)
                .description("Need a drill")
                .created(LocalDateTime.now())
                .items(List.of(item))
                .build();

        when(itemRequestService.getAll(anyLong()))
                .thenReturn(List.of(request));

        mockMvc.perform(get("/requests/all")
                        .header(USER_ID_HEADER, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].items.length()").value(1))
                .andExpect(jsonPath("$[0].items[0].name").value("Power Drill"));
    }

    @Test
    void getAll_shouldReturnEmptyListWhenNoOtherRequests() throws Exception {
        when(itemRequestService.getAll(anyLong()))
                .thenReturn(Collections.emptyList());

        mockMvc.perform(get("/requests/all")
                        .header(USER_ID_HEADER, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void getById_shouldReturn200AndRequest() throws Exception {
        ItemRequestDtoOut.ItemDtoForRequest item = ItemRequestDtoOut.ItemDtoForRequest.builder()
                .id(1L)
                .name("Power Drill")
                .ownerId(2L)
                .build();

        ItemRequestDtoOut responseDto = ItemRequestDtoOut.builder()
                .id(1L)
                .description("Need a drill")
                .created(LocalDateTime.now())
                .items(List.of(item))
                .build();

        when(itemRequestService.getById(anyLong(), anyLong()))
                .thenReturn(responseDto);

        mockMvc.perform(get("/requests/1")
                        .header(USER_ID_HEADER, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.description").value("Need a drill"))
                .andExpect(jsonPath("$.created").exists())
                .andExpect(jsonPath("$.items.length()").value(1))
                .andExpect(jsonPath("$.items[0].id").value(1L))
                .andExpect(jsonPath("$.items[0].ownerId").value(2L));
    }

    @Test
    void getById_shouldReturnRequestWithoutItems() throws Exception {
        ItemRequestDtoOut responseDto = ItemRequestDtoOut.builder()
                .id(1L)
                .description("Need a drill")
                .created(LocalDateTime.now())
                .items(Collections.emptyList())
                .build();

        when(itemRequestService.getById(anyLong(), anyLong()))
                .thenReturn(responseDto);

        mockMvc.perform(get("/requests/1")
                        .header(USER_ID_HEADER, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.items.length()").value(0));
    }
}
