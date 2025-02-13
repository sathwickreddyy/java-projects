package com.sathwick.ewallet.transaction.service;

import com.sathwick.ewallet.transaction.service.resource.TransactionRequest;

public interface TransactionService {
    boolean performTransaction(Long senderId, TransactionRequest transactionRequest);
}
