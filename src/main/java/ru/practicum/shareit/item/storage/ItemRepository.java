package ru.practicum.shareit.item.storage;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemRepository extends JpaRepository<Item, Long> {

    @Query("select i from Item i where i.owner.id = :ownerId order by i.id")
    List<Item> findAllByOwnerId(@Param("ownerId") long ownerId);

    @Query("select i from Item i " +
            "where (lower(i.name) like lower(concat('%', :text, '%')) " +
            "   or lower(i.description) like lower(concat('%', :text, '%'))) " +
            "  and i.available = true")
    List<Item> searchAvailableByText(@Param("text") String text);
}