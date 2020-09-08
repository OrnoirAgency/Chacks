package com.ornoiragency.chacks.Fragment;


import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.facebook.ads.AdError;
import com.facebook.ads.NativeAdsManager;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.miguelcatalan.materialsearchview.MaterialSearchView;
import com.ornoiragency.chacks.Activity.DownloadManagerActivity;
import com.ornoiragency.chacks.Adapter.AdapterPosts;
import com.ornoiragency.chacks.Notification.CreateNotification;
import com.ornoiragency.chacks.Playable;
import com.ornoiragency.chacks.Post.AddPostActivity;
import com.ornoiragency.chacks.Post.PostAudioActivity;
import com.ornoiragency.chacks.Post.PostSearchActivity;
import com.ornoiragency.chacks.Post.PostTextActivity;
import com.ornoiragency.chacks.Post.PostVideoActivity;
import com.ornoiragency.chacks.Post.PostVideoDetailActivity;
import com.ornoiragency.chacks.R;
import com.ornoiragency.chacks.Services.OnClearFromRecentService;
import com.ornoiragency.chacks.Utils.ViewAnimation;
import com.ornoiragency.chacks.models.Post;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.facebook.react.bridge.UiThreadUtil.runOnUiThread;

/**
 * A simple {@link Fragment} subclass.
 */
public class HomeFragment extends Fragment implements
        NativeAdsManager.Listener {

    public static final String TAG = "HomeFragment";
    View view;
    Toolbar toolbar;

    int position = 0;
    boolean isPlaying = false;

    private RecyclerView recyclerView;
    private AdapterPosts adapterPosts;
    private List<Post> postList;
    private NativeAdsManager mNativeAdsManager;

    ImageView searchView;

    private FloatingActionButton edit_music_btn,edit_video_btn,edit_image_btn,edit_btn,add_btn;
    private ImageView dload;

    private boolean isRotate = false;

    FirebaseUser firebaseUser;

    private static MediaPlayer mediaPlayer ;
    Runnable run;
    private Handler seekHandler = new Handler();

    private boolean firstLaunch = true;
    public boolean isClickable = true;
    private View viewDisableLayout;
    private TextView text_image,text_text,text_music,text_video;

    String audio;
    TextView artist,title;
    ImageView play_pause,close;
    ProgressBar mini_progress;
    ConstraintLayout mini_lecteur;

    public HomeFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_home, container, false);

        toolbar = view.findViewById(R.id.toolbar);
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        activity.setSupportActionBar(toolbar);

        viewDisableLayout = view.findViewById(R.id.viewDisableLayout);
        mini_lecteur = view.findViewById(R.id.mini_lecteur);
        artist = view.findViewById(R.id.artist);
        title = view.findViewById(R.id.title);
        mini_progress = view.findViewById(R.id.mini_progress);
        play_pause = view.findViewById(R.id.play_pause);
        close = view.findViewById(R.id.close);

        play_pause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                pushPlay(postList.get(position).getpAudio());
            }
        });

        play_pause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (mediaPlayer != null && mediaPlayer.isPlaying()){
                    mediaPlayer.stop();
                }
                mini_lecteur.setVisibility(View.GONE);
            }
        });

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        postList = new ArrayList<>();

        // Create some dummy post items

        String placement_id = "188086015840585_188087219173798";
        mNativeAdsManager = new NativeAdsManager(getActivity(), placement_id, 5);
        mNativeAdsManager.loadAds();
        mNativeAdsManager.setListener(this);

        recyclerView = view.findViewById(R.id.postsRecyclerview);
        recyclerView.setHasFixedSize(true);
        recyclerView.setItemViewCacheSize(20);
        recyclerView.setDrawingCacheEnabled(true);
        recyclerView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
        recyclerView.setNestedScrollingEnabled(false);

        adapterPosts = new AdapterPosts(getActivity(),postList,mNativeAdsManager);

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener(){
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy){
                if (dy<0 && !add_btn.isShown())
                    add_btn.show();
                else if(dy>0 && add_btn.isShown())
                    add_btn.hide();
            }

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }
        });

        adapterPosts.setOnItemClickListener(new AdapterPosts.OnItemClickListener() {
            @Override
            public void onItemClick(View view, RecyclerView.ViewHolder holder, Post post, int position) {


                if (post.getType().equals("video")){
                    //video(holder,post);

                } else if (post.getType().equals("audio")){

                    playAudio(post,holder);

                    audio = post.getpAudio();


                }else {

                }


            }
        });


        //initialse mediaplayer
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

        searchView = view.findViewById(R.id.search_view);
        add_btn = view.findViewById(R.id.add_btn);
        edit_btn = view.findViewById(R.id.edit_btn);
        edit_image_btn = view.findViewById(R.id.edit_image_btn);
        edit_video_btn = view.findViewById(R.id.edit_video_btn);
        edit_music_btn = view.findViewById(R.id.edit_music_btn);

        text_text = view.findViewById(R.id.text_text);
        text_image = view.findViewById(R.id.text_image);
        text_music = view.findViewById(R.id.text_music);
        text_video = view.findViewById(R.id.text_video);

        ViewAnimation.init(text_text);
        ViewAnimation.init(text_image);
        ViewAnimation.init(text_music);
        ViewAnimation.init(text_video);

        ViewAnimation.init(edit_btn);
        ViewAnimation.init(edit_image_btn);
        ViewAnimation.init(edit_video_btn);
        ViewAnimation.init(edit_music_btn);

        add_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                isRotate = ViewAnimation.rotateFab(v,!isRotate);
                adapterPosts.isClickable = false;

                if(isRotate){

                    viewDisableLayout.setVisibility(View.VISIBLE);

                    ViewAnimation.showIn(text_text);
                    ViewAnimation.showIn(text_image);
                    ViewAnimation.showIn(text_music);
                    ViewAnimation.showIn(text_video);

                    ViewAnimation.showIn(edit_btn);
                    ViewAnimation.showIn(edit_image_btn);
                    ViewAnimation.showIn(edit_video_btn);
                    ViewAnimation.showIn(edit_music_btn);

                }else{

                    viewDisableLayout.setVisibility(View.GONE);

                    ViewAnimation.showOut(text_text);
                    ViewAnimation.showOut(text_image);
                    ViewAnimation.showOut(text_music);
                    ViewAnimation.showOut(text_video);

                    ViewAnimation.showOut(edit_btn);
                    ViewAnimation.showOut(edit_image_btn);
                    ViewAnimation.showOut(edit_video_btn);
                    ViewAnimation.showOut(edit_music_btn);
                }

            }
        });



        edit_image_btn.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), AddPostActivity.class);
            startActivity(intent);

        });

        edit_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), PostTextActivity.class);
                startActivity(intent);

            }
        });

        edit_video_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), PostVideoActivity.class);
                startActivity(intent);

            }
        });

        edit_music_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), PostAudioActivity.class);
                startActivity(intent);

            }
        });

        dload = view.findViewById(R.id.dload);
        dload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              //  Intent intent = new Intent(getActivity(), DownloadManagerActivity.class);
              //  startActivity(intent);

            }
        });

        searchView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //  Intent intent = new Intent(getActivity(), PostSearchActivity.class);
                //  startActivity(intent);

            }
        });

        loadPosts();
        return view;
    }

    private void pushPlay(String getpAudio) {

        if (mediaPlayer.isPlaying() && mediaPlayer != null){

            play_pause.setImageResource(R.drawable.ic_play);
            mediaPlayer.pause();

        } else {

            if (firstLaunch){
                // Post post = postList.get(0);
              //  prepareSong(getpAudio);
            } else {

                mediaPlayer.start();
                firstLaunch = false;
            }

            play_pause.setImageResource(R.drawable.ic_pause);
        }
    }

    @Override
    public void onAdsLoaded() {

        if (getActivity() == null) {
            return;
        }
    }

    @Override
    public void onAdError(AdError adError) {

    }

    private void playAudio(Post post, RecyclerView.ViewHolder holder) {

        ((AdapterPosts.PostAudioViewHolder)holder).post_audi_play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer mp) {
                        //lancer la chanson

                        togglePlay(mp,holder,((AdapterPosts.PostAudioViewHolder)holder).post_audi_play,((AdapterPosts.PostAudioViewHolder)holder).post_audio_progress
                                ,((AdapterPosts.PostAudioViewHolder)holder).post_audio_duration);

                    }
                });


                mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mediaPlayer) {

                        ((AdapterPosts.PostAudioViewHolder)holder).post_audio_progress.setProgress(0);
                        ((AdapterPosts.PostAudioViewHolder)holder).post_audi_play.setImageResource(R.drawable.ic_play);
                        ((AdapterPosts.PostAudioViewHolder)holder).post_audio_duration.setText(milliSecondsToTimer(post.getpAudioDuration()));
                        seekHandler.removeCallbacks(run);

                    }
                });

                firstLaunch = false;
                prepareSong(post,holder);
               // CreateNotification.createNotification(getContext(), post,
                 //       R.drawable.ic_pause_black_24dp, position, postList.size()-1);
                //Controle de la chanson
                pushPlay(post,holder);
                mini_lecteur.setVisibility(View.VISIBLE);
                artist.setText(post.getArtist());
                title.setText(post.getTitle());


            }
        });


    }

    private void prepareSong(Post post, RecyclerView.ViewHolder holder){

        ((AdapterPosts.PostAudioViewHolder)holder).post_audi_load.setVisibility(View.VISIBLE);
        mini_progress.setVisibility(View.VISIBLE);

        String stream = post.getpAudio();

        mediaPlayer.reset();
        try {
            mediaPlayer.setDataSource(stream);
            mediaPlayer.prepareAsync();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void togglePlay(MediaPlayer mp, RecyclerView.ViewHolder holder, ImageView playAudio, ProgressBar progressBar, TextView
            audioDuration){

        if (mp.isPlaying()){
            mp.stop();
            mp.reset();

        } else {

            ((AdapterPosts.PostAudioViewHolder)holder).post_audi_load.setVisibility(View.GONE);
            mini_progress.setVisibility(View.GONE);

            mp.start();
            playAudio.setImageDrawable(ContextCompat.getDrawable(getActivity(),R.drawable.ic_pause));
            play_pause.setImageResource(R.drawable.ic_pause);
            seekHandler = new Handler();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    progressBar.setMax(100);
                    progressBar.setProgress((int)(((float)mediaPlayer.getCurrentPosition()/mediaPlayer.getDuration())*100));
                    audioDuration.setText(milliSecondsToTimer(mediaPlayer.getCurrentPosition()));
                    seekHandler.postDelayed(this,100);
                }
            });



        }

    }

    private void pushPlay(Post post, RecyclerView.ViewHolder holder){

        ((AdapterPosts.PostAudioViewHolder)holder).post_audi_play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (mediaPlayer.isPlaying() && mediaPlayer != null){
                    ((AdapterPosts.PostAudioViewHolder)holder).post_audi_play.setImageResource(R.drawable.ic_play);
                    play_pause.setImageResource(R.drawable.ic_play);
                    mediaPlayer.pause();
                    CreateNotification.createNotification(getContext(), post,
                            R.drawable.ic_play_arrow_black, position, postList.size()-1);
                } else {

                    if (firstLaunch){
                       // Post post = postList.get(0);
                        prepareSong(post,holder);
                    } else {
                        CreateNotification.createNotification(getContext(), post,
                                R.drawable.ic_pause, position, postList.size()-1);
                        mediaPlayer.start();
                        firstLaunch = false;
                    }
                    ((AdapterPosts.PostAudioViewHolder)holder).post_audi_play.setImageResource(R.drawable.ic_pause);
                    play_pause.setImageResource(R.drawable.ic_pause);
                }
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mediaPlayer != null && mediaPlayer.isPlaying()){
            mediaPlayer.pause();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null && mediaPlayer.isPlaying()){
            mediaPlayer.stop();
        }
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

    private void loadPosts() {

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                postList.clear();
                for (DataSnapshot ds: dataSnapshot.getChildren()){
                    Post post = ds.getValue(Post.class);

                    postList.add(post);

                }

                recyclerView.setAdapter(adapterPosts);
                adapterPosts.notifyDataSetChanged();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

                Toast.makeText(getActivity(), ""+databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    // home fragment
    public void onCreate(Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        super.onCreate(savedInstanceState);

    }


}
