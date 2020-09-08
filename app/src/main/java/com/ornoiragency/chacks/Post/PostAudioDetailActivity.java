package com.ornoiragency.chacks.Post;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.airbnb.lottie.LottieAnimationView;
import com.android.volley.AuthFailureError;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.dynamiclinks.ShortDynamicLink;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.gson.Gson;
import com.ornoiragency.chacks.Adapter.AdapterComment;
import com.ornoiragency.chacks.Adapter.AdapterPosts;
import com.ornoiragency.chacks.Notification.Data;
import com.ornoiragency.chacks.Notification.Sender;
import com.ornoiragency.chacks.Notification.Token;
import com.ornoiragency.chacks.PageTransformer;
import com.ornoiragency.chacks.R;
import com.ornoiragency.chacks.models.Comment;
import com.ornoiragency.chacks.models.Post;
import com.vanniktech.emoji.EmojiEditText;
import com.vanniktech.emoji.EmojiPopup;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.facebook.react.bridge.UiThreadUtil.runOnUiThread;


public class PostAudioDetailActivity extends AppCompatActivity {

    // detail of user post
    String myUid, myName,myDp,pAudio,postId,pLikes,hisDp,hisName;
    String hisUid,cover;

    ImageView uPictureIv;
    TextView uNameTv,pTimeTv,pLikesTv,pCommentTv;
    ImageView likeBtn,shareBtn;

    //volley request for notification
    private RequestQueue requestQueue;
    boolean notify = false;

    //add comment views
    ImageButton sendBtn;
    CircleImageView cAvatarIv;

    //player
    VideoView videoView;
    LottieAnimationView bufferProgress;

    private boolean isPlaying;

    private ActionBar actionBar;
    ProgressDialog pd;
    boolean mProcessComment = false;
    boolean mProcessLike = false;
    int countLikes;

    RecyclerView recyclerView;
    List<Comment> commentList;
    AdapterComment adapterComment;

    EmojiEditText commentEt;
    EmojiPopup emojiPopup;
    ImageButton emojiButton;
    View rootView;

    ImageView playPauseBtn,audio_cover;
    TextView currentTime,totalDurationTime;
    SeekBar seekBar;
    ProgressBar progressBar;

    MediaPlayer mediaPlayer;
    Handler seekHandler;

    String TAG = null;
    private boolean firstLaunch = true;
    Runnable run;

    ConstraintLayout back;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_audio_detail);

        back = findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                finish();
            }
        });

        requestQueue = Volley.newRequestQueue(getApplicationContext());

        playPauseBtn = findViewById(R.id.audio_play_pause);
        currentTime = findViewById(R.id.currentTime);
        totalDurationTime = findViewById(R.id.totalDurationTime);
        seekBar = findViewById(R.id.seekBar);
        progressBar = findViewById(R.id.progressBar);
        audio_cover = findViewById(R.id.audio_cover);
        shareBtn = findViewById(R.id.shareBtn);

        // get id of post using intent
        Intent intent = getIntent();
        postId = intent.getStringExtra("postId");

        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        myUid = firebaseAuth.getCurrentUser().getUid();

        recyclerView = findViewById(R.id.recyclerview);
        videoView = findViewById(R.id.pVideo_video);
        bufferProgress = findViewById(R.id.bufferProgress);

        //initialse mediaplayer
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {

                togglePlay(mediaPlayer);
            }
        });

        seekBar.setMax(100);

        playPauseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playAudio();
            }
        });

        seekBar.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                SeekBar seekBar = (SeekBar) v;
                int playPosition = (mediaPlayer.getDuration()/100)*seekBar.getProgress();
                mediaPlayer.seekTo(playPosition);
                currentTime.setText(milliSecondsToTimer(mediaPlayer.getCurrentPosition()));
                return false;
            }
        });

        //init views
        uPictureIv = findViewById(R.id.uPictureIv_audio);
        uNameTv = findViewById(R.id.uNameTv_audio);
        pTimeTv = findViewById(R.id.pTimeTv_audio);
        likeBtn = findViewById(R.id.likeBtn_audio);
        pLikesTv = findViewById(R.id.pLikesTv_audio);
        pCommentTv = findViewById(R.id.pCommentTv_audio);

        //init add views
        rootView = findViewById(R.id.root_view);
        commentEt = findViewById(R.id.commentEt);
        sendBtn = findViewById(R.id.sendBtn);
        cAvatarIv = findViewById(R.id.cAvatarIv);

        emojiButton = findViewById(R.id.main_activity_emoji);
        emojiButton.setColorFilter(ContextCompat.getColor(this, R.color.icon_checked), PorterDuff.Mode.SRC_IN);
        emojiButton.setOnClickListener(ignore -> emojiPopup.toggle());
        setUpEmojiPopup();

        loadPostInfo();
        loadUserInfo();
        setLikes();
        loadComments();
        //send Comment
        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                notify = true;
                postComment();
                commentEt.setText("");
            }
        });

        likeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                notify = true;
                likePost();
            }
        });


        pLikesTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(PostAudioDetailActivity.this, PostLikedByActivity.class);
                intent.putExtra("postId",postId);
                startActivity(intent);
            }
        });


    }

    private void shortLink(String pAudio) {

        Task<ShortDynamicLink> dynamicLinkUri = FirebaseDynamicLinks.getInstance().createDynamicLink()
                .setLink(Uri.parse(pAudio))
                .setDynamicLinkDomain("chacks.page.link")
                .buildShortDynamicLink()
                .addOnCompleteListener(new OnCompleteListener<ShortDynamicLink>() {
                    @Override
                    public void onComplete(@NonNull Task<ShortDynamicLink> task) {

                        if (task.isSuccessful()) {
                            // Short link created
                            Uri shortLink = task.getResult().getShortLink();
                            Uri flowchartLink = task.getResult().getPreviewLink();
                            Intent intent = new Intent();
                            intent.setAction(Intent.ACTION_SEND);
                            intent.putExtra(Intent.EXTRA_TEXT,  shortLink.toString());
                            intent.setType("text/plain");
                            startActivity(Intent.createChooser(intent, "Share audio!"));
                        } else {
                            // Error
                            // ...
                        }
                    }
                });

    }

    private void playAudio( ) {

                mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mediaPlayer) {

                        seekBar.setProgress(0);
                        playPauseBtn.setImageResource(R.drawable.ic_play);
                        currentTime.setText("00");
                        seekHandler.removeCallbacks(run);

                    }
                });

                firstLaunch = false;
                prepareSong();

                //Controle de la chanson
                pushPlay();


    }
    private void prepareSong(){

        progressBar.setVisibility(View.VISIBLE);

        mediaPlayer.reset();
        try {
            mediaPlayer.setDataSource(pAudio);
            mediaPlayer.prepareAsync();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void togglePlay(MediaPlayer mp){

        if (mp.isPlaying()){
            mp.stop();
            mp.reset();

        } else {

            progressBar.setVisibility(View.GONE);
            bufferProgress.setVisibility(View.VISIBLE);

            mp.start();
            playPauseBtn.setImageDrawable(ContextCompat.getDrawable(this,R.drawable.ic_pause));
            seekHandler = new Handler();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    seekBar.setMax(100);
                    seekBar.setProgress((int)(((float)mediaPlayer.getCurrentPosition()/mediaPlayer.getDuration())*100));
                    currentTime.setText(milliSecondsToTimer(mediaPlayer.getCurrentPosition()));
                    seekHandler.postDelayed(this,100);
                }
            });



        }

    }

    private void pushPlay(){

        playPauseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (mediaPlayer.isPlaying() && mediaPlayer != null){
                    playPauseBtn.setImageResource(R.drawable.ic_play);
                    mediaPlayer.pause();
                    bufferProgress.setVisibility(View.GONE);
                } else {

                    if (firstLaunch){
                        // Post post = postList.get(0);
                        prepareSong();
                    } else {

                        mediaPlayer.start();
                        firstLaunch = false;
                    }
                    playPauseBtn.setImageResource(R.drawable.ic_pause);
                    bufferProgress.setVisibility(View.VISIBLE);
                }
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

    @Override
    protected void onPause() {
        super.onPause();
        if (mediaPlayer.isPlaying()){
            mediaPlayer.pause();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!mediaPlayer.isPlaying()){
            mediaPlayer.start();
        }

    }

    @Override
    protected void onStop() {
        super.onStop();

        if (mediaPlayer.isPlaying()){
            mediaPlayer.stop();
        }
    }

    private void addToHisNotification(String hisUid, String pId, String notification){

        String timestamp = ""+System.currentTimeMillis();

        HashMap<Object,String> hashMap = new HashMap<>();
        hashMap.put("pId",pId);
        hashMap.put("timestamp",timestamp);
        hashMap.put("pUid",hisUid);
        hashMap.put("notification",notification);
        hashMap.put("sUid",myUid);
        hashMap.put("type","audio");


        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("user");
        ref.child(hisUid).child("Notifications").child(timestamp).setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                });
    }


    private void loadComments() {

        LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(layoutManager);

        commentList = new ArrayList<>();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts").child(postId).child("Comments");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                commentList.clear();
                for (DataSnapshot ds: dataSnapshot.getChildren()){

                    Comment comment = ds.getValue(Comment.class);
                    commentList.add(comment);
                    adapterComment = new AdapterComment(getApplicationContext(),commentList,myUid,postId);
                    recyclerView.setAdapter(adapterComment);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    private void setLikes() {
        DatabaseReference likesRef = FirebaseDatabase.getInstance().getReference().child("Likes");

        likesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.child(postId).hasChild(myUid)){
                    //user has liked this post
                    countLikes = (int) dataSnapshot.child(postId).getChildrenCount();
                    likeBtn.setImageResource(R.drawable.ic_liked__home_btn);
                    pLikesTv.setText((countLikes +(" "+ getApplicationContext().getString(R.string.like))));
                } else {
                    //user has not liked this post
                    countLikes = (int) dataSnapshot.child(postId).getChildrenCount();
                    likeBtn.setImageResource(R.drawable.ic_like_home_icon);
                    pLikesTv.setText((countLikes +(" "+ getApplicationContext().getString(R.string.like))));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void likePost() {

        mProcessLike = true;

        DatabaseReference likesRef = FirebaseDatabase.getInstance().getReference().child("Likes");

        likesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                likesRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        if (mProcessLike){
                            // already liked, remove like
                            if (dataSnapshot.child(postId).hasChild(myUid)){

                                likesRef.child(postId).child(myUid).removeValue();
                                mProcessLike = false;
                            }
                            else {
                                //not like, like it

                                likesRef.child(postId).child(myUid).setValue(true);
                                mProcessLike = false;

                                String msg = getString(R.string.liked);

                                if (notify) {
                                    sendNotifiaction(hisUid, myName,myDp, msg);
                                }
                                notify = false;

                                addToHisNotification(""+myUid,""+postId,getString(R.string.liked));
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    private void postComment() {
        pd = new ProgressDialog(this);
        pd.setMessage("adding comment");

        String comment = commentEt.getText().toString().trim();
        if (TextUtils.isEmpty(comment)){
            Toast.makeText(this, "comment empty...", Toast.LENGTH_SHORT).show();
            return;
        }

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts").child(postId).child("Comments");
        String timestamp = String.valueOf(System.currentTimeMillis());

        HashMap<String,Object> hashMap = new HashMap<>();
        hashMap.put("cId",timestamp);
        hashMap.put("timestamp",timestamp);
        hashMap.put("comment",comment);
        hashMap.put("uid",myUid);
        hashMap.put("uDp",myDp);
        hashMap.put("uName",myName);

        final String msg = comment;

        ref.child(timestamp).setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        pd.dismiss();
                        Toast.makeText(PostAudioDetailActivity.this, "comment added", Toast.LENGTH_SHORT).show();
                        if (notify) {
                            sendNotifiaction(hisUid, myName,myDp, msg);
                        }
                        notify = false;
                        updateCommentCount();

                        addToHisNotification(""+hisUid,""+postId,getString(R.string.commentOn));
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        pd.dismiss();
                        Toast.makeText(PostAudioDetailActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void sendNotifiaction(String receiver, final String username,final String image , final String message){
        DatabaseReference tokens = FirebaseDatabase.getInstance().getReference("Tokens");
        Query query = tokens.orderByKey().equalTo(receiver);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Token token = snapshot.getValue(Token.class);
                    Data data = new Data(""+myUid,
                            ""+image,
                            R.drawable.ic_notif,
                            ""+ username+": "+message,
                            getString(R.string.comment_on),
                            ""+postId,
                            ""+ hisUid,
                            "CommentNotification",
                            ""+"audio");

                    Sender sender = new Sender(data, token.getToken());

                    //fcm json object request
                    try {
                        JSONObject senderJsonObj = new JSONObject(new Gson().toJson(sender));
                        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest("https://fcm.googleapis.com/fcm/send", senderJsonObj,
                                new Response.Listener<JSONObject>() {
                                    @Override
                                    public void onResponse(JSONObject response) {

                                        //response of the request
                                        Log.d("JSON_RESPONSE","onResponse:" +response.toString());
                                    }
                                }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Log.d("JSON_RESPONSE","onResponse:" +error.toString());
                            }
                        }){
                            @Override
                            public Map<String, String> getHeaders() throws AuthFailureError {

                                // put params
                                Map<String,String> headers = new HashMap<>();
                                headers.put("Content_Type","application/json");
                                headers.put("Authorization","key=AAAADfNhaqg:APA91bFeZpxj66QdTFuDtj7jwhNGEiV4S5lLuJZptdNsjquLA9X8VtyW5FX8_i6akiERnAKxD4RK41QUbMi6N1WPNpz-augXR52uyXpZl6ChskzzQFEb_8PdBMV-4i1ozfCmgCq27YCd");

                                return headers;
                            }
                        };

                        //add this request to queue
                        requestQueue.add(jsonObjectRequest);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void updateCommentCount() {

        mProcessComment = true;
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts").child(postId);
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (mProcessComment){
                    String comments = ""+ dataSnapshot.child("pComments").getValue();
                    int newCommentVal = Integer.parseInt(comments) + 1;
                    ref.child("pComments").setValue(""+ newCommentVal);
                    mProcessComment = false;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void loadUserInfo() {

        Query myRef = FirebaseDatabase.getInstance().getReference("user");
        myRef.orderByChild("uid").equalTo(myUid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for (DataSnapshot ds: dataSnapshot.getChildren()){

                    myName = ""+ ds.child("name").getValue();
                    myDp = ""+ ds.child("image").getValue();

                    Glide.with(getApplicationContext())
                            .asBitmap()
                            .load(myDp)
                            .placeholder(R.drawable.profile_image)
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .into(cAvatarIv);
                }



            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
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
                    pAudio = ""+ ds.child("pAudio").getValue();
                    pLikes = ""+ ds.child("pLikes").getValue();
                    String pTimestamp = ""+ ds.child("pTime").getValue();
                    hisDp = ""+ ds.child("uImage").getValue();
                    hisUid = ""+ ds.child("uid").getValue();
                    hisName = ""+ ds.child("uName").getValue();
                    cover = ""+ ds.child("pAudioCover").getValue();
                    String audioDuration = ""+ ds.child("pAudioDuration").getValue();
                    String commentCount = ""+ ds.child("pComments").getValue();

                    totalDurationTime.setText(milliSecondsToTimer(Long.parseLong(audioDuration)));

                    //convert timestamp
                    Calendar cal = Calendar.getInstance(Locale.FRENCH);
                    cal.setTimeInMillis(Long.parseLong(pTimestamp));
                    String pTime = DateFormat.format("dd/MM/yyyy HH:mm ",cal).toString();

                    uNameTv.setText(hisName);
                    pLikesTv.setText(pLikes + getString(R.string.like));
                    pTimeTv.setText(pTime);
                    pCommentTv.setText(commentCount +" "+ getString(R.string.comment));

                    Glide.with(getApplicationContext())
                            .asBitmap()
                            .load(cover)
                            .placeholder(R.drawable.profile_image)
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .into(audio_cover);

                    //set User image
                    Glide.with(getApplicationContext())
                            .asBitmap()
                            .load(hisDp)
                            .placeholder(R.drawable.profile_image)
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .into(uPictureIv);

                    shareBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            shortLink(pAudio);

                        }
                    });

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void setUpEmojiPopup() {
        emojiPopup = EmojiPopup.Builder.fromRootView(rootView)
                .setOnEmojiBackspaceClickListener(ignore -> Log.d(TAG, "Clicked on Backspace"))
                .setOnEmojiClickListener((ignore, ignore2) -> Log.d(TAG, "Clicked on emoji"))
                .setOnEmojiPopupShownListener(() -> emojiButton.setImageResource(R.drawable.ic_keyboard))
                .setOnSoftKeyboardOpenListener(ignore -> Log.d(TAG, "Opened soft keyboard"))
                .setOnEmojiPopupDismissListener(() -> emojiButton.setImageResource(R.drawable.ic_emoticon))
                .setOnSoftKeyboardCloseListener(() -> Log.d(TAG, "Closed soft keyboard"))
                .setKeyboardAnimationStyle(R.style.emoji_fade_animation_style)
                .setPageTransformer(new PageTransformer())
                .build(commentEt);
    }


}
