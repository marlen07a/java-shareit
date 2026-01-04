package ru.practicum.shareit.booking;

import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingDtoOut;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.UserMapper;

public class BookingMapper {
    public static Booking toBooking(BookingDto dto) {
        Booking booking = new Booking();
        booking.setStart(dto.getStart());
        booking.setEnd(dto.getEnd());
        return booking;
    }

    public static BookingDtoOut toBookingDtoOut(Booking booking) {
        BookingDtoOut dto = new BookingDtoOut();
        dto.setId(booking.getId());
        dto.setStart(booking.getStart());
        dto.setEnd(booking.getEnd());
        dto.setStatus(booking.getStatus());
        dto.setItem(ItemMapper.toItemDto(booking.getItem()));
        dto.setBooker(UserMapper.toUserDto(booking.getBooker()));
        return dto;
    }

    public static ItemDto.BookingDtoShort toBookingDtoShort(Booking booking) {
        if (booking == null) return null;
        return new ItemDto.BookingDtoShort(
                booking.getId(),
                booking.getBooker().getId()
        );
    }
}