package com.ornoiragency.chacks.Activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;


import com.bumptech.glide.Glide;
import com.cometchat.pro.helpers.Logger;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.gson.Gson;

import com.ornoiragency.chacks.Adapter.AdapterChat;
import com.ornoiragency.chacks.Adapter.AdapterPosts;
import com.ornoiragency.chacks.CustomView.RecordAudio;
import com.ornoiragency.chacks.CustomView.RecordMicButton;
import com.ornoiragency.chacks.Helper.CCPermissionHelper;
import com.ornoiragency.chacks.Helper.RecordListener;
import com.ornoiragency.chacks.Notification.Data;
import com.ornoiragency.chacks.Notification.Sender;
import com.ornoiragency.chacks.Notification.Token;
import com.ornoiragency.chacks.PageTransformer;
import com.ornoiragency.chacks.R;

import com.ornoiragency.chacks.Utils.FileUtils;
import com.ornoiragency.chacks.Utils.MediaUtils;
import com.ornoiragency.chacks.Utils.Utility;
import com.ornoiragency.chacks.models.Chat;
import com.ornoiragency.chacks.models.Post;
import com.ornoiragency.chacks.models.User;
import com.vanniktech.emoji.EmojiPopup;


import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import de.hdodenhof.circleimageview.CircleImageView;
import me.alvince.android.avatarimageview.AvatarImageView;



public class ChatActivity extends AppCompatActivity implements View.OnClickListener{

    public static String[] RECORD_PERMISSION = {CCPermissionHelper.REQUEST_PERMISSION_RECORD_AUDIO,
            CCPermissionHelper.REQUEST_PERMISSION_WRITE_EXTERNAL_STORAGE};

    public static final int RECORD_CODE = 22;
    private String audioFileNamewithPath;

    private  String[] cameraPermission;
    private  String[] storagePermission;

    private RecordAudio recordAudioLayout;
    private MediaRecorder mediaRecorder;
    private RecordMicButton recordMicButton;

    static final String TAG = "ChatActivity";
    private static final int VIDEO_REQUEST = 1000;
    private Uri fileUri,fileUriAudio,videoUri,imageUri;

    AdapterChat adapterChat;
    ArrayList<Chat> chatList = new ArrayList<>();

    ConstraintLayout back,checkUser;

    private RecyclerView chatRv;
    private String userid,fUser;
    FirebaseUser fuser;
    DatabaseReference reference,ref;
    ValueEventListener seenListener;

    private Toolbar toolbar;
    private TextView TitleTv;
    private AvatarImageView IconIv;
    private ImageButton attachBtn,sendBtn,camera;
    private EditText messageEt;
    private TextView userLastSeen;

    private LinearLayout attachmentLayout;
    private boolean isHidden = true;
    CircleImageView btnDocument,btnCamera,btnGallery,btnAudio;

    //volley request for notification
    private RequestQueue requestQueue;
    boolean notify = false;
    private ProgressDialog pd;

    private static MediaPlayer mediaPlayer ;
    Runnable run;
    private Handler seekHandler = new Handler();
    String durationStr;
    ProgressBar progressBar;
    private boolean firstLaunch = true;

    EmojiPopup emojiPopup;
    RelativeLayout rootView;
    ImageButton emojiButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        initialiseViews();
        cameraPermission = new String[]{
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        };

        storagePermission = new String[]{
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        };


        requestQueue = Volley.newRequestQueue(getApplicationContext());

        pd = new ProgressDialog(this);
        progressBar = findViewById(R.id.progressBar);

        //initialse mediaplayer
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);


        chatRv = findViewById(R.id.chatRv);
        final LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);
        chatRv.setLayoutManager(mLayoutManager);
        mLayoutManager.setStackFromEnd(true);
        chatRv.setItemViewCacheSize(20);
        chatRv.setDrawingCacheEnabled(true);
        chatRv.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
        chatRv.setNestedScrollingEnabled(false);
        chatRv.getItemAnimator().setChangeDuration(0);
        adapterChat = new AdapterChat(ChatActivity.this, chatList);
        adapterChat.notifyDataSetChanged();

        adapterChat.setOnLongItemClickListener(new AdapterChat.OnLOngItemClickListener() {
            @Override
            public void onLongItemClick(View view, AdapterChat.holderChat holder, Chat chat, int position) {

                String message = chat.getMessage();
                  showDialog(chat,message);
            }
        });

        adapterChat.setOnItemClickListener(new AdapterChat.OnItemClickListener() {
            @Override
            public void onItemClick(View view, AdapterChat.holderChat holder, Chat chat, int position) {

                        Toast.makeText(ChatActivity.this, chat.getMessage(), Toast.LENGTH_SHORT).show();

                        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                            @Override
                            public void onPrepared(MediaPlayer mp) {
                                //lancer la note vocal
                                togglePlay(mp,holder.playVoice,holder.progressBar,holder.voiceTimer,holder);

                            }
                        });

                        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                            @Override
                            public void onCompletion(MediaPlayer mediaPlayer) {

                                progressBar.setProgress(0);
                                holder.playVoice.setImageResource(R.drawable.ic_play);
                                holder.voiceTimer.setText(milliSecondsToTimer(Long.parseLong(chat.getAudioDuration())));
                                seekHandler.removeCallbacks(run);

                            }
                        });

                        firstLaunch = false;
                        prepareSong(chat,holder);
                        //Controle de la note vocal
                        pushPlay(chat,holder);

            }
        });

        Intent intent = getIntent();
        userid = intent.getStringExtra("user_id");

        fuser = FirebaseAuth.getInstance().getCurrentUser();
        fUser = fuser.getUid();

        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                notify = true;
                String message = messageEt.getText().toString().trim();
                if (TextUtils.isEmpty(message)){

                    Toast.makeText(ChatActivity.this, "can't sen empty message", Toast.LENGTH_SHORT).show();
                } else {

                    sendMessage(fuser.getUid(), userid,message);
                }
            }
        });

        reference = FirebaseDatabase.getInstance().getReference("user").child(userid);
        reference.keepSynced(true);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                TitleTv.setText(user.getName());
                if (user.getImage().equals("default")){
                    IconIv.setImageResource(R.mipmap.ic_launcher);
                } else {
                    //and this
                    Glide.with(getApplicationContext())
                            .asBitmap()
                            .load(user.getImage())
                            .placeholder(R.drawable.profile_image)
                            .into(IconIv);
                }

                String typingStatus = ""+ dataSnapshot.child("typingTo").getValue();

                //check typing status
                if (typingStatus.equals(fuser.getUid())){
                    userLastSeen.setText("typing...");
                } else {
                    //check user status
                    String onlineStatus = ""+ dataSnapshot.child("onlineStatus").getValue();
                    if (onlineStatus.equals("online")){
                        userLastSeen.setText(onlineStatus);
                    } else {

                        // convert to timestamp
                        Calendar cal = Calendar.getInstance(Locale.FRENCH);
                        cal.setTimeInMillis(Long.parseLong(onlineStatus));
                        String dateTime = DateFormat.format("dd MMMM,yy HH:mm",cal).toString();
                        userLastSeen.setText(dateTime);
                    }
                }

                // loadMessage(fuser.getUid(), userid);
                loadMessageAdapter();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        //check edit text
        messageEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                if (s.toString().trim().length() == 0){
                    chekTypingStatus("noOne");
                } else {
                    chekTypingStatus(userid);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });


        seenMessage(userid);
        setUpEmojiPopup();
    }

    private void prepareSong(Chat chat,AdapterChat.holderChat holder){

        String stream = chat.getAudioFile();
        holder.voiceLoding.setVisibility(View.VISIBLE);

        mediaPlayer.reset();
        try {
            mediaPlayer.setDataSource(stream);
            mediaPlayer.prepareAsync();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void togglePlay(MediaPlayer mp, ImageView playVoice, ProgressBar progressBar, TextView audioDuration, AdapterChat.holderChat holder){

        if (mp.isPlaying()){
            mp.stop();
            mp.reset();

        } else {
            mp.start();
            playVoice.setImageDrawable(ContextCompat.getDrawable(this,R.drawable.ic_pause));
            holder.voiceLoding.setVisibility(View.GONE);

            run = new Runnable() {
                @Override
                public void run() {

                    progressBar.setMax(100);
                    progressBar.setProgress((int)(((float)mediaPlayer.getCurrentPosition()/mediaPlayer.getDuration())*100));

                    // Updateing SeekBar every 100 miliseconds

                    ChatActivity.this.seekHandler.postDelayed(run, 100);
                    //For Showing time of audio(inside runnable)
                    int miliSeconds = mediaPlayer.getCurrentPosition();
                    if(miliSeconds!=0) {
                        //if audio is playing, showing current time;
                        long minutes = TimeUnit.MILLISECONDS.toMinutes(miliSeconds);
                        long seconds = TimeUnit.MILLISECONDS.toSeconds(miliSeconds);
                        if (minutes == 0) {
                            audioDuration.setText("0:" + seconds + "/" +calculateDuration(mediaPlayer.getDuration()));
                        } else {
                            if (seconds >= 60) {
                                long sec = seconds - (minutes * 60);
                                audioDuration.setText(minutes + ":" + sec+ "/" +calculateDuration(mediaPlayer.getDuration()));
                            }
                        }
                    }else{
                        //Displaying total time if audio not playing
                        int totalTime=mediaPlayer.getDuration();
                        long minutes = TimeUnit.MILLISECONDS.toMinutes(totalTime);
                        long seconds = TimeUnit.MILLISECONDS.toSeconds(totalTime);
                        if (minutes == 0) {
                            audioDuration.setText("0:" + seconds);
                        } else {
                            if (seconds >= 60) {
                                long sec = seconds - (minutes * 60);
                                audioDuration.setText(minutes + ":" + sec);
                            }
                        }
                    }
                }
            };
            run.run();


        }

    }

    private String calculateDuration(int duration) {
        String finalDuration = "";
        long minutes = TimeUnit.MILLISECONDS.toMinutes(duration);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(duration);
        if (minutes == 0) {
            finalDuration = "0:" + seconds;
        } else {
            if (seconds >= 60) {
                long sec = seconds - (minutes * 60);
                finalDuration = minutes + ":" + sec;
            }
        }
        return finalDuration;
    }

    private void pushPlay(Chat chat, AdapterChat.holderChat holder){

        holder.playVoice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (mediaPlayer.isPlaying() && mediaPlayer != null){
                    holder.playVoice.setImageDrawable(ContextCompat.getDrawable(ChatActivity.this,R.drawable.ic_play));
                    mediaPlayer.pause();
                } else {

                    if (firstLaunch){
                        // Post post = postList.get(0);
                        prepareSong(chat,holder);
                    } else {

                        mediaPlayer.start();
                        firstLaunch = false;
                    }
                    holder.playVoice.setImageDrawable(ContextCompat.getDrawable(ChatActivity.this,R.drawable.ic_pause));
                }
            }
        });
    }

    private void showDialog(Chat chat, String message) {

        // custom dialog
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.message_options);
        dialog.setTitle("Title");

        // set the custom dialog components - text, image and button
        TextView ReplyMessage = dialog.findViewById(R.id.reply_message);
        TextView CopyMessage = dialog.findViewById(R.id.copy_message);
        TextView DeleteMessage = dialog.findViewById(R.id.delete_message);

        ReplyMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Toast.makeText(ChatActivity.this, "Coming soon", Toast.LENGTH_SHORT).show();
            }
        });

        CopyMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    final android.content.ClipboardManager clipboardManager = (android.content.ClipboardManager) getApplicationContext()
                            .getSystemService(Context.CLIPBOARD_SERVICE);
                    final android.content.ClipData clipData = android.content.ClipData
                            .newPlainText("Copy", message);
                    clipboardManager.setPrimaryClip(clipData);
                    Toast.makeText(ChatActivity.this, "Copied to clipboard", Toast.LENGTH_SHORT).show();

                } else {
                    final android.text.ClipboardManager clipboardManager = (android.text.ClipboardManager) getApplicationContext()
                            .getSystemService(Context.CLIPBOARD_SERVICE);
                    clipboardManager.setText(message);
                    Toast.makeText(ChatActivity.this, "Copied to clipboard", Toast.LENGTH_SHORT).show();

                }
            }
        });

        DeleteMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String msgTimeStamp = chat.getTimestamp();

                DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("Chats");
                Query query = dbRef.orderByChild("timestamp").equalTo(msgTimeStamp);
                query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        for (DataSnapshot ds: dataSnapshot.getChildren()){
                            // just for sender to delete
                            ds.getRef().removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {

                                    if (task.isSuccessful()){

                                        dialog.dismiss();
                                    }
                                }
                            });
                            //delete for sender and receiver

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        });

        dialog.show();
    }

    private void initialiseViews() {

        recordMicButton = findViewById(R.id.record_button);
        recordAudioLayout = findViewById(R.id.record_audio_view);

        //set recordAudioview
        recordMicButton.setListenForRecord(true, this);
        recordAudioLayout.setCancelOffset(8);
        recordAudioLayout.setLessThanSecondAllowed(false);
        recordAudioLayout.setSlideToCancelText(getString(R.string.slide_to_cancel));
        recordAudioLayout.setCustomSounds(R.raw.record_start, R.raw.record_finished, 0);
        recordMicButton.setRecordAudio(recordAudioLayout);
        setRecordListener();


        attachmentLayout = findViewById(R.id.menu_attachments);
        btnDocument =  findViewById(R.id.document_button);
        btnCamera =  findViewById(R.id.camera_button);
        btnGallery =  findViewById(R.id.gallery_button);
        btnAudio =  findViewById(R.id.audio_button);

        btnDocument.setOnClickListener(this);
        btnCamera.setOnClickListener(this);
        btnGallery.setOnClickListener(this);
        btnAudio.setOnClickListener(this);

        rootView = findViewById(R.id.root_view);
        emojiButton = findViewById(R.id.main_activity_emoji);
        emojiButton.setColorFilter(ContextCompat.getColor(this, R.color.icon_checked), PorterDuff.Mode.SRC_IN);
        emojiButton.setOnClickListener(ignore -> emojiPopup.toggle());

        TitleTv = findViewById(R.id.title);
        IconIv = findViewById(R.id.groupIconIv);
        userLastSeen = findViewById(R.id.user_last_seen);
        attachBtn = findViewById(R.id.attachBtn);
        camera = findViewById(R.id.camera);
        sendBtn = findViewById(R.id.sendBtn);
        messageEt = findViewById(R.id.messageEt);

        back = findViewById(R.id.back);
        checkUser = findViewById(R.id.check_profile);

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                finish();
            }
        });

        checkUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(ChatActivity.this,AllUserProfileActivity.class);
                intent.putExtra("user_id",userid);
                startActivity(intent);
            }
        });

        camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                com.github.dhaval2404.imagepicker.ImagePicker.Companion.with(ChatActivity.this)
                        .cameraOnly()
                        .cropSquare()
                        .compress(1024)
                        .saveDir(new File(Environment.getExternalStorageDirectory() + "/"+ "ChaCks" + "/" + "images/"))
                        .start();


            }
        });

        attachBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                showMenu();
            }
        });

    }

    private void setRecordListener() {
        recordAudioLayout.setOnRecordListener(new RecordListener() {
            @Override
            public void onStart() {

                Logger.error(TAG, "onStart: " + " record start");
                messageEt.setHint("");
                startRecording();

            }

            @Override
            public void onCancel() {

                Logger.error(TAG, "onCancel: " + "record cancel");

                messageEt.setHint(getString(R.string.message_hint));
                stopRecording(true);

            }

            @Override
            public void onFinish(long time) {
                Logger.error(TAG, "onFinish: " + "record finish");

                messageEt.setHint(getString(R.string.message_hint));
                File audioFile;
                if (audioFileNamewithPath != null) {
                    audioFile = new File(audioFileNamewithPath);
                    Logger.error("audioFileNamewithPath", audioFileNamewithPath);

                    uploadVocal(audioFile);

                }

                stopRecording(false);

            }

            @Override
            public void onLessTime() {
                Logger.error(TAG, "onLessTime: ");
                messageEt.setHint(getString(R.string.message_hint));
                stopRecording(true);

            }
        });
    }

    private void uploadVocal(File audioFile) {

        fileUriAudio = Uri.fromFile(audioFile);

        String timestamp = ""+ System.currentTimeMillis();
        StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("messages vocal");
        final StorageReference filePath = storageReference.child(timestamp +  ".mp3");
        filePath.putFile(fileUriAudio).addOnSuccessListener(
                new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                        Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                        while (!uriTask.isSuccessful());

                        String downloadUri = uriTask.getResult().toString();

                        if (uriTask.isSuccessful()){

                            notify = true;
                            sendMessageAudio(fuser.getUid(),userid,downloadUri);
                            Toast.makeText(ChatActivity.this, "Audio envoyé...", Toast.LENGTH_SHORT).show();

                            //  pd.dismiss();
                            progressBar.setVisibility(View.GONE);
                        }


                    }
                }
        ).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(@NonNull UploadTask.TaskSnapshot taskSnapshot) {

                double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                // System.out.println("Upload is " + progress + "% done");
                // pd.setMessage((int) progress + "% uploading");
                progressBar.setProgress((int) progress);
                progressBar.setVisibility(View.VISIBLE);
            }
        });

    }

    private void sendMessageAudio(String sender, String receiver, String myurl) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        String timestamp = ""+ System.currentTimeMillis();

        MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        mmr.setDataSource(getApplicationContext(), Uri.parse(audioFileNamewithPath));
        String durationString = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);


        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("sender",""+sender);
        hashMap.put("receiver",""+receiver);
        hashMap.put("message",""+"voice");
        hashMap.put("audioFile",""+myurl);
        hashMap.put("audioDuration",""+durationString);
        hashMap.put("audioName",""+fileUriAudio.getLastPathSegment());
        hashMap.put("messageId",""+timestamp);
        hashMap.put("type",""+"voice");
        hashMap.put("isseen",false);
        hashMap.put("timestamp",""+timestamp);
        reference.child("Chats").child(timestamp).setValue(hashMap);

        final String msg = "audio";

        reference = FirebaseDatabase.getInstance().getReference("user").child(fuser.getUid());
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                if (notify) {
                    sendNotifiaction(receiver, user.getName(),user.getImage(), msg);
                }
                notify = false;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void startRecording() {
        try {

            mediaRecorder = new MediaRecorder();
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            audioFileNamewithPath = FileUtils.getOutputMediaFile(ChatActivity.this);
            mediaRecorder.setOutputFile(audioFileNamewithPath);

            try {
                mediaRecorder.prepare();
            } catch (IOException e) {
                e.printStackTrace();
            }
            mediaRecorder.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void stopRecording(boolean isCancel) {
        try {
            if (mediaRecorder != null) {
                mediaRecorder.stop();
                mediaRecorder.release();
                mediaRecorder = null;
                if (isCancel) {
                    new File(audioFileNamewithPath).delete();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        stopRecording(false);
        Logger.error(TAG, "onStop:");

    }
    private void seenMessage(final String userid){
        reference = FirebaseDatabase.getInstance().getReference("Chats");
        seenListener = reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Chat chat = snapshot.getValue(Chat.class);
                    if (chat.getReceiver().equals(fuser.getUid()) && chat.getSender().equals(userid)){
                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("isseen", true);
                        snapshot.getRef().updateChildren(hashMap);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    private void sendMessage(String sender, final String receiver,String message) {

        String timestamp = ""+ System.currentTimeMillis();

        HashMap<String,Object> hashMap = new HashMap<>();
        hashMap.put("sender", ""+sender);
        hashMap.put("receiver", ""+receiver);
        hashMap.put("message",""+message);
        hashMap.put("timestamp",""+timestamp);
        hashMap.put("messageId",""+ timestamp);
        hashMap.put("isseen", false);
        hashMap.put("type",""+ "text");

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Chats");
        ref.child(timestamp)
                .setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(ChatActivity.this, "message sent", Toast.LENGTH_SHORT).show();
                        messageEt.setText("");
                        //  mediaPlayer.start();
                        MediaUtils.playSendSound(ChatActivity.this, R.raw.send);
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(ChatActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        // add user to chat fragment
        final DatabaseReference chatRef = FirebaseDatabase.getInstance().getReference("Chatlist")
                .child(fuser.getUid())
                .child(userid);

        chatRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()){
                    chatRef.child("id").setValue(userid);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        final DatabaseReference chatRefReceiver = FirebaseDatabase.getInstance().getReference("Chatlist")
                .child(userid)
                .child(fuser.getUid());
        chatRefReceiver.child("id").setValue(fuser.getUid());

        final String msg = message;

        reference = FirebaseDatabase.getInstance().getReference("user").child(fuser.getUid());
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                if (notify) {
                    sendNotifiaction(receiver, user.getName(),user.getImage(), msg);
                }
                notify = false;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }
    private void loadMessageAdapter() {

        reference = FirebaseDatabase.getInstance().getReference("Chats");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                chatList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Chat chat = snapshot.getValue(Chat.class);
                    chat.parseObject(snapshot);

                    if (chat.getReceiver().equals(fuser.getUid()) && chat.getSender().equals(userid) ||
                            chat.getReceiver().equals(userid) && chat.getSender().equals(fuser.getUid())){

                        chatList.add(chat);

                    }

                    chatRv.setAdapter(adapterChat);

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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        if (requestCode == 439 && resultCode == RESULT_OK && data != null && data.getData() != null){

            fileUri = data.getData();
            String fileExt = MimeTypeMap.getFileExtensionFromUrl(fileUri.toString());
            sendDocFiles(fileExt);
        }

        if (resultCode == Activity.RESULT_OK && data != null && data.getData() != null){

            imageUri = data.getData();
            uploadImage(imageUri);
        }


        if (requestCode == 1 && resultCode == RESULT_OK && data != null && data.getData() != null){

            fileUriAudio = data.getData();

            uploadAudioFiles(durationStr);
        }


        if (requestCode == VIDEO_REQUEST && null != data) {
            if (requestCode == VIDEO_REQUEST) {

                videoUri = data.getData();

                Intent intent = new Intent(ChatActivity.this,SendVideoFilesActivity.class);
                intent.putExtra("videoUri",videoUri.toString());
                intent.putExtra("user_id",userid);
                startActivity(intent);
            }
        }


    }

    private void uploadImage(Uri imageUri) {
        String timestamp = ""+ System.currentTimeMillis();
        StorageReference ImageFolder = FirebaseStorage.getInstance().getReference().child("Messages Images");
        final StorageReference Path = ImageFolder.child(timestamp + ".jpg" );
        Path.putFile(imageUri).addOnSuccessListener(
                new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                        Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                        while (!uriTask.isSuccessful());

                        String downloadUri = uriTask.getResult().toString();

                        if (uriTask.isSuccessful()){

                            notify = true;
                            sendImageMessage(fuser.getUid(),userid,downloadUri);
                            Toast.makeText(ChatActivity.this, "Image envoyé...", Toast.LENGTH_SHORT).show();

                            //  pd.dismiss();
                            progressBar.setVisibility(View.GONE);
                        }


                    }
                }
        ).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(@NonNull UploadTask.TaskSnapshot taskSnapshot) {

                double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                // System.out.println("Upload is " + progress + "% done");
                // pd.setMessage((int) progress + "% uploading");
                progressBar.setProgress((int) progress);
                progressBar.setVisibility(View.VISIBLE);
            }
        });
    }

    private void sendImageMessage(String sender, String receiver, String downloadUri) {

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        String timestamp = ""+ System.currentTimeMillis();

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("sender",sender);
        hashMap.put("receiver",receiver);
        hashMap.put("message",""+imageUri.getLastPathSegment());
        hashMap.put("imageFile",downloadUri);
        hashMap.put("messageId",timestamp);
        hashMap.put("type","image");
        hashMap.put("isseen",false);
        hashMap.put("timestamp",timestamp);
        reference.child("Chats").child(timestamp).setValue(hashMap);

        // add user to chat fragment
        final DatabaseReference chatRef = FirebaseDatabase.getInstance().getReference("Chatlist")
                .child(fuser.getUid())
                .child(userid);

        chatRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()){
                    chatRef.child("id").setValue(userid);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        final DatabaseReference chatRefReceiver = FirebaseDatabase.getInstance().getReference("Chatlist")
                .child(userid)
                .child(fuser.getUid());
        chatRefReceiver.child("id").setValue(fuser.getUid());


        final String msg = "Photo" ;

        reference = FirebaseDatabase.getInstance().getReference("user").child(fuser.getUid());
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                if (notify) {
                    sendNotifiaction(receiver, user.getName(),user.getImage(), msg);
                }
                notify = false;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void uploadAudioFiles(String durationStr) {

        String timestamp = ""+ System.currentTimeMillis();

        StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("Document Audio");
        final StorageReference Path = storageReference.child(timestamp + ".mp3" );

        Path.putFile(fileUriAudio).addOnSuccessListener(
                new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                        Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                        while (!uriTask.isSuccessful());

                        String downloadUri = uriTask.getResult().toString();

                        if (uriTask.isSuccessful()){

                            notify = true;
                            sendAudioDoc(fuser.getUid(),userid,downloadUri,durationStr);
                            Toast.makeText(ChatActivity.this, "Audio envoyé...", Toast.LENGTH_SHORT).show();

                            //  pd.dismiss();
                            progressBar.setVisibility(View.GONE);
                        }


                    }
                }
        ).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(@NonNull UploadTask.TaskSnapshot taskSnapshot) {

                double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                // System.out.println("Upload is " + progress + "% done");
                // pd.setMessage((int) progress + "% uploading");
                progressBar.setProgress((int) progress);
                progressBar.setVisibility(View.VISIBLE);
            }
        });

    }

    private void sendAudioDoc(String sender, String receiver, String downloadUri, String durationStr) {

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        String timestamp = ""+ System.currentTimeMillis();

        MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        mmr.setDataSource(getApplicationContext(),fileUriAudio);
        String durationString = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);


        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("sender",sender);
        hashMap.put("receiver",receiver);
        hashMap.put("message",""+fileUriAudio.getLastPathSegment());
        hashMap.put("audioFile",downloadUri);
        hashMap.put("audioDuration",""+ durationString);
        hashMap.put("messageId",timestamp);
        hashMap.put("type","audio");
        hashMap.put("isseen",false);
        hashMap.put("timestamp",timestamp);
        reference.child("Chats").child(timestamp).setValue(hashMap);

        // add user to chat fragment
        final DatabaseReference chatRef = FirebaseDatabase.getInstance().getReference("Chatlist")
                .child(fuser.getUid())
                .child(userid);

        chatRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()){
                    chatRef.child("id").setValue(userid);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        final DatabaseReference chatRefReceiver = FirebaseDatabase.getInstance().getReference("Chatlist")
                .child(userid)
                .child(fuser.getUid());
        chatRefReceiver.child("id").setValue(fuser.getUid());


        final String msg = "Audio" ;

        reference = FirebaseDatabase.getInstance().getReference("user").child(fuser.getUid());
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                if (notify) {
                    sendNotifiaction(receiver, user.getName(),user.getImage(), msg);
                }
                notify = false;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void sendDocFiles(String fileExt) {

        String timestamp = ""+ System.currentTimeMillis();

        StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("Document Files");
        final StorageReference filePath = storageReference.child(timestamp + fileExt );

        filePath.putFile(fileUri).addOnSuccessListener(
                new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                        Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                        while (!uriTask.isSuccessful());

                        String downloadUri = uriTask.getResult().toString();

                        if (uriTask.isSuccessful()){

                            notify = true;
                            sendMessageDoc(fuser.getUid(),userid,downloadUri,fileExt);
                            Toast.makeText(ChatActivity.this, "document envoyé...", Toast.LENGTH_SHORT).show();

                            progressBar.setVisibility(View.GONE);
                        }


                    }
                }
        ).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(@NonNull UploadTask.TaskSnapshot taskSnapshot) {

                double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                // System.out.println("Upload is " + progress + "% done");
                // pd.setMessage((int) progress + "% uploading");
                progressBar.setProgress((int) progress);
            }
        });


    }

    private void sendMessageDoc(String sender, String receiver, String fileExt, String ext) {

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        String timestamp = ""+ System.currentTimeMillis();


        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("sender",sender);
        hashMap.put("receiver",receiver);
        hashMap.put("message",fileUri.getLastPathSegment());
        hashMap.put("docFile",fileExt);
        hashMap.put("messageId",timestamp);
        hashMap.put("type",ext);
        hashMap.put("isseen",false);
        hashMap.put("timestamp",timestamp);
        reference.child("Chats").child(timestamp).setValue(hashMap);

        // add user to chat fragment
        final DatabaseReference chatRef = FirebaseDatabase.getInstance().getReference("Chatlist")
                .child(fuser.getUid())
                .child(userid);

        chatRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()){
                    chatRef.child("id").setValue(userid);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        final DatabaseReference chatRefReceiver = FirebaseDatabase.getInstance().getReference("Chatlist")
                .child(userid)
                .child(fuser.getUid());
        chatRefReceiver.child("id").setValue(fuser.getUid());


        final String msg = "document" + fileExt;

        reference = FirebaseDatabase.getInstance().getReference("user").child(fuser.getUid());
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                if (notify) {
                    sendNotifiaction(receiver, user.getName(),user.getImage(), msg);
                }
                notify = false;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

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
                    Data data = new Data(""+fuser.getUid(),
                            ""+image,
                            R.drawable.ic_notif,
                            ""+ username+": "+message,
                            getString(R.string.new_message),
                            "",
                            ""+ userid,
                            "ChatNotification",
                            "");

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

    private void checkOnlineStatus(String state){

        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("user").child(fuser.getUid());
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("onlineStatus",state);
        //currentUSer
        dbRef.updateChildren(hashMap);
    }

    private void chekTypingStatus(String typing){

        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("user").child(fuser.getUid());
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("typingTo",typing);
        dbRef.updateChildren(hashMap);
    }

    private void status(String status){
        reference = FirebaseDatabase.getInstance().getReference("user").child(fuser.getUid());
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("states", status);
        reference.updateChildren(hashMap);
    }

    @Override
    protected void onStart() {
        checkOnlineStatus("online");
        super.onStart();

    }

    @Override
    protected void onResume() {
        super.onResume();
        checkOnlineStatus("online");
        status("online");

    }

    @Override
    protected void onPause() {
        super.onPause();
        String timestamp = String.valueOf(System.currentTimeMillis());
        checkOnlineStatus(timestamp);
        chekTypingStatus("noOne");
        reference.removeEventListener(seenListener);
        status("offline");

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
                .build(messageEt);
    }

    public void openGalleryForVideo() {

        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("video/*");
        startActivityForResult(intent,VIDEO_REQUEST);
    }

    @Override
    public void onClick(View v) {
        hideMenu();

        switch (v.getId()) {
            case R.id.document_button:
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.setType("*/*");
                String[] mimetypes =  {"application/msword","application/vnd.openxmlformats-officedocument.wordprocessingml.document", // .doc & .docx
                        "application/vnd.ms-powerpoint","application/vnd.openxmlformats-officedocument.presentationml.presentation", // .ppt & .pptx
                        "application/vnd.ms-excel","application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", // .xls & .xlsx
                        "text/plain",
                        "application/pdf",
                        "application/zip",
                        "application/rar"};
                intent.putExtra(Intent.EXTRA_MIME_TYPES, mimetypes);
                startActivityForResult(Intent.createChooser(intent, "select document files"),439);
                break;
            case R.id.camera_button:

                openGalleryForVideo();
                break;
            case R.id.gallery_button:

                Intent Imageintent = new Intent(ChatActivity.this,SendMediaFilesActivity.class);
                Imageintent.putExtra("user_id",userid);
                startActivity(Imageintent);


                break;
            case R.id.audio_button:

                Intent songIntent = new Intent(Intent.ACTION_GET_CONTENT);
                songIntent.setType("audio/*");
                startActivityForResult(songIntent,1);
                break;

        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    void showMenu() {
        int cx = (attachmentLayout.getLeft() + attachmentLayout.getRight());
        int cy = attachmentLayout.getBottom();
        int radius = Math.max(attachmentLayout.getWidth(), attachmentLayout.getHeight());

        if (isHidden) {
            Animator anim = android.view.ViewAnimationUtils.createCircularReveal(attachmentLayout, cx, cy, 0, radius);
            attachmentLayout.setVisibility(View.VISIBLE);
            anim.start();
            isHidden = false;
        } else {
            Animator anim = android.view.ViewAnimationUtils.createCircularReveal(attachmentLayout, cx, cy, radius, 0);
            anim.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    attachmentLayout.setVisibility(View.INVISIBLE);
                    isHidden = true;
                }
            });
            anim.start();
        }
    }

    private void hideMenu() {
        attachmentLayout.setVisibility(View.GONE);
        isHidden = true;
    }

}