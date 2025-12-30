package ru.practicum.shareit.item.dto;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class ItemDtoJsonTest {

    @Autowired
    private JacksonTester<ItemDto> itemDtoJson;

    @Autowired
    private JacksonTester<CommentDto> commentDtoJson;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    @Test
    void testItemDtoSerialization() throws Exception {
        ItemDto dto = new ItemDto();
        dto.setId(1L);
        dto.setName("Test Item");
        dto.setDescription("Test Description");
        dto.setAvailable(true);
        dto.setRequestId(5L);

        JsonContent<ItemDto> result = itemDtoJson.write(dto);

        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.name").isEqualTo("Test Item");
        assertThat(result).extractingJsonPathStringValue("$.description").isEqualTo("Test Description");
        assertThat(result).extractingJsonPathBooleanValue("$.available").isTrue();
        assertThat(result).extractingJsonPathNumberValue("$.requestId").isEqualTo(5);
    }

    @Test
    void testItemDtoDeserialization() throws Exception {
        String json = """
                {
                    "id": 1,
                    "name": "Test Item",
                    "description": "Test Description",
                    "available": true,
                    "requestId": 5
                }
                """;

        ItemDto dto = itemDtoJson.parse(json).getObject();

        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getName()).isEqualTo("Test Item");
        assertThat(dto.getDescription()).isEqualTo("Test Description");
        assertThat(dto.getAvailable()).isTrue();
        assertThat(dto.getRequestId()).isEqualTo(5L);
    }

    @Test
    void testItemDtoWithBookingsAndComments() throws Exception {
        LocalDateTime now = LocalDateTime.now();

        CommentDto comment = new CommentDto();
        comment.setId(1L);
        comment.setText("Great item!");
        comment.setAuthorName("John Doe");
        comment.setCreated(now);

        ItemDto.BookingDtoShort lastBooking = new ItemDto.BookingDtoShort(10L, 99L);
        ItemDto.BookingDtoShort nextBooking = new ItemDto.BookingDtoShort(20L, 99L);

        ItemDto dto = new ItemDto();
        dto.setId(1L);
        dto.setName("Test Item");
        dto.setDescription("Test Description");
        dto.setAvailable(true);
        dto.setLastBooking(lastBooking); // Теперь типы совпадают
        dto.setNextBooking(nextBooking);
        dto.setComments(List.of(comment));

        JsonContent<ItemDto> result = itemDtoJson.write(dto);

        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(1);

        assertThat(result).extractingJsonPathNumberValue("$.lastBooking.id").isEqualTo(10);
        assertThat(result).extractingJsonPathNumberValue("$.lastBooking.bookerId").isEqualTo(99);

        assertThat(result).extractingJsonPathNumberValue("$.nextBooking.id").isEqualTo(20);
        assertThat(result).extractingJsonPathNumberValue("$.nextBooking.bookerId").isEqualTo(99);

        assertThat(result).extractingJsonPathArrayValue("$.comments").hasSize(1);
        assertThat(result).extractingJsonPathStringValue("$.comments[0].text").isEqualTo("Great item!");
    }

    @Test
    void testCommentDtoSerialization() throws Exception {
        LocalDateTime created = LocalDateTime.of(2025, 1, 15, 10, 30);

        CommentDto dto = new CommentDto();
        dto.setId(1L);
        dto.setText("Excellent item!");
        dto.setAuthorName("John Doe");
        dto.setCreated(created);

        JsonContent<CommentDto> result = commentDtoJson.write(dto);

        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.text").isEqualTo("Excellent item!");
        assertThat(result).extractingJsonPathStringValue("$.authorName").isEqualTo("John Doe");
        assertThat(result).extractingJsonPathStringValue("$.created")
                .isEqualTo(created.format(FORMATTER));
    }

    @Test
    void testCommentDtoDeserialization() throws Exception {
        String json = """
                {
                    "id": 1,
                    "text": "Excellent item!",
                    "authorName": "John Doe",
                    "created": "2025-01-15T10:30:00"
                }
                """;

        CommentDto dto = commentDtoJson.parse(json).getObject();

        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getText()).isEqualTo("Excellent item!");
        assertThat(dto.getAuthorName()).isEqualTo("John Doe");
        assertThat(dto.getCreated()).isEqualTo(LocalDateTime.of(2025, 1, 15, 10, 30));
    }

    @Test
    void testItemDtoWithNullOptionalFields() throws Exception {
        ItemDto dto = new ItemDto();
        dto.setId(1L);
        dto.setName("Test Item");
        dto.setDescription("Test Description");
        dto.setAvailable(true);
        dto.setComments(Collections.emptyList());

        JsonContent<ItemDto> result = itemDtoJson.write(dto);

        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.name").isEqualTo("Test Item");
        assertThat(result).doesNotHaveJsonPath("$.requestId");
        assertThat(result).doesNotHaveJsonPath("$.lastBooking");
        assertThat(result).doesNotHaveJsonPath("$.nextBooking");
    }

    @Test
    void testItemDtoAvailableFalse() throws Exception {
        ItemDto dto = new ItemDto();
        dto.setId(1L);
        dto.setName("Unavailable Item");
        dto.setDescription("Currently unavailable");
        dto.setAvailable(false);

        JsonContent<ItemDto> result = itemDtoJson.write(dto);

        assertThat(result).extractingJsonPathBooleanValue("$.available").isFalse();
    }
}