package ru.practicum.ewm.comment.service;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.comment.dto.CommentDto;
import ru.practicum.ewm.comment.dto.NewCommentDto;
import ru.practicum.ewm.comment.dto.UpdateCommentDto;
import ru.practicum.ewm.comment.mapper.CommentMapper;
import ru.practicum.ewm.comment.model.Comment;
import ru.practicum.ewm.comment.repository.CommentRepository;
import ru.practicum.ewm.event.enums.State;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.event.service.EventService;
import ru.practicum.ewm.exception.ConflictException;
import ru.practicum.ewm.exception.NotFoundException;
import ru.practicum.ewm.request.enums.Status;
import ru.practicum.ewm.request.model.ParticipationRequest;
import ru.practicum.ewm.request.repository.ParticipationRequestRepository;
import ru.practicum.ewm.user.model.User;
import ru.practicum.ewm.user.service.UserService;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@Data
public class CommentServiceImpl implements CommentService {

    public final CommentRepository commentRepository;

    public final EventService eventService;

    public final UserService userService;

    public final ParticipationRequestRepository requestRepository;

    @Override
    public CommentDto createComment(long userId, long eventId, NewCommentDto newComment) {

        validationUserParticipatesInEvent(userId, eventId);
        User user = userService.getUserByIdForService(userId);
        Event event = eventService.getEventByIdForService(eventId);

        if (!event.getAllowComments()) {
            throw new ConflictException("Comments are not allowed for the event");
        }
        Comment comment = new Comment();
        comment.setEvent(event);
        comment.setAuthor(user);
        comment.setText(newComment.getText());

        return CommentMapper.mapToCommentDto(commentRepository.save(comment));
    }

    @Override
    public CommentDto updateCommentByOwner(long userId, long commentId, UpdateCommentDto comment) {
        userService.getUserByIdForService(userId);
        Comment oldComment = getCommentByIdForService(commentId);

        if (oldComment.getAuthor().getId() != userId) {
            throw new ConflictException("The user is not the author of the comment");
        }
        if (comment.getText() != null) {
            oldComment.setText(comment.getText());
        }
        return CommentMapper.mapToCommentDto(commentRepository.save(oldComment));
    }

    @Override
    public void removeCommentByOwner(long userId, long commentId) {
        userService.getUserByIdForService(userId);
        Comment comment = getCommentByIdForService(commentId);

        if (comment.getAuthor().getId() != userId) {
            throw new ConflictException("The user is not the author of the comment");
        }
        commentRepository.deleteById(commentId);
    }

    @Override
    public CommentDto getCommentByOwnerId(long userId, long commentId) {
        Optional<Comment> commentByOwnerId = Optional.ofNullable(commentRepository.findByIdAndAuthorId(commentId, userId));
        if (commentByOwnerId.isEmpty()) {
            throw new NotFoundException("Comment with Id =" + commentId + " and with authorId =" + userId + " not found");
        }
        return CommentMapper.mapToCommentDto(commentByOwnerId.get());
    }

    @Override
    public List<CommentDto> getCommentsByOwnerByEventId(long userId, long eventId, PageRequest page) {
        validationUserParticipatesInEvent(userId, eventId);
        return CommentMapper.mapToCommentsDto(commentRepository.findByAuthorIdAndEventId(userId, eventId, page));
    }

    @Override
    public List<CommentDto> getCommentsByEventId(long eventId, PageRequest page) {
        eventService.getEventByIdForService(eventId);
        return CommentMapper.mapToCommentsDto(commentRepository.findByEventId(eventId, page));
    }

    @Override
    public void removeCommentByAdmin(long commentId) {
        if (!commentRepository.existsById(commentId)) {
            throw new NotFoundException("Comment with Id =" + commentId + " does not exist");
        }
        commentRepository.deleteById(commentId);
    }


    private Comment getCommentByIdForService(long commentId) {
        if (!commentRepository.existsById(commentId)) {
            throw new NotFoundException("Comment with Id =" + commentId + " does not exist");
        }
        return commentRepository.findById(commentId).get();
    }

    private void validationUserParticipatesInEvent(long userId, long eventId) {
        Optional<ParticipationRequest> request = Optional.ofNullable(requestRepository.findByEventIdAndRequesterIdAndStatus(eventId, userId, Status.CONFIRMED));
        userService.getUserByIdForService(userId);
        Event event = eventService.getEventByIdForService(eventId);

        if (!event.getState().equals(State.PUBLISHED)) {
            throw new ConflictException("Event not published");
        }
        if ((event.getInitiator().getId() != userId) && request.isEmpty()) {
            throw new ConflictException("The user is not involved in the event");
        }
    }
}
