package com.payvault.user.exception;

import com.payvault.user.dto.MessageResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UserServiceExceptions.DuplicateEmailException.class)
    public ResponseEntity<MessageResponse> handleDuplicateEmail(UserServiceExceptions.DuplicateEmailException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new MessageResponse(e.getMessage()));
    }

    @ExceptionHandler(UserServiceExceptions.InvalidCredentialsException.class)
    public ResponseEntity<MessageResponse> handleInvalidCredentials(UserServiceExceptions.InvalidCredentialsException e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new MessageResponse(e.getMessage()));
    }

    @ExceptionHandler(UserServiceExceptions.UserNotFoundException.class)
    public ResponseEntity<MessageResponse> handleUserNotFound(UserServiceExceptions.UserNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new MessageResponse(e.getMessage()));
    }

    @ExceptionHandler(UserServiceExceptions.InvalidOtpException.class)
    public ResponseEntity<MessageResponse> handleInvalidOtp(UserServiceExceptions.InvalidOtpException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new MessageResponse(e.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<MessageResponse> handleGeneric(Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new MessageResponse("Something went wrong: " + e.getMessage()));
    }
}
