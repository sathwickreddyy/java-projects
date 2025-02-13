package com.sathwick.ewallet.transaction.service.resource;


import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionRequest {
    @NotNull
    private Long receiverId;
    private String description;
    private String transactionType;
    @Min(value = 0)
    private Double amount;

}
