package com.ornoiragency.chacks.Fragment;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.miguelcatalan.materialsearchview.MaterialSearchView;
import com.ornoiragency.chacks.Activity.EditProfileActivity;
import com.ornoiragency.chacks.Adapter.UserListAdapter;
import com.ornoiragency.chacks.ChatGroup.CreateGroupActivity;
import com.ornoiragency.chacks.models.UserObject;
import com.ornoiragency.chacks.R;
import com.ornoiragency.chacks.Settings.SettingsActivity;
import com.ornoiragency.chacks.models.User;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;


import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.content.Context.MODE_PRIVATE;

/**
 * A simple {@link Fragment} subclass.
 */
public class FriendsFragment extends Fragment {

    public static final String TAG = "FriendsFragment";
    private View view;
    Toolbar toolbar;

    private RecyclerView mUserList;
    ArrayList<UserObject> userList, contactList;
    private UserListAdapter userListAdapter;
    private List<User> mUsers;

     ConstraintLayout mLayout,mLayoutGroup;
    private CircleImageView mUserImage;
    private TextView mUserName,friends_count,count_friends;

    MaterialSearchView searchView;

    String mUID;
    private FirebaseAuth mAuth;
    private  String[] contactsPermission;

    public FriendsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_friends, container, false);

        mAuth = FirebaseAuth.getInstance();
        mUID = mAuth.getCurrentUser().getUid();

        toolbar = view.findViewById(R.id.toolbar);
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        activity.setSupportActionBar(toolbar);
        try
        {
            activity.getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        catch (NullPointerException e){}

        mUserList = view.findViewById(R.id.userList);
        mUserList.setHasFixedSize(true);
        mUserList.setLayoutManager(new LinearLayoutManager(getContext()));

        mUsers = new ArrayList<>();
        contactList= new ArrayList<>();
        userList= new ArrayList<>();

        mUserName = view.findViewById(R.id.user_name);
        mUserImage = view.findViewById(R.id.user_image);

        count_friends = view.findViewById(R.id.count_friends);
        friends_count = view.findViewById(R.id.friend_count);

        searchView = view.findViewById(R.id.search_view);

        mLayoutGroup = view.findViewById(R.id.constraintLayout);
        mLayout = view.findViewById(R.id.mLayout);
        mLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                openEditProfileActivity();
            }
        });

        mLayoutGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

               // RequestNewGroup();
                Intent intent = new Intent(getActivity(),CreateGroupActivity.class);
                startActivity(intent);
            }
        });

        contactsPermission = new String[]{
                Manifest.permission.READ_CONTACTS,
                Manifest.permission.WRITE_CONTACTS
        };

        if (!checkContactsPermission()){

            requestContactsPermission();

        } else {

            getUserInfo();
            getContactList();
        }


        return view;
    }

    private boolean checkContactsPermission(){
        boolean result = ContextCompat.checkSelfPermission(getActivity(),Manifest.permission.READ_CONTACTS) == (PackageManager.PERMISSION_GRANTED);

        return result;
    }

    private void requestContactsPermission(){
        ActivityCompat.requestPermissions(getActivity(),contactsPermission,2);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

                if (grantResults.length > 0){

                    boolean writeStorageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;

                    if (writeStorageAccepted){
                        Toast.makeText(getActivity(), "contact permission garanted", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        Toast.makeText(getActivity(), "contact permission required", Toast.LENGTH_SHORT).show();
                    }
                }


    }

    UserObject mUser;
    private void getUserInfo() {
        mUser = new UserObject(FirebaseAuth.getInstance().getUid());
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("user");
        reference.keepSynced(true);

                reference.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        mUser.parseObject(dataSnapshot);

                       String name = mUser.getName();
                       mUserName.setText(name);

                        Picasso.get().load(mUser.getImage()).placeholder(R.drawable.profile_image).networkPolicy(NetworkPolicy.OFFLINE).into(mUserImage, new com.squareup.picasso.Callback() {
                            @Override
                            public void onSuccess() {

                            }

                            @Override
                            public void onError(Exception e) {

                                Picasso.get().load(mUser.getImage()).placeholder(R.drawable.profile_image).into(mUserImage);
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

    public void openEditProfileActivity(){
        Intent intent = new Intent(getActivity(), EditProfileActivity.class);
        intent.putExtra("userObject", mUser);
        startActivity(intent);
    }

    private void getContactList(){

        ContentResolver cr = getActivity().getContentResolver();
        String[] FieldList = {ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone.NORMALIZED_NUMBER,ContactsContract.CommonDataKinds.Phone.CONTACT_ID};
        Cursor c = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,FieldList,
                null,null,ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
        String name,phone,ContactID;
        HashSet<String> normalizedNumbers = new HashSet<>();
        if(c!=null)
        {
            while(c.moveToNext()!=false)
            {
                phone = c.getString(c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NORMALIZED_NUMBER));
                if(normalizedNumbers.add(phone)==true)
                {

                    name = c.getString(c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                    ContactID = c.getString(c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID));
                    UserObject mContact = new UserObject(ContactID, name, phone, " ", " ","");
                    contactList.add(mContact);

                    final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                    final DatabaseReference mUserDB = FirebaseDatabase.getInstance().getReference().child("user");
                    mUserDB.keepSynced(true);
                    Query query = mUserDB.orderByChild("phone").equalTo(mContact.getPhone());
                    query.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            // mUsers.clear();

                            if (dataSnapshot.exists()) {

                                for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                                    User user = childSnapshot.getValue( User.class );
                                    if (Objects.requireNonNull(user).getUid()!= null && !user.getUid().equals( firebaseUser.getUid() )) {

                                        mUsers.add(user);

                                    }


                                }

                                userListAdapter = new UserListAdapter(getContext(),mUsers);
                                mUserList.setAdapter(userListAdapter);
                                count_friends.setText( " " + mUsers.size());
                                friends_count.setText(" " + mUsers.size());

                            }
                        }


                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });

                }

            }
            c.close();
        }

    }

    private void searchUsers(String searchQuery){

      //  mUsers = new ArrayList<>();
        ContentResolver cr = getActivity().getContentResolver();
        String[] FieldList = {ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone.NORMALIZED_NUMBER,ContactsContract.CommonDataKinds.Phone.CONTACT_ID};
        Cursor c = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,FieldList,
                null,null,ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
        String name,phone,ContactID;
        HashSet<String> normalizedNumbers = new HashSet<>();
        if(c!=null)
        {
            while(c.moveToNext()!=false)
            {
                phone = c.getString(c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NORMALIZED_NUMBER));
                if(normalizedNumbers.add(phone)==true)
                {

                    name = c.getString(c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                    ContactID = c.getString(c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID));
                    UserObject mContact = new UserObject(ContactID, name, phone, " ", " ","");
                    contactList.add(mContact);

                    final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                    final DatabaseReference mUserDB = FirebaseDatabase.getInstance().getReference().child("user");
                    Query query = mUserDB.orderByChild("phone").equalTo(mContact.getPhone());
                    query.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            // mUsers.clear();

                            if (dataSnapshot.exists()) {

                                for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                                    User user = childSnapshot.getValue( User.class );
                                    if (Objects.requireNonNull(user).getUid()!= null && !user.getUid().equals(firebaseUser.getUid())
                                            && user.getSearch().toLowerCase().contains(searchQuery.toLowerCase())) {

                                        mUsers.add(user);

                                    }


                                }

                                userListAdapter = new UserListAdapter(getContext(),mUsers);
                                mUserList.setAdapter(userListAdapter);
                                count_friends.setText( " " + mUsers.size());
                                friends_count.setText(" " + mUsers.size());

                            }
                        }


                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });

                }

            }
            c.close();
        }

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
        MenuItem item = menu.findItem(R.id.action_settings);
        //searchview to search posts by post title/description

        MenuItem search = menu.findItem(R.id.action_search);
        searchView.setMenuItem(search);

        searchView.setOnQueryTextListener(new MaterialSearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                //Do some magic
                if (!TextUtils.isEmpty(s)){
                    searchUsers(s);

                } else {
                   // getContactList();
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                //Do some magic
                if (!TextUtils.isEmpty(s)){
                    searchUsers(s);
                } else {
                 //   getContactList();
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

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        int id = item.getItemId();
        if (id==R.id.action_settings){

            startActivity(new Intent(getActivity(), SettingsActivity.class));
        }

        return super.onOptionsItemSelected(item);
    }

}
