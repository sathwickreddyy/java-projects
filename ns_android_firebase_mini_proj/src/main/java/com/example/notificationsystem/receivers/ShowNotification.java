package com.example.notificationsystem.receivers;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.notificationsystem.R;
import com.example.notificationsystem.adapter.ShowViewAdapter;
import com.example.notificationsystem.senders.SendNotification;
import com.example.notificationsystem.templates.NotificationTemplate;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class ShowNotification extends AppCompatActivity {

    private TextView SUBJECT,CONTENT,NAME;
    private CircleImageView PROFILE_PIC_SENDERS;
    private RecyclerView recyclerView;
    private ShowViewAdapter showViewAdapter;

    private void Init(){
        SUBJECT=findViewById(R.id.subject);
        CONTENT=findViewById(R.id.show_content);
        NAME=findViewById(R.id.sender_name);
        PROFILE_PIC_SENDERS=findViewById(R.id.profile_pic);
        recyclerView=findViewById(R.id.recyclerView);
        LinearLayoutManager linearLayoutManager=new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setOrientation(RecyclerView.VERTICAL);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setHasFixedSize(true);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_notification);
        Init();
        Intent intent=getIntent();

        SUBJECT.setText(intent.getStringExtra("Subject"));
        CONTENT.setText(intent.getStringExtra("Content"));
        NAME.setText(intent.getStringExtra("Sender Name"));
        String url=intent.getStringExtra("Sender Pic");
        if(url==null || url.equals("default")){
            PROFILE_PIC_SENDERS.setImageResource(R.drawable.imh);
        }
        else{
            Glide.with(getApplicationContext()).load(url).into(PROFILE_PIC_SENDERS);
        }
        ArrayList<String> imageUris= intent.getStringArrayListExtra("Image Urls");
        if(imageUris!=null) {
            showViewAdapter = new ShowViewAdapter(getApplicationContext(), imageUris);
            recyclerView.setAdapter(showViewAdapter);
        }
    }
}
