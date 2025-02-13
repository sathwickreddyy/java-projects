package com.sathwick.ewallet.notification.service.resource;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationRequest {
    @NotBlank
    private Long userId;
    @Min(value = 0)
    private Double amount;
    @NotBlank
    private String userType;
    @NotBlank
    private String transactionStatus;
}
