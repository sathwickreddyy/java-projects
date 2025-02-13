package com.sathwick.ewallet.transaction.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sathwick.ewallet.transaction.service.TransactionService;
import com.sathwick.ewallet.transaction.service.resource.NotificationRequest;
import com.sathwick.ewallet.transaction.service.resource.TransactionRequest;
import com.sathwick.ewallet.transaction.service.resource.WalletTransactionRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
public class TransactionServiceImpl implements TransactionService {

    @Autowired
    RestTemplate restTemplate;

    @Autowired
    KafkaTemplate kafkaTemplate;

    @Value("${ewallet.notification.topic}")
    private String notificationTopic;

    ObjectMapper objectMapper = new ObjectMapper();
    @Override
    public boolean performTransaction(Long senderId, TransactionRequest transactionRequest) {
        log.info("Initiating transaction for user "+senderId);
        log.info("Updating wallets");
        WalletTransactionRequest walletTransactionRequest = WalletTransactionRequest.builder()
                .senderId(senderId)
                .receiverId(transactionRequest.getReceiverId())
                .description(transactionRequest.getDescription())
                .amount(transactionRequest.getAmount())
                .transactionType(transactionRequest.getTransactionType())
                .build();

        String url = "http://WALLET/wallet/transaction";
        try {
            ResponseEntity<Boolean> response = restTemplate.postForEntity(url, walletTransactionRequest, Boolean.class);
            // Note:  By default, the RestTemplate throws an exception for 4xx and 5xx status codes.
            /*
             * To handle this scenario properly, you have two options:
             * 1. Catch the HttpClientErrorException.BadRequest: You can catch the HttpClientErrorException.BadRequest and handle the error case accordingly. This approach is useful if you need to perform specific error handling or logging for the 400 Bad Request status code.
             * Check the response status code and boolean result: As mentioned in the previous response, you can check the response status code and the boolean result in the response body to handle both successful and unsuccessful cases without throwing an exception.
             */
            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("Transaction successful");
                log.info("Initiated success notifications");
                NotificationRequest senderNotificationRequest = new NotificationRequest();
                senderNotificationRequest.setTransactionStatus("SUCCESS");
                senderNotificationRequest.setUserId(senderId);
                senderNotificationRequest.setAmount(transactionRequest.getAmount());
                senderNotificationRequest.setUserType("SENDER");
                // send event to Notification service to both sender and receiver
                String content = Strings.EMPTY;
                try {
                    content = objectMapper.writeValueAsString(senderNotificationRequest);
                    kafkaTemplate.send(notificationTopic, content);
                } catch (JsonProcessingException e) {
                    log.error("Error while serializing notification request " + e.getLocalizedMessage());
                    throw new RuntimeException(e);
                }
                NotificationRequest receiverNotificationRequest = new NotificationRequest();
                receiverNotificationRequest.setTransactionStatus("SUCCESS");
                receiverNotificationRequest.setUserId(transactionRequest.getReceiverId());
                receiverNotificationRequest.setAmount(transactionRequest.getAmount());
                receiverNotificationRequest.setUserType("RECEIVER");
                try {
                    content = objectMapper.writeValueAsString(receiverNotificationRequest);
                    kafkaTemplate.send(notificationTopic, content);
                } catch (JsonProcessingException e) {
                    log.error("Error while serializing notification request " + e.getLocalizedMessage());
                    throw new RuntimeException(e);
                }
                return true;
            }
        }
        catch (HttpClientErrorException exception) {
            if (exception.getStatusCode() == HttpStatus.BAD_REQUEST) {
                // send event to FAILURE event receiver
                log.info("Transaction failed");
                log.info("Initiated failure notifications");
                NotificationRequest notificationRequest = new NotificationRequest();
                notificationRequest.setTransactionStatus("FAILURE");
                notificationRequest.setUserId(senderId);
                notificationRequest.setAmount(transactionRequest.getAmount());
                notificationRequest.setUserType("SENDER");
                String content = Strings.EMPTY;
                try {
                    content = objectMapper.writeValueAsString(notificationRequest);
                    kafkaTemplate.send(notificationTopic, content);
                } catch (JsonProcessingException e) {
                    log.error("Error while serializing notification request " + e.getLocalizedMessage());
                    throw new RuntimeException(e);
                }
                return false;
            }
        }
        return false;
    }
}
