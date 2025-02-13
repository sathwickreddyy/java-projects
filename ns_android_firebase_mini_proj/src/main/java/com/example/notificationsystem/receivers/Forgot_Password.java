package com.example.notificationsystem.receivers;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.notificationsystem.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class Forgot_Password extends AppCompatActivity {
    private EditText email;
    private Button btn;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot__password);
        mAuth=FirebaseAuth.getInstance();
        email = findViewById(R.id.email_forgot);
        btn = findViewById(R.id.resetBtn);
        try {
            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (new Validate().isValidEmail(email.getText().toString())) {
                        mAuth.sendPasswordResetEmail(email.getText().toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(Forgot_Password.this, "Reset Link Sent", Toast.LENGTH_LONG).show();
                                } else {
                                    Toast.makeText(Forgot_Password.this, "Unable to send reset link (No Such User Exists/No internet connection)", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    } else {
                        Toast.makeText(Forgot_Password.this, "Please Enter valid email address", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } catch (Exception e) {
            Toast.makeText(Forgot_Password.this, ""+e.toString(), Toast.LENGTH_SHORT).show();
        }
    }
}
