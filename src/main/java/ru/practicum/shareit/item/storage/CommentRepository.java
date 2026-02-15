package ru.practicum.shareit.item.storage;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.shareit.item.model.Comment;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    @Query("select c from Comment c where c.item.id = :itemId order by c.created asc")
    List<Comment> findAllByItemId(@Param("itemId") long itemId);

    @Query("select c from Comment c where c.item.owner.id = :ownerId order by c.created asc")
    List<Comment> findAllByOwnerItems(@Param("ownerId") long ownerId);
}