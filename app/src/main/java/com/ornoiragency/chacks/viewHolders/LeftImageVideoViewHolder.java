package com.ornoiragency.chacks.viewHolders;

import android.content.Context;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.Guideline;
import androidx.recyclerview.widget.RecyclerView;


import com.ornoiragency.chacks.R;

import me.alvince.android.avatarimageview.AvatarImageView;

import static android.content.Context.WINDOW_SERVICE;

public class LeftImageVideoViewHolder extends RecyclerView.ViewHolder{
    ConstraintLayout messageLayout;
    public AvatarImageView messageImage;
    public TextView timeTv,isSeenTv;
    public LeftImageVideoViewHolder(View leftImageMessageView) {
        super(leftImageMessageView);
        messageLayout = itemView.findViewById(R.id.messageLayout);
        messageImage = leftImageMessageView.findViewById(R.id.messageImage);
        timeTv = itemView.findViewById(R.id.timeTv);
        isSeenTv = itemView.findViewById(R.id.isSeenTv);
    }
}