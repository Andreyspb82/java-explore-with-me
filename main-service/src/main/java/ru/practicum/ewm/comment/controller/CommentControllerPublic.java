package ru.practicum.ewm.comment.controller;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.ewm.comment.dto.CommentDto;
import ru.practicum.ewm.comment.service.CommentService;

import javax.validation.constraints.Min;
import java.util.List;

@Validated
@RestController
@Slf4j
@RequestMapping(path = "/comments/{eventId}")
@AllArgsConstructor
public class CommentControllerPublic {

    public final CommentService commentService;

    @GetMapping
    public List<CommentDto> getCommentsByEventIdPublic(@Min(1) @PathVariable long eventId,
                                                       @Min(0) @RequestParam(defaultValue = "0") int from,
                                                       @Min(0) @RequestParam(defaultValue = "10") int size) {

        PageRequest page = PageRequest.of(from / size, size);
        log.info("Get List<Comment> by Event with eventId={}", eventId);
        return commentService.getCommentsByEventId(eventId, page);
    }
}
