package ru.practicum.shareit.booking.storage;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    @Query("select b from Booking b where b.booker.id = :userId order by b.start desc")
    List<Booking> findAllByBooker(@Param("userId") long userId);

    @Query("select b from Booking b where b.item.owner.id = :ownerId order by b.start desc")
    List<Booking> findAllByOwner(@Param("ownerId") long ownerId);

    @Query("select b from Booking b where b.booker.id = :userId and b.start <= :now and b.end >= :now order by b.start desc")
    List<Booking> findCurrentByBooker(@Param("userId") long userId, @Param("now") LocalDateTime now);

    @Query("select b from Booking b where b.booker.id = :userId and b.end < :now order by b.start desc")
    List<Booking> findPastByBooker(@Param("userId") long userId, @Param("now") LocalDateTime now);

    @Query("select b from Booking b where b.booker.id = :userId and b.start > :now order by b.start desc")
    List<Booking> findFutureByBooker(@Param("userId") long userId, @Param("now") LocalDateTime now);

    @Query("select b from Booking b where b.booker.id = :userId and b.status = :status order by b.start desc")
    List<Booking> findByBookerAndStatus(@Param("userId") long userId, @Param("status") BookingStatus status);

    @Query("select b from Booking b where b.item.owner.id = :ownerId and b.start <= :now and b.end >= :now order by b.start desc")
    List<Booking> findCurrentByOwner(@Param("ownerId") long ownerId, @Param("now") LocalDateTime now);

    @Query("select b from Booking b where b.item.owner.id = :ownerId and b.end < :now order by b.start desc")
    List<Booking> findPastByOwner(@Param("ownerId") long ownerId, @Param("now") LocalDateTime now);

    @Query("select b from Booking b where b.item.owner.id = :ownerId and b.start > :now order by b.start desc")
    List<Booking> findFutureByOwner(@Param("ownerId") long ownerId, @Param("now") LocalDateTime now);

    @Query("select b from Booking b where b.item.owner.id = :ownerId and b.status = :status order by b.start desc")
    List<Booking> findByOwnerAndStatus(@Param("ownerId") long ownerId, @Param("status") BookingStatus status);

    Optional<Booking> findById(Long id);

    @Query("select b from Booking b where b.item.id = :itemId and b.status = 'APPROVED' and b.start < :now order by b.start desc")
    List<Booking> findLastApproved(@Param("itemId") long itemId, @Param("now") LocalDateTime now);

    @Query("select b from Booking b where b.item.id = :itemId and b.status = 'APPROVED' and b.start > :now order by b.start asc")
    List<Booking> findNextApproved(@Param("itemId") long itemId, @Param("now") LocalDateTime now);

    boolean existsByItem_IdAndBooker_IdAndStatusAndEndBefore(long itemId, long bookerId, BookingStatus status, LocalDateTime time);
}