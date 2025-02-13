package com.sathwick.ewallet.wallet.service.impl;

import com.sathwick.ewallet.wallet.domain.Wallet;
import com.sathwick.ewallet.wallet.exception.WalletException;
import com.sathwick.ewallet.wallet.repository.WalletRepository;
import com.sathwick.ewallet.wallet.service.resource.WalletTransactionRequest;
import com.sathwick.ewallet.wallet.util.TransactionType;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class WalletServiceImplTest {

    @Test
    void performTransactionTestDeposit() {
        // Arrange
        WalletServiceImpl walletService = new WalletServiceImpl();
        WalletRepository walletRepository = mock(WalletRepository.class);
        walletService.setWalletRepository(walletRepository);

        Wallet wallet = new Wallet();
        wallet.setBalance(100.0);
        wallet.setUserId(102L);
        wallet.setId(1L);

        // after deposit of 100
        Wallet expectedWallet = new Wallet();
        expectedWallet.setBalance(200.0);
        expectedWallet.setUserId(102L);
        expectedWallet.setId(1L);

        // Act
        WalletTransactionRequest walletTransactionRequest = new WalletTransactionRequest();
        walletTransactionRequest.setAmount(100.0);
        walletTransactionRequest.setReceiverId(102L); // dummy id
        walletTransactionRequest.setSenderId(102L);
        walletTransactionRequest.setTransactionType(TransactionType.DEPOSIT.name());

        when(walletRepository.findByUserId(anyLong())).thenReturn(Optional.of(wallet)); // make sures we receive the wallet always to avoid exception
        when(walletRepository.save(any(Wallet.class))).thenReturn(wallet);

        walletService.performTransaction(walletTransactionRequest);

        // assert
        assertNotNull(wallet);
        assertEquals(expectedWallet.getBalance(), wallet.getBalance());
        // Add more assertions as needed
    }

    @Test
    void performTransactionForInvalidWallet(){
        // Arrange
        WalletServiceImpl walletService = new WalletServiceImpl();
        WalletRepository walletRepository = mock(WalletRepository.class);
        walletService.setWalletRepository(walletRepository);

        WalletTransactionRequest walletTransactionRequest = new WalletTransactionRequest();
        walletTransactionRequest.setAmount(100.0);
        walletTransactionRequest.setReceiverId(102L); // dummy id
        walletTransactionRequest.setSenderId(102L);
        walletTransactionRequest.setTransactionType(TransactionType.DEPOSIT.name());

        when(walletRepository.findByUserId(anyLong())).thenReturn(Optional.empty());

        // will not work as the exception is being handled there itself
        //        assertThrows(WalletException.class, () -> {
        //            walletService.performTransaction(walletTransactionRequest);
        //        });
        assertFalse(walletService.performTransaction(walletTransactionRequest));
    }
}