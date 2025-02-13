package com.sathwick.ewallet.userservice.service.resource;


import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@Builder
public class TransactionRequest {
    private Long receiverId;
    private Double amount;
    private String description;
    private String transactionType;

    @Override
    public String toString() {
        return "TransactionRequest{" +
                "receiverId=" + receiverId +
                ", amount=" + amount +
                ", description='" + description + '\'' +
                ", transactionType='" + transactionType + '\'' +
                '}';
    }
}
