package com.example.notificationsystem.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.notificationsystem.R;
import com.example.notificationsystem.receivers.ShowNotification;
import com.example.notificationsystem.templates.NotificationTemplate;


import java.util.ArrayList;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class NotificationsAdapter extends RecyclerView.Adapter<NotificationsAdapter.ViewHolder> {
    private Context mContext;
    private ArrayList<NotificationTemplate> notifications;
    private NotificationTemplate notificationTemplate;
    private HashMap<String,String> hashMap=new HashMap<>();

    public NotificationsAdapter(Context mContext, ArrayList<NotificationTemplate> notifications) {
        this.mContext = mContext;
        this.notifications = notifications;
        hashMap.put("MEC","MECHANICAL");
        hashMap.put("CHEM","CHEMICAL");
        hashMap.put("PROD","PRODUCTION");
        hashMap.put("CIV","CIVIL");
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(mContext).inflate(R.layout.notification_item,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        notificationTemplate=notifications.get(position);
        holder.stamp.setImageResource(R.drawable.light_blue);
        String department=notificationTemplate.getSenderDept();
        if(hashMap.containsKey(department))
            holder.dept.setText("DEPARTMENT OF "+hashMap.get(department));
        else
            holder.dept.setText("DEPARTMENT OF "+department);
        holder.subject.setText(notificationTemplate.getSubject());
        holder.level.setText("Teacher");
        holder.senderName.setText(notificationTemplate.getSender());
        String url=notificationTemplate.getSenderImg();
        if(url==null || url.equals("default"))
            holder.senderImage.setImageResource(R.drawable.imh);
        else
            Glide.with(mContext).load(url).into(holder.senderImage);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position=holder.getAdapterPosition();
                Intent intent=new Intent(mContext, ShowNotification.class);
                intent.putExtra("Subject",notifications.get(position).getSubject());
                intent.putExtra("Sender Name",notifications.get(position).getSender());
                intent.putExtra("Content",notifications.get(position).getContent());
                intent.putExtra("Sender Pic",notifications.get(position).getSenderImg());
                intent.putStringArrayListExtra("Image Urls", (ArrayList<String>) notifications.get(position).getImageURLS());
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                mContext.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return notifications.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder{
        private TextView dept,senderName,subject,level;
        private CircleImageView senderImage;
        private ImageView stamp;
        public ViewHolder(View itemView){
            super(itemView);
            dept=itemView.findViewById(R.id.department);
            senderName=itemView.findViewById(R.id.name);
            subject=itemView.findViewById(R.id.subject);
            level=itemView.findViewById(R.id.faculty);
            senderImage=itemView.findViewById(R.id.profile_pic);
            stamp=itemView.findViewById(R.id.stamp);
        }
    }
}
