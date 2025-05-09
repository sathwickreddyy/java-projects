package com.java.ticketbookingsystem.ticketservice.service.impl;

import com.java.ticketbookingsystem.ticketservice.exception.TicketServiceException;
import com.java.ticketbookingsystem.ticketservice.model.ShowRequest;
import com.java.ticketbookingsystem.ticketservice.repository.ShowsRepository;
import com.java.ticketbookingsystem.ticketservice.service.ShowService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShowServiceImpl implements ShowService {

    private final ShowsRepository showsRepository;

    @Override
    public void createShow(ShowRequest showRequest) throws TicketServiceException {
        log.info("Creating show: {}", showRequest);
        showsRepository.save(showRequest);
        log.info("Show created successfully");
    }
}
