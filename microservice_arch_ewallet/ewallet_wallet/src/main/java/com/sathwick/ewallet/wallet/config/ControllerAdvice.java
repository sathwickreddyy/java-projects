package com.sathwick.ewallet.wallet.config;

import com.sathwick.ewallet.wallet.exception.WalletException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class ControllerAdvice {

    @ExceptionHandler(WalletException.class)
    public ResponseEntity<Map<String, String>> handleUserException(WalletException e) {
        Map<String, String> errorsMap = new HashMap<>();
        errorsMap.put("type", e.getType());
        errorsMap.put("message", e.getMessage());
        return new ResponseEntity<>(errorsMap, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException exception){
        Map<String, String> errorsMap = new HashMap<>();
        exception.getBindingResult().getFieldErrors().forEach(error ->
                errorsMap.put(error.getField(), error.getDefaultMessage()));
        return new ResponseEntity<>(errorsMap, HttpStatus.BAD_REQUEST);
    }

}
