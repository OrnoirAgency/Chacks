package com.ornoiragency.chacks.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.ornoiragency.chacks.Post.PostAudioDetailActivity;
import com.ornoiragency.chacks.R;

import de.hdodenhof.circleimageview.CircleImageView;

public class DownloadSongActivity extends AppCompatActivity {

    String postId,song,song_name,song_artist,song_cover;

    CircleImageView songCover;
    TextView songName,songArtist,songTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download_song);

        // get id of post using intent
        Intent intent = getIntent();
        postId = intent.getStringExtra("postId");

        songName = findViewById(R.id.post_audio_title);
        songArtist = findViewById(R.id.post_audio_artist);
        songCover = findViewById(R.id.post_audio_cover);
        songTime = findViewById(R.id.post_audio_duration);

        loadPostInfo();

    }

    private void loadPostInfo() {

        //get post using id of post
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
        Query query = ref.orderByChild("pId").equalTo(postId);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //keep checking post until get the required post
                for (DataSnapshot ds: dataSnapshot.getChildren()){
                    song = ""+ ds.child("pAudio").getValue();
                    song_name = ""+ ds.child("title").getValue();
                    song_artist = ""+ ds.child("artist").getValue();
                    song_cover = ""+ ds.child("pAudioCover").getValue();

                    long song_time = Long.parseLong(""+ ds.child("pAudioDuration").getValue());

                    songTime.setText(milliSecondsToTimer(song_time));

                    songName.setText(song_name);
                    songArtist.setText(song_artist);

                    Glide.with(DownloadSongActivity.this)
                            .asBitmap()
                            .load(song_cover)
                            .placeholder(R.drawable.profile_image)
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .into(songCover);


                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private String milliSecondsToTimer(long milliseconds) {
        String finalTimerString = "";
        String secondsString = "";

        // Convert total duration into time
        int hours = (int) (milliseconds / (1000 * 60 * 60));
        int minutes = (int) (milliseconds % (1000 * 60 * 60)) / (1000 * 60);
        int seconds = (int) ((milliseconds % (1000 * 60 * 60)) % (1000 * 60) / 1000);
        // Add hours if there
        if (hours > 0) {
            finalTimerString = hours + ":";
        }

        // Prepending 0 to seconds if it is one digit
        if (seconds < 10) {
            secondsString = "0" + seconds;
        } else {
            secondsString = "" + seconds;
        }

        finalTimerString = finalTimerString + minutes + ":" + secondsString;

        // return timer string
        return finalTimerString;
    }

}