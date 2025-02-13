package com.sathwick.ewallet.notification.exception;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class NotificationException extends RuntimeException{
    private String type;
    private String message;
}
