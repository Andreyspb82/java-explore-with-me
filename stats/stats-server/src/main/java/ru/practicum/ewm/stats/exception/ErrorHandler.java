package ru.practicum.ewm.stats.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageConversionException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.practicum.ewm.stats.exception.dto.ApiError;


import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;

@RestControllerAdvice
@Slf4j
public class ErrorHandler {

    private static final DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
//
//    @ExceptionHandler({NotFoundException.class})
//    @ResponseStatus(HttpStatus.NOT_FOUND)
//    public ApiError handleNotFoundException(final NotFoundException e) throws IOException {
//
//        log.error("Error: " + e.getMessage(), e);
//        return ApiError.builder()
//                .errors(Collections.singletonList(error(e)))
//                .message(e.getLocalizedMessage())
//                .reason("Data not found")
//                .status(String.valueOf(HttpStatus.NOT_FOUND))
//                .timestamp(LocalDateTime.now().format(FORMAT))
//                .build();
//    }
//
//    @ExceptionHandler({ConflictException.class})
//    @ResponseStatus(HttpStatus.CONFLICT)
//    public ApiError handleConflictException(final Exception e) throws IOException {
//
//        log.error("Error: " + e.getMessage(), e);
//        return ApiError.builder()
//                .errors(Collections.singletonList(error(e)))
//                .message(e.getLocalizedMessage())
//                .reason("Data conflicts with existing data")
//                .status(String.valueOf(HttpStatus.CONFLICT))
//                .timestamp(LocalDateTime.now().format(FORMAT))
//                .build();
//    }

    @ExceptionHandler({BadRequestException.class, MethodArgumentNotValidException.class, RuntimeException.class, HttpMessageConversionException.class, MethodArgumentNotValidException.class, DataIntegrityViolationException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleBadRequestException(final Exception e) throws IOException {

        log.error("Error: " + e.getMessage(), e);
        return ApiError.builder()
                .errors(Collections.singletonList(error(e)))
                .message(e.getLocalizedMessage())
                .reason("Bad request")
                .status(String.valueOf(HttpStatus.BAD_REQUEST))
                .timestamp(LocalDateTime.now().format(FORMAT))
                .build();
    }

    private String error(Exception e) throws IOException {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        String error = sw.toString();
        sw.close();
        pw.close();
        return error;
    }

}
