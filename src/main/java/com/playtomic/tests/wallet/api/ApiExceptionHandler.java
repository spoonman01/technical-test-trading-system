package com.playtomic.tests.wallet.api;

import com.playtomic.tests.wallet.service.payment.StripeAmountTooSmallException;
import com.playtomic.tests.wallet.service.payment.StripeServiceException;
import com.playtomic.tests.wallet.service.wallet.WalletNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<ErrorResponse> handleDatabaseException(DataAccessException exception) {
        log.error("Database exception", exception);
        var errorResponse = new ErrorResponse("Internal Server Error");
        return new ResponseEntity(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(WalletNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleWalletNotFoundException(WalletNotFoundException exception) {
        log.warn("Cannot find wallet with id = {}", exception.getWalletId());
        var errorResponse = new ErrorResponse("Wallet not found");
        return new ResponseEntity(errorResponse, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(StripeAmountTooSmallException.class)
    public ResponseEntity<ErrorResponse> handleStripeAmountTooSmallException(StripeAmountTooSmallException exception) {
        log.warn("Stripe amount too small exception", exception);
        var errorResponse = new ErrorResponse("Amount too small");
        return new ResponseEntity(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(StripeServiceException.class)
    public ResponseEntity<ErrorResponse> handleStripeServiceException(StripeServiceException exception) {
        log.warn("Stripe amount too small exception", exception);
        var errorResponse = new ErrorResponse("Error occurred while processing payment");
        return new ResponseEntity(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
