package com.ornoiragency.chacks.Settings;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SwitchCompat;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.ornoiragency.chacks.Activity.EditProfileActivity;
import com.ornoiragency.chacks.Chacks;
import com.ornoiragency.chacks.R;
import com.ornoiragency.chacks.models.UserObject;

public class SettingsActivity extends AppCompatActivity{

    TextView card_theme,card_notif,card_profil,card_about;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        getUserInfo();
        card_theme = findViewById(R.id.card_theme);
        card_theme.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(SettingsActivity.this,ThemeActivity.class);
                startActivity(intent);
                return;
            }
        });

        card_notif = findViewById(R.id.card_notif);
        card_notif.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(SettingsActivity.this,NotificationActivity.class);
                startActivity(intent);
                return;
            }
        });
        card_profil = findViewById(R.id.card_profil);
        card_profil.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                openEditProfileActivity();
            }
        });

        card_about = findViewById(R.id.card_about);
        card_about.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(SettingsActivity.this, AboutActivity.class);
                startActivity(intent);
                return;
            }
        });


    }

    UserObject mUser;
    private void getUserInfo() {
        mUser = new UserObject(FirebaseAuth.getInstance().getUid());
        FirebaseDatabase.getInstance().getReference()
                .child("user")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        mUser.parseObject(dataSnapshot);
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
        Intent intent = new Intent(SettingsActivity.this, EditProfileActivity.class);
        intent.putExtra("userObject", mUser);
        startActivity(intent);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        menu.findItem(R.id.action_logout).setVisible(false);
        menu.findItem(R.id.action_groupinfo).setVisible(false);
        menu.findItem(R.id.action_settings).setVisible(false);
        menu.findItem(R.id.action_search).setVisible(false);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }

}
