package com.ornoiragency.chacks.Settings;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;
import com.ornoiragency.chacks.R;

public class NotificationActivity extends AppCompatActivity {

    SwitchCompat postSwitch;

    // to save state of switch
    SharedPreferences sp;
    SharedPreferences.Editor editor;

    private ActionBar actionBar;
    //constant for topic
    private static final String TOPIC_POST_NOTIFICATION = "POST";
    ConstraintLayout back;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        back = findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                finish();
            }
        });


        postSwitch = findViewById(R.id.postSwitch);

        sp = getSharedPreferences("Notification_SP",MODE_PRIVATE);
        boolean isPostEnabled = sp.getBoolean(""+ TOPIC_POST_NOTIFICATION,false);

        if (isPostEnabled){
            postSwitch.setChecked(true);
        }
        else {
            postSwitch.setChecked(false);
        }

        //implement switch change listener
        postSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                editor = sp.edit();
                editor.putBoolean(""+ TOPIC_POST_NOTIFICATION,isChecked);
                editor.apply();

                if (isChecked){

                    subscribePostNotification();

                } else {
                    unsubscribePostNotification();
                }
            }
        });

    }


    private void unsubscribePostNotification() {

        FirebaseMessaging.getInstance().unsubscribeFromTopic(""+TOPIC_POST_NOTIFICATION)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        String msg = getString(R.string.notif_msg_not);
                        if (!task.isSuccessful()){

                            msg = getString(R.string.msg_unsubsc);
                        }
                        Toast.makeText(NotificationActivity.this, msg, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void subscribePostNotification() {

        FirebaseMessaging.getInstance().subscribeToTopic(""+TOPIC_POST_NOTIFICATION)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        String msg = getString(R.string.notif_msg_yes);
                        if (!task.isSuccessful()){

                            msg = getString(R.string.msg_subsc);
                        }
                        Toast.makeText(NotificationActivity.this, msg, Toast.LENGTH_SHORT).show();
                    }
                });
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
