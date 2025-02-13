package com.sathwick.ewallet.notification.service.util;

public class MailContentUtil {

    public static String getReceiverSuccessEmailContent(String username, Double amount){
        StringBuilder builder = new StringBuilder();
        builder.append("Hi, " + username + "\n");
        builder.append("Your account has been credited with INR " + amount + "\n");
        builder.append("Thanks for using our service\n");
        return builder.toString();
    }

    public static String getSenderSuccessEmailContent(String username, Double amount){
        StringBuilder builder = new StringBuilder();
        builder.append("Hi, " + username + "\n");
        builder.append("Your account has been debited with INR " + amount + "\n");
        builder.append("Your transaction is successful"+"\n");
        builder.append("Thanks for using our service\n");
        return builder.toString();
    }

    public static String getFailureSenderEmailContent(String username, Double amount){
        StringBuilder builder = new StringBuilder();
        builder.append("Hi, " + username + "\n");
        builder.append("Your last transaction was unsuccessful. Please try again\n");
        builder.append("Thanks for using our service\n");
        return builder.toString();
    }

    public static String getSubjectTransactionSuccessful(){
        return "Transaction Successful";
    }

    public static String getSubjectTransactionUnsuccessful(){
        return "Transaction Unsuccessful";
    }

}
