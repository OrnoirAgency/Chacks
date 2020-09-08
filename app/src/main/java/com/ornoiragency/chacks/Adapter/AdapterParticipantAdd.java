package com.ornoiragency.chacks.Adapter;

import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ornoiragency.chacks.R;
import com.ornoiragency.chacks.models.User;


import java.util.ArrayList;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class AdapterParticipantAdd extends RecyclerView.Adapter<AdapterParticipantAdd.holderParticipantAdd> {

    private Context context;
    private ArrayList<User> userList;
    private String groupId,myGroupRole;

    public AdapterParticipantAdd(Context context, ArrayList<User> userList, String groupId, String myGroupRole) {
        this.context = context;
        this.userList = userList;
        this.groupId = groupId;
        this.myGroupRole = myGroupRole;
    }

    @NonNull
    @Override
    public holderParticipantAdd onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(context).inflate(R.layout.row_participant_add,parent,false);
        return new holderParticipantAdd(view);
    }

    @Override
    public void onBindViewHolder(@NonNull holderParticipantAdd holder, int position) {
     holder.setIsRecyclable(false);

        User user = userList.get(position);
        String name = user.getName();
        String image = user.getImage();
        String uid = user.getUid();

        //set Data
        holder.nameTv.setText(name);
        try {

            Glide.with(context)
                    .asBitmap()
                    .load(image)
                    .placeholder(R.drawable.profile_image)
                    .into(holder.avatarIv);



        } catch (Exception e){
            holder.avatarIv.setImageResource(R.drawable.profile_image);
        }

        checkIfAlreadyExists(user,holder);
        // handler click

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
                ref.child(groupId).child("participants").child(uid)
                        .addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                if (dataSnapshot.exists()){
                                    // user exists/participant
                                    String hisPreviousRole = ""+ dataSnapshot.child("role").getValue();

                                    String[] options;

                                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                                    builder.setTitle("choose option");
                                    if (myGroupRole.equals("creator")){
                                        if (hisPreviousRole.equals("admin")){
                                            //i'm creator, he is admin
                                            options = new String[] {"Remove Admin", "Remove User"};
                                            builder.setItems(options, new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {

                                                    if (which == 0){

                                                        //Remove Admin clicked
                                                        removeAdmin(user);

                                                    } else {
                                                        //Remove User clicked
                                                        removeParticipant(user);
                                                    }
                                                }
                                            }).show();

                                        }
                                        else if (hisPreviousRole.equals("participant")){
                                            //i'm creator, he is participant
                                            options = new String[]{"Make Admin", "Remove user"};
                                            builder.setItems(options, new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {

                                                    if (which == 0){

                                                        //Make Admin clicked
                                                        makeAdmin(user);

                                                    } else {
                                                        //Remove User clicked
                                                        removeParticipant(user);
                                                    }
                                                }
                                            }).show();

                                        }
                                    }
                                     else if (myGroupRole.equals("admin")){
                                         if (hisPreviousRole.equals("creator")){
                                             //i'm admin, he is creator
                                             Toast.makeText(context, "Creator of Group...", Toast.LENGTH_SHORT).show();
                                         }
                                           else if (hisPreviousRole.equals("admin")){
                                               //i'm admin, he is admin too
                                             options = new String[] {"Remove Admin", "Remove User"};
                                             builder.setItems(options, new DialogInterface.OnClickListener() {
                                                 @Override
                                                 public void onClick(DialogInterface dialog, int which) {

                                                     if (which == 0){

                                                         //Remove Admin clicked
                                                         removeAdmin(user);

                                                     } else {
                                                         //Remove User clicked
                                                         removeParticipant(user);
                                                     }
                                                 }
                                             }).show();
                                         }
                                           else if (hisPreviousRole.equals("participant")){
                                               //i'm admin, he is participant
                                             options = new String[]{"Make admin","Remove user"};
                                             builder.setItems(options, new DialogInterface.OnClickListener() {
                                                 @Override
                                                 public void onClick(DialogInterface dialog, int which) {

                                                     if (which == 0){

                                                         //Make Admin clicked
                                                         makeAdmin(user);

                                                     } else {
                                                         //Remove User clicked
                                                         removeParticipant(user);
                                                     }
                                                 }
                                             }).show();
                                         }
                                    }

                                } else {
                                    //user doesn't exists/no-participant:add
                                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                                    builder.setTitle("Add participant")
                                            .setMessage("Add this user in this group?")
                                            .setPositiveButton("ADD", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    //add user
                                                    addParticipant(user);
                                                }
                                            })
                                            .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    dialog.dismiss();
                                                }
                                            }).show();
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
            }
        });
    }

    private void addParticipant(User user) {

        //setup user data - add user in group
        String timestamp = ""+ System.currentTimeMillis();
        HashMap<String,String> hashMap = new HashMap<>();
        hashMap.put("uid",user.getUid());
        hashMap.put("role","participant");
        hashMap.put("timestamp",timestamp);
        //add that user in Group>groupId>Participant
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
        ref.child(groupId).child("participants").child(user.getUid()).setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        Toast.makeText(context, "Add successfully...", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

    }

    private void makeAdmin(User user) {
        //setup user data - make user admin
        HashMap<String,Object> hashMap = new HashMap<>();
        hashMap.put("role","admin");
        //update role db
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
        ref.child(groupId).child("participants").child(user.getUid()).updateChildren(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        Toast.makeText(context, "The user is now admin", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

    }

    private void removeParticipant(User user) {
        //remove participant from group
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
        ref.child(groupId).child("participants").child(user.getUid()).removeValue()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {


                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                });
    }

    private void removeAdmin(User user) {
        // setup data = remove admin role
        HashMap<String,Object> hashMap = new HashMap<>();
        hashMap.put("role","participant");
        //update role db
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
        ref.child(groupId).child("participants").child(user.getUid()).updateChildren(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        Toast.makeText(context, "The user is no longer admin", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }


    private void checkIfAlreadyExists(User user, holderParticipantAdd holder) {

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
        ref.child(groupId).child("participants").child(user.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        if (dataSnapshot.exists()){

                            String hisRole = "" + dataSnapshot.child("role").getValue();
                            holder.statusTv.setText(hisRole);

                        } else {
                            holder.statusTv.setText("");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    class holderParticipantAdd extends RecyclerView.ViewHolder{

        CircleImageView avatarIv;
        TextView nameTv,statusTv;

        public holderParticipantAdd(@NonNull View itemView) {
            super(itemView);

            avatarIv = itemView.findViewById(R.id.avatarIv);
            nameTv = itemView.findViewById(R.id.nameTv);
            statusTv = itemView.findViewById(R.id.statusTv);
        }
    }
}
