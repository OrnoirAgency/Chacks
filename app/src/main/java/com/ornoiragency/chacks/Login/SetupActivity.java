package com.ornoiragency.chacks.Login;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

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
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.ornoiragency.chacks.Activity.EditProfileActivity;
import com.ornoiragency.chacks.MainActivity;
import com.ornoiragency.chacks.R;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;
import me.alvince.android.avatarimageview.AvatarImageView;

public class SetupActivity extends AppCompatActivity {

    EditText s_user_name;
    Button s_button;
    FirebaseUser user;
    DatabaseReference mUserDB;

    ProgressDialog pd;
    CircleImageView circleImageView;

    String image = "https://firebasestorage.googleapis.com/v0/b/chacks-296cb.appspot.com/o/profile_image%2Fblank-profile-picture-973460_1280-1024x1024.png?alt=media&token=d4a1f045-eeb4-47e8-aadb-73edaf23652c";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        s_user_name = findViewById(R.id.s_user_name);
        s_button = findViewById(R.id.s_button);
        circleImageView = findViewById(R.id.circleImageView);

        user = FirebaseAuth.getInstance().getCurrentUser();

        pd = new ProgressDialog(this);

        s_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                SaveInfos();

            }
        });


    }

    private void SaveInfos() {

        String name = s_user_name.getText().toString().trim();

        if (TextUtils.isEmpty(name)){

            Toast.makeText(this, "write user name", Toast.LENGTH_SHORT).show();

        } else if (!TextUtils.isEmpty(name)){
            pd.setMessage("Saving...");
            pd.show();



            Map<String, Object> userMap = new HashMap<>();
            userMap.put("uid",user.getUid());
            userMap.put("phone", user.getPhoneNumber());
            userMap.put("name", name);
            userMap.put("image", image);
            userMap.put("search", name);
            userMap.put("onlineStatus","");
            userMap.put("typingTo","noOne");

            mUserDB = FirebaseDatabase.getInstance().getReference().child("user").child(user.getUid());
            mUserDB.updateChildren(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {

                    if (task.isSuccessful()){
                        Intent intent = new Intent(SetupActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                        pd.dismiss();
                    }
                }
            });

        }

    }



}
