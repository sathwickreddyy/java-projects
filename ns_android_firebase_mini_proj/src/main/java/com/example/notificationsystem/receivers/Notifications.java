package com.example.notificationsystem.receivers;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.WindowDecorActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.notificationsystem.R;
import com.example.notificationsystem.adapter.NotificationsAdapter;
import com.example.notificationsystem.templates.NotificationTemplate;
import com.example.notificationsystem.templates.SenderTemplate;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.LinkedList;

public class Notifications extends AppCompatActivity {

    private RecyclerView recyclerView;
    private NotificationsAdapter notificationsAdapter;
    private FirebaseUser firebaseUser;
    private DatabaseReference databaseReference;
    private ArrayList<NotificationTemplate> notifications;
    private NotificationTemplate notificationTemplate;
    private ImageView back;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);
        Toolbar toolbar=findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        back=findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Notifications.this,MainActivity.class));
            }
        });
        notifications=new ArrayList<>();
        recyclerView=findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(Notifications.this));
        recyclerView.setHasFixedSize(true);
        notificationsAdapter=new NotificationsAdapter(getApplicationContext(),notifications);
        recyclerView.setAdapter(notificationsAdapter);
        firebaseUser= FirebaseAuth.getInstance().getCurrentUser();
        databaseReference= FirebaseDatabase.getInstance().getReference("NOTIFICATIONS");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    notifications.clear();
                    for(DataSnapshot ds: dataSnapshot.getChildren()){
                        notificationTemplate=ds.getValue(NotificationTemplate.class);
                        if(notificationTemplate.getReceiver().equals(firebaseUser.getDisplayName()))
                        {
                            notifications.add(0,notificationTemplate);
                            notificationsAdapter.notifyDataSetChanged();
                        }
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
