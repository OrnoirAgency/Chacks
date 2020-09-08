package com.ornoiragency.chacks.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import com.google.firebase.auth.FirebaseAuth;
import com.ornoiragency.chacks.Activity.ChatActivity;
import com.ornoiragency.chacks.Activity.MessagesActivity;
import com.ornoiragency.chacks.models.UserObject;
import com.ornoiragency.chacks.R;

import java.util.ArrayList;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {
    Context context;
    private ArrayList<UserObject> userList;
    private boolean selectingMultipleUsers = false;

    private FirebaseAuth mAuth;
    private String senderUSerId;

    public UserAdapter(Context context, ArrayList<UserObject> userList){
        this.userList = userList;
        this.context = context;

    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user, null, false);
        RecyclerView.LayoutParams lp = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutView.setLayoutParams(lp);

        mAuth = FirebaseAuth.getInstance();
        senderUSerId = mAuth.getCurrentUser().getUid();

        ViewHolder rcv = new ViewHolder(layoutView);
        return rcv;
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        holder.mName.setText(userList.get(position).getName());
        holder.mDescription.setText(userList.get(position).getStatus());
        if( context != null ) {
            if (userList.get(holder.getAdapterPosition()).getImage().equals("")) {
                holder.mImage.setBackground(context.getResources().getDrawable(R.drawable.ic_user));
            } else {
                Glide.with(context)
                        .load(userList.get(holder.getLayoutPosition()).getImage())
                        .into(holder.mImage);
            }
        }

        holder.mLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userList.get(position).setSelected(true);
                Intent intent = new Intent(context, MessagesActivity.class);
                intent.putExtra("user_id",userList.get(position).getUid());
                intent.putExtra("senderUserId", senderUSerId);
                context.startActivity(intent);

            }
        });

    }


    @Override
    public int getItemCount() {
        return userList.size();
    }



    class ViewHolder extends RecyclerView.ViewHolder{
        TextView mName, mDescription;
        ImageView mImage;
        LinearLayout mLayout;
        ViewHolder(View view){
            super(view);
            mName = view.findViewById(R.id.name);
            mDescription = view.findViewById(R.id.description);
            mImage = view.findViewById(R.id.image);
            mLayout = view.findViewById(R.id.layout);
        }
    }
}