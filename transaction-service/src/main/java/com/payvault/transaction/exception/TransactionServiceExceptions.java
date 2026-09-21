package com.payvault.transaction.exception;

public class TransactionServiceExceptions {

    public static class InvalidPinException extends RuntimeException {
        public InvalidPinException(String message) { super(message); }
    }

    public static class InvalidTransferException extends RuntimeException {
        public InvalidTransferException(String message) { super(message); }
    }

    public static class TransactionNotFoundException extends RuntimeException {
        public TransactionNotFoundException(String message) { super(message); }
    }

    // Thrown by the resilience wrappers when the circuit is open or retries are exhausted.
    public static class ServiceUnavailableException extends RuntimeException {
        public ServiceUnavailableException(String message) { super(message); }
    }
}
