package com.ornoiragency.chacks.Adapter;

import android.content.Context;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ornoiragency.chacks.R;
import com.ornoiragency.chacks.models.GroupChat;

import com.stfalcon.frescoimageviewer.ImageViewer;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Locale;

import me.alvince.android.avatarimageview.AvatarImageView;

public class AdapterGroupChat extends RecyclerView.Adapter<AdapterGroupChat.holderGroupChat>  {

    private static final int MSG_TYPE_LEFT = 0;
    private static final int MSG_TYPE_RIGHT = 1;

    private Context context;
    private ArrayList<GroupChat> groupChatList;

    private FirebaseAuth firebaseAuth;

    public AdapterGroupChat(Context context, ArrayList<GroupChat> groupChatList) {
        this.context = context;
        this.groupChatList = groupChatList;

        firebaseAuth = FirebaseAuth.getInstance();
    }

    @NonNull
    @Override
    public holderGroupChat onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        if (viewType == MSG_TYPE_RIGHT){
            View view = LayoutInflater.from(context).inflate(R.layout.row_group_chat_right,parent,false);
            return new holderGroupChat(view);
        } else {

            View view = LayoutInflater.from(context).inflate(R.layout.row_group_chat_left,parent,false);
            return new holderGroupChat(view);
        }


    }

    @Override
    public void onBindViewHolder(@NonNull holderGroupChat holder, int position) {

        holder.setIsRecyclable(false);
        GroupChat model = groupChatList.get(position);

        String timestamp = model.getTimestamp();
        String message = model.getMessage();
        String senderUid = model.getSender();
        String messageType = model.getType();

        Calendar cal = Calendar.getInstance(Locale.FRENCH);
        cal.setTimeInMillis(Long.parseLong(timestamp));
        String dateTime = DateFormat.format(" HH:mm ",cal).toString();
        holder.timeTv.setText(dateTime);

        if (messageType.equals("text")){
            //text message
            holder.messageIv.setVisibility(View.GONE);
            holder.messageTv.setVisibility(View.VISIBLE);
            holder.messageTv.setText(message);
        } else {
            //image message
            holder.messageIv.setVisibility(View.VISIBLE);
            holder.messageTv.setVisibility(View.GONE);

            try {
                Glide.with(context.getApplicationContext())
                        .asBitmap()
                        .load(message)
                        .placeholder(R.drawable.image_placeholder)
                        .into(holder.messageIv);

            } catch (Exception e){


            }

            holder.messageIv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new ImageViewer.Builder(v.getContext(), Collections.singletonList(model.getMessage()))
                            .setStartPosition(0)
                            .show();
                }
            });
        }

        holder.timeTv.setText(dateTime);


        setUserName(holder,model);
    }

    private void setUserName(holderGroupChat holder, GroupChat model) {

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("user");
        ref.orderByChild("uid").equalTo(model.getSender())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        for (DataSnapshot ds: dataSnapshot.getChildren()){

                            String name = "" + ds.child("name").getValue();
                            String image = "" + ds.child("image").getValue();
                            holder.nameTv.setText(name);

                            Glide.with(context.getApplicationContext())
                                    .asBitmap()
                                    .load(image)
                                    .placeholder(R.drawable.image_placeholder)
                                    .into(holder.userImage);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    @Override
    public int getItemCount() {
        return groupChatList.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (groupChatList.get(position).getSender().equals(firebaseAuth.getUid())){

            return MSG_TYPE_RIGHT;
        } else {
            return MSG_TYPE_LEFT;
        }
    }

    class holderGroupChat extends RecyclerView.ViewHolder{

        private TextView nameTv,messageTv,timeTv;
        private ImageView messageIv;
        private AvatarImageView userImage;

        public holderGroupChat(@NonNull View itemView) {
            super(itemView);
            nameTv = itemView.findViewById(R.id.nameTv);
            messageTv = itemView.findViewById(R.id.messageTv);
            messageIv = itemView.findViewById(R.id.messageIv);
            timeTv = itemView.findViewById(R.id.timeTv);
            userImage = itemView.findViewById(R.id.userImage);
        }
    }
}
