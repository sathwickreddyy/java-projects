package com.example.notificationsystem.senders;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Canvas;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.notificationsystem.R;
import com.example.notificationsystem.adapter.UploadViewAdapter;
import com.example.notificationsystem.receivers.Login;
import com.example.notificationsystem.templates.NotificationTemplate;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator;

public class SendNotification extends AppCompatActivity {
    private static final int REQUEST_CODE = 1000;
    private static final int RUN_CODE = 1001;
    private static final int REQUEST_CODE_ALL = 1002;
    private static final int RESULT_LOAD_IMAGE = 1003;
    private Button show;
    private EditText subject,content;
    private TextView showText;
    private ProgressBar progressBar;
    private ArrayList<Integer> students;
    private Spinner course,section,batch;
    private DatabaseReference databaseReferenceOfStudents;
    private ArrayList<String> usersList;
    private ImageView open_camera,attachment,send;
    private Uri image_uri;
    private RecyclerView uploaded_list_view;
    private ArrayList<Uri> imageUris;
    private ArrayList<String> fileNameList,fileDoneList,imageURLS;
    private UploadViewAdapter uploadViewAdapter;
    private StorageReference storageReference;
    private DatabaseReference databaseReference;
    private ProgressDialog progressDialog;
    private FirebaseAuth mAuth;
    private String myName;
    private String imageUrl,senderDept;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_notification);
        Init();
        getUsers();
        setCameraSettings();
        setMultiImagesSettings();
        setNotificationSettings();
        FirebaseDatabase.getInstance().getReference("Senders").child(mAuth.getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                myName=dataSnapshot.child("name").getValue().toString();
                imageUrl=dataSnapshot.child("url").getValue().toString();
                senderDept=dataSnapshot.child("dept").getValue().toString();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void setNotificationSettings(){
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String Subject=subject.getText().toString();
                final String Content=content.getText().toString();
                final String sender=myName;
                final String senderImg=imageUrl;


                if(usersList.size()==0){
                    Toast.makeText(SendNotification.this,"Please Select Students to send Notification",Toast.LENGTH_LONG).show();
                    progressBar.setVisibility(View.INVISIBLE);
                }
                else if(Subject.isEmpty()){
                    Toast.makeText(SendNotification.this,"OOPs! you forgotten to add Subject",Toast.LENGTH_LONG).show();
                    progressBar.setVisibility(View.INVISIBLE);
                }
                else{
                    if(imageUris.size()!=0){
                        int year = Calendar.getInstance().get(Calendar.YEAR);
                        int month=Calendar.getInstance().get(Calendar.MONTH);
                        int day=Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
                        progressDialog=new ProgressDialog(SendNotification.this);
                        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                        progressDialog.setTitle("Uploading Images");
                        progressDialog.setProgress(0);
                        progressDialog.show();
                        progressBar.setVisibility(View.VISIBLE);
                        for(int i=0;i<imageUris.size();i++)
                        {
                            Uri fileUri=imageUris.get(i);

                            //UPDATING RECYCLER VIEW
                            fileDoneList.add(i,"fileUploading");
                            uploadViewAdapter.notifyDataSetChanged();

                            //UPLOADING IN FIREBASE
                            final StorageReference fileToUpload=storageReference.child("NOTIFICATION_ATTACHMENTS").child(""+year).child(""+month).child(""+day).child(fileNameList.get(i));
                            final int finalI = i;
                            fileToUpload.putFile(fileUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                    fileToUpload.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                        @Override
                                        public void onSuccess(Uri uri) {
                                            imageURLS.add(uri.toString());
                                            fileDoneList.remove(finalI);
                                            fileDoneList.add(finalI,"Done");
                                            uploadViewAdapter.notifyDataSetChanged();
                                            //After last image uploaded send notification to user regarding this
                                            if(finalI==(imageUris.size()-1)){
                                                for(int i=0;i<usersList.size();i++){
                                                    NotificationTemplate notificationTemplate =new NotificationTemplate(sender,usersList.get(i),Subject,Content,senderDept,senderImg,imageURLS);
                                                    sendNotification(notificationTemplate,i);
                                                }
                                            }
                                        }
                                    });
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(SendNotification.this,"Unable to upload at the moment please make sure you have internet Connection",Toast.LENGTH_LONG).show();
                                    progressBar.setVisibility(View.INVISIBLE);
                                }
                            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                                    int currentProgress= (int) (100*(taskSnapshot.getBytesTransferred()/taskSnapshot.getTotalByteCount()));
                                    progressDialog.setProgress(currentProgress);
                                    if(progressDialog.getProgress()==100){
                                        progressDialog.dismiss();
                                    }
                                }
                            });
                        }
                    }
                    else{
                        for(int i=0;i<usersList.size();i++){
                            NotificationTemplate notificationTemplate =new NotificationTemplate(sender,usersList.get(i),Subject,Content,senderDept,senderImg);
                            sendNotification(notificationTemplate,i);
                        }
                    }
                }
            }
        });
    }

    private void sendNotification(NotificationTemplate notificationTemplate,int i){
        String key=databaseReference.push().getKey();
        databaseReference.child(key).setValue(notificationTemplate);
        if(i==usersList.size()-1){
            Toast.makeText(SendNotification.this,"Successfully Sent",Toast.LENGTH_LONG).show();
            progressBar.setVisibility(View.INVISIBLE);
        }
    }

    private void Init(){
        mAuth=FirebaseAuth.getInstance();
        imageURLS=new ArrayList<>();
        students=new ArrayList<>();
        usersList=new ArrayList<>();
        storageReference= FirebaseStorage.getInstance().getReference();
        databaseReference=FirebaseDatabase.getInstance().getReference("NOTIFICATIONS");
        send=findViewById(R.id.send);
        imageUris=new ArrayList<>();
        fileDoneList=new ArrayList<>();
        fileNameList=new ArrayList<>();

        //recycler view
        uploaded_list_view=findViewById(R.id.recyclerView);
        LinearLayoutManager linearLayoutManager=new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        uploaded_list_view.setLayoutManager(linearLayoutManager);
        uploaded_list_view.setHasFixedSize(true);
        new ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(uploaded_list_view);

        subject=findViewById(R.id.subject);
        content=findViewById(R.id.content);
        attachment=findViewById(R.id.attachment);
        progressBar=findViewById(R.id.progressbar);
        progressBar.setVisibility(View.INVISIBLE);
        show=findViewById(R.id.select);
        showText=findViewById(R.id.show);
        batch=findViewById(R.id.batch);
        course=findViewById(R.id.course);
        section=findViewById(R.id.section);
        databaseReferenceOfStudents=FirebaseDatabase.getInstance().getReference("Course");
        open_camera=findViewById(R.id.open_camera);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.CAMERA)
                    == PackageManager.PERMISSION_DENIED || checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_DENIED || checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_DENIED) {
                String[] permission = {
                        Manifest.permission.CAMERA,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE
                };
                requestPermissions(permission, REQUEST_CODE_ALL);
            }
        }


        String[] arr;
        arr=getResources().getStringArray(R.array.Batch);
        ArrayAdapter<String> spinnerArrayAdapter;
        spinnerArrayAdapter= new ArrayAdapter<>(SendNotification.this,R.layout.spinner_item,arr);
        batch.setAdapter(spinnerArrayAdapter);
        arr=getResources().getStringArray(R.array.Course);
        spinnerArrayAdapter= new ArrayAdapter<>(SendNotification.this,R.layout.spinner_item,arr);
        course.setAdapter(spinnerArrayAdapter);
        arr=getResources().getStringArray(R.array.Section);
        spinnerArrayAdapter= new ArrayAdapter<>(SendNotification.this,R.layout.spinner_item,arr);
        section.setAdapter(spinnerArrayAdapter);
    }

    private void getUsers(){
        show.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String s=String.valueOf(batch.getSelectedItem());
                if (s.equals("Batch")) {
                    Toast.makeText(SendNotification.this, "Invalid Batch! Note:- Select Batch To Display Students", Toast.LENGTH_SHORT).show();
                } else {
                    usersList.clear();
                    progressBar.setVisibility(View.VISIBLE);
                    final  String Course = (String.valueOf(course.getSelectedItem())).trim();
                    final String Section = (String.valueOf(section.getSelectedItem())).trim();
                    final String batch= ""+(Integer.parseInt(s.substring(0,2))-1);
                    //databaseReferenceOfStudents.child(Course);.child(Section);
                    databaseReferenceOfStudents.child(Course).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if(dataSnapshot.exists() && dataSnapshot.hasChild(Section)) {
                                DataSnapshot dataSnapshot1=dataSnapshot.child(Section);
                                if (dataSnapshot1.exists() && dataSnapshot1.hasChild(batch)) {
                                    ArrayList<String> userIds = new ArrayList<>();
                                    String listItems[];

                                    for (DataSnapshot ds : dataSnapshot1.child(batch).getChildren()) {
                                        userIds.add(ds.getValue().toString());
                                    }
                                    Collections.sort(userIds);
                                    listItems = new String[userIds.size()];
                                    for (int i = 0; i < userIds.size(); i++) {
                                        listItems[i] = userIds.get(i);
                                    }
                                    boolean checkedItems[] = new boolean[userIds.size()];
                                    progressBar.setVisibility(View.INVISIBLE);
                                    openDialog(listItems, checkedItems,userIds);
                                }
                                else {
                                    Toast.makeText(SendNotification.this, "No Such Batch Exists", Toast.LENGTH_SHORT).show();
                                    progressBar.setVisibility(View.INVISIBLE);
                                }
                            }
                            else {
                                Toast.makeText(SendNotification.this, "No Such Batch Exists", Toast.LENGTH_SHORT).show();
                                progressBar.setVisibility(View.INVISIBLE);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }
            }
        });
    }

    private void setCameraSettings() {
        open_camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (checkSelfPermission(Manifest.permission.CAMERA)
                            == PackageManager.PERMISSION_DENIED || checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                            == PackageManager.PERMISSION_DENIED) {
                        String[] permission = {
                                Manifest.permission.CAMERA,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE
                        };
                        requestPermissions(permission, REQUEST_CODE);
                    }
                    else{
                        openCamera();
                    }
                }
                else{
                    openCamera();
                }
            }
        });
    }

    private void setMultiImagesSettings(){
        attachment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent();
                intent.setType("image/*");
                intent.putExtra(intent.EXTRA_ALLOW_MULTIPLE,true);
                intent.setAction(intent.ACTION_GET_CONTENT);
                startActivityForResult(intent.createChooser(intent,"Select Pictures"),RESULT_LOAD_IMAGE);
            }
        });
    }


    ItemTouchHelper.SimpleCallback itemTouchHelperCallback = new ItemTouchHelper.SimpleCallback(0,ItemTouchHelper.RIGHT|ItemTouchHelper.LEFT) {
        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            return false;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
            imageUris.remove(viewHolder.getAdapterPosition());
            fileNameList.remove(viewHolder.getAdapterPosition());
            uploadViewAdapter.notifyDataSetChanged();
        }

        @Override
        public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {

            new RecyclerViewSwipeDecorator.Builder(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
                    .addBackgroundColor(ContextCompat.getColor(SendNotification.this, R.color.red))
                    .addActionIcon(R.drawable.ic_delete_forever_black_24dp)
                    .create()
                    .decorate();

            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);

        }
    };


    private void displaySelectedUsers(List<String> usersList){
        String s="Selected Students are ";
        for(int i=0;i<usersList.size();i++){
            if(i==usersList.size()-1)
                s+=usersList.get(i)+".";
            else
                s+=usersList.get(i)+", ";
        }
        if(s.isEmpty())
            showText.setText("No one");
        else
            showText.setText(s);
    }



    private void openDialog(final String[] listItems,final boolean[] checkedItems,final List<String> proceedList)
    {
        try {
            AlertDialog.Builder alert = new AlertDialog.Builder(SendNotification.this);
            alert.setTitle("Select Students");
            alert.setMultiChoiceItems(listItems, checkedItems, new DialogInterface.OnMultiChoiceClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int position, boolean isChecked) {
                    if (isChecked) {
                        if (!students.contains(position)) {
                            students.add(position);
                        } else {
                            students.remove(position);
                        }
                    }
                }
            });
            alert.setCancelable(false);
            alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    String s = "";
                    for (int i = 0; i < students.size(); i++) {
                        s += listItems[students.get(i)] + " ";
                        usersList.add(listItems[students.get(i)]);
                    }
                    displaySelectedUsers(usersList);
                }
            });
            alert.setNegativeButton("Dismiss", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            alert.setNeutralButton("Select all", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    usersList = (ArrayList<String>) proceedList;
                    displaySelectedUsers(proceedList);
                }
            });
            AlertDialog dialog = alert.create();
            dialog.show();
        }
        catch (Exception e){
            Toast.makeText(SendNotification.this,"Please give us a moment",Toast.LENGTH_LONG).show();
        }
    }


    private void openCamera(){
        ContentValues contentValues=new ContentValues();
        contentValues.put(MediaStore.Images.Media.TITLE,"NEW PICTURE");
        contentValues.put(MediaStore.Images.Media.DESCRIPTION,"From the camera");
        image_uri=getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,contentValues);
        Intent camera_intent=new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        camera_intent.putExtra(MediaStore.EXTRA_OUTPUT,image_uri);
        startActivityForResult(camera_intent,RUN_CODE);
    }

    private String getFileName(Uri uri){
        String result=null;

        if(uri.getScheme().equals("content")){
            Cursor cursor=getContentResolver().query(uri,null,null,null,null);
            try{
                if(cursor!=null && cursor.moveToFirst()){
                    result=cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            }
            finally {
                cursor.close();
            }
        }

        if(result==null){
            result=uri.getPath();
            int cut=result.lastIndexOf("/");
            if(cut!=-1){
                result=result.substring(cut+1);
            }
        }

        return result;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode==RESULT_OK) {
            switch (requestCode){
                case RUN_CODE:{
                    Uri uri=image_uri;
                    if(!imageUris.contains(uri)){
                        imageUris.add(uri);
                        fileNameList.add(getFileName(uri));
                    }
                    uploadViewAdapter=new UploadViewAdapter(SendNotification.this,fileNameList,fileDoneList,imageUris);
                    uploaded_list_view.setAdapter(uploadViewAdapter);
                    break;
                }
                case RESULT_LOAD_IMAGE:{
                    ClipData clipData=data.getClipData();
                    if(clipData!=null){
                        for(int i=0;i<clipData.getItemCount();i++){
                            Uri uri=clipData.getItemAt(i).getUri();
                            if(!imageUris.contains(uri)){
                                imageUris.add(uri);
                                fileNameList.add(getFileName(uri));
                            }
                        }
                    }
                    else if(data.getData()!=null){
                        Uri uri=data.getData();
                        if(!imageUris.contains(uri)){
                            imageUris.add(uri);
                            fileNameList.add(getFileName(uri));
                        }
                    }
                    uploadViewAdapter=new UploadViewAdapter(SendNotification.this,fileNameList,fileDoneList,imageUris);
                    uploaded_list_view.setAdapter(uploadViewAdapter);
                    break;
                }
            }
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode){
            case REQUEST_CODE:{
                if(grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED)
                    openCamera();
                else
                    Toast.makeText(SendNotification.this,"Permission Denied",Toast.LENGTH_SHORT).show();
            }
        }
    }
}
