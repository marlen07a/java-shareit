package ru.practicum.shareit.booking.dto;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.booking.BookingState;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.UserDto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class BookingDtoJsonTest {

    @Autowired
    private JacksonTester<BookingDto> bookingDtoJson;

    @Autowired
    private JacksonTester<BookingDtoOut> bookingDtoOutJson;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    @Test
    void testBookingDtoSerialization() throws Exception {
        LocalDateTime start = LocalDateTime.of(2025, 1, 15, 10, 0);
        LocalDateTime end = LocalDateTime.of(2025, 1, 20, 10, 0);

        BookingDto dto = new BookingDto(1L, start, end, 5L);

        JsonContent<BookingDto> result = bookingDtoJson.write(dto);

        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.start")
                .isEqualTo(start.format(FORMATTER));
        assertThat(result).extractingJsonPathStringValue("$.end")
                .isEqualTo(end.format(FORMATTER));
        assertThat(result).extractingJsonPathNumberValue("$.itemId").isEqualTo(5);
    }

    @Test
    void testBookingDtoDeserialization() throws Exception {
        String json = "{"
                + "\"id\": 1,"
                + "\"start\": \"2025-01-15T10:00:00\","
                + "\"end\": \"2025-01-20T10:00:00\","
                + "\"itemId\": 5"
                + "}";

        BookingDto dto = bookingDtoJson.parse(json).getObject();

        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getStart()).isEqualTo(LocalDateTime.of(2025, 1, 15, 10, 0));
        assertThat(dto.getEnd()).isEqualTo(LocalDateTime.of(2025, 1, 20, 10, 0));
        assertThat(dto.getItemId()).isEqualTo(5L);
    }

    @Test
    void testBookingDtoOutSerialization() throws Exception {
        LocalDateTime start = LocalDateTime.of(2025, 1, 15, 10, 0);
        LocalDateTime end = LocalDateTime.of(2025, 1, 20, 10, 0);

        BookingDtoOut dto = new BookingDtoOut();
        dto.setId(1L);
        dto.setStart(start);
        dto.setEnd(end);
        dto.setStatus(BookingState.APPROVED);

        ItemDto itemDto = new ItemDto();
        itemDto.setId(5L);
        itemDto.setName("Test Item");
        dto.setItem(itemDto);

        UserDto userDto = new UserDto(10L, "John Doe", "john@example.com");
        dto.setBooker(userDto);

        JsonContent<BookingDtoOut> result = bookingDtoOutJson.write(dto);

        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.start")
                .isEqualTo(start.format(FORMATTER));
        assertThat(result).extractingJsonPathStringValue("$.end")
                .isEqualTo(end.format(FORMATTER));
        assertThat(result).extractingJsonPathStringValue("$.status").isEqualTo("APPROVED");
        assertThat(result).extractingJsonPathNumberValue("$.item.id").isEqualTo(5);
        assertThat(result).extractingJsonPathStringValue("$.item.name").isEqualTo("Test Item");
        assertThat(result).extractingJsonPathNumberValue("$.booker.id").isEqualTo(10);
        assertThat(result).extractingJsonPathStringValue("$.booker.name").isEqualTo("John Doe");
    }

    @Test
    void testBookingDtoOutDeserialization() throws Exception {
        String json = "{"
                + "\"id\": 1,"
                + "\"start\": \"2025-01-15T10:00:00\","
                + "\"end\": \"2025-01-20T10:00:00\","
                + "\"status\": \"WAITING\","
                + "\"item\": {"
                + "\"id\": 5,"
                + "\"name\": \"Test Item\","
                + "\"description\": \"Test Description\","
                + "\"available\": true"
                + "},"
                + "\"booker\": {"
                + "\"id\": 10,"
                + "\"name\": \"John Doe\","
                + "\"email\": \"john@example.com\""
                + "}"
                + "}";

        BookingDtoOut dto = bookingDtoOutJson.parse(json).getObject();

        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getStart()).isEqualTo(LocalDateTime.of(2025, 1, 15, 10, 0));
        assertThat(dto.getEnd()).isEqualTo(LocalDateTime.of(2025, 1, 20, 10, 0));
        assertThat(dto.getStatus()).isEqualTo(BookingState.WAITING);
        assertThat(dto.getItem()).isNotNull();
        assertThat(dto.getItem().getId()).isEqualTo(5L);
        assertThat(dto.getItem().getName()).isEqualTo("Test Item");
        assertThat(dto.getBooker()).isNotNull();
        assertThat(dto.getBooker().getId()).isEqualTo(10L);
        assertThat(dto.getBooker().getName()).isEqualTo("John Doe");
    }

    @Test
    void testBookingDtoWithNullId() throws Exception {
        LocalDateTime start = LocalDateTime.of(2025, 1, 15, 10, 0);
        LocalDateTime end = LocalDateTime.of(2025, 1, 20, 10, 0);

        BookingDto dto = new BookingDto(null, start, end, 5L);

        JsonContent<BookingDto> result = bookingDtoJson.write(dto);

        assertThat(result).extractingJsonPathStringValue("$.start").isNotNull();
        assertThat(result).extractingJsonPathStringValue("$.end").isNotNull();
        assertThat(result).extractingJsonPathNumberValue("$.itemId").isEqualTo(5);
    }
}