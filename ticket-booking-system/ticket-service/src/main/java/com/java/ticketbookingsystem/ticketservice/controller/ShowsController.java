package com.java.ticketbookingsystem.ticketservice.controller;

import com.java.ticketbookingsystem.ticketservice.model.ResponseHandler;
import com.java.ticketbookingsystem.ticketservice.model.ShowRequest;
import com.java.ticketbookingsystem.ticketservice.service.ShowService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/v1/shows")
@RequiredArgsConstructor
public class ShowsController {

    private final ShowService showService;

    @PostMapping
    public ResponseEntity<ResponseHandler<String>> createShow(@RequestBody @Valid List<ShowRequest> showRequests) {
        log.info("Request to create Show request: {}", showRequests.size());
        showService.createShow(showRequests);
        ResponseHandler<String> responseHandler = new ResponseHandler<>();
        responseHandler.setMessage("Shows created successfully");
        return ResponseEntity.status(HttpStatus.CREATED).body(responseHandler);
    }

}
