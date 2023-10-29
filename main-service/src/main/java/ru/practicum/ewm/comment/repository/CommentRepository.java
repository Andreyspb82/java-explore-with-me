package ru.practicum.ewm.comment.repository;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.ewm.comment.model.Comment;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    Comment findByIdAndAuthorId(long commentId, long userId);

    List<Comment> findByAuthorIdAndEventId(long userId, long eventId, PageRequest page);

    List<Comment> findByEventId(long eventId, PageRequest page);
}
