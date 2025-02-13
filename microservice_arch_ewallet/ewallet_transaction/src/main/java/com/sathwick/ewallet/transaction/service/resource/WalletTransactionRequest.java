package com.sathwick.ewallet.transaction.service.resource;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class WalletTransactionRequest {
    @NotBlank
    private Long senderId;
    @NotBlank
    private Long receiverId;
    @Min(value = 0)
    private Double amount;
    private String description;
    private String transactionType;
}
