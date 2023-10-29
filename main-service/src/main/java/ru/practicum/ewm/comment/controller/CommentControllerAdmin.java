package ru.practicum.ewm.comment.controller;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.ewm.comment.dto.CommentDto;
import ru.practicum.ewm.comment.service.CommentService;

import javax.validation.constraints.Min;
import java.util.List;

@Validated
@RestController
@Slf4j
@RequestMapping(path = "admin/comments")
@AllArgsConstructor
public class CommentControllerAdmin {

    public final CommentService commentService;

    @GetMapping("{eventId}")
    public List<CommentDto> getCommentsByEventIdPublic(@Min(1) @PathVariable long eventId,
                                                       @Min(0) @RequestParam(defaultValue = "0") int from,
                                                       @Min(0) @RequestParam(defaultValue = "10") int size) {

        PageRequest page = PageRequest.of(from / size, size);
        log.info("Get List<Comment> by admin with eventId={}", eventId);
        return commentService.getCommentsByEventId(eventId, page);
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeCommentByAdmin(@Min(1) @RequestParam long commentId) {

        log.info("Delete Comment by Owner with commentId={}", commentId);
        commentService.removeCommentByAdmin(commentId);
    }
}
