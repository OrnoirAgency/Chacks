package com.ornoiragency.chacks.Post;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
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
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.ornoiragency.chacks.MainActivity;
import com.ornoiragency.chacks.PageTransformer;
import com.ornoiragency.chacks.R;
import com.vanniktech.emoji.EmojiEditText;
import com.vanniktech.emoji.EmojiPopup;


import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

public class PostTextActivity extends AppCompatActivity {

    private static final String TAG = "PostText";

    private DatabaseReference UsersRef;
    private FirebaseAuth mAuth;
    private ProgressDialog pd;

    private String uid,name,image;

    private ActionBar actionBar;
    //edit info
    EmojiEditText post_edit_ext;
    EmojiPopup emojiPopup;
    ImageButton emojiButton;
    View rootView;

    private Button post_btn;
    //edit info
    String editDescription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_text);

        actionBar = getSupportActionBar();
        actionBar.setTitle("Add Post");
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

        post_edit_ext = findViewById(R.id.new_post_text);
        post_btn = findViewById(R.id.post_btn);

        rootView = findViewById(R.id.root_view);
        emojiButton = findViewById(R.id.main_activity_emoji);
        emojiButton.setColorFilter(ContextCompat.getColor(this, R.color.icon_checked), PorterDuff.Mode.SRC_IN);
        emojiButton.setOnClickListener(ignore -> emojiPopup.toggle());
        setUpEmojiPopup();

        Intent intent = getIntent();

        // get data and type from intent
        String action = intent.getAction();
        String type = intent.getType();

        if (Intent.ACTION_SEND.equals(action) && type!=null){

            if ("text/plain".equals(type)){
                handleSendText(intent);
            }
        }

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

        String isUpdateKey = ""+intent.getStringExtra("key");
        String editPostId = ""+intent.getStringExtra("editPostId");

        if (isUpdateKey.equals("editPost")){

            //update
            actionBar.setTitle(getString(R.string.upload));
            post_btn.setText(getString(R.string.update));
            loadPostData(editPostId);

        } else {
            actionBar.setTitle(getString(R.string.upload));
            post_btn.setText(getString(R.string.upload));
        }


        post_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                String description = post_edit_ext.getText().toString().trim();
                if (TextUtils.isEmpty(description)){
                    Toast.makeText(PostTextActivity.this, "Enter description", Toast.LENGTH_SHORT).show();
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
    private void handleSendText(Intent intent) {

        String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
        if (sharedText!=null){

            post_edit_ext.setText(sharedText);
        }
    }

    private void beginUpdate(String description, String editPostId) {

        pd.setMessage("Updating post...");
        pd.show();

            //with image
            updateWasWithImage(description,editPostId);

    }

    private void loadPostData(String editPostId) {

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
        Query fquery = ref.orderByChild("pId").equalTo(editPostId);
        fquery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for (DataSnapshot ds: dataSnapshot.getChildren()){

                    editDescription = ""+ds.child("pText").getValue();
                    post_edit_ext.setText(editDescription);


                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void updateWasWithImage(String description, String editPostId) {

        HashMap postsMap = new HashMap();
        postsMap.put("uid", uid);
        postsMap.put("pText", description);
        postsMap.put("type","text");
        postsMap.put("uImage", image);
        postsMap.put("uName", name);

        //database
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
        ref.child(editPostId).updateChildren(postsMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        pd.dismiss();
                        Toast.makeText(PostTextActivity.this, "Updating...", Toast.LENGTH_SHORT).show();
                        SendUserToMainActivity();

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        pd.dismiss();
                        Toast.makeText(PostTextActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

    }

    private void uploadData(String description) {

        pd.setMessage("publishing post");
        pd.show();
        String timestamp = String.valueOf(System.currentTimeMillis());

        HashMap postsMap = new HashMap();
        postsMap.put("uid", uid);
        postsMap.put("pLikes", "0");
        postsMap.put("pComments", "0");
        postsMap.put("pText",description);
        postsMap.put("search",description);
        postsMap.put("type","text");
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
                        Toast.makeText(PostTextActivity.this, "post published", Toast.LENGTH_SHORT).show();
                        SendUserToMainActivity();
                        post_edit_ext.setText("");

                        //send notification
                        prepareNotification(
                                ""+timestamp,
                                ""+name+""+getString(R.string.add_post),
                                ""+description,
                                "text",
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
                        Toast.makeText(PostTextActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
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
                        Toast.makeText(PostTextActivity.this, ""+error.toString(), Toast.LENGTH_SHORT).show();
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
        hashMap.put("type","text");

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
                .build(post_edit_ext);
    }
    private void SendUserToMainActivity()
    {
        Intent mainIntent = new Intent(PostTextActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
    }
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }
}
