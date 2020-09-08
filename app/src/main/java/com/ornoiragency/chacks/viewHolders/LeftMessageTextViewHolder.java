package com.ornoiragency.chacks.viewHolders;

import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.ornoiragency.chacks.R;
import com.vanniktech.emoji.EmojiTextView;

public class LeftMessageTextViewHolder extends RecyclerView.ViewHolder{

    ConstraintLayout messageLayout;

    public TextView timeTv,isSeenTv;
    public EmojiTextView messageTv;

    public LeftMessageTextViewHolder(@NonNull View itemView) {
        super(itemView);

        messageLayout = itemView.findViewById(R.id.messageLayout);
        messageTv = itemView.findViewById(R.id.messageTv);
        timeTv = itemView.findViewById(R.id.timeTv);
        isSeenTv = itemView.findViewById(R.id.isSeenTv);
    }

}
