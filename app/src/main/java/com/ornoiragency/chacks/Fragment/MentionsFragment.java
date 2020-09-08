package com.ornoiragency.chacks.Fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ornoiragency.chacks.Adapter.AdapterNotification;
import com.ornoiragency.chacks.R;
import com.ornoiragency.chacks.Utils.DividerItemDecoration;
import com.ornoiragency.chacks.models.Notification;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 */
public class MentionsFragment extends Fragment {

    View view;

    RecyclerView notificationRv;
    FirebaseAuth firebaseAuth;

    private ArrayList<Notification> notificationList;
    private AdapterNotification adapterNotification;

    public MentionsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        view = inflater.inflate(R.layout.fragment_mentions, container, false);

        notificationRv = view.findViewById(R.id.notificationRv);

        RecyclerView.ItemDecoration itemDecoration = new DividerItemDecoration(getActivity().getApplicationContext(),
                DividerItemDecoration.VERTICAL_LIST);
        notificationRv.addItemDecoration(itemDecoration);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        //show new post first, for this load from last
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);
        notificationRv.setLayoutManager(layoutManager);

        firebaseAuth = FirebaseAuth.getInstance();

        getAllNotifications();

        return view;

    }


    private void getAllNotifications() {
        notificationList = new ArrayList<>();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("user");
        ref.child(firebaseAuth.getUid()).child("Notifications")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        notificationList.clear();
                        for (DataSnapshot ds: dataSnapshot.getChildren()){

                            Notification model = ds.getValue(Notification.class);
                            if (!firebaseAuth.getUid().equals(model.getsUid())){
                                notificationList.add(model);
                            }

                        }

                        //adapter
                        adapterNotification = new AdapterNotification(getActivity(),notificationList);
                        notificationRv.setAdapter(adapterNotification);

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

}
