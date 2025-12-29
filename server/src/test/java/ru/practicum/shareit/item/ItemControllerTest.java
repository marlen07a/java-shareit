package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ItemController.class)
class ItemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ItemService itemService;

    private static final String USER_ID_HEADER = "X-Sharer-User-Id";

    @Test
    void add_shouldReturn200AndItem() throws Exception {
        ItemDto itemDto = new ItemDto();
        itemDto.setName("Test Item");
        itemDto.setDescription("Test Description");
        itemDto.setAvailable(true);

        ItemDto responseDto = new ItemDto();
        responseDto.setId(1L);
        responseDto.setName("Test Item");
        responseDto.setDescription("Test Description");
        responseDto.setAvailable(true);

        when(itemService.addNewItem(anyLong(), any(ItemDto.class)))
                .thenReturn(responseDto);

        mockMvc.perform(post("/items")
                        .header(USER_ID_HEADER, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Test Item"))
                .andExpect(jsonPath("$.description").value("Test Description"))
                .andExpect(jsonPath("$.available").value(true));
    }

    @Test
    void update_shouldReturn200AndUpdatedItem() throws Exception {
        ItemDto updateDto = new ItemDto();
        updateDto.setName("Updated Name");

        ItemDto responseDto = new ItemDto();
        responseDto.setId(1L);
        responseDto.setName("Updated Name");
        responseDto.setDescription("Original Description");
        responseDto.setAvailable(true);

        when(itemService.updateItem(anyLong(), anyLong(), any(ItemDto.class)))
                .thenReturn(responseDto);

        mockMvc.perform(patch("/items/1")
                        .header(USER_ID_HEADER, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Updated Name"));
    }

    @Test
    void get_shouldReturn200AndItem() throws Exception {
        ItemDto itemDto = new ItemDto();
        itemDto.setId(1L);
        itemDto.setName("Test Item");
        itemDto.setDescription("Test Description");
        itemDto.setAvailable(true);
        itemDto.setComments(Collections.emptyList());

        when(itemService.getItemById(anyLong(), anyLong()))
                .thenReturn(itemDto);

        mockMvc.perform(get("/items/1")
                        .header(USER_ID_HEADER, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Test Item"));
    }

    @Test
    void getAll_shouldReturn200AndItemList() throws Exception {
        ItemDto item1 = new ItemDto();
        item1.setId(1L);
        item1.setName("Item 1");
        item1.setComments(Collections.emptyList());

        ItemDto item2 = new ItemDto();
        item2.setId(2L);
        item2.setName("Item 2");
        item2.setComments(Collections.emptyList());

        when(itemService.getItemsByOwner(anyLong()))
                .thenReturn(List.of(item1, item2));

        mockMvc.perform(get("/items")
                        .header(USER_ID_HEADER, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[1].id").value(2L));
    }

    @Test
    void getAll_shouldReturnEmptyListWhenNoItems() throws Exception {
        when(itemService.getItemsByOwner(anyLong()))
                .thenReturn(Collections.emptyList());

        mockMvc.perform(get("/items")
                        .header(USER_ID_HEADER, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void search_shouldReturn200AndMatchingItems() throws Exception {
        ItemDto item = new ItemDto();
        item.setId(1L);
        item.setName("Drill");
        item.setDescription("Power drill");
        item.setAvailable(true);

        when(itemService.searchItems(anyString()))
                .thenReturn(List.of(item));

        mockMvc.perform(get("/items/search")
                        .param("text", "drill"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Drill"));
    }

    @Test
    void search_shouldReturnEmptyListForBlankText() throws Exception {
        when(itemService.searchItems(anyString()))
                .thenReturn(Collections.emptyList());

        mockMvc.perform(get("/items/search")
                        .param("text", ""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void search_shouldHandleNoResults() throws Exception {
        when(itemService.searchItems(anyString()))
                .thenReturn(Collections.emptyList());

        mockMvc.perform(get("/items/search")
                        .param("text", "nonexistent"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void createComment_shouldReturn200AndComment() throws Exception {
        CommentDto commentDto = new CommentDto();
        commentDto.setText("Great item!");

        CommentDto responseDto = new CommentDto();
        responseDto.setId(1L);
        responseDto.setText("Great item!");
        responseDto.setAuthorName("User Name");
        responseDto.setCreated(LocalDateTime.now());

        when(itemService.addComment(anyLong(), anyLong(), any(CommentDto.class)))
                .thenReturn(responseDto);

        mockMvc.perform(post("/items/1/comment")
                        .header(USER_ID_HEADER, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.text").value("Great item!"))
                .andExpect(jsonPath("$.authorName").value("User Name"))
                .andExpect(jsonPath("$.created").exists());
    }
}