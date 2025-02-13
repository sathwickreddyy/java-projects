package com.sathwick.ewallet.userservice.exception;

import lombok.Getter;
import lombok.Setter;

public class UserException extends RuntimeException{

    @Getter @Setter
    private String type;
    @Getter @Setter
    private String message;

    public UserException(String type, String message) {
        this.type = type;
        this.message = message;
    }



}
