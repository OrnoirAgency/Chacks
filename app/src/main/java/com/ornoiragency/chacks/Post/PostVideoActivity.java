package com.ornoiragency.chacks.Post;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.VideoView;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnPausedListener;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.ornoiragency.chacks.MainActivity;
import com.ornoiragency.chacks.R;


import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

public class PostVideoActivity extends AppCompatActivity {

    private static final int VIDEO_REQUEST = 1000;

    private DatabaseReference UsersRef;
    private FirebaseAuth mAuth;
    private ProgressDialog pd;

    private String uid,name,image;

    private ActionBar actionBar;

    VideoView post_video;
    EditText post_desc;
    Button post_btn,pick_video;

    Uri videoUri;
    ImageView video_cover;
    String durationString;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_video);

        post_video = findViewById(R.id.new_post_video);
        post_desc = findViewById(R.id.new_post_desc);
        post_btn = findViewById(R.id.post_btn);
        pick_video = findViewById(R.id.pick_video);

        video_cover = findViewById(R.id.video_cover);

        post_video.requestFocus();
        post_video.start();

        post_video.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {

               int  duration = mp.getDuration()/1000;
                 durationString = String.format("%02d:%02d",duration/60,duration % 60);
               // durationTimer.setText(durationString);

            }
        });

        pd = new ProgressDialog(this);
        mAuth = FirebaseAuth.getInstance();
        uid = mAuth.getCurrentUser().getUid();

        UsersRef = FirebaseDatabase.getInstance().getReference().child("user");

        Query query = UsersRef.orderByChild("uid").equalTo(uid);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for (DataSnapshot ds: dataSnapshot.getChildren()){

                    name = ""+ ds.child("name").getValue();
                    image = ""+ ds.child("image").getValue();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        pick_video.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                openGalleryForVideo();
            }
        });

        post_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String description = post_desc.getText().toString().trim();
                if (TextUtils.isEmpty(description)){
                    Toast.makeText(PostVideoActivity.this, "Enter description", Toast.LENGTH_SHORT).show();

                } else {

                    uploadData(description,videoUri);

                }

            }
        });


    }

    public void openGalleryForVideo() {
        Intent intent = new Intent(
                Intent.ACTION_PICK,
                android.provider.MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
        long maxVideoSize = 5 * 1024 * 1024; // 10 MB
        intent.putExtra(MediaStore.EXTRA_SIZE_LIMIT, maxVideoSize);
        startActivityForResult(Intent.createChooser(intent, "Select video"), VIDEO_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == VIDEO_REQUEST && null != data) {
            if (requestCode == VIDEO_REQUEST) {

                videoUri = data.getData();
                post_video.setVideoURI(videoUri);

                Glide.with(this)
                        .load(videoUri)
                        .into(video_cover);

            }
        }

    }

    private void uploadData(String description, Uri videoUri) {

       // pd.setMessage("publishing post");
        pd.show();
        pd.setCanceledOnTouchOutside(false);
        String timestamp = String.valueOf(System.currentTimeMillis());
        String filePathName = "Posts/" + "post_" + timestamp;

            if (post_video != null){

                StorageReference ref = FirebaseStorage.getInstance().getReference(filePathName + ".mp4");
                ref.putFile(videoUri)
                        .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {


                                Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                                while (!uriTask.isSuccessful());

                                String downloadUri = uriTask.getResult().toString();

                                if (uriTask.isSuccessful()){

                                    uploadCover(downloadUri,description);
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


    }

    private void uploadCover(String downloadUri, String description) {

        String timestamp = String.valueOf(System.currentTimeMillis());
        String filePathName = "Posts/" + "post_" + timestamp;

        if (video_cover.getDrawable() != null){

            Bitmap bitmap = ((BitmapDrawable) video_cover.getDrawable()).getBitmap();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 60, baos);
            byte[] imageData = baos.toByteArray();

            StorageReference ref = FirebaseStorage.getInstance().getReference(filePathName + ".jpg");
            ref.putBytes(imageData).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                    Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                    while (!uriTask.isSuccessful());

                    String downloadVideoCoverUri = uriTask.getResult().toString();

                    if (uriTask.isSuccessful()){

                        HashMap postsMap = new HashMap();
                        postsMap.put("uid", uid);
                        postsMap.put("pDescr", description);
                        postsMap.put("search", description);
                        postsMap.put("pLikes", "0");
                        postsMap.put("pComments", "0");
                        postsMap.put("type","video");
                        postsMap.put("videoCover",downloadVideoCoverUri);
                        postsMap.put("pVideoDuration",durationString);
                        postsMap.put("pVideo", downloadUri);
                        postsMap.put("uImage", image);
                        postsMap.put("uName", name);
                        postsMap.put("pTime", timestamp);
                        postsMap.put("pId",timestamp);

                        //database
                        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
                        ref.child(timestamp).setValue(postsMap)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {

                                        pd.dismiss();
                                        Toast.makeText(PostVideoActivity.this, "post published", Toast.LENGTH_SHORT).show();
                                        Intent intent = new Intent(PostVideoActivity.this, MainActivity.class);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        startActivity(intent);
                                        post_desc.setText("");
                                        post_video.setVideoURI(null);


                                        //send notification
                                        prepareNotification(
                                                ""+timestamp,
                                                ""+name+""+getString(R.string.add_post),
                                                ""+description,
                                                "video",
                                                ""+image,
                                                "PostNotification",
                                                "POST");

                                        addToHisNotification(""+uid,""+timestamp,getString(R.string.add_post));
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {

                                        pd.dismiss();
                                        Toast.makeText(PostVideoActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    pd.dismiss();
                }
            });
        }
    }


    private void prepareNotification(String pId ,String title,String description,String type,String user_image,String notificationType,String notificationTopic){

        String NOTIFICATION_TOPIC = "/topics/" + notificationTopic;
        String NOTIFICATION_TITLE = title;
        String NOTIFICATION_MESSAGE = description;
        String NOTIFICATION_TYPE = notificationType;

        //prepare json what to send and where to send
        JSONObject notificationJo = new JSONObject();
        JSONObject notificationBodyJo = new JSONObject();

        try {
            //what to send
            notificationBodyJo.put("notificationType",NOTIFICATION_TYPE);
            notificationBodyJo.put("sender",uid);
            notificationBodyJo.put("pId",pId);
            notificationBodyJo.put("type",type);
            notificationBodyJo.put("user_image",user_image);
            notificationBodyJo.put("pTitle",NOTIFICATION_TITLE);
            notificationBodyJo.put("pDescription",NOTIFICATION_MESSAGE);

            //where to send
            notificationJo.put("to",NOTIFICATION_TOPIC);

            notificationJo.put("data",notificationBodyJo);
        } catch (JSONException e) {
            Toast.makeText(this, ""+ e.getMessage(), Toast.LENGTH_SHORT).show();
        }

        sendPostNotification(notificationJo);

    }

    private void sendPostNotification(JSONObject notificationJo) {


        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest("https://fcm.googleapis.com/fcm/send", notificationJo,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        Log.d("FCM_RESPONSE","onResponse:" +response.toString());
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(PostVideoActivity.this, ""+error.toString(), Toast.LENGTH_SHORT).show();
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
        Volley.newRequestQueue(this).add(jsonObjectRequest);

    }

    private void addToHisNotification(String hisUid,String pId,String notification){

        String timestamp = ""+System.currentTimeMillis();

        HashMap<Object,String> hashMap = new HashMap<>();
        hashMap.put("pId",pId);
        hashMap.put("timestamp",timestamp);
        hashMap.put("notification",notification);
        hashMap.put("sUid",uid);
        hashMap.put("type","video");

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Notifications");
        ref.child(timestamp).setValue(hashMap)
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


}
