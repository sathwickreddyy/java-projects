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
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.example.notificationsystem.R;
import com.example.notificationsystem.templates.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.Calendar;

public class Registration extends AppCompatActivity
{
    private static final int ALLOW_CODE = 100;
    private ToggleButton login;
    private ImageButton regBtn;
    private ImageView upload_pic;
    private Spinner course;
    static int PReqCode = 1,REQUESTCODE=1;
    private Uri pickedImgUri=null;
    private EditText name,email,id,pass;
    private TextView DOB;
    private ProgressBar loadingProgress;
    private FirebaseAuth mAuth;
    private DatabaseReference databaseReference,userref;
    private FirebaseDatabase rootNode;
    private RadioGroup rg;
    private DatePickerDialog.OnDateSetListener mDateSetListener;

    private void updateUserInfo(final FirebaseUser currentUser,final String email, final String name,final String id,String dob,Uri pickedImgUri,final String course,final String section)
    {
        try
        {
            rootNode=FirebaseDatabase.getInstance();
            databaseReference= rootNode.getReference("users");
            userref=rootNode.getReference("Course").child(course).child(section);
            String batch = id.substring(0,2);
            userref.child(batch).child(currentUser.getUid()).setValue(id);
            User user=new User(name,email,id,dob,course,section);
            databaseReference.child(id).setValue(user);
            databaseReference.child(id).child("mobile").setValue("0000000000");
            databaseReference.child(id).child("uid").setValue(currentUser.getUid());
            StorageReference mStorageRef = FirebaseStorage.getInstance().getReference()
                    .child("USERS")
                    .child(id.substring(0,3))
                    .child(course+"-"+section)
                    .child(id);
            final StorageReference imageFilePath = mStorageRef.child("profile_pic");
            imageFilePath.putFile(pickedImgUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>()
                        {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                Task<Uri> downloadUrl=imageFilePath.getDownloadUrl();
                                downloadUrl.addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(final Uri uri) {
                                        final String imageref=uri.toString();
                                        databaseReference.child(id).child("url").setValue(imageref);
                                        currentUser.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if(task.isSuccessful())
                                                {
                                                    Toast.makeText(Registration.this,"Registered Successfully please verify your email",Toast.LENGTH_LONG).show();
                                                    UserProfileChangeRequest profileUpdates=new UserProfileChangeRequest.Builder()
                                                            .setDisplayName(id)
                                                            .setPhotoUri(uri)
                                                            .build();
                                                    currentUser.updateProfile(profileUpdates).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            if(task.isSuccessful()){
                                                                Toast.makeText(Registration.this,"Profile Update Completed",Toast.LENGTH_LONG).show();
                                                                loadingProgress.setVisibility(View.INVISIBLE);
                                                                regBtn.setVisibility(View.VISIBLE);
                                                                startActivity(new Intent(Registration.this, Login.class));
                                                                finish();
                                                            }
                                                            else{
                                                                Toast.makeText(Registration.this,"Profile not Updated "+task.getException().getMessage(),Toast.LENGTH_LONG).show();
                                                                loadingProgress.setVisibility(View.INVISIBLE);
                                                                regBtn.setVisibility(View.VISIBLE);
                                                            }
                                                        }
                                                    });
                                                }
                                                else
                                                {
                                                    Toast.makeText(Registration.this,""+task.getException().getMessage(),Toast.LENGTH_LONG).show();
                                                }
                                            }
                                        });
                                    }
                                });
                            }
                        })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(Registration.this,"Profile pic upload unsuccessful",Toast.LENGTH_SHORT).show();
                            loadingProgress.setVisibility(View.INVISIBLE);
                            regBtn.setVisibility(View.VISIBLE);
                        }
                    });


        }
        catch (Exception e){
            Toast.makeText(Registration.this, "Make sure u have Internet connection"+e.toString(), Toast.LENGTH_LONG).show();
        }
    }

    private void createUserAccount(final String name,final String email, String pass,  final String id, final String dob,final String course,final String section)
    {
        try {
            mAuth.createUserWithEmailAndPassword(email, pass)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>()
                    {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task)
                        {
                            if (task.isSuccessful())
                            {
                                updateUserInfo(mAuth.getCurrentUser(),email,name,id,dob,pickedImgUri,course,section);
                            }
                            else {
                                if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                                    Toast.makeText(Registration.this,
                                            "User with this email already exist.", Toast.LENGTH_SHORT).show();

                                }
                                else {
                                    Toast.makeText(Registration.this, "Authentication failed", Toast.LENGTH_SHORT).show();
                                }
                                loadingProgress.setVisibility(View.INVISIBLE);
                                regBtn.setVisibility(View.VISIBLE);
                            }
                        }
                    });

        }
        catch(Exception e){
            Toast.makeText(Registration.this, ""+e.toString(), Toast.LENGTH_LONG).show();
        }

    }

    private void openGallery() {
        Intent galleryIntent=new Intent(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        startActivityForResult(Intent.createChooser(galleryIntent,"Select Picture"),REQUESTCODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode==RESULT_OK && requestCode==REQUESTCODE && data!=null)
        {
            pickedImgUri=data.getData();
            try{
                Bitmap bitmap= MediaStore.Images.Media.getBitmap(getContentResolver(),pickedImgUri);
                upload_pic.setImageBitmap(bitmap);
            }
            catch(Exception e){
                e.printStackTrace();
        }

        }
    }
    private void setDate()
    {
        Calendar cal = Calendar.getInstance();
        int year=cal.get(Calendar.YEAR);
        int month=cal.get(Calendar.MONTH);
        int day=cal.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog dialog=new DatePickerDialog(Registration.this,android.R.style.Theme_Holo_Dialog_NoActionBar_MinWidth,mDateSetListener,year,month,day);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();

        mDateSetListener=new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                month+=1;
                DOB.setText(dayOfMonth+"/"+month+"/"+year);
            }
        };
    }


    private void Init(){
        mAuth=FirebaseAuth.getInstance();
        upload_pic=findViewById(R.id.upload_pic);
        loadingProgress=findViewById(R.id.regprogress);
        loadingProgress.setVisibility(View.INVISIBLE);
        name=findViewById(R.id.uname);
        course=findViewById(R.id.course);
        email=findViewById(R.id.uemail);
        pass=findViewById(R.id.passwd);
        DOB=findViewById(R.id.DOB);
        DOB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setDate();
            }
        });
        id=findViewById(R.id.identity);
        regBtn=findViewById(R.id.regbtn);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case ALLOW_CODE:{
                if(grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED)
                    openGallery();
                else
                    Toast.makeText(Registration.this,"Permission Denied",Toast.LENGTH_SHORT);
            }

        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);
        Init();
        upload_pic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
        regBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try
                {
                    regBtn.setVisibility(View.INVISIBLE);
                    loadingProgress.setVisibility(View.VISIBLE);
                    rg=findViewById(R.id.section);
                    int radioid=rg.getCheckedRadioButtonId();
                    RadioButton rb=findViewById(radioid);
                    final String section=rb.getText().toString();
                    final String corse=String.valueOf(course.getSelectedItem());
                    final String Name = name.getText().toString();
                    final String Email = email.getText().toString();
                    final String Password = pass.getText().toString();
                    final String dob = DOB.getText().toString();
                    final String ID = id.getText().toString();
                    if(ID.length()!=12){
                        Toast.makeText(Registration.this, "Invalid Roll Number Length must be 12", Toast.LENGTH_LONG).show();
                        regBtn.setVisibility(View.VISIBLE);
                        loadingProgress.setVisibility(View.INVISIBLE);
                    }
                    else if (Name.isEmpty() || Email.isEmpty() || Password.isEmpty() || dob.isEmpty() || ID.isEmpty() || pickedImgUri==null) {//gender.isEmpty() ||
                        regBtn.setVisibility(View.VISIBLE);
                        loadingProgress.setVisibility(View.INVISIBLE);
                        Toast.makeText(Registration.this, "Please fill the details to register and click on image button to upload image", Toast.LENGTH_LONG).show();
                    }
                    else
                        {
                            DatabaseReference df=FirebaseDatabase.getInstance().getReference("users");
                            df.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if(dataSnapshot.hasChild(ID)){
                                        String email=dataSnapshot.child(ID).child("uemail").getValue().toString();
                                        Toast.makeText(Registration.this,"Roll Number already exists linked to "+email.substring(0,4)+"....."+email.substring(email.length()-4),Toast.LENGTH_LONG).show();
                                        regBtn.setVisibility(View.VISIBLE);
                                        loadingProgress.setVisibility(View.INVISIBLE);
                                    }
                                    else{
                                        if(new Validate().isValidEmail(Email)) {
                                            createUserAccount(Name, Email, Password, ID, dob, corse, section);
                                        }
                                        else{
                                            Toast.makeText(Registration.this,"Email Validation Error: please enter valid email address",Toast.LENGTH_LONG).show();
                                        }
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });
                    }
                }
                catch (Exception e){
                    Toast.makeText(Registration.this,""+e.toString(),Toast.LENGTH_SHORT).show();
                }
            }
        });
        login=findViewById(R.id.havelogin);
        login.setTextColor(Color.parseColor("#000000"));
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login.setTextColor(Color.parseColor("#808080"));
                finish();
                startActivity(new Intent(Registration.this,Login.class));
            }
        });

    }
}
