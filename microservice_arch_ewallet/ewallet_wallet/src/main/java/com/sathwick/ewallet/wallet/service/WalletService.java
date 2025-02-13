package com.sathwick.ewallet.wallet.service;

import com.sathwick.ewallet.wallet.domain.Wallet;
import com.sathwick.ewallet.wallet.service.resource.WalletResponse;
import com.sathwick.ewallet.wallet.service.resource.WalletTransactionRequest;

public interface WalletService {
    void createWallet(Long userId);
    Wallet deleteWallet(Long userId);
    WalletResponse getWallet(Long userId);
    boolean performTransaction(WalletTransactionRequest walletTransactionRequest);
}
