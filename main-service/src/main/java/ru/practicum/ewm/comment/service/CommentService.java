package ru.practicum.ewm.comment.service;

import org.springframework.data.domain.PageRequest;
import ru.practicum.ewm.comment.dto.CommentDto;
import ru.practicum.ewm.comment.dto.NewCommentDto;
import ru.practicum.ewm.comment.dto.UpdateCommentDto;

import java.util.List;

public interface CommentService {

    public CommentDto createComment(long userId, long eventId, NewCommentDto newComment);

    public CommentDto updateCommentByOwner(long userId, long commentId, UpdateCommentDto comment);

    public void removeCommentByOwner(long userId, long commentId);

    public CommentDto getCommentByOwnerId(long userId, long commentId);

    public List<CommentDto> getCommentsByOwnerByEventId(long userId, long eventId, PageRequest page);

    public List<CommentDto> getCommentsByEventId(long eventId, PageRequest page);

    public void removeCommentByAdmin(long commentId);
}
