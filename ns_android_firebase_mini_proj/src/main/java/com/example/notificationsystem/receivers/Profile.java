package com.example.notificationsystem.receivers;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.notificationsystem.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.Calendar;

public class Profile extends AppCompatActivity {
    private static final int ALLOW_CODE = 100;
    static int REQUESTCODE=1;
    private FirebaseAuth mAuth;
    private DatabaseReference databaseReference;
    private EditText name,mobile,email,info;
    private TextView dob;
    private TextView uname;
    private de.hdodenhof.circleimageview.CircleImageView profile_pic;
    private Button edit;
    private DatePickerDialog.OnDateSetListener mDateSetListener;
    private ProgressBar pb;
    private StorageReference storageReference;
    private void load()
    {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                try {
                    mobile.setText(dataSnapshot.child("mobile").getValue().toString());
                    uname.setText(dataSnapshot.child("uname").getValue().toString());
                    name.setText(dataSnapshot.child("uname").getValue().toString());
                    dob.setText(dataSnapshot.child("dob").getValue().toString());
                    email.setText(dataSnapshot.child("uemail").getValue().toString());
                    info.setText(dataSnapshot.child("course").getValue().toString()+"_"+dataSnapshot.child("section").getValue().toString()+"@"+dataSnapshot.child("id").getValue().toString());
                    Glide.with(getApplicationContext()).load(dataSnapshot.child("url").getValue().toString()).into(profile_pic);
                }
                catch (Exception e)
                {
                    Toast.makeText(Profile.this,"Internet Connection required",Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    private void update(String name,String dob,String mobile)
    {
        try {
            databaseReference.child("uname").setValue(name);
            databaseReference.child("dob").setValue(dob);
            databaseReference.child("mobile").setValue(mobile);
            Toast.makeText(Profile.this,"Update Successful",Toast.LENGTH_SHORT).show();
        }
        catch (Exception e)
        {
            Toast.makeText(Profile.this,"Internet Connection required",Toast.LENGTH_SHORT).show();
        }
    }

    private void openGallery()
    {
        Intent galleryIntent=new Intent(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        startActivityForResult(Intent.createChooser(galleryIntent,"Select Picture"),REQUESTCODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode==RESULT_OK && requestCode==REQUESTCODE && data!=null)
        {
            Uri pickedImgUri=data.getData();
            try{
                Bitmap bitmap= MediaStore.Images.Media.getBitmap(getContentResolver(),pickedImgUri);
                profile_pic.setImageBitmap(bitmap);
                String id=mAuth.getCurrentUser().getDisplayName();
                String Class=info.getText().toString().substring(0,3);
                Class+="-"+info.getText().toString().charAt(4);
                storageReference= FirebaseStorage.getInstance().getReference().child("USERS").child(id.substring(0,3)).child(Class).child(id);
                final StorageReference imageFilepath = storageReference.child("profile_pic");
                imageFilepath.putFile(pickedImgUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        imageFilepath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                String url=uri.toString();
                                databaseReference.child("url").setValue(url);
                                Toast.makeText(Profile.this,"Upload Successful",Toast.LENGTH_LONG).show();
                                pb.setVisibility(View.INVISIBLE);
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(Profile.this,"Upload URL Unsuccessful",Toast.LENGTH_LONG).show();
                                pb.setVisibility(View.INVISIBLE);
                            }
                        });
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(Profile.this,"Upload Unsuccessful",Toast.LENGTH_LONG).show();
                        pb.setVisibility(View.INVISIBLE);
                    }
                });
            }
            catch (Exception e){
                Toast.makeText(Profile.this,"No image selected or internet issue"+e.toString(),Toast.LENGTH_SHORT).show();
                pb.setVisibility(View.INVISIBLE);
            }
        }
        else{
            pb.setVisibility(View.INVISIBLE);
        }

    }

    private void setDate(){
        Calendar cal = Calendar.getInstance();
        int year=cal.get(Calendar.YEAR);
        int month=cal.get(Calendar.MONTH);
        int day=cal.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog dialog=new DatePickerDialog(Profile.this,android.R.style.Theme_Holo_Dialog_NoActionBar_MinWidth,mDateSetListener,year,month,day);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();

        mDateSetListener=new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                month+=1;
                dob.setText(dayOfMonth+"/"+month+"/"+year);
            }
        };
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        getSupportActionBar().setTitle("Profile");
        mAuth=FirebaseAuth.getInstance();
        pb=findViewById(R.id.progressbar_profile_page);
        pb.setVisibility(View.INVISIBLE);
        email=findViewById(R.id.showEmail);
        email.setEnabled(false);
        profile_pic=findViewById(R.id.profile_pic1);
        info=findViewById(R.id.showInfo);
        info.setEnabled(false);
        uname=findViewById(R.id.under_name);
        name=findViewById(R.id.showName);
        name.setEnabled(false);
        mobile=findViewById(R.id.showMobile);
        mobile.setEnabled(false);
        dob=findViewById(R.id.showdob);

        dob.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(edit.getText().toString()=="Update"){
                    setDate();
                }
            }
        });

        databaseReference= FirebaseDatabase.getInstance().getReference("users").child(mAuth.getCurrentUser().getDisplayName());
        load();
        edit=findViewById(R.id.update);
        edit.setText("Edit");
        edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(edit.getText().toString()=="Edit") {
                    //Toast.makeText(Profile.this, "UPDATION POSSIBLE FOR ONLY NAME,  MOBILE,  DOB,  PROFILE PIC", Toast.LENGTH_LONG).show();
                    name.setBackgroundColor(Color.parseColor("#FFFEEE"));
                    mobile.setBackgroundColor(Color.parseColor("#FFFEEE"));
                    dob.setBackgroundColor(Color.parseColor("#FFFEEE"));
                    name.setEnabled(true);
                    mobile.setEnabled(true);
                    edit.setText("Update");
                }
                else{
                    String Name=name.getText().toString();
                    String Dob=dob.getText().toString();
                    String Mobile=mobile.getText().toString();
                    pb.setVisibility(View.VISIBLE);
                    update(Name,Dob,Mobile);
                    pb.setVisibility(View.INVISIBLE);
                    name.setEnabled(false);
                    mobile.setEnabled(false);
                    name.setBackgroundColor(Color.parseColor("#FFFFFF"));
                    mobile.setBackgroundColor(Color.parseColor("#FFFFFF"));
                    dob.setBackgroundColor(Color.parseColor("#FFFFFF"));
                    edit.setText("Edit");
                }
            }
        });

        profile_pic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pb.setVisibility(View.VISIBLE);
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
                        String[] permission={Manifest.permission.READ_EXTERNAL_STORAGE};
                        requestPermissions(permission,ALLOW_CODE);
                    }
                    else{
                        openGallery();
                    }
                }
                else
                    openGallery();
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case ALLOW_CODE:{
                if(grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED)
                    openGallery();
                else
                    Toast.makeText(Profile.this,"Permission Denied",Toast.LENGTH_SHORT);
            }

        }
    }
}
