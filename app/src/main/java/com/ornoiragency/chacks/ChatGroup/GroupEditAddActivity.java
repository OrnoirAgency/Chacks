package com.ornoiragency.chacks.ChatGroup;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.Continuation;
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
import com.google.firebase.storage.UploadTask;
import com.ornoiragency.chacks.MainActivity;
import com.ornoiragency.chacks.R;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.UUID;

import id.zelory.compressor.Compressor;

public class GroupEditAddActivity extends AppCompatActivity {

    private String groupId;

    ImageView group_image;
    EditText group_name,group_desc;
    Button updateGroup_btn;

    Uri fileUri;
    String myurl;
    UploadTask uploadTask;
    private Bitmap compressedImageFile;
    StorageReference storageReference;
    FirebaseAuth firebaseAuth;
    FirebaseUser fUser;

    private ProgressDialog loadingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_edit_add);


        firebaseAuth = FirebaseAuth.getInstance();
        fUser = firebaseAuth.getCurrentUser();
        storageReference = FirebaseStorage.getInstance().getReference().child("Groups Images");

        loadingBar = new ProgressDialog(this);

        updateGroup_btn = findViewById(R.id.updateGroup_btn);
        group_image = findViewById(R.id.group_image);
        group_name = findViewById(R.id.group_text);
        group_desc = findViewById(R.id.group_desc);

        groupId = getIntent().getStringExtra("groupId");
        loadGroupInfo();

        updateGroup_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                startUpdatingGroup();
            }
        });

        group_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .start(GroupEditAddActivity.this);
            }
        });
    }

    private void startUpdatingGroup() {

        String groupTitle = group_name.getText().toString().trim();
        String groupDescription = group_desc.getText().toString().trim();

        // validate data
        if (TextUtils.isEmpty(groupTitle)){
            Toast.makeText(this, "Group title required...", Toast.LENGTH_SHORT).show();
            return;
        }
        loadingBar.setTitle("Update group info");
        loadingBar.setMessage("Please wait...");
        loadingBar.setCanceledOnTouchOutside(false);
        loadingBar.show();

        if (fileUri == null){
            //update group without icon

            HashMap<String,Object> hashMap = new HashMap<>();
            hashMap.put("groupTitle",groupTitle);
            hashMap.put("groupDescription",groupDescription);

            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
            ref.child(groupId).updateChildren(hashMap)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            loadingBar.dismiss();
                            Toast.makeText(GroupEditAddActivity.this, "Group info updated...", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(GroupEditAddActivity.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    loadingBar.dismiss();
                    Toast.makeText(GroupEditAddActivity.this, ""+ e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

        } else {

            final String randomName = UUID.randomUUID().toString();

            // PHOTO UPLOAD
            File newImageFile = new File(fileUri.getPath());
            try {

                compressedImageFile = new Compressor(GroupEditAddActivity.this)
                        .setQuality(20)
                        .compressToBitmap(newImageFile);

            } catch (IOException e) {
                e.printStackTrace();
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            compressedImageFile.compress(Bitmap.CompressFormat.JPEG, 20, baos);
            byte[] imageData = baos.toByteArray();

            final StorageReference ref = storageReference.child(randomName + ".jpg");
            uploadTask = ref.putBytes(imageData);


            uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }

                    // Continue with the task to get the download URL
                    return ref.getDownloadUrl();

                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {
                        Uri downloadUri = task.getResult();
                        myurl = downloadUri.toString();

                        HashMap<String,Object> hashMap = new HashMap<>();
                        hashMap.put("groupTitle",groupTitle);
                        hashMap.put("groupDescription",groupDescription);
                        hashMap.put("groupIcon",myurl);

                        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
                        ref.child(groupId).updateChildren(hashMap)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        loadingBar.dismiss();
                                        Toast.makeText(GroupEditAddActivity.this, "Group info updated...", Toast.LENGTH_SHORT).show();
                                        Intent intent = new Intent(GroupEditAddActivity.this, MainActivity.class);
                                        startActivity(intent);
                                        finish();
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                loadingBar.dismiss();
                                Toast.makeText(GroupEditAddActivity.this, ""+ e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });


                    } else {
                        // Handle failures
                        // ...
                        loadingBar.dismiss();
                    }
                }
            });

        }
    }

    private void loadGroupInfo() {

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
        ref.orderByChild("groupId").equalTo(groupId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        for (DataSnapshot ds: dataSnapshot.getChildren()){

                            String groupId = "" + ds.child("groupId").getValue();
                            String groupTitle = "" + ds.child("groupTitle").getValue();
                            String groupDescription = "" + ds.child("groupDescription").getValue();
                            String groupIcon = "" + ds.child("groupIcon").getValue();
                            String timestamp = "" + ds.child("timestamp").getValue();
                            String createdBy = "" + ds.child("createdBy").getValue();

                            Calendar cal = Calendar.getInstance(Locale.FRENCH);
                            cal.setTimeInMillis(Long.parseLong(timestamp));
                            String dateTime = DateFormat.format("dd/MM/yyyy hh:mm ",cal).toString();

                            group_name.setText(groupTitle);
                            group_desc.setText(groupDescription);

                            try {
                                Glide.with(GroupEditAddActivity.this)
                                        .asBitmap()
                                        .load(groupIcon)
                                        .placeholder(R.drawable.image_placeholder)
                                        .into(group_image);

                            } catch (Exception e){
                                group_image.setImageResource(R.drawable.profile_image);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {

                fileUri = result.getUri();
                group_image.setImageURI(fileUri);

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {

                Exception error = result.getError();

            }
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }
}
