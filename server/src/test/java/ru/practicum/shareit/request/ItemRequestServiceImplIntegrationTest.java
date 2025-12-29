package ru.practicum.shareit.request;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestDtoOut;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class ItemRequestServiceImplIntegrationTest {

    @Autowired
    private ItemRequestService itemRequestService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ItemRequestRepository itemRequestRepository;

    @Autowired
    private ItemRepository itemRepository;

    private User requestor;
    private User otherUser;

    @BeforeEach
    void setUp() {
        requestor = new User();
        requestor.setName("Requestor");
        requestor.setEmail("requestor@test.com");
        requestor = userRepository.save(requestor);

        otherUser = new User();
        otherUser.setName("Other");
        otherUser.setEmail("other@test.com");
        otherUser = userRepository.save(otherUser);
    }

    @Test
    void create_shouldCreateRequestSuccessfully() {
        // Given
        ItemRequestDto dto = new ItemRequestDto();
        dto.setDescription("Need a drill");

        // When
        ItemRequestDtoOut result = itemRequestService.create(requestor.getId(), dto);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isNotNull();
        assertThat(result.getDescription()).isEqualTo("Need a drill");
        assertThat(result.getCreated()).isNotNull();
    }

    @Test
    void getOwn_shouldReturnOwnRequestsWithItems() {
        // Given
        ItemRequestDto dto = new ItemRequestDto();
        dto.setDescription("Need a drill");
        ItemRequestDtoOut created = itemRequestService.create(requestor.getId(), dto);

        ItemRequest request = itemRequestRepository.findById(created.getId()).orElseThrow();
        Item item = new Item();
        item.setName("Drill");
        item.setDescription("Power drill");
        item.setAvailable(true);
        item.setOwner(otherUser);
        item.setRequest(request);
        itemRepository.save(item);

        // When
        List<ItemRequestDtoOut> result = itemRequestService.getOwn(requestor.getId());

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getItems()).hasSize(1);
        assertThat(result.get(0).getItems().get(0).getName()).isEqualTo("Drill");
    }

    @Test
    void getAll_shouldReturnOtherUsersRequests() {
        // Given
        ItemRequestDto dto = new ItemRequestDto();
        dto.setDescription("Need a drill");
        itemRequestService.create(requestor.getId(), dto);

        // When
        List<ItemRequestDtoOut> result = itemRequestService.getAll(otherUser.getId());

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getDescription()).isEqualTo("Need a drill");
    }

    @Test
    void getAll_shouldNotReturnOwnRequests() {
        // Given
        ItemRequestDto dto = new ItemRequestDto();
        dto.setDescription("Need a drill");
        itemRequestService.create(requestor.getId(), dto);

        // When
        List<ItemRequestDtoOut> result = itemRequestService.getAll(requestor.getId());

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void getById_shouldReturnRequestWithItems() {
        // Given
        ItemRequestDto dto = new ItemRequestDto();
        dto.setDescription("Need a drill");
        ItemRequestDtoOut created = itemRequestService.create(requestor.getId(), dto);

        // When
        ItemRequestDtoOut result = itemRequestService.getById(otherUser.getId(), created.getId());

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(created.getId());
        assertThat(result.getDescription()).isEqualTo("Need a drill");
    }

    @Test
    void getById_shouldThrowExceptionWhenNotFound() {
        // When & Then
        assertThatThrownBy(() -> itemRequestService.getById(requestor.getId(), 999L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Request not found");
    }
}