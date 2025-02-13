package com.example.notificationsystem.receivers;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.example.notificationsystem.R;

public class HomeActivity extends AppCompatActivity {
    private CardView  class_,marks_,lab_,transport_,exam_,attendence_;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
}
