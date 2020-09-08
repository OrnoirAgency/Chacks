package com.ornoiragency.chacks.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.VideoView;

import com.airbnb.lottie.LottieAnimationView;
import com.danikula.videocache.HttpProxyCacheServer;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.ornoiragency.chacks.R;

import static com.ornoiragency.chacks.Chacks.getProxy;

public class PlayMessageVideoActivity extends AppCompatActivity {

    //player
    VideoView videoView;
    ProgressBar progressBar;
    String videoId;
    private boolean isPlaying;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_message_video);

        Intent intent = getIntent();
        videoId = intent.getStringExtra("videoId");

        videoView = findViewById(R.id.video);
        progressBar = findViewById(R.id.progressBar);

        HttpProxyCacheServer proxy = getProxy(getApplicationContext());
        String proxyUrl = proxy.getProxyUrl(videoId);
        prepareVideoPlayer(proxyUrl);


    }

    private void prepareVideoPlayer(String proxyUrl) {

        MediaController mediaController = new MediaController(this);
        videoView.setMediaController(mediaController);
        videoView.setVideoPath(proxyUrl);
        videoView.requestFocus();
        videoView.start();
        isPlaying = true;
        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {

                LinearLayout viewGroupLevel1 = (LinearLayout)  mediaController.getChildAt(0);
                //Set your color with desired transparency here:
                viewGroupLevel1.setBackgroundColor(getResources().getColor(R.color.transparent));
                if (isPlaying){
                    progressBar.setVisibility(View.GONE);
                }

                mp.setOnInfoListener(new MediaPlayer.OnInfoListener() {
                    @Override
                    public boolean onInfo(MediaPlayer mp, int what, int extra) {
                        if (what == MediaPlayer.MEDIA_INFO_BUFFERING_START)
                            progressBar.setVisibility(View.VISIBLE);
                        if (what == MediaPlayer.MEDIA_INFO_BUFFERING_END)
                            progressBar.setVisibility(View.INVISIBLE);
                        return false;
                    }
                });
            }
        });
        videoView.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                progressBar.setVisibility(View.INVISIBLE);
                return false;
            }
        });


        videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                if(null!=videoView){
                    videoView.stopPlayback();
                    //  post_video_play.setVisibility(View.VISIBLE);
                  //  video_cover.setVisibility(View.VISIBLE);
                }


            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(null!=videoView){
            videoView.stopPlayback();

        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(null!=videoView){
            videoView.stopPlayback();

        }
    }
}