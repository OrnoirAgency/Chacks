package com.ornoiragency.chacks.Activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
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

import com.airbnb.lottie.LottieAnimationView;
import com.android.volley.AuthFailureError;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.cometchat.pro.helpers.Logger;
import com.danikula.videocache.HttpProxyCacheServer;
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
import com.ornoiragency.chacks.Adapter.ChatAdapter;
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
import com.ornoiragency.chacks.models.Chat;
import com.ornoiragency.chacks.models.User;
import com.ornoiragency.chacks.viewHolders.RightAudioViewHolder;
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

import static com.facebook.react.bridge.UiThreadUtil.runOnUiThread;
import static com.ornoiragency.chacks.Chacks.getProxy;


public class MessagesActivity extends AppCompatActivity implements View.OnClickListener{

    public static String[] RECORD_PERMISSION = {CCPermissionHelper.REQUEST_PERMISSION_RECORD_AUDIO,
            CCPermissionHelper.REQUEST_PERMISSION_WRITE_EXTERNAL_STORAGE};

    public static final int RECORD_CODE = 22;
    private String audioFileNamewithPath,proxyUrl;
    private static final int VIDEO_REQUEST = 1000;
    private  String[] cameraPermission;
    private  String[] storagePermission;

    private RecordAudio recordAudioLayout;
    private MediaRecorder mediaRecorder;
    private RecordMicButton recordMicButton;

    static final String TAG = "MessageActivity";
    private Uri imageUri,fileUriAudio,videoUri,fileUri;

    ArrayList<Chat> chatList;
    ChatAdapter chatAdapter;

    private RecyclerView chatRv;
    private String user_id,fUser;
    FirebaseUser fuser;
    DatabaseReference reference;
    ValueEventListener seenListener;

    //volley request for notification
    private RequestQueue requestQueue;
    boolean notify = false;
    ConstraintLayout back,checkUser;
    private TextView TitleTv;
    private AvatarImageView IconIv;
    private ImageButton attachBtn,sendBtn,camera;
    private EditText messageEt;
    private TextView userLastSeen;
    ProgressBar progressBar;

    EmojiPopup emojiPopup;
    RelativeLayout rootView;
    ImageButton emojiButton;

    String durationStr;

    private LinearLayout attachmentLayout;
    private boolean isHidden = true;
    CircleImageView btnDocument,btnCamera,btnGallery,btnAudio;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messages);

        initialiseViews();

        storagePermission = new String[]{
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        };

        requestQueue = Volley.newRequestQueue(getApplicationContext());

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

        back = findViewById(R.id.back);
        checkUser = findViewById(R.id.check_profile);

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                finish();
            }
        });

        camera = findViewById(R.id.camera);
        camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                com.github.dhaval2404.imagepicker.ImagePicker.Companion.with(MessagesActivity.this)
                        .cameraOnly()
                        .cropSquare()
                        .compress(1024)
                        .saveDir(new File(Environment.getExternalStorageDirectory() + "/"+ "ChaCks" + "/" + "images/"))
                        .start();


            }
        });
        progressBar = findViewById(R.id.progressBar);
        chatRv = findViewById(R.id.chatRv);
        final LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);
        chatRv.setLayoutManager(mLayoutManager);
        mLayoutManager.setStackFromEnd(true);
        chatRv.setItemViewCacheSize(20);
        chatRv.setDrawingCacheEnabled(true);
        chatRv.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
        chatRv.setNestedScrollingEnabled(false);
        chatRv.getItemAnimator().setChangeDuration(0);

        Intent intent = getIntent();
        user_id = intent.getStringExtra("user_id");
        fuser = FirebaseAuth.getInstance().getCurrentUser();
        fUser = fuser.getUid();

        checkUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(MessagesActivity.this,AllUserProfileActivity.class);
                intent.putExtra("user_id",user_id);
                startActivity(intent);
            }
        });

        TitleTv = findViewById(R.id.title);
        IconIv = findViewById(R.id.groupIconIv);
        userLastSeen = findViewById(R.id.user_last_seen);
        attachBtn = findViewById(R.id.attachBtn);
        messageEt = findViewById(R.id.messageEt);
        sendBtn = findViewById(R.id.sendBtn);

        rootView = findViewById(R.id.root_view);
        emojiButton = findViewById(R.id.main_activity_emoji);
        emojiButton.setColorFilter(ContextCompat.getColor(this, R.color.icon_checked), PorterDuff.Mode.SRC_IN);
        emojiButton.setOnClickListener(ignore -> emojiPopup.toggle());

        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                notify = true;
                String message = messageEt.getText().toString().trim();
                if (TextUtils.isEmpty(message)){
                    Toast.makeText(MessagesActivity.this, "can't sen empty message", Toast.LENGTH_SHORT).show();
                } else {
                    senMessage(fuser.getUid(), user_id,message);
                }
                messageEt.setText("");
            }
        });

        setUpEmojiPopup();
        userInfo();
        seenMessage(user_id);
        loadMessageAdapter();
    }

    private void initialiseViews() {

        attachmentLayout = findViewById(R.id.menu_attachments);
        btnDocument =  findViewById(R.id.document_button);
        btnCamera =  findViewById(R.id.camera_button);
        btnGallery =  findViewById(R.id.gallery_button);
        btnAudio =  findViewById(R.id.audio_button);

        btnDocument.setOnClickListener(this);
        btnCamera.setOnClickListener(this);
        btnGallery.setOnClickListener(this);
        btnAudio.setOnClickListener(this);

        attachBtn = findViewById(R.id.attachBtn);
        attachBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                showMenu();
            }
        });

    }

    private void userInfo() {

        reference = FirebaseDatabase.getInstance().getReference("user").child(user_id);
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
                    chekTypingStatus(user_id);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }
    private void senMessage(String uid, String user_id, String message) {

        String timestamp = ""+ System.currentTimeMillis();

        HashMap<String,Object> hashMap = new HashMap<>();
        hashMap.put("sender", ""+uid);
        hashMap.put("receiver", ""+user_id);
        hashMap.put("message",""+message);
        hashMap.put("timestamp",""+timestamp);
        hashMap.put("messageId",""+ timestamp);
        hashMap.put("isseen", false);
        hashMap.put("type",""+ "text");

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Messages");
        ref.child(timestamp)
                .setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        MediaUtils.playSendSound(MessagesActivity.this, R.raw.send);
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(MessagesActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        // add user to chat fragment
        final DatabaseReference chatRef = FirebaseDatabase.getInstance().getReference("ChatList")
                .child(fuser.getUid())
                .child(user_id);

        chatRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()){
                    chatRef.child("id").setValue(user_id);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        final DatabaseReference chatRefReceiver = FirebaseDatabase.getInstance().getReference("ChatList")
                .child(user_id)
                .child(fuser.getUid());
        chatRefReceiver.child("id").setValue(fuser.getUid());

        final String msg = message;

        reference = FirebaseDatabase.getInstance().getReference("user").child(fuser.getUid());
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                if (notify) {
                    sendNotification(user_id, user.getName(),user.getImage(), msg);
                }
                notify = false;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }
    private void seenMessage(final String user_id){
        reference = FirebaseDatabase.getInstance().getReference("Messages");
        seenListener = reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Chat chat = snapshot.getValue(Chat.class);
                    if (chat.getReceiver().equals(fuser.getUid()) && chat.getSender().equals(user_id)){
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
    private void loadMessageAdapter() {

        chatList = new ArrayList<>();

        reference = FirebaseDatabase.getInstance().getReference("Messages");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                chatList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Chat chat = snapshot.getValue(Chat.class);

                    if (chat.getReceiver().equals(fuser.getUid()) && chat.getSender().equals(user_id) ||
                            chat.getReceiver().equals(user_id) && chat.getSender().equals(fuser.getUid())){
                        chatList.add(chat);

                    }

                    chatAdapter = new ChatAdapter(MessagesActivity.this, chatList);
                    chatRv.setAdapter(chatAdapter);
                    chatAdapter.notifyDataSetChanged();

                }
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
                            sendAudioDoc(fuser.getUid(),user_id,downloadUri,durationStr);
                            Toast.makeText(MessagesActivity.this, "Audio envoyé...", Toast.LENGTH_SHORT).show();

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
        final DatabaseReference chatRef = FirebaseDatabase.getInstance().getReference("ChatList")
                .child(fuser.getUid())
                .child(user_id);

        chatRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()){
                    chatRef.child("id").setValue(user_id);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        final DatabaseReference chatRefReceiver = FirebaseDatabase.getInstance().getReference("ChatList")
                .child(user_id)
                .child(fuser.getUid());
        chatRefReceiver.child("id").setValue(fuser.getUid());


        final String msg = "Audio" ;

        reference = FirebaseDatabase.getInstance().getReference("user").child(fuser.getUid());
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                if (notify) {
                    sendNotification(receiver, user.getName(),user.getImage(), msg);
                }
                notify = false;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

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


                            sendImageMessage(fuser.getUid(),user_id,downloadUri);
                            Toast.makeText(MessagesActivity.this, "Image envoyé...", Toast.LENGTH_SHORT).show();

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
        reference.child("Messages").child(timestamp).setValue(hashMap);

        // add user to chat fragment
        final DatabaseReference chatRef = FirebaseDatabase.getInstance().getReference("ChatList")
                .child(fuser.getUid())
                .child(user_id);

        chatRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()){
                    chatRef.child("id").setValue(user_id);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        final DatabaseReference chatRefReceiver = FirebaseDatabase.getInstance().getReference("ChatList")
                .child(user_id)
                .child(fuser.getUid());
        chatRefReceiver.child("id").setValue(fuser.getUid());


        final String msg = "Photo" ;

        reference = FirebaseDatabase.getInstance().getReference("user").child(fuser.getUid());
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                if (notify) {
                    sendNotification(receiver, user.getName(),user.getImage(), msg);
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
                            sendMessageDoc(fuser.getUid(),user_id,downloadUri,fileExt);
                            Toast.makeText(MessagesActivity.this, "document envoyé...", Toast.LENGTH_SHORT).show();

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
        hashMap.put("message","document");
        hashMap.put("fileName",fileUri.getLastPathSegment());
        hashMap.put("docFile",fileExt);
        hashMap.put("messageId",timestamp);
        hashMap.put("type","file");
        hashMap.put("fileType",ext);
        hashMap.put("isseen",false);
        hashMap.put("timestamp",timestamp);
        reference.child("Messages").child(timestamp).setValue(hashMap);

        // add user to chat fragment
        final DatabaseReference chatRef = FirebaseDatabase.getInstance().getReference("ChatList")
                .child(fuser.getUid())
                .child(user_id);

        chatRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()){
                    chatRef.child("id").setValue(user_id);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        final DatabaseReference chatRefReceiver = FirebaseDatabase.getInstance().getReference("ChatList")
                .child(user_id)
                .child(fuser.getUid());
        chatRefReceiver.child("id").setValue(fuser.getUid());


        final String msg = "document" + fileExt;

        reference = FirebaseDatabase.getInstance().getReference("user").child(fuser.getUid());
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                if (notify) {
                    sendNotification(receiver, user.getName(),user.getImage(), msg);
                }
                notify = false;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }
    private void sendNotification(String receiver, final String username,final String image , final String message){
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
                            ""+ user_id,
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

    //voice record and upload
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
                            sendMessageAudio(fuser.getUid(),user_id,downloadUri);
                            Toast.makeText(MessagesActivity.this, "Audio envoyé...", Toast.LENGTH_SHORT).show();

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
        hashMap.put("audioName",""+"audio");
        hashMap.put("messageId",""+timestamp);
        hashMap.put("type",""+"audio");
        hashMap.put("isseen",false);
        hashMap.put("timestamp",""+timestamp);
        reference.child("Messages").child(timestamp).setValue(hashMap);

        final String msg = "audio";

        reference = FirebaseDatabase.getInstance().getReference("user").child(fuser.getUid());
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                if (notify) {
                    sendNotification(receiver, user.getName(),user.getImage(), msg);
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
            audioFileNamewithPath = FileUtils.getOutputMediaFile(MessagesActivity.this);
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 439 && resultCode == RESULT_OK && data != null && data.getData() != null){

            fileUri = data.getData();
            String fileExt = MimeTypeMap.getFileExtensionFromUrl(fileUri.toString());
            sendDocFiles(fileExt);
        }


        if (requestCode == 1 && resultCode == RESULT_OK && data != null && data.getData() != null){

            fileUriAudio = data.getData();

            uploadAudioFiles(durationStr);
        }


        if (requestCode == VIDEO_REQUEST && null != data) {
            if (requestCode == VIDEO_REQUEST) {

                videoUri = data.getData();

                Intent intent = new Intent(MessagesActivity.this,SendVideoFilesActivity.class);
                intent.putExtra("videoUri",videoUri.toString());
                intent.putExtra("user_id",user_id);
                startActivity(intent);
            }
        }

        if (resultCode == Activity.RESULT_OK && data != null && data.getData() != null){

            imageUri = data.getData();
            uploadImage(imageUri);
        }
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

        Logger.error(TAG, "onPause: ");

        if (chatAdapter!=null) {
            chatAdapter.stopPlayer();
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        chatAdapter.onDestroy();

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

                Intent Imageintent = new Intent(MessagesActivity.this,SendMediaFilesActivity.class);
                Imageintent.putExtra("user_id",user_id);
                startActivity(Imageintent);


                break;
            case R.id.audio_button:

                Intent songIntent = new Intent(Intent.ACTION_GET_CONTENT);
                songIntent.setType("audio/*");
                startActivityForResult(songIntent,1);
                break;

        }
    }

    public void openGalleryForVideo() {

        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("video/*");
        startActivityForResult(intent,VIDEO_REQUEST);
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