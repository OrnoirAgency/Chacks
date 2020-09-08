package com.ornoiragency.chacks;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ornoiragency.chacks.Fragment.ChatFragment;
import com.ornoiragency.chacks.Fragment.FriendsFragment;
import com.ornoiragency.chacks.Fragment.GroupFragment;
import com.ornoiragency.chacks.Fragment.HomeFragment;
import com.ornoiragency.chacks.Fragment.NotificationsFragment;
import com.ornoiragency.chacks.Login.PhoneNumberActivity;
import com.ornoiragency.chacks.Login.SetupActivity;
import com.ornoiragency.chacks.models.UserObject;

import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;


import java.util.HashMap;


public class MainActivity extends AppCompatActivity {

    private DatabaseReference reference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Fresco.initialize(this);

        reference = FirebaseDatabase.getInstance().getReference().child("user");
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null){
            Intent intent = new Intent(MainActivity.this, PhoneNumberActivity.class);
            startActivity(intent);
            finish();

        } else {

            checkUserExistence();
        }


        if(PermissionCheck.readAndWriteExternalStorage(this)){}


        BottomNavigationView navigation = findViewById(R.id.bottom_navigation_view);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationListener);

        if (savedInstanceState == null) {
            showFragment(HomeFragment.TAG);
        }

      //  getPermissions();
       // getUserInfo();
        AppEventsLogger logger = AppEventsLogger.newLogger(this);
        logSentFriendRequestEvent(logger);

    }

    // permission
    public static class PermissionCheck{
        public static boolean readAndWriteExternalStorage(Context context) {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
                return false;
            } else {
                return true;
            }
        }
    }

    /**
     * This function assumes logger is an instance of AppEventsLogger and has been
     * created using AppEventsLogger.newLogger() call.
     * @param logger
     */
    public void logSentFriendRequestEvent(AppEventsLogger logger) {
        logger.logEvent("sentFriendRequest");
    }

    private final BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.nav_home:
                    showFragment(HomeFragment.TAG);
                    return true;
                case R.id.nav_notifications:
                    showFragment(NotificationsFragment.TAG);
                    return true;
                case R.id.nav_chat:
                    showFragment(ChatFragment.TAG);
                    return true;
                case R.id.nav_group:
                    showFragment(GroupFragment.TAG);
                    return true;
                case R.id.nav_friends:
                    showFragment(FriendsFragment.TAG);
                    return true;
            }
            return false;
        }
    };


    private void showFragment(@NonNull String tag) {
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(tag);
        if (fragment == null) {
            switch (tag) {
                case NotificationsFragment.TAG: {
                    fragment = new NotificationsFragment();
                    break;
                }
                case ChatFragment.TAG: {
                    fragment = new ChatFragment();
                    break;
                }
                case GroupFragment.TAG: {
                    fragment = new GroupFragment();
                    break;
                }
                case FriendsFragment.TAG: {
                    fragment = new FriendsFragment();
                    break;
                }
                default: {
                    fragment = new HomeFragment();
                    break;
                }
            }
        }

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.container, fragment, tag)
                .addToBackStack(tag)
                .commit();

    }

    private void getPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{Manifest.permission.WRITE_CONTACTS, Manifest.permission.READ_CONTACTS}, 2);
        }

    }

    private void checkUserExistence() {

        final String current_user_id = FirebaseAuth.getInstance().getCurrentUser().getUid();

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (!dataSnapshot.hasChild(current_user_id)){
                    sendUserToSetup();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void sendUserToSetup() {

        Intent intent = new Intent(MainActivity.this, SetupActivity.class);
        startActivity(intent);
        finish();
        return;
    }



}
