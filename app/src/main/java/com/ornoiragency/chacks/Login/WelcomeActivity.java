package com.ornoiragency.chacks.Login;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.ornoiragency.chacks.MainActivity;
import com.ornoiragency.chacks.R;

public class WelcomeActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        Thread thread = new Thread()
        {
            @Override
            public void run()
            {
                try
                {
                    sleep(1000);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }

                finally
                {
                    Intent loginIntent = new Intent(WelcomeActivity.this, MainActivity.class);
                    loginIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(loginIntent);
                    finish();
                }
            }
        };

        thread.start();
    }

    @Override
    protected void onPause()
    {
        super.onPause();

        finish();
    }
}
