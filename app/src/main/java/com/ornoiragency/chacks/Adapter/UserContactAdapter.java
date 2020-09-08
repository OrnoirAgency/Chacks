package com.ornoiragency.chacks.Adapter;

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

import com.google.firebase.auth.FirebaseAuth;
import com.ornoiragency.chacks.Activity.ChatActivity;
import com.ornoiragency.chacks.Activity.MessagesActivity;
import com.ornoiragency.chacks.R;
import com.ornoiragency.chacks.models.User;

import java.util.List;

public class UserContactAdapter extends RecyclerView.Adapter<UserContactAdapter.ViewHolder> {

    private Context mContext;
    private List<User> mUsers;
    private boolean ischat;

    String theLastMessage;
    private FirebaseAuth mAuth;
    private String senderUSerId;

    public UserContactAdapter(Context mContext, List<User> mUsers, boolean ischat){
        this.mUsers = mUsers;
        this.mContext = mContext;
        this.ischat = ischat;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_user, parent, false);

        mAuth = FirebaseAuth.getInstance();
        senderUSerId = mAuth.getCurrentUser().getUid();

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        final User user = mUsers.get(position);
        holder.username.setText(user.getName());
        holder.mDescription.setText(user.getStatus());
        if (user.getImage().equals("default")){
            holder.profile_image.setImageResource(R.mipmap.ic_launcher);
        } else {
            Glide.with(mContext).load(user.getImage())
                    .into(holder.profile_image);
        }


        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, MessagesActivity.class);
                intent.putExtra("user_id",mUsers.get(position).getUid());
              //  intent.putExtra("senderUserId", senderUSerId);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                mContext.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mUsers.size();
    }

    public  class ViewHolder extends RecyclerView.ViewHolder{

        public TextView username,mDescription;
        public ImageView profile_image;


        public ViewHolder(View itemView) {
            super(itemView);

            username = itemView.findViewById(R.id.name);
            profile_image = itemView.findViewById(R.id.image);
            mDescription = itemView.findViewById(R.id.description);

        }
    }


}
