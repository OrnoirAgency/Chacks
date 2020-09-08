package com.ornoiragency.chacks.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.ornoiragency.chacks.models.User;
import com.ornoiragency.chacks.models.UserObject;
import com.ornoiragency.chacks.R;

import com.stfalcon.frescoimageviewer.ImageViewer;

import java.util.Collections;

import de.hdodenhof.circleimageview.CircleImageView;

public class AllUserProfileActivity extends AppCompatActivity {


    private CircleImageView mUserImage;
    private TextView mUserName;
    private ImageButton sendMessageBtn;

    private String user_id,senderUSerId;
    private FirebaseAuth mAuth;

    ImageView mBack,mCover;
     String uid,ucover;

    private DatabaseReference UsersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_user_profile);

        user_id = getIntent().getStringExtra("user_id");

        mCover = findViewById(R.id.cover);

        mBack = findViewById(R.id.back);
        mBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        mAuth = FirebaseAuth.getInstance();

        sendMessageBtn = findViewById(R.id.send_message);
        mUserName = findViewById(R.id.user_name);
        mUserImage = findViewById(R.id.user_image);

        sendMessageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent chatIntent = new Intent(AllUserProfileActivity.this, MessagesActivity.class);
                chatIntent.putExtra("user_id", user_id);
                startActivity(chatIntent);
            }
        });

        mAuth = FirebaseAuth.getInstance();
        uid = mAuth.getCurrentUser().getUid();

        UsersRef = FirebaseDatabase.getInstance().getReference("user").child(user_id);
        UsersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);



            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        getUserInfo();
    }

    UserObject mUser;
    private void getUserInfo() {
        mUser = new UserObject(FirebaseAuth.getInstance().getUid());
        FirebaseDatabase.getInstance().getReference()
                .child("user")
                .child(user_id)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        mUser.parseObject(dataSnapshot);

                        String name = mUser.getName();
                        mUserName.setText(name);


                        Glide.with(getApplicationContext())
                                .asBitmap()
                                .load(mUser.getImage())
                                .placeholder(R.drawable.profile_image)
                                .into(mUserImage);

                        Glide.with(getApplicationContext())
                                .asBitmap()
                                .load(mUser.getImage())
                                .placeholder(R.drawable.ic_backgroud_profil)
                                .into(mCover);


                        mUserImage.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                new ImageViewer.Builder(v.getContext(), Collections.singletonList(mUser.getImage()))
                                        .setStartPosition(0)
                                        .show();
                            }
                        });

                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

    }
    public UserObject getUser() {
        return mUser;
    }
}
