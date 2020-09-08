package com.ornoiragency.chacks.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.android.volley.AuthFailureError;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnPausedListener;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.google.gson.Gson;
import com.ornoiragency.chacks.Notification.Data;
import com.ornoiragency.chacks.Notification.Sender;
import com.ornoiragency.chacks.Notification.Token;
import com.ornoiragency.chacks.Post.AddPostActivity;
import com.ornoiragency.chacks.R;
import com.ornoiragency.chacks.models.User;


import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import me.alvince.android.avatarimageview.AvatarImageView;

public class SendVideoFilesActivity extends AppCompatActivity {

    String video,user_id,myurl,durationString,legend;
    private Uri videoUri;

    ImageView back;
    CircleImageView profile_image;
    VideoView videoView;
    EditText video_legend;

    FirebaseUser fuser;
    DatabaseReference reference,ref;
    StorageReference storageReference;
    private Bitmap compressedImageFile;
    private StorageTask uploadTask;

    boolean notify = false;
    //volley request for notification
    private RequestQueue requestQueue;
    private ProgressDialog pd;

    MaterialButton sendVideo;
    TextView videoTimer;
    AvatarImageView videoCover;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_video_files);

        requestQueue = Volley.newRequestQueue(getApplicationContext());
        ref = FirebaseDatabase.getInstance().getReference();
        storageReference = FirebaseStorage.getInstance().getReference().child("Messages Images");

        videoCover = findViewById(R.id.videoCover);
        videoTimer = findViewById(R.id.videoTimer);
        pd = new ProgressDialog(this);

        Intent intent = getIntent();
        video = intent.getStringExtra("videoUri");
        user_id = intent.getStringExtra("user_id");
        videoUri = Uri.parse(video);
        video_legend = findViewById(R.id.video_legend);
        videoView = findViewById(R.id.video);
        videoView.setVideoURI(videoUri);
        videoView.requestFocus();

        MediaController mediaController = new MediaController(this);
        videoView.setMediaController(mediaController);

        Glide.with(this)
                .load(videoUri)
                .into(videoCover);

        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {

                int  duration = mp.getDuration()/1000;
                durationString = String.format("%02d:%02d",duration/60,duration % 60);
                videoTimer.setText(durationString);

            }
        });

        sendVideo = findViewById(R.id.sendImage);
        sendVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                 legend = video_legend.getText().toString().trim();
                uploadVideoData();
            }
        });

        profile_image = findViewById(R.id.profileImage);
        back = findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // and this
                finish();
            }
        });

        fuser = FirebaseAuth.getInstance().getCurrentUser();

        reference = FirebaseDatabase.getInstance().getReference("user").child(user_id);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);

                if (user.getImage().equals("default")){
                    profile_image.setImageResource(R.mipmap.ic_launcher);
                } else {
                    //and this

                    Glide.with(getApplicationContext())
                            .asBitmap()
                            .load(user.getImage())
                            .placeholder(R.drawable.profile_image)
                            .into(profile_image);


                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void uploadVideoData() {

        pd.show();
        pd.setCanceledOnTouchOutside(true);
        String timestamp = String.valueOf(System.currentTimeMillis());
        String filePathName = "Posts/" + "post_" + timestamp;

        StorageReference ref = FirebaseStorage.getInstance().getReference(filePathName);
        ref.putFile(videoUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {


                        Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                        while (!uriTask.isSuccessful());

                        String downloadUri = uriTask.getResult().toString();

                        if (uriTask.isSuccessful()){

                            uploadCover(downloadUri);

                            Toast.makeText(SendVideoFilesActivity.this, "video envoyé...", Toast.LENGTH_SHORT).show();

                            pd.dismiss();
                        }

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        pd.dismiss();
                    }
                }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                // System.out.println("Upload is " + progress + "% done");
                pd.setMessage((int) progress + "% uploading");
            }
        }).addOnPausedListener(new OnPausedListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onPaused(UploadTask.TaskSnapshot taskSnapshot) {
                System.out.println("Upload is paused");
            }
        });



    }

    private void uploadCover(String downloadUri) {

        //get Image from imageview
        Bitmap bitmap = ((BitmapDrawable)videoCover.getDrawable()).getBitmap();
        ByteArrayOutputStream boas = new ByteArrayOutputStream();
        //image compress
        bitmap.compress(Bitmap.CompressFormat.PNG,10,boas);
        byte[] data = boas.toByteArray();

        String timestamp = ""+ System.currentTimeMillis();

        final StorageReference ref = storageReference.child(timestamp + ".jpg");
        ref.putBytes(data)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                        Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                        while (!uriTask.isSuccessful());

                        String cover = uriTask.getResult().toString();

                        notify = true;
                        sendMessageVideo(fuser.getUid(),user_id,downloadUri,cover);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        Toast.makeText(SendVideoFilesActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

    }

    private void sendMessageVideo(String sender, String receiver, String downloadUri, String cover) {

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        String timestamp = ""+ System.currentTimeMillis();


        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("sender",sender);
        hashMap.put("receiver",receiver);
        hashMap.put("message","video");
        hashMap.put("video",downloadUri);
        hashMap.put("messageId",timestamp);
        hashMap.put("messageVideoDuration",durationString);
        hashMap.put("videoLegend",legend);
        hashMap.put("videoCover",cover);
        hashMap.put("type","video");
        hashMap.put("isseen",false);
        hashMap.put("timestamp",timestamp);
        reference.child("Messages").child(timestamp).setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                if (task.isSuccessful()){

                    Intent intent = new Intent(SendVideoFilesActivity.this,ChatActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_TASK_ON_HOME);
                    intent.putExtra("user_id",user_id);
                    startActivity(intent);
                    finish();
                    return;
                }
            }
        });

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


        final String msg = "video ";

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

}