package com.ornoiragency.chacks.Post;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.loader.content.CursorLoader;

import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

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
import com.ornoiragency.chacks.Activity.SendVideoFilesActivity;
import com.ornoiragency.chacks.MainActivity;
import com.ornoiragency.chacks.R;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;

public class PostAudioActivity extends AppCompatActivity {

    private static final int AUDIO_REQUEST = 1000;

    private DatabaseReference UsersRef;
    private FirebaseAuth mAuth;
    private ProgressDialog pd;

    private String uid,name,image;
    private ActionBar actionBar;

    ImageView playPauseBtn;
    TextView post_audio_desc,totalDurationTime;
    ProgressBar progressBar;

    Button audioBtn,pickCover;

    MediaPlayer mediaPlayer;

    private Uri ImageUri;
    private Bitmap compressedImageFile;
    Uri AudioUri;
    String path;

    CircleImageView post_audio_cover;

    EditText post_artist,post_title,post_song_desc;
    Button post_btn;
    TextView post_audio_artist,post_audio_title;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_audio);

        progressBar = findViewById(R.id.progressBar);
        pickCover = findViewById(R.id.audio_pick_cover);
        post_audio_cover = findViewById(R.id.post_audio_cover);
        post_audio_artist = findViewById(R.id.post_audio_artist);
        post_audio_title = findViewById(R.id.post_audio_title);
        post_audio_desc = findViewById(R.id.post_audio_desc);
        post_song_desc = findViewById(R.id.post_song_desc);

        pd = new ProgressDialog(this);
        mAuth = FirebaseAuth.getInstance();
        uid = mAuth.getCurrentUser().getUid();

        post_artist = findViewById(R.id.post_song_artist);
        post_title = findViewById(R.id.post_song_title);
        post_btn = findViewById(R.id.post_btn);

        post_artist.addTextChangedListener(new TextWatcher() {

            public void afterTextChanged(Editable s) {}

            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {
                post_audio_artist.setText(s);

            }
        });

        post_title.addTextChangedListener(new TextWatcher() {

            public void afterTextChanged(Editable s) {}

            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {
                post_audio_title.setText(s);

            }
        });

        post_song_desc.addTextChangedListener(new TextWatcher() {

            public void afterTextChanged(Editable s) {}

            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {
                post_audio_desc.setText(s);

            }
        });


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

        post_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String artist = post_artist.getText().toString().trim();
                String title = post_title.getText().toString().trim();
                String description = post_song_desc.getText().toString().trim();

                post_audio_title.setText(title);
                post_audio_artist.setText(artist);

                if (TextUtils.isEmpty(artist) && TextUtils.isEmpty(title)){
                    Toast.makeText(PostAudioActivity.this, "Complete info", Toast.LENGTH_SHORT).show();

                } else {

                    uploadData(artist,title,description,AudioUri);

                }

            }
        });

        pickCover.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                OpenGallery();
            }
        });


        playPauseBtn = findViewById(R.id.playPauseBtn);
        totalDurationTime = findViewById(R.id.totalDurationTime);

        audioBtn = findViewById(R.id.audio_button);

        audioBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                openGalleryForAudio();
            }
        });

        mediaPlayer = new MediaPlayer();

        playPauseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mediaPlayer != null && mediaPlayer.isPlaying()){
                    mediaPlayer.pause();
                    playPauseBtn.setImageResource(R.drawable.ic_play);
                } else {
                    mediaPlayer.start();
                    playPauseBtn.setImageResource(R.drawable.ic_pause);

                }
            }
        });




        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {

                progressBar.setProgress(0);
                playPauseBtn.setImageResource(R.drawable.ic_play);
                totalDurationTime.setText("00");
                mediaPlayer.stop();
                prepareMediaPlayer(path);
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


    public void openGalleryForAudio() {
        Intent intent = new Intent(
                Intent.ACTION_PICK,
                android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(Intent.createChooser(intent, "Select Audio"), AUDIO_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == AUDIO_REQUEST && null != data) {
            if (requestCode == AUDIO_REQUEST) {

                 AudioUri = data.getData();

                try {
                    Uri uri= data.getData();
                     path = getRealPathFromURI(uri);
                     prepareMediaPlayer(path);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE){

            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if (resultCode == RESULT_OK)
            {
                ImageUri = result.getUri();
                post_audio_cover.setImageURI(ImageUri);

            }
            else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE)
            {
                Exception error = result.getError();
            }
        }

    }

    private void prepareMediaPlayer(String path)  {
        try {
            mediaPlayer.setDataSource(path);
            mediaPlayer.prepare();
            totalDurationTime.setText(milliSecondsToTimer(mediaPlayer.getDuration()));
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private String getRealPathFromURI(Uri contentUri) {
        String[] proj = { MediaStore.Images.Media.DATA };
        CursorLoader loader = new CursorLoader(getApplicationContext(), contentUri, proj, null, null, null);
        Cursor cursor = loader.loadInBackground();
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        String result = cursor.getString(column_index);
        cursor.close();
        return result;
    }


    private void uploadData(String artist, String title, String description, Uri audioUri) {

        // pd.setMessage("publishing post");
        pd.show();
        pd.setCanceledOnTouchOutside(false);
        String timestamp = String.valueOf(System.currentTimeMillis());
        String filePathName = "Posts/" + "post_song_" + timestamp;

        if (path != null){

            StorageReference ref = FirebaseStorage.getInstance().getReference(filePathName+".mp3");
            ref.putFile(audioUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {


                            Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                            while (!uriTask.isSuccessful());

                            String downloadAudioUri = uriTask.getResult().toString();

                          if (uriTask.isSuccessful()){

                              uploadCover(artist,title,description,audioUri,downloadAudioUri);
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


    private void uploadCover(String artist, String title, String description, Uri audioUri, String downloadAudioUri) {

        pd.show();
        pd.setCanceledOnTouchOutside(false);


        String timestamp = String.valueOf(System.currentTimeMillis());
        String filePathName = "Posts/" + "post_" + timestamp;

        MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        mmr.setDataSource(getApplicationContext(),audioUri);
        String durationStr = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        int millSecond = Integer.parseInt(durationStr);

        if (ImageUri != null){

            // PHOTO UPLOAD
            File newImageFile = new File(ImageUri.getPath());
            try {

                compressedImageFile = new Compressor(PostAudioActivity.this)
                        .setQuality(60)
                        .compressToBitmap(newImageFile);

            } catch (IOException e) {
                e.printStackTrace();
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            compressedImageFile.compress(Bitmap.CompressFormat.JPEG, 60, baos);
            byte[] imageData = baos.toByteArray();


            StorageReference ref = FirebaseStorage.getInstance().getReference(filePathName + ".jpg");
            ref.putBytes(imageData)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {


                            Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                            while (!uriTask.isSuccessful());

                            String downloadUri = uriTask.getResult().toString();

                            if (uriTask.isSuccessful()){

                                HashMap postsMap = new HashMap();
                                postsMap.put("uid", uid);
                                postsMap.put("artist", artist);
                                postsMap.put("title", title);
                                postsMap.put("audioDesc",description);
                                postsMap.put("search", artist);
                                postsMap.put("pLikes", "0");
                                postsMap.put("pComments", "0");
                                postsMap.put("type","audio");
                                postsMap.put("pAudioDuration",millSecond);
                                postsMap.put("pAudio", downloadAudioUri);
                                postsMap.put("pAudioCover", downloadUri);
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
                                                Toast.makeText(PostAudioActivity.this, "post published", Toast.LENGTH_SHORT).show();
                                                SendUserToMainActivity();
                                                SendUserToMainActivity();
                                                post_artist.setText("");
                                                post_title.setText("");

                                                //send notification
                                                prepareNotification(
                                                        ""+timestamp,
                                                        ""+name+""+getString(R.string.add_post),
                                                        ""+artist + title,
                                                        "audio",
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
                                                Toast.makeText(PostAudioActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        });

                                pd.dismiss();
                            }

                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            pd.dismiss();
                        }
                    });

        }

    }

    private void OpenGallery()
    {
        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .start(PostAudioActivity.this);
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
                        Toast.makeText(PostAudioActivity.this, ""+error.toString(), Toast.LENGTH_SHORT).show();
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
        hashMap.put("type","audio");

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

    private void SendUserToMainActivity()
    {
        Intent mainIntent = new Intent(PostAudioActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
    }
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }
}
