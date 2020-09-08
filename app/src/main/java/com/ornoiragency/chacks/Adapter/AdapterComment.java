package com.ornoiragency.chacks.Adapter;

import android.content.Context;
import android.content.DialogInterface;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ornoiragency.chacks.R;
import com.ornoiragency.chacks.models.Comment;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class AdapterComment extends RecyclerView.Adapter<AdapterComment.MyHolder>{

    Context context;
    List<Comment> commentList;
    String myUid,postId;

    public AdapterComment(Context context, List<Comment> commentList, String myUid, String postId) {
        this.context = context;
        this.commentList = commentList;
        this.myUid = myUid;
        this.postId = postId;
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_comment,parent,false);
        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {

        holder.setIsRecyclable(false);

        String uid = commentList.get(position).getUid();
        String uName = commentList.get(position).getuName();
        String uDp = commentList.get(position).getuDp();
        String cId = commentList.get(position).getcId();
        String timestamp = commentList.get(position).getTimestamp();
        String comment = commentList.get(position).getComment();

        Calendar cal = Calendar.getInstance(Locale.FRENCH);
        cal.setTimeInMillis(Long.parseLong(timestamp));
        String time = DateFormat.format("dd/MM HH:mm ",cal).toString();

        holder.nameTv.setText(uName);
        holder.timeTv.setText(time);
        holder.commentTv.setText(comment);

        Glide.with(context.getApplicationContext())
                .asBitmap()
                .load(uDp)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(holder.avatarIv);


        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                if (myUid.equals(uid)){
                    //my comment
                    AlertDialog.Builder builder = new AlertDialog.Builder(v.getRootView().getContext());
                    builder.setTitle("delete");
                    builder.setMessage("are you sure...?");
                    builder.setPositiveButton("delete", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            deleteComment(cId);
                        }
                    });
                    builder.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    builder.create().show();
                } else {
                    // not my comment

                }

                return false;
            }
        });
    }

    private void deleteComment(String cId) {

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts").child(postId);
        ref.child("Comments").child(cId).removeValue();

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String comments = ""+ dataSnapshot.child("pComments").getValue();
                int newCommentVal = Integer.parseInt(comments) - 1;
                ref.child("pComments").setValue(""+ newCommentVal);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return commentList.size();
    }

    class MyHolder extends RecyclerView.ViewHolder{


        CircleImageView avatarIv;
        TextView nameTv,commentTv,timeTv;

        public MyHolder(@NonNull View itemView) {
            super(itemView);

            avatarIv = itemView.findViewById(R.id.avatarIv);
            nameTv = itemView.findViewById(R.id.nameTv);
            commentTv = itemView.findViewById(R.id.commentTv);
            timeTv = itemView.findViewById(R.id.timeTv);
        }
    }
}
