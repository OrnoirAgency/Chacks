package com.ornoiragency.chacks.ChatGroup;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
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
import com.ornoiragency.chacks.Adapter.AdapterParticipantAdd;
import com.ornoiragency.chacks.MainActivity;
import com.ornoiragency.chacks.R;
import com.ornoiragency.chacks.models.User;


import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class GroupInfoActivity extends AppCompatActivity {

    private String groupId;
    private String myGroupRole = "";

    private FirebaseAuth firebaseAuth;

    private ImageView groupIconIv;
    private TextView descriptionTv,createdByTv,editGroupTv,addParticipantTv,leaveGroupTv,participantsTv;
    private RecyclerView participantsRv;

    private ArrayList<User> userList;
    private AdapterParticipantAdd adapterParticipantAdd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_info);


        groupIconIv = findViewById(R.id.groupIconIv);
        descriptionTv = findViewById(R.id.descriptionTv);
        createdByTv = findViewById(R.id.createdByTv);
        editGroupTv = findViewById(R.id.editGroupTv);
        addParticipantTv = findViewById(R.id.addParticipantTv);
        leaveGroupTv = findViewById(R.id.leaveGroupTv);
        participantsTv = findViewById(R.id.participantsTv);
        participantsRv = findViewById(R.id.participantsRv);

        groupId = getIntent().getStringExtra("groupId");

        firebaseAuth = FirebaseAuth.getInstance();
        loadGroupInfo();
        loadMyGroupRole();

        addParticipantTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(GroupInfoActivity.this,GroupParticipantAddActivity.class);
                intent.putExtra("groupId",groupId);
                startActivity(intent);
            }
        });

        editGroupTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(GroupInfoActivity.this,GroupEditAddActivity.class);
                intent.putExtra("groupId",groupId);
                startActivity(intent);
            }
        });

        leaveGroupTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String dialogTitle="";
                String dialogDescription="";
                String positiveButtonTitle="";
                if (myGroupRole.equals("creator")){
                    dialogTitle = "Delete group";
                    dialogDescription = "Are you sure you want to delete group parmanently?";
                    positiveButtonTitle = "DELETE";
                } else {

                    dialogTitle = "Leave group";
                    dialogDescription = "Are you sure you want to Leave group parmanently?";
                    positiveButtonTitle = "LEAVE";
                }
                AlertDialog.Builder builder = new AlertDialog.Builder(GroupInfoActivity.this);
                builder.setTitle(dialogTitle)
                        .setMessage(dialogDescription)
                        .setPositiveButton(positiveButtonTitle, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (myGroupRole.equals("creator")){
                                    // creator of group: delete group
                                    deleteGroup();
                                } else{
                                    // participant/admin: leave group
                                    leaveGroup();
                                }
                            }
                        })
                .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .show();
            }
        });
    }

    private void leaveGroup() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
        ref.child(groupId).child("participants").child(firebaseAuth.getUid())
                .removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(GroupInfoActivity.this, "Group left successfully...", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(GroupInfoActivity.this, MainActivity.class));
                finish();
            }
        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(GroupInfoActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void deleteGroup() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
        ref.child(groupId)
                .removeValue()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(GroupInfoActivity.this, "Group delete successfully...", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(GroupInfoActivity.this, MainActivity.class));
                        finish();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(GroupInfoActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void loadGroupInfo() {

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
        ref.orderByChild("groupId").equalTo(groupId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        for (DataSnapshot ds: dataSnapshot.getChildren()){

                            String groupId = "" + ds.child("groupId").getValue();
                            String groupTitle = "" + ds.child("groupTitle").getValue();
                            String groupDescription = "" + ds.child("groupDescription").getValue();
                            String groupIcon = "" + ds.child("groupIcon").getValue();
                            String timestamp = "" + ds.child("timestamp").getValue();
                            String createdBy = "" + ds.child("createdBy").getValue();

                            Calendar cal = Calendar.getInstance(Locale.FRENCH);
                            cal.setTimeInMillis(Long.parseLong(timestamp));
                            String dateTime = DateFormat.format("dd/MM/yyyy hh:mm ",cal).toString();

                            loadCreatorInfo(dateTime,createdBy);

                            //actionBar.setTitle(groupTitle);
                            descriptionTv.setText(groupDescription);

                            try {
                                Glide.with(GroupInfoActivity.this)
                                        .asBitmap()
                                        .load(groupIcon)
                                        .placeholder(R.drawable.image_placeholder)
                                        .into(groupIconIv);

                            } catch (Exception e){
                                groupIconIv.setImageResource(R.drawable.profile_image);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void loadCreatorInfo(String dateTime, String createdBy) {

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("user");
        ref.orderByChild("uid").equalTo(createdBy).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for (DataSnapshot ds: dataSnapshot.getChildren()){

                    String name = "" + ds.child("name").getValue();
                    createdByTv.setText("Created by "+ name + " on " + dateTime);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void loadMyGroupRole() {

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
        ref.child(groupId).child("participants").orderByChild("uid")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        for (DataSnapshot ds: dataSnapshot.getChildren()){

                            myGroupRole = ""+ds.child("role").getValue();
                          //  actionBar.setTitle(myGroupRole);

                            if (myGroupRole.equals("participant")){
                                editGroupTv.setVisibility(View.GONE);
                                addParticipantTv.setVisibility(View.GONE);
                                leaveGroupTv.setText("Leave group");
                            } else if (myGroupRole.equals("admin")){
                                editGroupTv.setVisibility(View.GONE);
                                addParticipantTv.setVisibility(View.VISIBLE);
                                leaveGroupTv.setText("Leave group");
                            } else if (myGroupRole.equals("creator")){
                                editGroupTv.setVisibility(View.VISIBLE);
                                addParticipantTv.setVisibility(View.VISIBLE);
                                leaveGroupTv.setText("Delete group");
                            }
                        }

                        loadParticipants();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void loadParticipants() {

        userList = new ArrayList<>();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
        ref.child(groupId).child("participants").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userList.clear();

                for (DataSnapshot ds: dataSnapshot.getChildren()){
                    //get uid from Group> Participant
                    String uid = ""+ds.child("uid").getValue();
                    //get info of user uid we
                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference("user");
                    ref.orderByChild("uid").equalTo(uid).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                            for (DataSnapshot ds : dataSnapshot.getChildren()){

                                User user = ds.getValue(User.class);

                                userList.add(user);
                            }

                            adapterParticipantAdd = new AdapterParticipantAdd(GroupInfoActivity.this,userList,groupId,myGroupRole);
                            participantsRv.setAdapter(adapterParticipantAdd);
                            participantsTv.setText("participants("+ userList.size()+")");
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
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }
}
