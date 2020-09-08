package com.ornoiragency.chacks.Post;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ornoiragency.chacks.Adapter.UserListAdapter;
import com.ornoiragency.chacks.R;
import com.ornoiragency.chacks.Utils.DividerItemDecoration;
import com.ornoiragency.chacks.models.User;

import java.util.ArrayList;
import java.util.List;

public class PostLikedByActivity extends AppCompatActivity {

    String postId;
    private RecyclerView recyclerView;
    private List<User> userList;
    private UserListAdapter adapterUser;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_liked_by);


        recyclerView = findViewById(R.id.recyclerview);
        RecyclerView.ItemDecoration itemDecoration = new DividerItemDecoration(getApplicationContext(),
                DividerItemDecoration.VERTICAL_LIST);
        recyclerView.addItemDecoration(itemDecoration);

        Intent intent = getIntent();
        postId = intent.getStringExtra("postId");

        userList = new ArrayList<>();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Likes");
        ref.child(postId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userList.clear();
                for (DataSnapshot ds: dataSnapshot.getChildren()){

                    String hisUid = ""+ds.getRef().getKey();

                    getUsers(hisUid);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getUsers(String hisUid) {

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("user");
        ref.orderByChild("uid").equalTo(hisUid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot ds: dataSnapshot.getChildren()){

                            User model = ds.getValue(User.class);
                            userList.add(model);
                        }

                        adapterUser = new UserListAdapter(PostLikedByActivity.this,userList);
                        recyclerView.setAdapter(adapterUser);
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
