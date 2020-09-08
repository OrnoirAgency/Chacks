package com.ornoiragency.chacks.Adapter;


import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;


import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ornoiragency.chacks.Post.PostAudioDetailActivity;
import com.ornoiragency.chacks.Post.PostDetailActivity;
import com.ornoiragency.chacks.Post.PostTextDetailActivity;
import com.ornoiragency.chacks.Post.PostVideoDetailActivity;
import com.ornoiragency.chacks.R;
import com.ornoiragency.chacks.models.Notification;


import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class AdapterNotification extends RecyclerView.Adapter<AdapterNotification.HolderNotification> {

    private Context context;
    private ArrayList<Notification> notificationList;
    private FirebaseAuth firebaseAuth;


    public AdapterNotification(Context context, ArrayList<Notification> notificationList) {
        this.context = context;
        this.notificationList = notificationList;

        firebaseAuth = FirebaseAuth.getInstance();
    }

    @NonNull
    @Override
    public HolderNotification onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(context).inflate(R.layout.row_notification,parent,false);
        return new HolderNotification(view);

    }

    @Override
    public void onBindViewHolder(@NonNull HolderNotification holder, int position) {
        holder.setIsRecyclable(false);


        //get data
        Notification model = notificationList.get(position);
        String name = model.getsName();
        String notification = model.getNotification();
        String image = model.getsImage();
        String timestamp = model.getTimestamp();
        String senderUid = model.getsUid();
        String pId = model.getpId();

        String fromType = model.getType();

        Calendar cal = Calendar.getInstance(Locale.FRENCH);
        cal.setTimeInMillis(Long.parseLong(timestamp));
        String time = DateFormat.format("dd MMMM, yyyy HH:mm ",cal).toString();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("user");
        reference.orderByChild("uid").equalTo(senderUid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        for (DataSnapshot ds: dataSnapshot.getChildren()){

                            String name = ""+ds.child("name").getValue();
                            String image = ""+ds.child("image").getValue();

                            model.setsName(name);
                            model.setsImage(image);

                            holder.nameTv.setText(name);

                            Glide.with(context.getApplicationContext())
                                    .asBitmap()
                                    .load(image)
                                    .placeholder(R.drawable.profile_image)
                                    .into(holder.avatarIv);

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });



        holder.timeTv.setText(time);
        holder.notificationTv.setText(notification);

        //  if (fromType.equals("text"));

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                if (fromType.equals("text")){
                    Intent intent = new Intent(context, PostTextDetailActivity.class);
                    intent.putExtra("postId",pId);
                    context.startActivity(intent);

                } else if (fromType.equals("image")){
                    Intent intent = new Intent(context, PostDetailActivity.class);
                    intent.putExtra("postId",pId);
                    context.startActivity(intent);
                } else if (fromType.equals("video")){

                    Intent intent = new Intent(context, PostVideoDetailActivity.class);
                    intent.putExtra("postId",pId);
                    context.startActivity(intent);

                } else if (fromType.equals("audio")){

                    Intent intent = new Intent(context, PostAudioDetailActivity.class);
                    intent.putExtra("postId",pId);
                    context.startActivity(intent);
                }
            }
        });

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Delete");
                builder.setMessage("Are you sure to delete this notification?");
                builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("user");
                        ref.child(firebaseAuth.getUid()).child("Notifications").child(timestamp)
                                .removeValue()
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Toast.makeText(context, "notification deleted...", Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                builder.create().show();
                return false;
            }
        });



    }



    @Override
    public int getItemCount() {
        return notificationList.size();
    }

    class HolderNotification extends RecyclerView.ViewHolder{

        CircleImageView avatarIv;
        TextView nameTv,notificationTv,timeTv;

        public HolderNotification(@NonNull View itemView) {
            super(itemView);

            avatarIv = itemView.findViewById(R.id.avatarIv);
            nameTv = itemView.findViewById(R.id.nameTv);
            notificationTv = itemView.findViewById(R.id.notificationTv);
            timeTv = itemView.findViewById(R.id.timeTv);


        }
    }
}
