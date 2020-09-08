package com.ornoiragency.chacks.Adapter;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ornoiragency.chacks.Activity.ChatActivity;
import com.ornoiragency.chacks.Activity.MessagesActivity;
import com.ornoiragency.chacks.R;
import com.ornoiragency.chacks.models.Chat;
import com.ornoiragency.chacks.models.User;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class AdapterChatList extends RecyclerView.Adapter<AdapterChatList.ViewHolder> {

    private Context mContext;
    private List<User> mUsers;
    private boolean ischat;

    String theLastMessage,theLastMessageDate;

    public AdapterChatList(Context mContext, List<User> mUsers, boolean ischat){
        this.mUsers = mUsers;
        this.mContext = mContext;
        this.ischat = ischat;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.user_chat_list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        holder.setIsRecyclable(false);

        holder.last_msg_date.setText("");
        holder.last_msg.setText("");

        final User user = mUsers.get(position);
        holder.username.setText(user.getName());
        if (user.getImage().equals("default")){
            holder.profile_image.setImageResource(R.drawable.ic_user_chat_icon);
        } else {
            Glide.with(mContext.getApplicationContext())
                    .asBitmap()
                    .load(user.getImage())
                    .placeholder(R.drawable.ic_user_chat_icon)
                    .into(holder.profile_image);

        }

        if (ischat){
            lastMessage(user.getUid(), holder.last_msg,holder.msg_read);
            lastMessageDate(user.getUid(),holder.last_msg_date);
            lastMessageUnread(user.getUid(),holder.last_msg,holder.msg_read);
        } else {
            holder.last_msg.setVisibility(View.GONE);
            holder.last_msg_date.setVisibility(View.GONE);
            holder.msg_read.setVisibility(View.GONE);
        }

        if (ischat){
            if (user.getStates().equals("online")){
                holder.img_on.setVisibility(View.VISIBLE);
                holder.img_off.setVisibility(View.GONE);
            } else {
                holder.img_on.setVisibility(View.GONE);
                holder.img_off.setVisibility(View.VISIBLE);
            }
        } else {
            holder.img_on.setVisibility(View.GONE);
            holder.img_off.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, MessagesActivity.class);
                intent.putExtra("user_id", user.getUid());
                mContext.startActivity(intent);
            }
        });

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                builder.setTitle("Supprimer la conversation?");
                builder.setPositiveButton("Supprimer", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        FirebaseUser fuser = FirebaseAuth.getInstance().getCurrentUser();
                        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("Chatlist").child(fuser.getUid()).child(user.getUid());
                        databaseReference.removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {

                                if (task.isSuccessful()){
                                    Toast.makeText(mContext, "deleted", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });

                    }
                });
                builder.setNegativeButton("Annuler",null);
                builder.show();

                return false;
            }
        });

    }

    private void lastMessageUnread(String uid, TextView last_msg, TextView msg_read) {
        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Chats");
        reference.keepSynced(true);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                int unread = 0;
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Chat chat = snapshot.getValue(Chat.class);

                    if (firebaseUser != null && chat != null) {
                        if (chat.getReceiver().equals(firebaseUser.getUid()) && chat.getSender().equals(uid) && !chat.isIsseen()) {
                            unread++;
                            msg_read.setText(Integer.toString(unread));
                            msg_read.setVisibility(View.VISIBLE);
                            last_msg.setTextColor(Color.GREEN);
                        }
                    }


                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    @Override
    public int getItemCount() {
        return mUsers.size();
    }

    public  class ViewHolder extends RecyclerView.ViewHolder{

        public TextView username;
        public ImageView profile_image;
        private ImageView img_on;
        private ImageView img_off;
        private TextView last_msg,last_msg_date,msg_read;

        public ViewHolder(View itemView) {
            super(itemView);

            username = itemView.findViewById(R.id.username);
            profile_image = itemView.findViewById(R.id.profile_image);
            img_on = itemView.findViewById(R.id.img_on);
            img_off = itemView.findViewById(R.id.img_off);
            last_msg = itemView.findViewById(R.id.last_msg);
            last_msg_date = itemView.findViewById(R.id.last_msg_date);
            msg_read = itemView.findViewById(R.id.msg_read);
        }
    }

    //check for last message
    private void lastMessage(final String userid, final TextView last_msg, TextView msg_read){
        theLastMessage = "default";
        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Chats");
        reference.keepSynced(true);

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Chat chat = snapshot.getValue(Chat.class);
                    if (firebaseUser != null && chat != null) {
                        if (chat.getReceiver().equals(firebaseUser.getUid()) && chat.getSender().equals(userid) ||
                                chat.getReceiver().equals(userid) && chat.getSender().equals(firebaseUser.getUid()) ) {
                            theLastMessage = chat.getMessage();
                        }
                    }

                    last_msg.setText(theLastMessage);
                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    //check for last message date
    private void
    lastMessageDate(final String user_id, final TextView last_msg_date){
        theLastMessageDate = "default";

        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Chats");
        reference.keepSynced(true);

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Chat chat = snapshot.getValue(Chat.class);
                    if (firebaseUser != null && chat != null) {
                        if (chat.getReceiver().equals(firebaseUser.getUid()) && chat.getSender().equals(user_id) ||
                                chat.getReceiver().equals(user_id) && chat.getSender().equals(firebaseUser.getUid())) {
                            String timestamp = chat.getTimestamp();
                            Calendar cal = Calendar.getInstance(Locale.FRENCH);
                            cal.setTimeInMillis(Long.parseLong(timestamp));
                            theLastMessageDate = DateFormat.format("dd MMMM,yyyy HH:mm ",cal).toString();

                        }
                    }

                    last_msg_date.setText(theLastMessageDate);
                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

}
