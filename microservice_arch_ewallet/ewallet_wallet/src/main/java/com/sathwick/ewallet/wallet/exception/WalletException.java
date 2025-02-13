package com.sathwick.ewallet.wallet.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
public class WalletException extends RuntimeException {
    private final String type;
    private final String message;
}
