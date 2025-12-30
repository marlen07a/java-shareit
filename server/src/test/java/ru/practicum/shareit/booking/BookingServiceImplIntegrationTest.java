package ru.practicum.shareit.booking;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingDtoOut;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.ItemRepository;
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
class BookingServiceImplIntegrationTest {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private BookingRepository bookingRepository;

    private User owner;
    private User booker;
    private Item item;

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

        item = new Item();
        item.setName("Test Item");
        item.setDescription("Test Description");
        item.setAvailable(true);
        item.setOwner(owner);
        item = itemRepository.save(item);
    }

    @Test
    void create_shouldCreateBookingSuccessfully() {
        BookingDto bookingDto = new BookingDto(
                null,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2),
                item.getId()
        );

        BookingDtoOut result = bookingService.create(booker.getId(), bookingDto);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isNotNull();
        assertThat(result.getStatus()).isEqualTo(BookingState.WAITING);
        assertThat(result.getBooker().getId()).isEqualTo(booker.getId());
        assertThat(result.getItem().getId()).isEqualTo(item.getId());
    }

    @Test
    void create_shouldThrowExceptionWhenItemNotAvailable() {
        item.setAvailable(false);
        itemRepository.save(item);

        BookingDto bookingDto = new BookingDto(
                null,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2),
                item.getId()
        );

        assertThatThrownBy(() -> bookingService.create(booker.getId(), bookingDto))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("not available");
    }

    @Test
    void create_shouldThrowExceptionWhenOwnerTriesToBook() {
        BookingDto bookingDto = new BookingDto(
                null,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2),
                item.getId()
        );

        assertThatThrownBy(() -> bookingService.create(owner.getId(), bookingDto))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Owner cannot book");
    }

    @Test
    void create_shouldThrowExceptionWhenEndBeforeStart() {
        BookingDto bookingDto = new BookingDto(
                null,
                LocalDateTime.now().plusDays(2),
                LocalDateTime.now().plusDays(1),
                item.getId()
        );

        assertThatThrownBy(() -> bookingService.create(booker.getId(), bookingDto))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Wrong dates");
    }

    @Test
    void approve_shouldApproveBookingSuccessfully() {
        Booking booking = createAndSaveBooking();

        BookingDtoOut result = bookingService.approve(owner.getId(), booking.getId(), true);

        assertThat(result.getStatus()).isEqualTo(BookingState.APPROVED);
    }

    @Test
    void approve_shouldRejectBookingSuccessfully() {
        Booking booking = createAndSaveBooking();

        BookingDtoOut result = bookingService.approve(owner.getId(), booking.getId(), false);

        assertThat(result.getStatus()).isEqualTo(BookingState.REJECTED);
    }

    @Test
    void approve_shouldThrowExceptionWhenNotOwner() {
        Booking booking = createAndSaveBooking();

        assertThatThrownBy(() -> bookingService.approve(booker.getId(), booking.getId(), true))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("not the owner");
    }

    @Test
    void getAllByUser_shouldReturnAllBookings() {
        createAndSaveBooking();
        createAndSaveBooking();

        List<BookingDtoOut> result = bookingService.getAllByUser(booker.getId(), "ALL", 0, 10);

        assertThat(result).hasSize(2);
    }

    @Test
    void getAllByUser_shouldReturnWaitingBookings() {

        createAndSaveBooking();

        List<BookingDtoOut> result = bookingService.getAllByUser(booker.getId(), "WAITING", 0, 10);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStatus()).isEqualTo(BookingState.WAITING);
    }

    @Test
    void getAllByOwner_shouldReturnOwnerBookings() {
        createAndSaveBooking();

        List<BookingDtoOut> result = bookingService.getAllByOwner(owner.getId(), "ALL", 0, 10);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getItem().getId()).isEqualTo(item.getId());
    }

    private Booking createAndSaveBooking() {
        Booking booking = new Booking();
        booking.setStart(LocalDateTime.now().plusDays(1));
        booking.setEnd(LocalDateTime.now().plusDays(2));
        booking.setItem(item);
        booking.setBooker(booker);
        booking.setStatus(BookingState.WAITING);
        return bookingRepository.save(booking);
    }

    @Test
    void getById_shouldReturnBooking_WhenUserIsBooker() {
        Booking booking = createAndSaveBooking();
        BookingDtoOut result = bookingService.getById(booker.getId(), booking.getId());
        assertThat(result.getId()).isEqualTo(booking.getId());
    }

    @Test
    void getById_shouldReturnBooking_WhenUserIsOwner() {
        Booking booking = createAndSaveBooking();
        BookingDtoOut result = bookingService.getById(owner.getId(), booking.getId());
        assertThat(result.getId()).isEqualTo(booking.getId());
    }

    @Test
    void getById_shouldThrow_WhenUserIsStranger() {
        Booking booking = createAndSaveBooking();
        User stranger = new User();
        stranger.setName("Stranger");
        stranger.setEmail("stranger@test.com");
        userRepository.save(stranger);

        assertThatThrownBy(() -> bookingService.getById(stranger.getId(), booking.getId()))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void getById_shouldThrow_WhenBookingNotFound() {
        assertThatThrownBy(() -> bookingService.getById(owner.getId(), 9999L))
                .isInstanceOf(NotFoundException.class);
    }
}