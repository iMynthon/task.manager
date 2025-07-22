package com.mynthon.task.manager.common.exception;

import com.mynthon.task.manager.common.dto.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(EntityNotFoundException.class)
    public ErrorResponse catchEntityNotFoundException(EntityNotFoundException ene){
        return new ErrorResponse(HttpStatus.NOT_FOUND.value(),ene.getMessage());
    }
}
