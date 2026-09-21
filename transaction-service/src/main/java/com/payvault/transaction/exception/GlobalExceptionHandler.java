package com.payvault.transaction.exception;

import com.payvault.transaction.dto.MessageResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(TransactionServiceExceptions.InvalidPinException.class)
    public ResponseEntity<MessageResponse> handleInvalidPin(TransactionServiceExceptions.InvalidPinException e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new MessageResponse(e.getMessage()));
    }

    @ExceptionHandler(TransactionServiceExceptions.InvalidTransferException.class)
    public ResponseEntity<MessageResponse> handleInvalidTransfer(TransactionServiceExceptions.InvalidTransferException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new MessageResponse(e.getMessage()));
    }

    @ExceptionHandler(TransactionServiceExceptions.TransactionNotFoundException.class)
    public ResponseEntity<MessageResponse> handleNotFound(TransactionServiceExceptions.TransactionNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new MessageResponse(e.getMessage()));
    }

    @ExceptionHandler(TransactionServiceExceptions.ServiceUnavailableException.class)
    public ResponseEntity<MessageResponse> handleServiceUnavailable(TransactionServiceExceptions.ServiceUnavailableException e) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(new MessageResponse(e.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<MessageResponse> handleGeneric(Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new MessageResponse("Something went wrong: " + e.getMessage()));
    }
}
