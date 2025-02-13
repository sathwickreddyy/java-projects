package com.sathwick.ewallet.notification.service;

import com.sathwick.ewallet.notification.service.resource.NotificationRequest;

public interface NotificationService {

    void sendCommunication(NotificationRequest notificationRequest);
}
