package com.java.oops.bms.service.payment;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@RequiredArgsConstructor
public class Payment {
    public enum PaymentStatus  {
            PENDING, COMPLETED, CANCELLED, REFUNDED
    }
    public enum PaymentMethod {
        CASg, CREDIT_CARD, DEBIT_CARD
    }
    private final String paymentId;
    private long createdOn;
    private final long amount;
    private final PaymentMethod paymentMethod;
    private PaymentStatus paymentStatus;
}
