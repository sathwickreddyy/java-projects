package com.example.notificationsystem.receivers;

import android.app.Notification;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;

import com.bumptech.glide.Glide;
import com.example.notificationsystem.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.core.view.GravityCompat;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import androidx.drawerlayout.widget.DrawerLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.Menu;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener
{

    private TextView title;
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        //getMenuInflater().inflate(R.menu.main_page_side_menu,menu);
        return true;
    }
    private void setNavigationViewListener() {
        NavigationView navigationView =  findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }
    @Override
    public boolean onSupportNavigateUp() {
        return true;
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item)
    {
        Intent intent;
        intent=new Intent(this, WebActivity.class);
        String url;
        switch (item.getItemId())
        {
            case R.id.nav_attendence: {
                ProgressDialog pd=new ProgressDialog(this);
                pd.show();
                url="https://erp.cbit.org.in/Login.aspx";
                intent.putExtra("url",url);
                startActivity(intent);
                pd.dismiss();
                break;
            }

            case R.id.open_website:
            {
                ProgressDialog pd=new ProgressDialog(this);
                pd.show();
                url="https://www.cbit.ac.in/";
                intent.putExtra("url",url);
                startActivity(intent);
                pd.dismiss();
                break;
            }

            case R.id.nav_lms:
            {
                ProgressDialog pd=new ProgressDialog(this);
                pd.show();
                url="http://lms.cbit.org.in/login/index.php";
                intent.putExtra("url",url);
                startActivity(intent);
                pd.dismiss();
                break;
            }
            case R.id.logout:
            {
                mAuth.signOut();
                finish();
                startActivity(new Intent(MainActivity.this, Login.class));
                break;
            }
            case R.id.nav_profile:{
                startActivity(new Intent(MainActivity.this, Profile.class));
                break;
            }
            case R.id.nav_notification:{
                startActivity(new Intent(MainActivity.this, Notifications.class));
                break;
            }
        }
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private Toolbar toolbar;
    private DrawerLayout drawerLayout;
    NavigationView navigationView;
    FirebaseAuth mAuth;
    DatabaseReference databaseReference;
    de.hdodenhof.circleimageview.CircleImageView profile_pic;
    TextView profile_name,profile_rollnumber;

    private void updateHeader()
    {
        //View hView=navigationView.inflateHeaderView(R.layout.nav_header_main);
        View hView=navigationView.getHeaderView(0);
        profile_name=hView.findViewById(R.id.profile_name);
        profile_rollnumber=hView.findViewById(R.id.profile_rollnumber);
        profile_pic=hView.findViewById(R.id.profile_pic);
        databaseReference= FirebaseDatabase.getInstance().getReference("users").child(mAuth.getCurrentUser().getDisplayName());
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {
                    String name = dataSnapshot.child("uname").getValue().toString();
                    String rollnumber = dataSnapshot.child("id").getValue().toString();
                    profile_name.setText(name);
                    title.setText("WELCOME " + profile_name.getText().toString().toUpperCase());
                    profile_rollnumber.setText(rollnumber);
                    Glide.with(getApplicationContext()).load(dataSnapshot.child("url").getValue().toString()).into(profile_pic);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_);
        mAuth=FirebaseAuth.getInstance();
        if(mAuth.getCurrentUser().getDisplayName().length()!=12){
            mAuth.signOut();
            startActivity(new Intent(MainActivity.this,Login.class));
            finish();
        }
        else {
            navigationView = findViewById(R.id.nav_view);
            setNavigationViewListener();
            updateHeader();
            FloatingActionButton fab = findViewById(R.id.fab);
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Snackbar.make(view, "Opening Notifications Window", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                    startActivity(new Intent(MainActivity.this, Notifications.class));
                    finish();
                }
            });
            toolbar = findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);
            title = findViewById(R.id.title_toolbar);
            toolbar.getMenu().clear();
            drawerLayout = findViewById(R.id.drawer_layout);
            ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
            drawerLayout.addDrawerListener(toggle);
            toggle.syncState();
            navigationView.setCheckedItem(R.id.nav_home);
        }
    }

}
