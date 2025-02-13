package com.example.notificationsystem.senders;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.bumptech.glide.Glide;
import com.example.notificationsystem.R;
import com.example.notificationsystem.receivers.Login;
import com.example.notificationsystem.receivers.MainActivity;
import com.example.notificationsystem.templates.SenderTemplate;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import de.hdodenhof.circleimageview.CircleImageView;

public class HomePageSender extends AppCompatActivity {

    private CircleImageView profile_pic;
    private TextView name;
    private CardView send;
    private Button logout;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private DatabaseReference databaseReference;

    private void Init(){

        profile_pic=findViewById(R.id.profile_pic);
        name=findViewById(R.id.name);
        send=findViewById(R.id.send);
        logout=findViewById(R.id.logout);


        mAuth=FirebaseAuth.getInstance();
        currentUser=mAuth.getCurrentUser();
        databaseReference= FirebaseDatabase.getInstance().getReference("Senders");

    }

    private void load(){
        databaseReference.child(currentUser.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                SenderTemplate senderTemplate=dataSnapshot.getValue(SenderTemplate.class);
                String url=senderTemplate.getImageUri();
                if(url==null || url.equals("default"))
                    profile_pic.setImageResource(R.drawable.imh);
                else
                    Glide.with(getApplicationContext()).load(url).into(profile_pic);
                name.setText(senderTemplate.getName());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page_sender);
        Init();
        load();
        sendNotification();
        logout();
    }

    private void sendNotification(){
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(HomePageSender.this,SendNotification.class));
            }
        });
    }

    private void logout(){
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAuth.signOut();
                startActivity(new Intent(HomePageSender.this, Login.class));
                finish();
            }
        });
    }
}
