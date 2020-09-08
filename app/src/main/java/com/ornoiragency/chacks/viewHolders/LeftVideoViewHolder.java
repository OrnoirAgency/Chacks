package com.ornoiragency.chacks.viewHolders;

import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.ornoiragency.chacks.R;

import me.alvince.android.avatarimageview.AvatarImageView;

public class LeftVideoViewHolder extends RecyclerView.ViewHolder{
    ConstraintLayout messageLayout;
    public AvatarImageView messageVideo;
    public TextView timeTv,isSeenTv;
    public ImageButton btnPlayVideo;

    public LeftVideoViewHolder( View rightVideoMessageView) {
        super(rightVideoMessageView);
        messageLayout = itemView.findViewById(R.id.messageLayout);
        messageVideo = rightVideoMessageView.findViewById(R.id.videoMessage);
        timeTv = itemView.findViewById(R.id.timeTv);
        isSeenTv = itemView.findViewById(R.id.isSeenTv);
        btnPlayVideo = itemView.findViewById(R.id.btnPlayVideo);
    }
}
