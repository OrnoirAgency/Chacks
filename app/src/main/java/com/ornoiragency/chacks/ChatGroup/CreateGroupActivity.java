package com.ornoiragency.chacks.ChatGroup;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
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
import java.util.HashMap;
import java.util.UUID;

import id.zelory.compressor.Compressor;

public class CreateGroupActivity extends AppCompatActivity {

    ImageView group_image;
    EditText group_name,group_desc;
    Button group_btn;

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
        setContentView(R.layout.activity_create_group);

        firebaseAuth = FirebaseAuth.getInstance();
        fUser = firebaseAuth.getCurrentUser();
        storageReference = FirebaseStorage.getInstance().getReference().child("Groups Images");

        loadingBar = new ProgressDialog(this);

        group_btn = findViewById(R.id.group_btn);
        group_image = findViewById(R.id.group_image);
        group_name = findViewById(R.id.group_text);
        group_desc = findViewById(R.id.group_desc);

        group_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                startCreatingGroup();
            }
        });

        group_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .start(CreateGroupActivity.this);
            }
        });
    }

    private void startCreatingGroup() {

        loadingBar.setTitle("create group ");
        loadingBar.setMessage("Please wait...");
        loadingBar.setCanceledOnTouchOutside(false);
        loadingBar.show();


        String g_timestamp = ""+ System.currentTimeMillis();
        String groupTitle = group_name.getText().toString().trim();
        String groupDescription = group_desc.getText().toString().trim();

        if (TextUtils.isEmpty(groupTitle)){
            Toast.makeText(this, "enter your group name", Toast.LENGTH_SHORT).show();
        }

        if (fileUri == null){
            //create group
            createGroup(""+g_timestamp,""+groupTitle,""+groupDescription,"");

        } else {
            //create group with icon

            final String randomName = UUID.randomUUID().toString();

            // PHOTO UPLOAD
            File newImageFile = new File(fileUri.getPath());
            try {

                compressedImageFile = new Compressor(CreateGroupActivity.this)
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

                        Toast.makeText(CreateGroupActivity.this, "image sauvégarder...", Toast.LENGTH_SHORT).show();
                        createGroup(""+g_timestamp,""+groupTitle,""+groupDescription,myurl);
                        Intent intent = new Intent(CreateGroupActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        // Handle failures
                        // ...
                        loadingBar.dismiss();
                    }
                }
            });


        }
    }

    private void createGroup(String g_timestamp,String groupTitle,String groupDescription,String groupIcon) {

        loadingBar.setTitle("create group ");
        loadingBar.setMessage("Please wait...");
        loadingBar.setCanceledOnTouchOutside(false);
        loadingBar.show();


        //setup group infos
        HashMap<String,String> hashMap = new HashMap<>();
        hashMap.put("groupId",""+g_timestamp);
        hashMap.put("groupTitle",""+groupTitle);
        hashMap.put("groupDescription",""+groupDescription);
        hashMap.put("groupIcon",""+groupIcon);
        hashMap.put("timestamp",""+g_timestamp);
        hashMap.put("createdBy",""+fUser.getUid());

        //create group
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
        ref.child(g_timestamp).setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {

                HashMap<String,String> hashMap1 = new HashMap<>();
                hashMap1.put("uid",fUser.getUid());
                hashMap1.put("role","creator");
                hashMap1.put("timestamp",g_timestamp);

                DatabaseReference ref1 = FirebaseDatabase.getInstance().getReference("Groups");
                ref1.child(g_timestamp).child("participants").child(fUser.getUid()).setValue(hashMap1)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        loadingBar.dismiss();
                        Toast.makeText(CreateGroupActivity.this, "created successfully", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(CreateGroupActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    }
                }) .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        loadingBar.dismiss();
                        Toast.makeText(CreateGroupActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        })
        .addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(CreateGroupActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
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


}
