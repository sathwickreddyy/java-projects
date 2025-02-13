package com.sathwick.ewallet.wallet.service.kafka_consumer;

import com.sathwick.ewallet.wallet.service.WalletService;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class UserActionConsumer {

    @Autowired
    private WalletService walletService;


    @KafkaListener(topics = "${kafka.topic.user-created}", groupId = "walletGroup")
    public void consumeUserCreated(String message) {
        // log below message
        log.info("Consumed message from topic: " + "USER_CREATED" + " - " + message);
        // trigger create wallet service
        walletService.createWallet(Long.valueOf(message));
    }

    @KafkaListener(topics = "${kafka.topic.user-deleted}", groupId = "walletGroup")
    public void consumeUserDeleted(String message) {
        // log below message
        log.info("Consumed message from topic: " + "USER_DELETED" + " - " + message);
        // trigger create wallet service
        walletService.deleteWallet(Long.valueOf(message));
    }
}
