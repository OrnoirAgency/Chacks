package com.ornoiragency.chacks.ChatGroup;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.ornoiragency.chacks.Adapter.AdapterGroupChat;
import com.ornoiragency.chacks.R;
import com.ornoiragency.chacks.models.GroupChat;


import java.util.ArrayList;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class GroupChatActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;

    private String groupId,myGroupRole;
    private Toolbar toolbar;
    private ImageButton attachBtn,sendBtn;
    private EditText messageEt;
    private TextView groupTitleTv;
    private CircleImageView groupIconIv;
    LinearLayout chatLayout;
    RecyclerView chatRv;

    private ArrayList<GroupChat> groupChatArrayList;
    private AdapterGroupChat adapterGroupChat;

    private static final int CAMERA_REQUEST_CODE = 200;
    private static final int STORAGE_REQUEST_CODE = 400;

    private static final int IMAGE_PICK_GALLERY_CODE = 1000;
    private static final int IMAGE_PICK_CAMERA_CODE = 1000;

    private  String[] cameraPermission;
    private  String[] storagePermission;

    private Uri image_uri = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        chatLayout = findViewById(R.id.chatLayout);
        attachBtn = findViewById(R.id.attachBtn);
        sendBtn = findViewById(R.id.sendBtn);
        messageEt = findViewById(R.id.messageEt);
        groupTitleTv = findViewById(R.id.groupTitleTv);
        groupIconIv = findViewById(R.id.groupIconIv);
        chatRv = findViewById(R.id.chatRv);

        Intent intent = getIntent();
        groupId = intent.getStringExtra("groupId");


        cameraPermission = new String[]{
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        };

        storagePermission = new String[]{
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        };

        firebaseAuth = FirebaseAuth.getInstance();
        loadGroupInfo();
        loadGroupMessage();
        loadGroupRole();

        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String message = messageEt.getText().toString().trim();
                if (TextUtils.isEmpty(message)){
                    Toast.makeText(GroupChatActivity.this, "can't sen empty message", Toast.LENGTH_SHORT).show();
                } else {
                    senMessage(message);
                }
            }
        });

        attachBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                showImageImportDialog();

            }
        });
    }




    private void showImageImportDialog() {
        String[] options = {"Camera","Gallery"};

        //dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Pick image")
                .setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        //handle clicks
                        if (which == 0){

                            //camera
                            if (!checkCameraPermission()){

                                requestCameraPermission();

                            } else {
                                cameraPick();
                            }

                        } else {
                            //gallery pick
                            if (!checkStoragePermission()){
                                requestStoragePermission();
                            } else {
                                pickGallery();
                            }
                        }
                    }
                })
                .show();
    }

    private void pickGallery(){
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent,IMAGE_PICK_GALLERY_CODE);
    }

    private void cameraPick(){

        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.Images.Media.TITLE, "GroupImageTitle");
        contentValues.put(MediaStore.Images.Media.DESCRIPTION, "GroupImageDescription");

        image_uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT,image_uri);
        startActivityForResult(intent,IMAGE_PICK_CAMERA_CODE);
    }

    private void requestStoragePermission(){
        ActivityCompat.requestPermissions(this,storagePermission,STORAGE_REQUEST_CODE);
    }

    private boolean checkStoragePermission(){
        boolean result = ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);

        return result;
    }

    private void requestCameraPermission(){
        ActivityCompat.requestPermissions(this,cameraPermission,CAMERA_REQUEST_CODE);
    }

    private boolean checkCameraPermission(){
        boolean result = ContextCompat.checkSelfPermission(this,Manifest.permission.CAMERA) == (PackageManager.PERMISSION_GRANTED);
        boolean result1 = ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);

        return result && result1;
    }

    private void loadGroupRole() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
        ref.child(groupId).child("participants")
                .orderByChild("uid").equalTo(firebaseAuth.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        for (DataSnapshot ds: dataSnapshot.getChildren()){

                            myGroupRole = ""+ ds.child("role").getValue();
                            //refresh menu item
                            invalidateOptionsMenu();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

    }

    private void loadGroupMessage() {

        groupChatArrayList = new ArrayList<>();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
        ref.keepSynced(true);
        ref.child(groupId).child("Messages").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                groupChatArrayList.clear();
                for (DataSnapshot ds: dataSnapshot.getChildren()){

                        GroupChat model = ds.getValue(GroupChat.class);
                        groupChatArrayList.add(model);
                }

                adapterGroupChat = new AdapterGroupChat(GroupChatActivity.this,groupChatArrayList);
                chatRv.setAdapter(adapterGroupChat);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void senMessage(String message) {

        String timestamp = ""+ System.currentTimeMillis();

        HashMap<String,Object> hashMap = new HashMap<>();
        hashMap.put("sender",""+firebaseAuth.getUid());
        hashMap.put("message",""+message);
        hashMap.put("timestamp",""+timestamp);
        hashMap.put("type",""+ "text");

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
        ref.child(groupId).child("Messages").child(timestamp)
                .setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(GroupChatActivity.this, "message sent", Toast.LENGTH_SHORT).show();
                        messageEt.setText("");
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(GroupChatActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sendImageMessage() {
        ProgressDialog pd = new ProgressDialog(this);
        pd.setTitle("please wait...");
        pd.setMessage("sending image...");
        pd.setCanceledOnTouchOutside(false);
        pd.show();

        //file name and path storage
        String filenamePath = "Messages Images/" + "" + System.currentTimeMillis();
        StorageReference storageReference = FirebaseStorage.getInstance().getReference(filenamePath);
        storageReference.putFile(image_uri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                        Task<Uri>p_uriTask = taskSnapshot.getStorage().getDownloadUrl();
                        while (!p_uriTask.isSuccessful());
                        Uri p_downloadUri = p_uriTask.getResult();

                        if (p_uriTask.isSuccessful()){

                            String timestamp = ""+ System.currentTimeMillis();

                            HashMap<String,Object> hashMap = new HashMap<>();
                            hashMap.put("sender",""+firebaseAuth.getUid());
                            hashMap.put("message",""+p_downloadUri);
                            hashMap.put("timestamp",""+timestamp);
                            hashMap.put("type",""+ "image");

                            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
                            ref.child(groupId).child("Messages").child(timestamp)
                                    .setValue(hashMap)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Toast.makeText(GroupChatActivity.this, "message sent", Toast.LENGTH_SHORT).show();
                                            messageEt.setText("");
                                            pd.dismiss();
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    pd.dismiss();
                                    Toast.makeText(GroupChatActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(GroupChatActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        pd.dismiss();
                    }
                });
    }
    private void loadGroupInfo() {

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
        ref.orderByChild("groupId").equalTo(groupId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        for (DataSnapshot ds: dataSnapshot.getChildren()){
                            String groupTitle = "" + ds.child("groupTitle").getValue();
                            String groupDescription = "" + ds.child("groupDescription").getValue();
                            String groupIcon = "" + ds.child("groupIcon").getValue();
                            String timestamp = "" + ds.child("timestamp").getValue();
                            String createBy = "" + ds.child("createdBy").getValue();

                            groupTitleTv.setText(groupTitle);
                            try {

                                Glide.with(GroupChatActivity.this)
                                        .asBitmap()
                                        .load(groupIcon)
                                        .placeholder(R.drawable.image_placeholder)
                                        .into(groupIconIv);


                            } catch (Exception e){
                                groupIconIv.setImageResource(R.drawable.ic_group);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main,menu);

        menu.findItem(R.id.action_logout).setVisible(false);
        menu.findItem(R.id.action_search).setVisible(false);
        menu.findItem(R.id.action_settings).setVisible(false);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.action_groupinfo){
            Intent intent = new Intent(this,GroupInfoActivity.class);
            intent.putExtra("groupId",groupId);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK){
            if (requestCode == IMAGE_PICK_GALLERY_CODE){
                image_uri = data.getData();
                sendImageMessage();
            }


        }
    }



    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case CAMERA_REQUEST_CODE:
                if (grantResults.length>0){
                    boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean writeStorageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;

                    if (cameraAccepted && writeStorageAccepted){
                        cameraPick();
                    }
                      else {
                        Toast.makeText(this, "camera  & storage permission are required", Toast.LENGTH_SHORT).show();
                    }
                }
                break;

            case STORAGE_REQUEST_CODE:
                if (grantResults.length > 0){

                    boolean writeStorageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;

                    if (writeStorageAccepted){
                        pickGallery();
                    }
                    else {
                        Toast.makeText(this, "storage permission required", Toast.LENGTH_SHORT).show();
                    }
                }
                break;
        }
    }
}
