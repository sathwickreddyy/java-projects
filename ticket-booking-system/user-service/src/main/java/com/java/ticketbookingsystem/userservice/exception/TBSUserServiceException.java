package com.java.ticketbookingsystem.userservice.exception;

public class TBSUserServiceException extends RuntimeException {

    public TBSUserServiceException(String message) {
        super(message);
    }

    public TBSUserServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}