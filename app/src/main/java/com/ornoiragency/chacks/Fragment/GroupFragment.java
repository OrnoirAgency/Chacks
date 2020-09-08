package com.ornoiragency.chacks.Fragment;


import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ornoiragency.chacks.Adapter.AdapterGroupChatList;
import com.ornoiragency.chacks.R;
import com.ornoiragency.chacks.models.GroupChatList;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 */
public class GroupFragment extends Fragment {

    public static final String TAG = "GroupFragment";
    View view;

    private RecyclerView groupsRv;
    FirebaseAuth firebaseAuth;
    Toolbar toolbar;
    TextView group_count;

    private ArrayList<GroupChatList> groupChatLists;
    private AdapterGroupChatList adapterGroupChatList;

    public GroupFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_group, container, false);
        toolbar = view.findViewById(R.id.toolbar);
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        activity.setSupportActionBar(toolbar);

        group_count = view.findViewById(R.id.group_count);
        groupsRv = view.findViewById(R.id.groupsRv);
        firebaseAuth = FirebaseAuth.getInstance();

        loadGroupChatList();

        return view;
    }

    private void loadGroupChatList() {

        groupChatLists = new ArrayList<>();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Groups");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                groupChatLists.clear();
                for (DataSnapshot ds: dataSnapshot.getChildren()){

                    // if current user's uid exist in participants list of group then show that group

                    if (ds.child("participants").child(firebaseAuth.getUid()).exists()){
                        GroupChatList model = ds.getValue(GroupChatList.class);
                        groupChatLists.add(model);
                    }
                }

                adapterGroupChatList = new AdapterGroupChatList(getActivity(),groupChatLists);
                groupsRv.setAdapter(adapterGroupChatList);
               // group_count.setText(" " + groupChatLists.size());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void searchGroupChatList(String query) {

        groupChatLists = new ArrayList<>();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Groups");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                groupChatLists.clear();
                for (DataSnapshot ds: dataSnapshot.getChildren()){

                    // if current user's uid exist in participants list of group then show that group

                    if (ds.child("participants").child(firebaseAuth.getUid()).exists()){

                        //search by group title

                        if (ds.child("groupTitle").toString().toLowerCase().contains(query.toLowerCase())){

                            GroupChatList model = ds.getValue(GroupChatList.class);
                            groupChatLists.add(model);
                        }
                    }
                }

                adapterGroupChatList = new AdapterGroupChatList(getActivity(),groupChatLists);
                groupsRv.setAdapter(adapterGroupChatList);
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
        menu.findItem(R.id.action_settings).setVisible(false);
        menu.findItem(R.id.action_search).setVisible(false);

        //searchview to search posts by post title/description
        MenuItem item = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(item);



        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        int id = item.getItemId();

        return super.onOptionsItemSelected(item);
    }

}
