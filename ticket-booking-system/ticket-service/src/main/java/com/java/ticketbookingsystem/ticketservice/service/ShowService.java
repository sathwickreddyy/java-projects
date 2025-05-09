package com.java.ticketbookingsystem.ticketservice.service;

import com.java.ticketbookingsystem.ticketservice.exception.TicketServiceException;
import com.java.ticketbookingsystem.ticketservice.model.ShowRequest;

public interface ShowService {
    void createShow(ShowRequest showRequest) throws TicketServiceException;
}
