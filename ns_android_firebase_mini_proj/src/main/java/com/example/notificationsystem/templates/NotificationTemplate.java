package com.example.notificationsystem.templates;


import java.util.List;

public class NotificationTemplate {
    private String sender,receiver,subject,content,senderDept;
    private String senderImg;
    private List<String> imageURLS;

    public NotificationTemplate(String sender, String receiver, String subject, String content, String senderDept, String senderImg, List<String> imageURLS) {
        this.sender = sender;
        this.receiver = receiver;
        this.subject = subject;
        this.content = content;
        this.senderDept = senderDept;
        this.senderImg = senderImg;
        this.imageURLS = imageURLS;
    }

    public NotificationTemplate(String sender, String receiver, String subject, String content, String senderDept, String senderImg) {
        this.sender = sender;
        this.receiver = receiver;
        this.subject = subject;
        this.content = content;
        this.senderDept = senderDept;
        this.senderImg = senderImg;
    }

    public NotificationTemplate(){

    }
    public String getSenderImg() {
        return senderImg;
    }

    public String getSenderDept() {
        return senderDept;
    }

    public void setSenderDept(String senderDept) {
        this.senderDept = senderDept;
    }

    public void setSenderImg(String senderImg) {
        this.senderImg = senderImg;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public List<String> getImageURLS() {
        return imageURLS;
    }

    public void setImageURLS(List<String> imageURLS) {
        this.imageURLS = imageURLS;
    }

}
