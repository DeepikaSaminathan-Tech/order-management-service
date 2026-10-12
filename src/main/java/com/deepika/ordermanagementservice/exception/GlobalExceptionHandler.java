package com.deepika.ordermanagementservice.exception;

import com.deepika.ordermanagementservice.dto.ErrorResponse;
import jakarta.persistence.OptimisticLockException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;


@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(OrderNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleOrderNotFoundException(OrderNotFoundException e) {
        return new ErrorResponse(404, e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleMethodArgumentNotValidException(MethodArgumentNotValidException exception) {
        String errorMessage = exception.getBindingResult().getFieldErrors()
                .stream()
                .map(FieldError::getField)
                .findFirst()
                .orElse("Invalid Request");
        return new ErrorResponse(400, errorMessage);
    }

    //Concurrent request can update same order result in OptimisticLockingException using @Version
    @ExceptionHandler(OptimisticLockException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleOptimisticLockException(OptimisticLockException exception) {
        return new ErrorResponse(409, exception.getMessage());
    }

    @ExceptionHandler(OptimisticLockingFailureException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleOptimisticLockingFailureException(
            OptimisticLockingFailureException exception) {
        return new ErrorResponse(409, exception.getMessage());
    }

}
