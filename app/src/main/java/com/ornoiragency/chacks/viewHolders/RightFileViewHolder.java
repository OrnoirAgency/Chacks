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

public class RightFileViewHolder extends RecyclerView.ViewHolder {

    private static final String TAG = "RightFileViewHolder";
    public TextView fileType,fileName,isSeenTv;

    public TextView timeTv,senderName;
    public CircleImageView avatar;


    public RightFileViewHolder(@NonNull View itemView) {
        super(itemView);

        isSeenTv=itemView.findViewById(R.id.isSeenTv);
        timeTv = itemView.findViewById(R.id.timeTv);
        fileName=itemView.findViewById(R.id.fileName);
        fileType=itemView.findViewById(R.id.fileType);


    }
}
