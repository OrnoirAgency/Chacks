package com.ornoiragency.chacks.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.ornoiragency.chacks.Activity.AllUserProfileActivity;
import com.ornoiragency.chacks.R;
import com.ornoiragency.chacks.models.User;


import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserListAdapter extends RecyclerView.Adapter<UserListAdapter.UserListViewHolder> {

    Context context;
    private List<User> userList;

    public UserListAdapter(Context context, List<User> userList){
        this.userList = userList;
        this.context = context;

    }

    @NonNull
    @Override
    public UserListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_user, parent, false);

        return new UserListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final UserListViewHolder holder, final int position) {
        holder.setIsRecyclable(false);
        holder.mName.setText(userList.get(position).getName());
        holder.mDescription.setText(userList.get(position).getStatus());
        if( context != null ) {
            if (userList.get(holder.getAdapterPosition()).getImage().equals("")) {
                holder.mImage.setBackground(context.getResources().getDrawable(R.drawable.ic_user));
            } else {

                Glide.with(context)
                        .asBitmap()
                        .load(userList.get(position).getImage())
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(holder.mImage);

            }
        }

        holder.mLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(context, AllUserProfileActivity.class);
                intent.putExtra("user_id",userList.get(position).getUid());
                context.startActivity(intent);
            }
        });



    }


    @Override
    public int getItemCount() {
        return userList.size();
    }



    class UserListViewHolder extends RecyclerView.ViewHolder{
        TextView mName, mDescription;
        CircleImageView mImage;
        ConstraintLayout mLayout;
        UserListViewHolder(View view){
            super(view);
            mName = view.findViewById(R.id.name);
            mDescription = view.findViewById(R.id.description);
            mImage = view.findViewById(R.id.image);
            mLayout = view.findViewById(R.id.layout);
        }
    }


}
