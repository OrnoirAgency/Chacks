package com.ornoiragency.chacks.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
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
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.google.gson.Gson;
import com.ornoiragency.chacks.Adapter.MediaAdapter;
import com.ornoiragency.chacks.Notification.Data;
import com.ornoiragency.chacks.Notification.Sender;
import com.ornoiragency.chacks.Notification.Token;
import com.ornoiragency.chacks.R;
import com.ornoiragency.chacks.models.User;


import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;

public class SendMediaFilesActivity extends AppCompatActivity {
    int PICK_IMAGE_INTENT = 1;

    MaterialButton sendImage,addImage;
    String image,user_id,myurl,legend;
    private Uri ImageUri;

    ImageView back;
    CircleImageView profile_image;
    EditText image_legend;

    FirebaseUser fuser;
    DatabaseReference reference,ref;
    StorageReference storageReference;
    private Bitmap compressedImageFile;
    private StorageTask uploadTask;

    boolean notify = false;
    //volley request for notification
    private RequestQueue requestQueue;
    private ProgressDialog pd;

    private RecyclerView.Adapter mMediaAdapter;
    ArrayList<String> mediaUriList = new ArrayList<>();
    ArrayList urlStrings;
    private int upload_count = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_media_files);

        requestQueue = Volley.newRequestQueue(getApplicationContext());
        ref = FirebaseDatabase.getInstance().getReference();
        storageReference = FirebaseStorage.getInstance().getReference().child("Messages Images");

        pd = new ProgressDialog(this);

        Intent intent = getIntent();
       // image = intent.getStringExtra("imageUri");
        user_id = intent.getStringExtra("user_id");
       // ImageUri = Uri.parse(image);

        image_legend = findViewById(R.id.image_legend);
        sendImage = findViewById(R.id.sendImage);
        addImage = findViewById(R.id.addImage);


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

        sendImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                legend = image_legend.getText().toString().trim();
             //  sendImage();
               upload();
            }
        });
        addImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                openGallery();
            }
        });

        initializeMedia();
    }



    //send message image
    private void sendMessageImage(String sender, String receiver, String myurl, ArrayList urlStrings) {

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        String timestamp = ""+ System.currentTimeMillis();


        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("sender",sender);
        hashMap.put("receiver",receiver);
        hashMap.put("message","photo");
        hashMap.put("image",myurl);
        hashMap.put("messageId",timestamp);
        hashMap.put("type","multiple_image");
        hashMap.put("isseen",false);
        hashMap.put("timestamp",timestamp);

        HashMap<String, Object> ImageMap = new HashMap<>();

        for (int i = 0; i <urlStrings.size() ; i++) {
            ImageMap.put("image"+i, urlStrings.get(i));

        }

        String str3 = "media";


        reference.child("Chats").child(timestamp).setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                if (task.isSuccessful()){

                    ref.child("Chats").child(timestamp).child(str3).updateChildren(ImageMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            Intent intent = new Intent(SendMediaFilesActivity.this,ChatActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_TASK_ON_HOME);
                            intent.putExtra("user_id",user_id);
                            startActivity(intent);

                        }
                    });

                }
            }
        });

        // add user to chat fragment
        final DatabaseReference chatRef = FirebaseDatabase.getInstance().getReference("Chatlist")
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

        final DatabaseReference chatRefReceiver = FirebaseDatabase.getInstance().getReference("Chatlist")
                .child(user_id)
                .child(fuser.getUid());
        chatRefReceiver.child("id").setValue(fuser.getUid());


        final String msg = "photo";

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

    private void initializeMedia() {
        this.mediaUriList = new ArrayList<>();
        RecyclerView mMedia = (RecyclerView) findViewById(R.id.mediaList);
        mMedia.setNestedScrollingEnabled(false);
        mMedia.setHasFixedSize(false);
        mMedia.setLayoutManager(new LinearLayoutManager(getApplicationContext(), RecyclerView.HORIZONTAL, false));
        this.mMediaAdapter =  new MediaAdapter(getApplicationContext(), this.mediaUriList);
        mMedia.setAdapter((RecyclerView.Adapter) this.mMediaAdapter);
    }


    /* access modifiers changed from: private */
    public void openGallery() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.putExtra("android.intent.extra.ALLOW_MULTIPLE", true);
        intent.setAction("android.intent.action.GET_CONTENT");
        startActivityForResult(Intent.createChooser(intent, "Select Picture(s)"), this.PICK_IMAGE_INTENT);
    }

    /* access modifiers changed from: protected */
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == -1 && requestCode == this.PICK_IMAGE_INTENT) {
            if (data.getClipData() == null) {
                this.mediaUriList.add(data.getData().toString());
            } else {
                for (int i = 0; i < data.getClipData().getItemCount(); i++) {
                    this.mediaUriList.add(data.getClipData().getItemAt(i).getUri().toString());
                }
            }
            this.mMediaAdapter.notifyDataSetChanged();
        }
    }

   private void upload(){

       urlStrings = new ArrayList<>();
       pd.show();
       StorageReference ImageFolder = FirebaseStorage.getInstance().getReference().child("Messages Images");

       for (upload_count = 0; upload_count < mediaUriList.size(); upload_count++) {

           Uri IndividualImage = Uri.parse(mediaUriList.get(upload_count));
           final StorageReference ImageName = ImageFolder.child("Images" + IndividualImage.getLastPathSegment());

           ImageName.putFile(IndividualImage).addOnSuccessListener(
                   new OnSuccessListener<UploadTask.TaskSnapshot>() {
                       @Override
                       public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                           ImageName.getDownloadUrl().addOnSuccessListener(
                                   new OnSuccessListener<Uri>() {
                                       @Override
                                       public void onSuccess(Uri uri) {

                                           urlStrings.add(String.valueOf(uri));

                                           if (urlStrings.size() == mediaUriList.size()){
                                               notify = true;
                                               sendMessageImage(fuser.getUid(),user_id,myurl,urlStrings);
                                               pd.dismiss();
                                           }

                                       }
                                   }
                           );
                       }
                   }
           ).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
               @Override
               public void onProgress(@NonNull UploadTask.TaskSnapshot taskSnapshot) {

                   double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                   // System.out.println("Upload is " + progress + "% done");
                   pd.setMessage((int) progress + "% uploading");
               }
           });


       }


   }



}