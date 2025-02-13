package com.sathwick.ewallet.wallet.controller;

import com.sathwick.ewallet.wallet.domain.Wallet;
import com.sathwick.ewallet.wallet.service.WalletService;
import com.sathwick.ewallet.wallet.service.resource.WalletResponse;
import com.sathwick.ewallet.wallet.service.resource.WalletTransactionRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class WalletController {

    @Autowired
    private WalletService walletService;

    @GetMapping("/wallet/{user-id}")
    public ResponseEntity<WalletResponse> getWalletDetails(@PathVariable("user-id") Long userId){
        return new ResponseEntity<>(walletService.getWallet(userId), HttpStatus.OK);
    }

    @PostMapping("/wallet/transaction")
    public ResponseEntity<Boolean> performTransaction(@RequestBody @Valid WalletTransactionRequest walletTransactionRequest) {
        Boolean response = walletService.performTransaction(walletTransactionRequest);
        return Boolean.TRUE.equals(response) ? new ResponseEntity<>(response, HttpStatus.NO_CONTENT) : new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }
}
