package com.java.ticketbookingsystem.ticketservice.service.impl;

import com.java.ticketbookingsystem.ticketservice.enums.TicketServiceExceptionType;
import com.java.ticketbookingsystem.ticketservice.exception.TicketServiceException;
import com.java.ticketbookingsystem.ticketservice.model.ShowRequest;
import com.java.ticketbookingsystem.ticketservice.repository.ShowsRepository;
import com.java.ticketbookingsystem.ticketservice.service.ShowService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShowServiceImpl implements ShowService {

    private final ShowsRepository showsRepository;

    @Override
    public void createShow(List<ShowRequest> showRequests) throws TicketServiceException {
        try {
            log.info("Creating shows: {}", showRequests.size());
            showsRepository.saveAll(showRequests);
            log.info("Show created successfully");
        }
        catch (DataAccessException e) {
            throw new TicketServiceException(TicketServiceExceptionType.SHOW_CREATION_FAILED, "Error creating shows in database.");
        }
    }
}
