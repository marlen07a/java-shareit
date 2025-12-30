package ru.practicum.shareit.booking;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    Page<Booking> findAllByBookerId(long bookerId, Pageable pageable);

    Page<Booking> findAllByBookerIdAndStatus(long bookerId, BookingState state, Pageable pageable);

    Page<Booking> findAllByBookerIdAndEndBefore(long bookerId, LocalDateTime now, Pageable pageable);

    Page<Booking> findAllByBookerIdAndStartAfter(long bookerId, LocalDateTime now, Pageable pageable);

    Page<Booking> findAllByBookerIdAndStartBeforeAndEndAfter(long bookerId, LocalDateTime start, LocalDateTime end, Pageable pageable);

    Page<Booking> findAllByItemOwnerId(long ownerId, Pageable pageable);

    Page<Booking> findAllByItemOwnerIdAndStatus(long ownerId, BookingState state, Pageable pageable);

    Page<Booking> findAllByItemOwnerIdAndEndBefore(long ownerId, LocalDateTime now, Pageable pageable);

    Page<Booking> findAllByItemOwnerIdAndStartAfter(long ownerId, LocalDateTime now, Pageable pageable);

    Page<Booking> findAllByItemOwnerIdAndStartBeforeAndEndAfter(long ownerId, LocalDateTime start, LocalDateTime end, Pageable pageable);

    @Query("SELECT b FROM Booking b " +
            "LEFT JOIN FETCH b.booker " +
            "WHERE b.item.id = :itemId " +
            "AND b.status = :status " +
            "ORDER BY b.start ASC")
    List<Booking> findAllByItemIdAndStatusOrderByStartAsc(@Param("itemId") Long itemId,
                                                          @Param("status") BookingState state);

    boolean existsByBookerIdAndItemIdAndStatusAndEndBefore(Long bookerId, Long itemId, BookingState status, LocalDateTime now);

    @Query("SELECT b FROM Booking b " +
            "LEFT JOIN FETCH b.booker " +
            "LEFT JOIN FETCH b.item " +
            "WHERE b.item.id IN :itemIds " +
            "AND b.status = :status " +
            "ORDER BY b.start ASC")
    List<Booking> findAllByItemIdInAndStatus(@Param("itemIds") List<Long> itemIds,
                                             @Param("status") BookingState state);
}