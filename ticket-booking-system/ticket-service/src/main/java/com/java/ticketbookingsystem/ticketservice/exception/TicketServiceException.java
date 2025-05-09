package com.java.ticketbookingsystem.ticketservice.exception;

import com.java.ticketbookingsystem.ticketservice.enums.TicketServiceExceptionType;

public class TicketServiceException extends RuntimeException {
    public TicketServiceException(String message, TicketServiceExceptionType ticketServiceExceptionType) {
        super(ticketServiceExceptionType+" : "+message);
    }
}
