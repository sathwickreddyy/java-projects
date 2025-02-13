package com.example.notificationsystem.adapter;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.media.Image;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.notificationsystem.R;

import java.util.ArrayList;


public class UploadViewAdapter extends RecyclerView.Adapter<UploadViewAdapter.ViewHolder> {

    private Context mContext;
    private ArrayList<Uri> imageUris;
    private ArrayList<String> fileNameList,fileDoneList;

    public UploadViewAdapter(Context context, ArrayList<String> fileNameList,ArrayList<String> fileDoneList,ArrayList<Uri> imageUris){
        this.mContext=context;
        this.fileDoneList=fileDoneList;
        this.fileNameList=fileNameList;
        this.imageUris=imageUris;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(mContext).inflate(R.layout.attachment_item,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder,final int position) {
        String fileName=fileNameList.get(position);
        holder.filename.setText(fileName);
        holder.filename.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                holder.filename.setTextColor(Color.parseColor("#000000"));
                AlertDialog.Builder alertDialog=new AlertDialog.Builder(mContext);
                alertDialog.setTitle(holder.filename.getText().toString());
                ImageView imageView=new ImageView(mContext);
                imageView.setImageURI(imageUris.get(position));
                alertDialog.setNegativeButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        holder.filename.setTextColor(Color.parseColor("#F56A6A"));
                        dialog.dismiss();
                    }
                });
                alertDialog.setView(imageView);
                AlertDialog dialog=alertDialog.create();
                dialog.show();
            }
        });
        holder.cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(mContext,"Swipe To Delete",Toast.LENGTH_SHORT).show();
            }
        });
        if(fileDoneList.size()!=0) {
            String fileDone = fileDoneList.get(position);

            if (fileDone.equals("fileUploading")) {
                holder.progress.setImageResource(R.mipmap.progress);
            } else {
                holder.progress.setImageResource(R.mipmap.checked);
            }
        }
    }

    @Override
    public int getItemCount() {
        return fileNameList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder{

        private ImageView progress,cancel;
        private TextView filename;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            progress=itemView.findViewById(R.id.progress);
            filename=itemView.findViewById(R.id.file_name);
            cancel=itemView.findViewById(R.id.cancel);
        }

    }
}
