package com.sathwick.ewallet.notification.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sathwick.ewallet.notification.service.NotificationService;
import com.sathwick.ewallet.notification.service.resource.NotificationRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class NotificationConsumer {

    @Autowired
    private NotificationService notificationService;
    ObjectMapper mapper = new ObjectMapper();

    @KafkaListener(topics = "notification-topic", groupId = "notificationGroup")
    public void consumer(String message)  {
        log.info("Consumed message by notification consumer: "+message);
        try {
            NotificationRequest notificationRequest = mapper.readValue(message, NotificationRequest.class);
            notificationService.sendCommunication(notificationRequest);
        } catch (JsonProcessingException e) {
            log.error("Error while parsing notification content : "+e.getMessage());
        }
    }

}
