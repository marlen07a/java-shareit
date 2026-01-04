package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingDtoOut;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.UserDto;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BookingController.class)
class BookingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BookingService bookingService;

    private static final String USER_ID_HEADER = "X-Sharer-User-Id";

    @Test
    void create_shouldReturn200AndBooking() throws Exception {
        BookingDto bookingDto = new BookingDto(
                null,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2),
                1L
        );

        BookingDtoOut responseDto = new BookingDtoOut();
        responseDto.setId(1L);
        responseDto.setStart(bookingDto.getStart());
        responseDto.setEnd(bookingDto.getEnd());
        responseDto.setStatus(BookingState.WAITING);

        ItemDto itemDto = new ItemDto();
        itemDto.setId(1L);
        itemDto.setName("Test Item");
        responseDto.setItem(itemDto);

        UserDto userDto = new UserDto(1L, "Booker", "booker@test.com");
        responseDto.setBooker(userDto);

        when(bookingService.create(anyLong(), any(BookingDto.class)))
                .thenReturn(responseDto);

        mockMvc.perform(post("/bookings")
                        .header(USER_ID_HEADER, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookingDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.status").value("WAITING"))
                .andExpect(jsonPath("$.item.id").value(1L))
                .andExpect(jsonPath("$.booker.id").value(1L));
    }

    @Test
    void approve_shouldReturn200AndApprovedBooking() throws Exception {
        BookingDtoOut responseDto = new BookingDtoOut();
        responseDto.setId(1L);
        responseDto.setStatus(BookingState.APPROVED);

        when(bookingService.approve(anyLong(), anyLong(), anyBoolean()))
                .thenReturn(responseDto);

        mockMvc.perform(patch("/bookings/1")
                        .header(USER_ID_HEADER, 1L)
                        .param("approved", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.status").value("APPROVED"));
    }

    @Test
    void approve_shouldReturn200AndRejectedBooking() throws Exception {
        BookingDtoOut responseDto = new BookingDtoOut();
        responseDto.setId(1L);
        responseDto.setStatus(BookingState.REJECTED);

        when(bookingService.approve(anyLong(), anyLong(), anyBoolean()))
                .thenReturn(responseDto);

        mockMvc.perform(patch("/bookings/1")
                        .header(USER_ID_HEADER, 1L)
                        .param("approved", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.status").value("REJECTED"));
    }

    @Test
    void getById_shouldReturn200AndBooking() throws Exception {
        BookingDtoOut responseDto = new BookingDtoOut();
        responseDto.setId(1L);
        responseDto.setStatus(BookingState.WAITING);
        responseDto.setStart(LocalDateTime.now().plusDays(1));
        responseDto.setEnd(LocalDateTime.now().plusDays(2));

        when(bookingService.getById(anyLong(), anyLong()))
                .thenReturn(responseDto);

        mockMvc.perform(get("/bookings/1")
                        .header(USER_ID_HEADER, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.status").value("WAITING"));
    }

    @Test
    void getAllByUser_shouldReturn200AndBookingList() throws Exception {
        BookingDtoOut booking1 = new BookingDtoOut();
        booking1.setId(1L);
        booking1.setStatus(BookingState.WAITING);

        BookingDtoOut booking2 = new BookingDtoOut();
        booking2.setId(2L);
        booking2.setStatus(BookingState.APPROVED);

        when(bookingService.getAllByUser(anyLong(), anyString(), anyInt(), anyInt()))
                .thenReturn(List.of(booking1, booking2));

        mockMvc.perform(get("/bookings")
                        .header(USER_ID_HEADER, 1L)
                        .param("state", "ALL")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[1].id").value(2L))
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void getAllByUser_shouldUseDefaultParameters() throws Exception {
        when(bookingService.getAllByUser(eq(1L), eq("ALL"), eq(0), eq(10)))
                .thenReturn(List.of());

        mockMvc.perform(get("/bookings")
                        .header(USER_ID_HEADER, 1L))
                .andExpect(status().isOk());
    }

    @Test
    void getAllByUser_withStateFilter_shouldReturn200() throws Exception {
        BookingDtoOut booking = new BookingDtoOut();
        booking.setId(1L);
        booking.setStatus(BookingState.WAITING);

        when(bookingService.getAllByUser(anyLong(), eq("WAITING"), anyInt(), anyInt()))
                .thenReturn(List.of(booking));

        mockMvc.perform(get("/bookings")
                        .header(USER_ID_HEADER, 1L)
                        .param("state", "WAITING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("WAITING"));
    }

    @Test
    void getAllByOwner_shouldReturn200AndBookingList() throws Exception {
        BookingDtoOut booking1 = new BookingDtoOut();
        booking1.setId(1L);
        BookingDtoOut booking2 = new BookingDtoOut();
        booking2.setId(2L);

        when(bookingService.getAllByOwner(anyLong(), anyString(), anyInt(), anyInt()))
                .thenReturn(List.of(booking1, booking2));

        mockMvc.perform(get("/bookings/owner")
                        .header(USER_ID_HEADER, 1L)
                        .param("state", "ALL")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void getAllByOwner_shouldUseDefaultParameters() throws Exception {
        when(bookingService.getAllByOwner(eq(1L), eq("ALL"), eq(0), eq(10)))
                .thenReturn(List.of());

        mockMvc.perform(get("/bookings/owner")
                        .header(USER_ID_HEADER, 1L))
                .andExpect(status().isOk());
    }

    @Test
    void getAllByOwner_withPagination_shouldReturn200() throws Exception {
        when(bookingService.getAllByOwner(eq(1L), eq("ALL"), eq(5), eq(20)))
                .thenReturn(List.of());

        mockMvc.perform(get("/bookings/owner")
                        .header(USER_ID_HEADER, 1L)
                        .param("from", "5")
                        .param("size", "20"))
                .andExpect(status().isOk());
    }
}
