package com.payvault.wallet.exception;

public class WalletServiceExceptions {

    public static class WalletNotFoundException extends RuntimeException {
        public WalletNotFoundException(String message) { super(message); }
    }

    public static class InsufficientBalanceException extends RuntimeException {
        public InsufficientBalanceException(String message) { super(message); }
    }

    public static class WalletAlreadyExistsException extends RuntimeException {
        public WalletAlreadyExistsException(String message) { super(message); }
    }

    public static class ConcurrentUpdateException extends RuntimeException {
        public ConcurrentUpdateException(String message) { super(message); }
    }
}
