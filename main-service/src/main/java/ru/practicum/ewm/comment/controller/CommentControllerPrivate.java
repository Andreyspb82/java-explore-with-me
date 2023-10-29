package ru.practicum.ewm.comment.controller;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.ewm.comment.dto.CommentDto;
import ru.practicum.ewm.comment.dto.NewCommentDto;
import ru.practicum.ewm.comment.dto.UpdateCommentDto;
import ru.practicum.ewm.comment.service.CommentService;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import java.util.List;

@Validated
@RestController
@Slf4j
@RequestMapping(path = "/users/{userId}/comments")
@AllArgsConstructor
public class CommentControllerPrivate {

    public final CommentService commentService;

    @PostMapping("/{eventId}")
    @ResponseStatus(HttpStatus.CREATED)
    public CommentDto createComment(@Min(1) @PathVariable long userId,
                                    @Min(1) @PathVariable long eventId,
                                    @Valid @RequestBody NewCommentDto newComment) {

        log.info("Creating Comment with userId={}, eventId={}, text={}", userId, eventId, newComment.getText());
        return commentService.createComment(userId, eventId, newComment);
    }

    @PatchMapping
    public CommentDto updateCommentByOwner(@Min(1) @PathVariable long userId,
                                           @Min(1) @RequestParam long commentId,
                                           @Valid @RequestBody UpdateCommentDto comment) {

        log.info("Update Comment by Owner with ownerId={}, commentId={}, text={}", userId, commentId, comment.getText());
        return commentService.updateCommentByOwner(userId, commentId, comment);
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeCommentByOwner(@Min(1) @PathVariable long userId,
                                     @Min(1) @RequestParam long commentId) {

        log.info("Delete Comment by Owner with ownerId={}, commentId={}", userId, commentId);
        commentService.removeCommentByOwner(userId, commentId);
    }

    @GetMapping
    public CommentDto getCommentByOwnerId(@Min(1) @PathVariable long userId,
                                          @Min(1) @RequestParam long commentId) {

        log.info("Get Comment by Owner with ownerId={}, commentId={}", userId, commentId);
        return commentService.getCommentByOwnerId(userId, commentId);
    }

    @GetMapping("/{eventId}")
    public List<CommentDto> getCommentsByOwnerByEventId(@Min(1) @PathVariable long userId,
                                                        @Min(1) @PathVariable long eventId,
                                                        @Min(0) @RequestParam(defaultValue = "0") int from,
                                                        @Min(0) @RequestParam(defaultValue = "10") int size) {

        PageRequest page = PageRequest.of(from / size, size);
        log.info("Get List<Comment> by Event by Owner with ownerId={}, eventId={}", userId, eventId);
        return commentService.getCommentsByOwnerByEventId(userId, eventId, page);
    }
}
