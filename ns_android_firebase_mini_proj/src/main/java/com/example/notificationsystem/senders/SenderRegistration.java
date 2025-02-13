package com.example.notificationsystem.senders;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.notificationsystem.R;
import com.example.notificationsystem.receivers.Login;
import com.example.notificationsystem.receivers.Registration;
import com.example.notificationsystem.receivers.Validate;
import com.example.notificationsystem.templates.SenderTemplate;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SenderRegistration extends AppCompatActivity {

    private EditText firstName,password,confirmPassword,email;
    private Spinner prefix,dept,lastName;
    private ImageButton register;
    private ToggleButton haveLogin;
    private ProgressBar progressBar;

    private SenderTemplate sender;
    private FirebaseUser currentUser;
    private FirebaseAuth mAuth;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sender_registration);
        Init();
        registrationSettings();
    }


    private void Init(){
        firstName=findViewById(R.id.firstname);
        password=findViewById(R.id.password);
        confirmPassword=findViewById(R.id.confirmpassword);
        email=findViewById(R.id.email);
        prefix=findViewById(R.id.prefix);
        lastName=findViewById(R.id.suffix);
        dept=findViewById(R.id.course);
        register=findViewById(R.id.register);
        haveLogin=findViewById(R.id.havelogin);
        progressBar=findViewById(R.id.progress);
        progressBar.setVisibility(View.INVISIBLE);
        mAuth=FirebaseAuth.getInstance();
        databaseReference= FirebaseDatabase.getInstance().getReference("Senders");
        haveLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SenderRegistration.this,Login.class));
                finish();
            }
        });
    }

    private void registrationSettings(){
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name=prefix.getSelectedItem()+firstName.getText().toString();
                firstName.setText(name);
                String last=String.valueOf(lastName.getSelectedItem());
                if(!last.equals("other"))
                    name+=" "+last;
                String Email=email.getText().toString();
                String Password=password.getText().toString();
                String ConfirmPassword=confirmPassword.getText().toString();
                String Dept=String.valueOf(dept.getSelectedItem());

                if(firstName.getText().toString().isEmpty() || last.equals("Choose") || Email.isEmpty() || Password.isEmpty() ||ConfirmPassword.isEmpty())
                    Toast.makeText(SenderRegistration.this,"Please fill details",Toast.LENGTH_SHORT).show();
                else if(!new Validate().isValidEmail(Email))
                    Toast.makeText(SenderRegistration.this,"Invalid Email! Email Validation Error",Toast.LENGTH_SHORT).show();
                else if(!Password.equals(ConfirmPassword))
                    Toast.makeText(SenderRegistration.this,"Passwords Didn't matched",Toast.LENGTH_SHORT).show();
                else
                {
                    progressBar.setVisibility(View.VISIBLE);
                    register.setVisibility(View.INVISIBLE);
                    sender=new SenderTemplate(name,Email,Dept);
                    mAuth.createUserWithEmailAndPassword(Email,Password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful())
                            {
                                currentUser=mAuth.getCurrentUser();
                                currentUser.sendEmailVerification().addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Toast.makeText(SenderRegistration.this, "Account Created Please Verify Email", Toast.LENGTH_SHORT).show();
                                        sender.setId(currentUser.getUid());
                                        databaseReference.child(currentUser.getUid()).setValue(sender);
                                        databaseReference.child(currentUser.getUid()).child("url").setValue("default");
                                        progressBar.setVisibility(View.INVISIBLE);
                                        register.setVisibility(View.VISIBLE);
                                        DatabaseReference dref=FirebaseDatabase.getInstance().getReference("Course").child(sender.getDept()).child("FACULTY");
                                        dref.child(currentUser.getUid()).setValue(sender.getName());
                                        startActivity(new Intent(SenderRegistration.this, Login.class));
                                        finish();
                                    }
                                });
                                progressBar.setVisibility(View.INVISIBLE);
                                register.setVisibility(View.VISIBLE);
                            }
                            else {
                                if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                                    Toast.makeText(SenderRegistration.this,
                                            "User with this email already exist.", Toast.LENGTH_SHORT).show();

                                }
                                else {
                                    Toast.makeText(SenderRegistration.this, "Authentication failed", Toast.LENGTH_SHORT).show();
                                }
                                progressBar.setVisibility(View.INVISIBLE);
                                register.setVisibility(View.VISIBLE);
                            }
                        }
                    });
                }
            }
        });
    }
}
