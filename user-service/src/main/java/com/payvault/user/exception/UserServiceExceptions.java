package com.payvault.user.exception;

public class UserServiceExceptions {

    public static class DuplicateEmailException extends RuntimeException {
        public DuplicateEmailException(String message) { super(message); }
    }

    public static class InvalidCredentialsException extends RuntimeException {
        public InvalidCredentialsException(String message) { super(message); }
    }

    public static class UserNotFoundException extends RuntimeException {
        public UserNotFoundException(String message) { super(message); }
    }

    public static class InvalidOtpException extends RuntimeException {
        public InvalidOtpException(String message) { super(message); }
    }
}
