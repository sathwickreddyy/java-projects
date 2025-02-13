package com.example.notificationsystem.adapter;

import android.content.Context;
import android.content.Intent;
import android.media.Image;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.Target;
import com.example.notificationsystem.R;

import java.util.ArrayList;

public class ShowViewAdapter extends RecyclerView.Adapter<ShowViewAdapter.ViewHolder> {
    ArrayList<String> imageUris;
    Context mContext;

    public ShowViewAdapter(Context context,ArrayList<String> imageUris){
        this.mContext=context;
        this.imageUris=imageUris;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(mContext).inflate(R.layout.receivers_attachment_item,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String url=imageUris.get(position);
        if(!url.isEmpty()){
            holder.pb.setVisibility(View.VISIBLE);
            Glide.with(mContext)
                    .load(url)
                    .override(Target.SIZE_ORIGINAL)
                    .into(holder.imageView);
            holder.pb.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        return imageUris.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder{
        ImageView imageView;
        ProgressBar pb;
        public ViewHolder(View viewItem){
            super(viewItem);
            imageView=viewItem.findViewById(R.id.imageView);
            pb=viewItem.findViewById(R.id.progress);
            pb.setVisibility(View.INVISIBLE);
        }
    }
}
