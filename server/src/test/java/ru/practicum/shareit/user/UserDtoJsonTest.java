package ru.practicum.shareit.user;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class UserDtoJsonTest {

    @Autowired
    private JacksonTester<UserDto> userDtoJson;

    @Test
    void testUserDtoSerialization() throws Exception {
        UserDto dto = new UserDto(1L, "John Doe", "john@example.com");

        JsonContent<UserDto> result = userDtoJson.write(dto);

        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.name").isEqualTo("John Doe");
        assertThat(result).extractingJsonPathStringValue("$.email").isEqualTo("john@example.com");
    }

    @Test
    void testUserDtoDeserialization() throws Exception {
        String json = "{"
                + "\"id\": 1,"
                + "\"name\": \"John Doe\","
                + "\"email\": \"john@example.com\""
                + "}";

        UserDto dto = userDtoJson.parse(json).getObject();

        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getName()).isEqualTo("John Doe");
        assertThat(dto.getEmail()).isEqualTo("john@example.com");
    }

    @Test
    void testUserDtoWithNullId() throws Exception {
        UserDto dto = new UserDto(null, "John Doe", "john@example.com");

        JsonContent<UserDto> result = userDtoJson.write(dto);

        assertThat(result).extractingJsonPathStringValue("$.name").isEqualTo("John Doe");
        assertThat(result).extractingJsonPathStringValue("$.email").isEqualTo("john@example.com");
    }

    @Test
    void testUserDtoDeserializationWithNullId() throws Exception {
        String json = "{"
                + "\"name\": \"John Doe\","
                + "\"email\": \"john@example.com\""
                + "}";

        UserDto dto = userDtoJson.parse(json).getObject();

        assertThat(dto.getId()).isNull();
        assertThat(dto.getName()).isEqualTo("John Doe");
        assertThat(dto.getEmail()).isEqualTo("john@example.com");
    }

    @Test
    void testUserDtoWithDifferentEmailFormats() throws Exception {
        UserDto dto1 = new UserDto(1L, "User One", "user.one@example.com");
        UserDto dto2 = new UserDto(2L, "User Two", "user+test@example.co.uk");
        UserDto dto3 = new UserDto(3L, "User Three", "user_three@sub.example.org");

        JsonContent<UserDto> result1 = userDtoJson.write(dto1);
        JsonContent<UserDto> result2 = userDtoJson.write(dto2);
        JsonContent<UserDto> result3 = userDtoJson.write(dto3);

        assertThat(result1).extractingJsonPathStringValue("$.email")
                .isEqualTo("user.one@example.com");
        assertThat(result2).extractingJsonPathStringValue("$.email")
                .isEqualTo("user+test@example.co.uk");
        assertThat(result3).extractingJsonPathStringValue("$.email")
                .isEqualTo("user_three@sub.example.org");
    }

    @Test
    void testUserDtoWithLongName() throws Exception {
        String longName = "John Jacob Jingleheimer Schmidt The Third";
        UserDto dto = new UserDto(1L, longName, "john@example.com");

        JsonContent<UserDto> result = userDtoJson.write(dto);

        assertThat(result).extractingJsonPathStringValue("$.name").isEqualTo(longName);
    }

    @Test
    void testUserDtoRoundTrip() throws Exception {
        UserDto original = new UserDto(42L, "Alice Smith", "alice.smith@example.com");

        String json = userDtoJson.write(original).getJson();
        UserDto parsed = userDtoJson.parse(json).getObject();

        assertThat(parsed.getId()).isEqualTo(original.getId());
        assertThat(parsed.getName()).isEqualTo(original.getName());
        assertThat(parsed.getEmail()).isEqualTo(original.getEmail());
    }
}