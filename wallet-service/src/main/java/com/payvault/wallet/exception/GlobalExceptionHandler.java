package com.payvault.wallet.exception;

import com.payvault.wallet.dto.MessageResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(WalletServiceExceptions.WalletNotFoundException.class)
    public ResponseEntity<MessageResponse> handleNotFound(WalletServiceExceptions.WalletNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new MessageResponse(e.getMessage()));
    }

    @ExceptionHandler(WalletServiceExceptions.InsufficientBalanceException.class)
    public ResponseEntity<MessageResponse> handleInsufficientBalance(WalletServiceExceptions.InsufficientBalanceException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new MessageResponse(e.getMessage()));
    }

    @ExceptionHandler(WalletServiceExceptions.WalletAlreadyExistsException.class)
    public ResponseEntity<MessageResponse> handleAlreadyExists(WalletServiceExceptions.WalletAlreadyExistsException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new MessageResponse(e.getMessage()));
    }

    // Thrown automatically by JPA when the @Version column doesn't match —
    // i.e. two concurrent debit/credit calls collided on the same wallet.
    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<MessageResponse> handleOptimisticLock(ObjectOptimisticLockingFailureException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new MessageResponse("This wallet was updated concurrently — please retry the operation"));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<MessageResponse> handleGeneric(Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new MessageResponse("Something went wrong: " + e.getMessage()));
    }
}
