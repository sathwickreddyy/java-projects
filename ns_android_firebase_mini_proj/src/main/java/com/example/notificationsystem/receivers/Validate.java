package com.example.notificationsystem.receivers;

import com.google.firebase.database.DatabaseReference;

import java.util.regex.Pattern;

public class Validate{
    private DatabaseReference databaseReference;

    public boolean isValidEmail(String email) {
        if (email.isEmpty())
            return false;
        final String EMAIL_PATTERN = "^[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
        return Pattern.compile(EMAIL_PATTERN).matcher(email).matches();
    }
    
}
