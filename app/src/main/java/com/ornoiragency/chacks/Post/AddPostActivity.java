package com.ornoiragency.chacks.Post;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.ornoiragency.chacks.Activity.EditProfileActivity;
import com.ornoiragency.chacks.MainActivity;
import com.ornoiragency.chacks.PageTransformer;
import com.ornoiragency.chacks.R;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;
import com.vanniktech.emoji.EmojiEditText;
import com.vanniktech.emoji.EmojiPopup;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import id.zelory.compressor.Compressor;

public class AddPostActivity extends AppCompatActivity {

    private static final String TAG = "Post";

    private EmojiEditText post_desc;
    private Button post_btn;
    private ImageView post_image;
    private Uri ImageUri;
    private Bitmap compressedImageFile;
    private DatabaseReference UsersRef;
    private FirebaseAuth mAuth;
    private ProgressDialog pd;
    private String uid,name,image;

    EmojiPopup emojiPopup;
    ImageButton emojiButton;
    View rootView;

    //edit info
    String editDescription,editImage;
    Button PickGallery;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_add);

        post_desc = findViewById(R.id.new_post_desc);
        post_image = findViewById(R.id.new_post_image);
        post_btn = findViewById(R.id.post_btn);
        PickGallery = findViewById(R.id.PickGallery);

        rootView = findViewById(R.id.root_view);
        emojiButton = findViewById(R.id.main_activity_emoji);
        emojiButton.setColorFilter(ContextCompat.getColor(this, R.color.icon_checked), PorterDuff.Mode.SRC_IN);
        emojiButton.setOnClickListener(ignore -> emojiPopup.toggle());
        setUpEmojiPopup();

        pd = new ProgressDialog(this);
        mAuth = FirebaseAuth.getInstance();
        uid = mAuth.getCurrentUser().getUid();

        Intent intent = getIntent();

       // get data and type from intent
        String action = intent.getAction();
        String type = intent.getType();

        if (Intent.ACTION_SEND.equals(action) && type!=null){

            if ("text/plain".equals(type)){
                handleSendText(intent);
            } else if (type.startsWith("image")){
                handleSendImage(intent);
            }
        }

        String isUpdateKey = ""+intent.getStringExtra("key");
        String editPostId = ""+intent.getStringExtra("editPostId");

        if (isUpdateKey.equals("editPost")){

            //update
            post_btn.setText(getString(R.string.update));
            loadPostData(editPostId);

        } else {
            post_btn.setText(getString(R.string.upload));
        }

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

        PickGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                OpenGallery();
            }
        });

        post_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                String description = post_desc.getText().toString().trim();
                if (TextUtils.isEmpty(description)){
                    Toast.makeText(AddPostActivity.this, "Enter description", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (isUpdateKey.equals("editPost")){

                    beginUpdate(description,editPostId);
                } else {
                    uploadData(description);

                }


            }
        });
    }

    private void handleSendImage(Intent intent) {

        Uri imageURI = intent.getParcelableExtra(Intent.EXTRA_STREAM);
        if (imageURI != null){
            ImageUri = imageURI;
            post_image.setImageURI(ImageUri);
        }
    }

    private void handleSendText(Intent intent) {

        String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
        if (sharedText!=null){

            post_desc.setText(sharedText);
        }
    }

    private void beginUpdate(String description, String editPostId) {

        pd.setMessage("Updating post...");
        pd.show();

        if (!editImage.equals("noImage")){

            //with image
            updateWasWithImage(description,editPostId);

        }
    }

    private void updateWasWithImage(String description, String editPostId) {

        StorageReference mPictureRef = FirebaseStorage.getInstance().getReferenceFromUrl(editImage);
        mPictureRef.delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        String timestamp = String.valueOf(System.currentTimeMillis());
                        String filePathName = "Posts/"+ "post_" + timestamp;

                        //get Image from imageview
                        Bitmap bitmap = ((BitmapDrawable)post_image.getDrawable()).getBitmap();
                        ByteArrayOutputStream boas = new ByteArrayOutputStream();
                        //image compress
                        bitmap.compress(Bitmap.CompressFormat.PNG,15,boas);
                        byte[] data = boas.toByteArray();

                        StorageReference ref = FirebaseStorage.getInstance().getReference().child(filePathName);
                        ref.putBytes(data)
                                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                                        Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                                        while (!uriTask.isSuccessful());

                                        String downloadUri = uriTask.getResult().toString();
                                        if (uriTask.isSuccessful()){

                                            HashMap postsMap = new HashMap();
                                            postsMap.put("uid", uid);
                                            postsMap.put("pDescr", description);
                                            postsMap.put("type","image");
                                            postsMap.put("pImage", downloadUri);
                                            postsMap.put("uImage", image);
                                            postsMap.put("uName", name);

                                            //database
                                            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
                                            ref.child(editPostId).updateChildren(postsMap)
                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void aVoid) {

                                                            pd.dismiss();
                                                            Toast.makeText(AddPostActivity.this, "Updating...", Toast.LENGTH_SHORT).show();

                                                        }
                                                    })
                                                    .addOnFailureListener(new OnFailureListener() {
                                                        @Override
                                                        public void onFailure(@NonNull Exception e) {

                                                            pd.dismiss();
                                                            Toast.makeText(AddPostActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                                                        }
                                                    });
                                        }
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {

                                        Toast.makeText(AddPostActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                });
    }

    private void loadPostData(String editPostId) {

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
        Query fquery = ref.orderByChild("pId").equalTo(editPostId);
        fquery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for (DataSnapshot ds: dataSnapshot.getChildren()){

                    editDescription = ""+ds.child("pDescr").getValue();
                    editImage = ""+ds.child("pImage").getValue();

                    post_desc.setText(editDescription);
                    //set image
                    if (!editImage.equals("noImage")){
                        try {

                            Glide.with(AddPostActivity.this)
                                    .asBitmap()
                                    .load(editImage)
                                    .into(post_image);
                        } catch (Exception e){

                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void uploadData(String description) {

        pd.setMessage("publishing post");
        pd.show();
        String timestamp = String.valueOf(System.currentTimeMillis());
        String filePathName = "Posts/" + "post_" + timestamp;

        if (post_image.getDrawable() != null){

            // PHOTO UPLOAD
            File newImageFile = new File(ImageUri.getPath());
            try {

                compressedImageFile = new Compressor(AddPostActivity.this)
                        .setQuality(70)
                        .compressToBitmap(newImageFile);

            } catch (IOException e) {
                e.printStackTrace();
            }

            Bitmap bitmap = ((BitmapDrawable) post_image.getDrawable()).getBitmap();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 70, baos);
            byte[] imageData = baos.toByteArray();


            StorageReference ref = FirebaseStorage.getInstance().getReference(filePathName);
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
                                    postsMap.put("pDescr", description);
                                    postsMap.put("search", description);
                                    postsMap.put("pLikes", "0");
                                    postsMap.put("pComments", "0");
                                    postsMap.put("type","image");
                                    postsMap.put("pImage", downloadUri);
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
                                                    Toast.makeText(AddPostActivity.this, "post published", Toast.LENGTH_SHORT).show();
                                                    SendUserToMainActivity();
                                                    post_desc.setText("");
                                                    post_image.setImageURI(null);
                                                    ImageUri = null;

                                                    //send notification
                                                    prepareNotification(
                                                            ""+timestamp,
                                                            ""+name+" "+getString(R.string.add_post),
                                                            ""+description,
                                                            "image",
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
                                                    Toast.makeText(AddPostActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                                                }
                                            });
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
                        Toast.makeText(AddPostActivity.this, ""+error.toString(), Toast.LENGTH_SHORT).show();
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
        hashMap.put("type","image");

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

    private void OpenGallery()
    {
        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .start(AddPostActivity.this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);


        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE){

            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if (resultCode == RESULT_OK)
            {
                ImageUri = result.getUri();
                post_image.setImageURI(ImageUri);

            }
            else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE)
            {
                Exception error = result.getError();
            }
        }

    }

    private void SendUserToMainActivity()
    {
        Intent mainIntent = new Intent(AddPostActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
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
                .build(post_desc);
    }
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }
}
