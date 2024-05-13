package com.lucarospocher.trading.api;

import com.lucarospocher.trading.service.SymbolNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class ApiExceptionHandler {
    private Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleAnyException(Exception exception) {
        log.error("Unhandled exception", exception);
        var errorResponse = new ErrorResponse("Internal Server Error");
        return new ResponseEntity(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException exception) {
        log.warn("Parameter missing or invalid", exception);
        var errorResponse = new ErrorResponse(exception.getMessage());
        return new ResponseEntity(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadableException(HttpMessageNotReadableException exception) {
        log.warn("Body missing or invalid", exception);
        var errorResponse = new ErrorResponse(exception.getMessage());
        return new ResponseEntity(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<ErrorResponse> handleDatabaseException(DataAccessException exception) {
        log.error("Database exception", exception);
        var errorResponse = new ErrorResponse("Internal Server Error");
        return new ResponseEntity(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(SymbolNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleSymbolNotFoundException(SymbolNotFoundException exception) {
        log.warn("Cannot find trades with symbol = {}", exception.getSymbol());
        var errorResponse = new ErrorResponse("Symbol not found");
        return new ResponseEntity(errorResponse, HttpStatus.NOT_FOUND);
    }
}
