package com.example.notificationsystem.receivers;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.example.notificationsystem.R;
import com.example.notificationsystem.senders.HomePageSender;
import com.example.notificationsystem.senders.SendNotification;
import com.example.notificationsystem.senders.SenderRegistration;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class Login extends AppCompatActivity {
    private ImageView showhide;
    private FirebaseAuth mAuth;
    private Button login;
    private ProgressBar pb;
    private EditText pass,email;
    private TextView redirect,forgot;
    boolean show=false;
    private Switch aSwitch;


    @Override
    protected void onStart() {
        super.onStart();
        if(mAuth.getCurrentUser()!=null && mAuth.getCurrentUser().isEmailVerified())
        {
            if(mAuth.getCurrentUser().getDisplayName().length()==12)
                startActivity(new Intent(Login.this, MainActivity.class));
            else
                startActivity(new Intent(Login.this, HomePageSender.class));
        }
    }
    private void Init(){
        aSwitch=findViewById(R.id.switch1);
        aSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    getSupportActionBar().setTitle("Sender Login");
                }
                else{
                    getSupportActionBar().setTitle("Receiver Login");
                }
            }
        });
        getSupportActionBar().setTitle("Receiver Login");
        mAuth=FirebaseAuth.getInstance();
        forgot=findViewById(R.id.forgot_password);
        forgot.setTextColor(Color.parseColor("#000000"));
        forgot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                forgot.setTextColor(Color.parseColor("#FFFFFF"));
                startActivity(new Intent(Login.this, Forgot_Password.class));
            }
        });
        pb=findViewById(R.id.progressBar_login_page);
        pb.setVisibility(View.INVISIBLE);
        email=findViewById(R.id.email);
        showhide= findViewById(R.id.showhide);
        pass= findViewById(R.id.pass);
        showhide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!pass.getText().toString().isEmpty()) {
                    if (!show) {
                        pass.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                        show = true;
                    } else {
                        pass.setTransformationMethod(PasswordTransformationMethod.getInstance());
                        show = false;
                    }
                }
            }
        });
        redirect=findViewById(R.id.redirect);
        redirect.setTextColor(Color.parseColor("#000000"));
        redirect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                redirect.setTextColor(Color.parseColor("#FFFFFF"));
                if(getSupportActionBar().getTitle().toString().equals("Sender Login"))
                    startActivity(new Intent(Login.this, SenderRegistration.class));
                else
                    startActivity(new Intent(Login.this, Registration.class));
                finish();
            }
        });
        login=findViewById(R.id.button_login);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login2);
        Init();
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String Email,Pass;
                Email=email.getText().toString();
                Pass=pass.getText().toString();
                login.setTextColor(Color.parseColor("#FFFFFF"));
                if(Email.isEmpty() || Pass.isEmpty())
                {
                    Toast.makeText(Login.this,"Please fill details to proceed",Toast.LENGTH_SHORT).show();
                    login.setTextColor(Color.parseColor("#000000"));
                }
                else {
                    if(new Validate().isValidEmail(Email)) {
                        login.setTextColor(Color.parseColor("#000000"));
                        pb.setVisibility(View.VISIBLE);
                        login.setVisibility(View.INVISIBLE);
                        mAuth.signInWithEmailAndPassword(Email, Pass).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                            @Override
                            public void onSuccess(AuthResult authResult) {
                                if(getSupportActionBar().getTitle().toString().equals("Receiver Login") && mAuth.getCurrentUser().getDisplayName()!=null) {
                                    if (mAuth.getCurrentUser().isEmailVerified()) {
                                        Toast.makeText(Login.this, "Login Successful", Toast.LENGTH_SHORT).show();
                                        Intent intent = new Intent(Login.this, MainActivity.class);
                                        startActivity(intent);
                                        finish();
                                    } else {
                                        Toast.makeText(Login.this, "Please Verify your email address", Toast.LENGTH_SHORT).show();
                                        pb.setVisibility(View.INVISIBLE);
                                        login.setVisibility(View.VISIBLE);
                                    }
                                }
                                else if(getSupportActionBar().getTitle().toString().equals("Sender Login") && mAuth.getCurrentUser().getDisplayName()==null){
                                    if (mAuth.getCurrentUser().isEmailVerified()) {
                                        Toast.makeText(Login.this, "Login Successful", Toast.LENGTH_SHORT).show();
                                        Intent intent = new Intent(Login.this, HomePageSender.class);
                                        startActivity(intent);
                                        finish();
                                    } else {
                                        Toast.makeText(Login.this, "Please Verify your email address", Toast.LENGTH_SHORT).show();
                                        pb.setVisibility(View.INVISIBLE);
                                        login.setVisibility(View.VISIBLE);
                                    }
                                }
                                else{
                                    Toast.makeText(Login.this,"Unauthorized Login switch login page",Toast.LENGTH_LONG).show();
                                    mAuth.signOut();
                                    email.setText(Email);
                                    pass.setText(Pass);
                                    pb.setVisibility(View.INVISIBLE);
                                    login.setVisibility(View.VISIBLE);
                                }
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(Login.this, "Login Unsuccessful please enter valid email address and password", Toast.LENGTH_LONG).show();
                                pb.setVisibility(View.INVISIBLE);
                                login.setVisibility(View.VISIBLE);
                            }
                        });
                    }
                    else{
                        Toast.makeText(Login.this, "Please Enter Valid email address", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }
}
