package com.sathwick.ewallet.wallet.service.resource;

import com.sathwick.ewallet.wallet.domain.Wallet;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class WalletResponse {
    private Long userId;
    private Long walletId;
    private Double balance;

    public WalletResponse(Wallet wallet){
        this.userId = wallet.getUserId();
        this.walletId = wallet.getId();
        this.balance = wallet.getBalance();
    }
}
