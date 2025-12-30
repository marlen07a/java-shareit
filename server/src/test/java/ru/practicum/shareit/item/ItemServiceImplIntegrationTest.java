package ru.practicum.shareit.item;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.BookingState;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class ItemServiceImplIntegrationTest {

    @Autowired
    private ItemService itemService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private CommentRepository commentRepository;

    private User owner;
    private User booker;

    @BeforeEach
    void setUp() {
        owner = new User();
        owner.setName("Owner");
        owner.setEmail("owner@test.com");
        owner = userRepository.save(owner);

        booker = new User();
        booker.setName("Booker");
        booker.setEmail("booker@test.com");
        booker = userRepository.save(booker);
    }

    @Test
    void addNewItem_shouldCreateItemSuccessfully() {
        ItemDto itemDto = new ItemDto();
        itemDto.setName("Test Item");
        itemDto.setDescription("Test Description");
        itemDto.setAvailable(true);

        ItemDto result = itemService.addNewItem(owner.getId(), itemDto);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isNotNull();
        assertThat(result.getName()).isEqualTo("Test Item");
        assertThat(result.getDescription()).isEqualTo("Test Description");
        assertThat(result.getAvailable()).isTrue();
    }

    @Test
    void getItemsByOwner_shouldReturnItemsWithBookingsAndComments() {
        Item item = createAndSaveItem();
        Booking pastBooking = createAndSaveBooking(item, LocalDateTime.now().minusDays(2), LocalDateTime.now().minusDays(1));
        Booking futureBooking = createAndSaveBooking(item, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2));
        Comment comment = createAndSaveComment(item);

        List<ItemDto> result = itemService.getItemsByOwner(owner.getId());

        assertThat(result).hasSize(1);
        ItemDto itemDto = result.get(0);
        assertThat(itemDto.getId()).isEqualTo(item.getId());
        assertThat(itemDto.getLastBooking()).isNotNull();
        assertThat(itemDto.getLastBooking().getId()).isEqualTo(pastBooking.getId());
        assertThat(itemDto.getNextBooking()).isNotNull();
        assertThat(itemDto.getNextBooking().getId()).isEqualTo(futureBooking.getId());
        assertThat(itemDto.getComments()).hasSize(1);
        assertThat(itemDto.getComments().get(0).getText()).isEqualTo(comment.getText());
    }

    @Test
    void updateItem_shouldUpdateSuccessfully() {
        Item item = createAndSaveItem();
        ItemDto updateDto = new ItemDto();
        updateDto.setName("Updated Name");
        updateDto.setDescription("Updated Description");

        ItemDto result = itemService.updateItem(owner.getId(), item.getId(), updateDto);

        assertThat(result.getName()).isEqualTo("Updated Name");
        assertThat(result.getDescription()).isEqualTo("Updated Description");
    }

    @Test
    void updateItem_shouldThrowExceptionWhenNotOwner() {
        Item item = createAndSaveItem();
        ItemDto updateDto = new ItemDto();
        updateDto.setName("Updated Name");

        assertThatThrownBy(() -> itemService.updateItem(booker.getId(), item.getId(), updateDto))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("not the owner");
    }

    @Test
    void searchItems_shouldReturnMatchingItems() {
        createAndSaveItem();

        List<ItemDto> result = itemService.searchItems("drill");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).containsIgnoringCase("drill");
    }

    @Test
    void searchItems_shouldReturnEmptyListForBlankText() {
        List<ItemDto> result = itemService.searchItems("");

        assertThat(result).isEmpty();
    }

    @Test
    void addComment_shouldCreateCommentSuccessfully() {
        Item item = createAndSaveItem();
        Booking booking = createAndSaveBooking(item, LocalDateTime.now().minusDays(2), LocalDateTime.now().minusDays(1));

        CommentDto commentDto = new CommentDto();
        commentDto.setText("Great item!");

        CommentDto result = itemService.addComment(booker.getId(), item.getId(), commentDto);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isNotNull();
        assertThat(result.getText()).isEqualTo("Great item!");
        assertThat(result.getAuthorName()).isEqualTo(booker.getName());
        assertThat(result.getCreated()).isNotNull();
    }

    @Test
    void addComment_shouldThrowExceptionWhenNoCompletedBooking() {
        Item item = createAndSaveItem();
        CommentDto commentDto = new CommentDto();
        commentDto.setText("Great item!");

        assertThatThrownBy(() -> itemService.addComment(booker.getId(), item.getId(), commentDto))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Booking not found or not suitable for comment");
    }

    private Item createAndSaveItem() {
        Item item = new Item();
        item.setName("Power Drill");
        item.setDescription("Electric drill for home use");
        item.setAvailable(true);
        item.setOwner(owner);
        return itemRepository.save(item);
    }

    private Booking createAndSaveBooking(Item item, LocalDateTime start, LocalDateTime end) {
        Booking booking = new Booking();
        booking.setStart(start);
        booking.setEnd(end);
        booking.setItem(item);
        booking.setBooker(booker);
        booking.setStatus(BookingState.APPROVED);
        return bookingRepository.save(booking);
    }

    private Comment createAndSaveComment(Item item) {
        Comment comment = new Comment();
        comment.setText("Excellent tool!");
        comment.setItem(item);
        comment.setAuthor(booker);
        comment.setCreated(LocalDateTime.now());
        return commentRepository.save(comment);
    }

    @Test
    void getItemById_shouldReturnItemWithBookings_WhenRequestorIsOwner() {
        Item item = createAndSaveItem();
        Booking booking = createAndSaveBooking(item, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2));

        ItemDto result = itemService.getItemById(owner.getId(), item.getId());

        assertThat(result.getId()).isEqualTo(item.getId());
        assertThat(result.getNextBooking()).isNotNull();
        assertThat(result.getNextBooking().getId()).isEqualTo(booking.getId());
    }

    @Test
    void getItemById_shouldReturnItemWithoutBookings_WhenRequestorIsNotOwner() {
        Item item = createAndSaveItem();
        createAndSaveBooking(item, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2));

        ItemDto result = itemService.getItemById(booker.getId(), item.getId());

        assertThat(result.getId()).isEqualTo(item.getId());
        assertThat(result.getNextBooking()).isNull();
    }

    @Test
    void getItemById_shouldThrowException_WhenItemNotFound() {
        assertThatThrownBy(() -> itemService.getItemById(owner.getId(), 999L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void updateItem_shouldUpdateOnlyName() {
        Item item = createAndSaveItem();
        ItemDto updateDto = new ItemDto();
        updateDto.setName("New Name"); // Description and Available are null

        ItemDto result = itemService.updateItem(owner.getId(), item.getId(), updateDto);

        assertThat(result.getName()).isEqualTo("New Name");
        assertThat(result.getDescription()).isEqualTo(item.getDescription());
        assertThat(result.getAvailable()).isEqualTo(item.getAvailable());
    }

    @Test
    void updateItem_shouldUpdateOnlyDescription() {
        Item item = createAndSaveItem();
        ItemDto updateDto = new ItemDto();
        updateDto.setDescription("New Description");

        ItemDto result = itemService.updateItem(owner.getId(), item.getId(), updateDto);

        assertThat(result.getDescription()).isEqualTo("New Description");
        assertThat(result.getName()).isEqualTo(item.getName());
    }

    @Test
    void updateItem_shouldUpdateOnlyAvailable() {
        Item item = createAndSaveItem();

        boolean originalStatus = item.getAvailable();

        ItemDto updateDto = new ItemDto();
        updateDto.setAvailable(!originalStatus);

        ItemDto result = itemService.updateItem(owner.getId(), item.getId(), updateDto);

        assertThat(result.getAvailable()).isNotEqualTo(originalStatus);

        assertThat(result.getAvailable()).isEqualTo(!originalStatus);

        assertThat(result.getName()).isEqualTo(item.getName());
    }
}