package com.ornoiragency.chacks.viewHolders;

import android.content.Context;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.Guideline;
import androidx.recyclerview.widget.RecyclerView;


import com.cometchat.pro.helpers.Logger;
import com.ornoiragency.chacks.R;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.content.Context.WINDOW_SERVICE;

public class LeftFileViewHolder extends RecyclerView.ViewHolder {

    private static final String TAG = "LeftFileViewHolder";
    public TextView fileName;
    public TextView fileType;
    public TextView timeTv,senderName;
    public CircleImageView avatar;


    public LeftFileViewHolder(@NonNull View itemView) {
        super(itemView);

        fileType=itemView.findViewById(R.id.fileType);
        fileName=itemView.findViewById(R.id.fileName);
        timeTv = itemView.findViewById(R.id.timeTv);
        avatar = itemView.findViewById(R.id.imgAvatar);
        senderName = itemView.findViewById(R.id.senderName);

    }
}
