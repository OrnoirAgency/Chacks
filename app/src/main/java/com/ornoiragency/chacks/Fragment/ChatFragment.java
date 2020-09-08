package com.ornoiragency.chacks.Fragment;


import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.miguelcatalan.materialsearchview.MaterialSearchView;
import com.ornoiragency.chacks.Adapter.AdapterChatList;
import com.ornoiragency.chacks.Notification.Token;
import com.ornoiragency.chacks.R;
import com.ornoiragency.chacks.Utils.DividerItemDecoration;
import com.ornoiragency.chacks.models.Chatlist;
import com.ornoiragency.chacks.models.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;

/**
 * A simple {@link Fragment} subclass.
 */
public class ChatFragment extends Fragment {

    public static final String TAG = "ChatFragment";
    View view;

    private RecyclerView recyclerView;

    private AdapterChatList userAdapter;
    private List<User> mUsers;
    private FirebaseAuth mAuth;
    private DatabaseReference reference;

    private List<Chatlist> usersList;


    String mUID;
    Toolbar toolbar;
    MaterialSearchView searchView;

    FirebaseUser firebaseUser;

    public ChatFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat, container, false);

        toolbar = view.findViewById(R.id.toolbar);
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        activity.setSupportActionBar(toolbar);
        try
        {
            activity.getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        catch (NullPointerException e){}


        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        recyclerView = view.findViewById(R.id.chatList);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setNestedScrollingEnabled(false);

        mAuth = FirebaseAuth.getInstance();
        mUID = mAuth.getCurrentUser().getUid();

        searchView = view.findViewById(R.id.search_view);

        updateToken(FirebaseInstanceId.getInstance().getToken());

        loadUserChat();

        return view;
    }

    private void loadUserChat() {

        usersList = new ArrayList<>();
        reference = FirebaseDatabase.getInstance().getReference("ChatList").child(mUID);
        reference.keepSynced(true);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                usersList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Chatlist chatlist = snapshot.getValue(Chatlist.class);
                    usersList.add(chatlist);
                }
                chatList();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void updateToken(String token){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Tokens");
        Token token1 = new Token(token);
        reference.child(mUID).setValue(token1);
    }

    private void chatList() {
        mUsers = new ArrayList<>();
        reference = FirebaseDatabase.getInstance().getReference("user");
        reference.keepSynced(true);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mUsers.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    User user = snapshot.getValue(User.class);
                    for (Chatlist chatlist : usersList){
                        if (user.getUid().equals(chatlist.getId())){
                            mUsers.add(user);
                        }
                    }
                }
                userAdapter = new AdapterChatList(getContext(), mUsers, true);
                recyclerView.setAdapter(userAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    private void searchChatList(String s) {

        mUsers = new ArrayList<>();
        reference = FirebaseDatabase.getInstance().getReference("user");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mUsers.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    User user = snapshot.getValue(User.class);
                    for (Chatlist chatlist : usersList){
                        if (user.getUid().equals(chatlist.getId()) && user.getSearch().toLowerCase().contains(s.toLowerCase())){
                            mUsers.add(user);
                        }

                    }
                }
                userAdapter = new AdapterChatList(getContext(), mUsers, true);
                recyclerView.setAdapter(userAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void onCreate(Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        super.onCreate(savedInstanceState);

    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {

        inflater.inflate(R.menu.menu_main, menu);
        menu.findItem(R.id.action_logout).setVisible(false);
        menu.findItem(R.id.action_groupinfo).setVisible(false);
        menu.findItem(R.id.action_search).setVisible(true);
        menu.findItem(R.id.action_settings).setVisible(false);

        //searchview to search posts by post title/description

        MenuItem search = menu.findItem(R.id.action_search);
        searchView.setMenuItem(search);

        searchView.setOnQueryTextListener(new MaterialSearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                //Do some magic
                if (!TextUtils.isEmpty(s)){
                    searchChatList(s);
                } else {
                     chatList();
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                //Do some magic
                if (!TextUtils.isEmpty(s)){
                    searchChatList(s);
                } else {
                    chatList();
                }
                return false;
            }
        });

        searchView.setOnSearchViewListener(new MaterialSearchView.SearchViewListener() {
            @Override
            public void onSearchViewShown() {
                //Do some magic
            }

            @Override
            public void onSearchViewClosed() {
                //Do some magic
            }
        });

        super.onCreateOptionsMenu(menu, inflater);
    }

    private void status(String status){

        reference = FirebaseDatabase.getInstance().getReference("user").child(firebaseUser.getUid());
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("states", status);
        reference.updateChildren(hashMap);
    }

    private void checkOnlineStatus(String state){

        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("user").child(firebaseUser.getUid());
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("onlineStatus",state);

        dbRef.updateChildren(hashMap);
    }


    @Override
    public void onStart() {
        super.onStart();
        checkOnlineStatus("online");

        SharedPreferences sp = getActivity().getSharedPreferences("SP_USER", MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString("Current_USERID", mUID);
        editor.apply();
    }

    @Override
    public void onResume() {
        status("online");
        super.onResume();
        checkOnlineStatus("online");
    }

    @Override
    public void onPause() {
        super.onPause();
        String timestamp = String.valueOf(System.currentTimeMillis());
        checkOnlineStatus(timestamp);
        status("offline");
    }
}

