package com.java.ticketbookingsystem.ticketservice.service;

import com.java.ticketbookingsystem.ticketservice.exception.TicketServiceException;
import com.java.ticketbookingsystem.ticketservice.model.ShowRequest;

import java.util.List;

public interface ShowService {
    void createShow(List<ShowRequest> showRequests) throws TicketServiceException;
}
