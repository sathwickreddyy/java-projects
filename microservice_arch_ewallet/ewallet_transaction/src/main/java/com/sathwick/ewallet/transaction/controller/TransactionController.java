package com.sathwick.ewallet.transaction.controller;

import com.sathwick.ewallet.transaction.service.TransactionService;
import com.sathwick.ewallet.transaction.service.resource.TransactionRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
public class TransactionController {

    @Autowired
    TransactionService transactionService;

    @PostMapping("/transactions/{user-id}")
    public ResponseEntity<Boolean> createTransaction(@PathVariable("user-id") Long senderId, @RequestBody @Valid TransactionRequest transactionRequest) {
        log.info("Transaction initiated for user "+senderId);
        Boolean result = transactionService.performTransaction(senderId, transactionRequest);
        return new ResponseEntity<>(result, HttpStatus.CREATED);
    }
}
