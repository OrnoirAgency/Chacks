package com.ornoiragency.chacks.Activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;


import com.bumptech.glide.Glide;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.ornoiragency.chacks.models.UserObject;
import com.ornoiragency.chacks.R;

import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;
public class EditProfileActivity extends AppCompatActivity implements View.OnClickListener {

    ImageView mBack;
    MaterialButton mConfirm;
    EditText username,
            mStatus;
    TextView mPhone;

    CircleImageView mProfileImage;
    FloatingActionButton appCompatImageButton2;
    CollapsingToolbarLayout collapsingToolbarLayout;

    ImageView mCover;

    FirebaseAuth mAuth;
    DatabaseReference mUserDatabase;
    private Bitmap compressedImageFile;

    private StorageTask uploadTask;

    String      userId = "",
            name = "--",
            image="--",
            cover="--",
            status = "--",
            phone = "--";



    private Uri mainImageURI = null;

    UserObject mUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        mUser = (UserObject) getIntent().getSerializableExtra("userObject");

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        initializeObjects();

        mAuth = FirebaseAuth.getInstance();
        userId = mAuth.getCurrentUser().getUid();

        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("user").child(userId);

        getUserInfo();

        appCompatImageButton2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .start(EditProfileActivity.this);
            }
        });



    }

    ProgressDialog mDialog;
    public void  showProgressDialog(String message){
        mDialog = new ProgressDialog(this);
        mDialog.setMessage(message);
        mDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mDialog.show();
    }
    public void  dismissProgressDialog(){
        if(mDialog!=null)
            mDialog.dismiss();
    }

    private void getUserInfo() {
        name = mUser.getName();
        image = mUser.getImage();
        cover = mUser.getCover();
        status = mUser.getStatus();
        phone = mUser.getPhone();

        if(phone != null)
            mPhone.setText(phone);
        if(status != null)
            mStatus.setText(status);
        if(name != null)
            username.setText(name);

        Glide.with(getApplicationContext())
                .asBitmap()
                .load(image)
                .placeholder(R.drawable.profile_image)
                .into(mProfileImage);



        if (image != null && getApplication() != null){

            Glide.with(getApplicationContext())
                    .asBitmap()
                    .load(image)
                    .placeholder(R.color.icon_checked)
                    .into(mCover);

        }
    }

    private void saveUserInformation() {
        showProgressDialog("Saving Data...");

        if(!username.getText().toString().isEmpty())
            name = username.getText().toString();
        if(!mStatus.getText().toString().isEmpty())
            status = mStatus.getText().toString();

        Map userInfo = new HashMap();
        userInfo.put("name", name);
        userInfo.put("status", status);
        userInfo.put("states","offline");
        userInfo.put("onlineStatus","");
        userInfo.put("typingTo","noOne");

        if(image != null)
            userInfo.put("image", image);

        mUserDatabase.updateChildren(userInfo);


        if(mainImageURI != null) {

            // PHOTO UPLOAD
            File newImageFile = new File(mainImageURI.getPath());
            try {

                compressedImageFile = new Compressor(EditProfileActivity.this)
                        .setQuality(30)
                        .compressToBitmap(newImageFile);

            } catch (IOException e) {
                e.printStackTrace();
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            compressedImageFile.compress(Bitmap.CompressFormat.JPEG, 30, baos);
            byte[] imageData = baos.toByteArray();

            final StorageReference filePath = FirebaseStorage.getInstance().getReference().child("profile_image").child(userId +".jpg");
            uploadTask = filePath.putBytes(imageData);
            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    finish();
                    return;
                }
            });
            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            Map newImage = new HashMap();
                            newImage.put("image", uri.toString());
                            mUserDatabase.updateChildren(newImage);

                            finish();
                            return;
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            finish();
                            return;
                        }
                    });
                }
            });
        }else{
            finish();
        }





    }




    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE){

            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if (resultCode == RESULT_OK)
            {
                mainImageURI = result.getUri();
                mProfileImage.setImageURI(mainImageURI);

            }
            else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE)
            {
                Exception error = result.getError();
            }
        }
    }



    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.back:
                finish();
                break;
            case R.id.confirm:
                saveUserInformation();
                break;

        }
    }

    private void initializeObjects() {

        username = findViewById(R.id.username);
        mStatus = findViewById(R.id.status);
        mPhone = findViewById(R.id.phone);
        mProfileImage = findViewById(R.id.profileImage);
        mBack = findViewById(R.id.back);
        mConfirm = findViewById(R.id.confirm);
        mBack.setOnClickListener(this);
        mConfirm.setOnClickListener(this);
        mCover = findViewById(R.id.appCompatImageView);
        appCompatImageButton2 = findViewById(R.id.appCompatImageButton2);
    }


}
