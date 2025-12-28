package ru.practicum.shareit.booking;

import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingDtoOut;

import java.util.List;

public interface BookingService {
    BookingDtoOut create(Long userId, BookingDto bookingDto);

    BookingDtoOut approve(Long userId, Long bookingId, Boolean approved);

    BookingDtoOut getById(Long userId, Long bookingId);

    List<BookingDtoOut> getAllByUser(Long userId, String state);

    List<BookingDtoOut> getAllByOwner(Long userId, String state);
}
