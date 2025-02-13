package com.sathwick.ewallet.notification.service.impl;

import com.sathwick.ewallet.notification.exception.NotificationException;
import com.sathwick.ewallet.notification.service.NotificationService;
import com.sathwick.ewallet.notification.service.resource.NotificationRequest;
import com.sathwick.ewallet.notification.service.resource.UserResponse;
import com.sathwick.ewallet.notification.service.util.MailContentUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class NotificationServiceImpl implements NotificationService {

    @Autowired
    RestTemplate restTemplate;

    @Autowired
    private JavaMailSender mailSender;
    @Override
    public void sendCommunication(NotificationRequest notificationRequest) {
        // get user email
        // get notification type
        // populate the values / template
        // send email
        if(notificationRequest.getTransactionStatus().equalsIgnoreCase("SUCCESS") && notificationRequest.getUserType().equalsIgnoreCase("SENDER")){
            // fetch the user from userService
            ResponseEntity<UserResponse> responseEntity = restTemplate.getForEntity("http://USERSERVICE/users/"+notificationRequest.getUserId(), UserResponse.class);
            if(responseEntity.getStatusCode().is2xxSuccessful()){
                UserResponse response = responseEntity.getBody();
                SimpleMailMessage message = new SimpleMailMessage();
                message.setSubject(MailContentUtil.getSubjectTransactionSuccessful());
                message.setText(MailContentUtil.getSenderSuccessEmailContent(response.getName(), notificationRequest.getAmount()));
                message.setTo(response.getEmail());
                mailSender.send(message);
            }else{
                throw new NotificationException("EWALLET_USER_NOT_FOUND", "Unable to fetch / user not found");
            }
        }
        if(notificationRequest.getTransactionStatus().equalsIgnoreCase("SUCCESS") && notificationRequest.getUserType().equalsIgnoreCase("RECEIVER")){
            // fetch the user from userService
            ResponseEntity<UserResponse> responseEntity = restTemplate.getForEntity("http://USERSERVICE/users/"+notificationRequest.getUserId(), UserResponse.class);
            if(responseEntity.getStatusCode().is2xxSuccessful()){
                UserResponse response = responseEntity.getBody();
                SimpleMailMessage message = new SimpleMailMessage();
                message.setSubject(MailContentUtil.getSubjectTransactionSuccessful());
                message.setText(MailContentUtil.getReceiverSuccessEmailContent(response.getName(), notificationRequest.getAmount()));
                message.setTo(response.getEmail());
                mailSender.send(message);
            }
            else{
                throw new NotificationException("EWALLET_USER_NOT_FOUND", "Unable to fetch / user not found");
            }
        }

        // if getTransactionStatus is FAILURE and userType is SENDER then send failure notification
        if(notificationRequest.getTransactionStatus().equalsIgnoreCase("FAILURE") && notificationRequest.getUserType().equalsIgnoreCase("SENDER")){
            // fetch the user from userService
            ResponseEntity<UserResponse> responseEntity = restTemplate.getForEntity("http://USERSERVICE/users/"+notificationRequest.getUserId(), UserResponse.class);
            if(responseEntity.getStatusCode().is2xxSuccessful()){
                UserResponse response = responseEntity.getBody();
                SimpleMailMessage message = new SimpleMailMessage();
                message.setSubject(MailContentUtil.getSubjectTransactionUnsuccessful());
                message.setText(MailContentUtil.getFailureSenderEmailContent(response.getName(), notificationRequest.getAmount()));
                message.setTo(response.getEmail());
                mailSender.send(message);
            }
            else{
                throw new NotificationException("EWALLET_USER_NOT_FOUND", "Unable to fetch / user not found");
            }
        }

    }
}
