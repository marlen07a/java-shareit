package ru.practicum.shareit.request.dto;

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
class ItemRequestDtoJsonTest {

    @Autowired
    private JacksonTester<ItemRequestDto> itemRequestDtoJson;

    @Autowired
    private JacksonTester<ItemRequestDtoOut> itemRequestDtoOutJson;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    @Test
    void testItemRequestDtoSerialization() throws Exception {
        LocalDateTime created = LocalDateTime.of(2025, 1, 15, 10, 0);

        ItemRequestDto dto = new ItemRequestDto();
        dto.setId(1L);
        dto.setDescription("Need a power drill");
        dto.setRequestorId(5L);
        dto.setCreated(created);

        JsonContent<ItemRequestDto> result = itemRequestDtoJson.write(dto);

        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.description")
                .isEqualTo("Need a power drill");
        assertThat(result).extractingJsonPathNumberValue("$.requestorId").isEqualTo(5);
        assertThat(result).extractingJsonPathStringValue("$.created")
                .isEqualTo(created.format(FORMATTER));
    }

    @Test
    void testItemRequestDtoDeserialization() throws Exception {
        String json = "{"
                + "\"id\": 1,"
                + "\"description\": \"Need a power drill\","
                + "\"requestorId\": 5,"
                + "\"created\": \"2025-01-15T10:00:00\""
                + "}";

        ItemRequestDto dto = itemRequestDtoJson.parse(json).getObject();

        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getDescription()).isEqualTo("Need a power drill");
        assertThat(dto.getRequestorId()).isEqualTo(5L);
        assertThat(dto.getCreated()).isEqualTo(LocalDateTime.of(2025, 1, 15, 10, 0));
    }

    @Test
    void testItemRequestDtoOutSerialization() throws Exception {
        LocalDateTime created = LocalDateTime.of(2025, 1, 15, 10, 0);

        ItemRequestDtoOut.ItemDtoForRequest item1 = ItemRequestDtoOut.ItemDtoForRequest.builder()
                .id(10L)
                .name("Power Drill")
                .ownerId(20L)
                .build();

        ItemRequestDtoOut.ItemDtoForRequest item2 = ItemRequestDtoOut.ItemDtoForRequest.builder()
                .id(11L)
                .name("Hammer Drill")
                .ownerId(21L)
                .build();

        ItemRequestDtoOut dto = ItemRequestDtoOut.builder()
                .id(1L)
                .description("Need a power drill")
                .created(created)
                .items(List.of(item1, item2))
                .build();

        JsonContent<ItemRequestDtoOut> result = itemRequestDtoOutJson.write(dto);

        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.description")
                .isEqualTo("Need a power drill");
        assertThat(result).extractingJsonPathStringValue("$.created")
                .isEqualTo(created.format(FORMATTER));
        assertThat(result).extractingJsonPathArrayValue("$.items").hasSize(2);
        assertThat(result).extractingJsonPathNumberValue("$.items[0].id").isEqualTo(10);
        assertThat(result).extractingJsonPathStringValue("$.items[0].name")
                .isEqualTo("Power Drill");
        assertThat(result).extractingJsonPathNumberValue("$.items[0].ownerId").isEqualTo(20);
        assertThat(result).extractingJsonPathNumberValue("$.items[1].id").isEqualTo(11);
    }

    @Test
    void testItemRequestDtoOutDeserialization() throws Exception {
        String json = "{"
                + "\"id\": 1,"
                + "\"description\": \"Need a power drill\","
                + "\"created\": \"2025-01-15T10:00:00\","
                + "\"items\": ["
                + "{"
                + "\"id\": 10,"
                + "\"name\": \"Power Drill\","
                + "\"ownerId\": 20"
                + "}"
                + "]"
                + "}";

        ItemRequestDtoOut dto = itemRequestDtoOutJson.parse(json).getObject();

        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getDescription()).isEqualTo("Need a power drill");
        assertThat(dto.getCreated()).isEqualTo(LocalDateTime.of(2025, 1, 15, 10, 0));
        assertThat(dto.getItems()).hasSize(1);
        assertThat(dto.getItems().get(0).getId()).isEqualTo(10L);
        assertThat(dto.getItems().get(0).getName()).isEqualTo("Power Drill");
        assertThat(dto.getItems().get(0).getOwnerId()).isEqualTo(20L);
    }

    @Test
    void testItemRequestDtoOutWithEmptyItems() throws Exception {
        LocalDateTime created = LocalDateTime.of(2025, 1, 15, 10, 0);

        ItemRequestDtoOut dto = ItemRequestDtoOut.builder()
                .id(1L)
                .description("Need a power drill")
                .created(created)
                .items(Collections.emptyList())
                .build();

        JsonContent<ItemRequestDtoOut> result = itemRequestDtoOutJson.write(dto);

        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(result).extractingJsonPathArrayValue("$.items").isEmpty();
    }

    @Test
    void testItemRequestDtoWithNullFields() throws Exception {
        ItemRequestDto dto = new ItemRequestDto();
        dto.setDescription("Need something");

        JsonContent<ItemRequestDto> result = itemRequestDtoJson.write(dto);

        assertThat(result).extractingJsonPathStringValue("$.description")
                .isEqualTo("Need something");
    }

    @Test
    void testItemDtoForRequestSerialization() throws Exception {
        ItemRequestDtoOut.ItemDtoForRequest item = ItemRequestDtoOut.ItemDtoForRequest.builder()
                .id(100L)
                .name("Electric Drill XL2000")
                .ownerId(50L)
                .build();

        ItemRequestDtoOut dto = ItemRequestDtoOut.builder()
                .id(1L)
                .description("Request")
                .created(LocalDateTime.now())
                .items(List.of(item))
                .build();

        JsonContent<ItemRequestDtoOut> result = itemRequestDtoOutJson.write(dto);

        assertThat(result).extractingJsonPathNumberValue("$.items[0].id").isEqualTo(100);
        assertThat(result).extractingJsonPathStringValue("$.items[0].name")
                .isEqualTo("Electric Drill XL2000");
        assertThat(result).extractingJsonPathNumberValue("$.items[0].ownerId").isEqualTo(50);
    }

    @Test
    void testItemRequestDtoRoundTrip() throws Exception {
        LocalDateTime created = LocalDateTime.of(2025, 1, 15, 10, 0);

        ItemRequestDto original = new ItemRequestDto();
        original.setId(1L);
        original.setDescription("Need a ladder");
        original.setRequestorId(42L);
        original.setCreated(created);

        String json = itemRequestDtoJson.write(original).getJson();
        ItemRequestDto parsed = itemRequestDtoJson.parse(json).getObject();

        assertThat(parsed.getId()).isEqualTo(original.getId());
        assertThat(parsed.getDescription()).isEqualTo(original.getDescription());
        assertThat(parsed.getRequestorId()).isEqualTo(original.getRequestorId());
        assertThat(parsed.getCreated()).isEqualTo(original.getCreated());
    }
}