package com.ornoiragency.chacks.Adapter;

import android.content.Context;
import android.content.Intent;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ornoiragency.chacks.ChatGroup.GroupChatActivity;
import com.ornoiragency.chacks.R;
import com.ornoiragency.chacks.models.GroupChatList;


import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import me.alvince.android.avatarimageview.AvatarImageView;

public class AdapterGroupChatList extends RecyclerView.Adapter<AdapterGroupChatList.holderGroupChatList> {

    private Context context;
    private ArrayList<GroupChatList> groupChatLists;

    public AdapterGroupChatList(Context context, ArrayList<GroupChatList> groupChatLists) {
        this.context = context;
        this.groupChatLists = groupChatLists;
    }

    @NonNull
    @Override
    public holderGroupChatList onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(context).inflate(R.layout.row_groupchats_list,parent,false);

        return new holderGroupChatList(view);
    }

    @Override
    public void onBindViewHolder(@NonNull holderGroupChatList holder, int position) {

        holder.setIsRecyclable(false);

        GroupChatList model = groupChatLists.get(position);
        String groupId = model.getGroupId();
        String groupIcon = model.getGroupIcon();
        String groupTitle = model.getGroupTitle();

        holder.nameTv.setText("");
        holder.timeTv.setText("");
        holder.messageTv.setText("");

        //load last message
        loadLastMessage(model,holder);

        //set data
        holder.groupTitleTv.setText(groupTitle);
        try {
            Glide.with(context.getApplicationContext())
                    .asBitmap()
                    .load(groupIcon)
                    .placeholder(R.drawable.ic_create_group)
                    .into(holder.groupIconIv);


        } catch (Exception e){

            holder.groupIconIv.setImageResource(R.drawable.ic_group);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(context, GroupChatActivity.class);
                intent.putExtra("groupId",groupId);
                context.startActivity(intent);
            }
        });

    }

    private void loadLastMessage(GroupChatList model, holderGroupChatList holder) {

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
        ref.child(model.getGroupId()).child("Messages").limitToLast(1)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        for (DataSnapshot ds: dataSnapshot.getChildren()){
                            //get data
                            String message = ""+ ds.child("message").getValue();
                            String timestamp = ""+ ds.child("timestamp").getValue();
                            String sender = ""+ ds.child("sender").getValue();
                            String messageType = ""+ ds.child("type").getValue();

                            //convert time
                            Calendar cal = Calendar.getInstance(Locale.FRENCH);
                            cal.setTimeInMillis(Long.parseLong(timestamp));
                            String dateTime = DateFormat.format("dd/MM/yyyy hh:mm ",cal).toString();

                            if (messageType.equals("image")){
                                holder.messageTv.setText("Sent photo");
                            } else {
                                holder.messageTv.setText(message);
                            }
                            holder.timeTv.setText(dateTime);

                            //get info of sender of last message
                            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("user");
                            ref.orderByChild("uid").equalTo(sender)
                                    .addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                            for (DataSnapshot ds: dataSnapshot.getChildren()){
                                                String name = ""+ds.child("name").getValue();
                                                holder.nameTv.setText(name);
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                        }
                                    });

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    @Override
    public int getItemCount() {
        return groupChatLists.size();
    }

    //view Holder class
   class holderGroupChatList extends RecyclerView.ViewHolder{

       private AvatarImageView groupIconIv;
       private TextView groupTitleTv,nameTv,messageTv,timeTv;

        public holderGroupChatList(@NonNull View itemView) {
            super(itemView);

            groupIconIv = itemView.findViewById(R.id.groupIconIv);
            groupTitleTv = itemView.findViewById(R.id.groupTitleTv);
            nameTv = itemView.findViewById(R.id.nameTv);
            messageTv = itemView.findViewById(R.id.messageTv);
            timeTv = itemView.findViewById(R.id.timeTv);
        }
    }
}
