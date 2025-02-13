package com.sathwick.ewallet.wallet.service.resource;

import com.sathwick.ewallet.wallet.util.TransactionType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class WalletTransactionRequest {
    @NotNull
    private Long senderId;
    @NotNull
    private Long receiverId;
    @Min(value = 0)
    private Double amount;
    private String description;
    private String transactionType;
}
