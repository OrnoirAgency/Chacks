package com.ornoiragency.chacks.Adapter;

import android.Manifest;
import android.app.Activity;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Environment;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.facebook.ads.AdOptionsView;
import com.facebook.ads.MediaView;
import com.facebook.ads.NativeAd;
import com.facebook.ads.NativeAdLayout;
import com.facebook.ads.NativeAdsManager;
import com.google.android.gms.tasks.OnCompleteListener;
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
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.dynamiclinks.ShortDynamicLink;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.gson.Gson;
import com.ornoiragency.chacks.Activity.AllUserProfileActivity;
import com.ornoiragency.chacks.Activity.DownloadManagerActivity;
import com.ornoiragency.chacks.Activity.DownloadSongActivity;
import com.ornoiragency.chacks.MainActivity;
import com.ornoiragency.chacks.Notification.Data;
import com.ornoiragency.chacks.Notification.Sender;
import com.ornoiragency.chacks.Notification.Token;
import com.ornoiragency.chacks.Post.AddPostActivity;
import com.ornoiragency.chacks.Post.PostAudioDetailActivity;
import com.ornoiragency.chacks.Post.PostDetailActivity;
import com.ornoiragency.chacks.Post.PostLikedByActivity;
import com.ornoiragency.chacks.Post.PostTextActivity;
import com.ornoiragency.chacks.Post.PostTextDetailActivity;
import com.ornoiragency.chacks.Post.PostVideoDetailActivity;
import com.ornoiragency.chacks.R;
import com.ornoiragency.chacks.Utils.MediaUtils;
import com.ornoiragency.chacks.models.Chat;
import com.ornoiragency.chacks.models.GroupChat;
import com.ornoiragency.chacks.models.Notification;
import com.ornoiragency.chacks.models.Post;

import com.stfalcon.frescoimageviewer.ImageViewer;
import com.vanniktech.emoji.EmojiTextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import me.alvince.android.avatarimageview.AvatarImageView;

public class AdapterPosts extends RecyclerView.Adapter<RecyclerView.ViewHolder> {


    private static final int POST_IMAGE = 1;
    private static final int POST_TEXT = 2;
    private static final int POST_VIDEO = 3;
    private static final int POST_AUDIO = 4;
    private static final int AD_TYPE = 5;

    List<Post> postList;
    String myUid,hisUid,postId;

    private static final int AD_DISPLAY_FREQUENCY = 5;
    private List<NativeAd> mAdItems;
    private NativeAdsManager mNativeAdsManager;
    private Activity mActivity;

    private DatabaseReference likesRef;

    //volley request for notification
    private RequestQueue requestQueue;
    boolean notify = false;
    String myDp,myName;

    boolean mProcessLike = false;
    public boolean isClickable = true;
    private OnItemClickListener mOnItemClickListener;

    public AdapterPosts(Activity activity, List<Post> postItems,NativeAdsManager nativeAdsManager) {

        postList = postItems;
        mNativeAdsManager = nativeAdsManager;
        mAdItems = new ArrayList<>();
        mActivity = activity;

    }

    public interface OnItemClickListener {
        void onItemClick(View view, RecyclerView.ViewHolder holder, Post post, int position);
    }

    public void setOnItemClickListener(final AdapterPosts.OnItemClickListener mItemClickListener) {
        this.mOnItemClickListener = mItemClickListener;
    }



    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {

         myUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        requestQueue = Volley.newRequestQueue(mActivity.getApplicationContext());
        likesRef = FirebaseDatabase.getInstance().getReference().child("Likes");

        View view;
        if (viewType == POST_IMAGE) { // for call layout
            view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.post_image_item, viewGroup, false);
            return new PostImageViewHolder(view);
        } else if (viewType == POST_TEXT){ // for email layout
            view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.post_text_item, viewGroup, false);
            return new PostTextViewHolder(view);
        } else if (viewType == POST_VIDEO){
            view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.post_video_item, viewGroup, false);
            return new PostVideoViewHolder(view);
        } else if (viewType == POST_AUDIO){
            view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.post_music_item, viewGroup, false);
            return new PostAudioViewHolder(view);
        } else {
            NativeAdLayout inflatedView = (NativeAdLayout) LayoutInflater.from(viewGroup.getContext())
                   .inflate(R.layout.native_ad_unit, viewGroup, false);
            return new AdHolder(inflatedView);
        }

    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {


        //Calculate where the next postItem index is by subtracting ads we've shown.
       // int index = position - (position / AD_DISPLAY_FREQUENCY) - 1;
        Post postItem = postList.get(position);
        String uid = postItem.getUid();
        String pId = postItem.getpId();
        String pImage = postItem.getpImage();
        String pVideo = postItem.getpVideo();
        String pAudio = postItem.getpAudio();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("user");
        ref.orderByChild("uid").equalTo(myUid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        for (DataSnapshot ds: dataSnapshot.getChildren()){

                             myName = "" + ds.child("name").getValue();
                             myDp = "" + ds.child("image").getValue();

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });


        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (mOnItemClickListener != null) {
                    mOnItemClickListener.onItemClick(v, holder, postItem, position);
                }
            }
        });

        if (getItemViewType(position) == POST_TEXT) {

            ((PostTextViewHolder)holder).pText.setText(postItem.getpText());
            String pTimestamp = postItem.getpTime();
            Calendar cal = Calendar.getInstance(Locale.FRENCH);
            cal.setTimeInMillis(Long.parseLong(pTimestamp));
            String pTime = DateFormat.format("dd MMMM, yyyy HH:mm ",cal).toString();
            ((PostTextViewHolder)holder).pTimeTv.setText(pTime);

            setUserNameText(holder,postItem);
            ((PostTextViewHolder) holder).setLikesForText(holder,postItem.getpId());

            ((PostTextViewHolder)holder).pCommentTv.setText(postItem.getpComments() +" "+ mActivity.getString(R.string.comment));

            ((AdapterPosts.PostTextViewHolder)holder).moreBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    showMoreOptionsForText(((AdapterPosts.PostTextViewHolder)holder).moreBtn,uid,myUid,pId);
                }
            });
            ((AdapterPosts.PostTextViewHolder)holder).likeBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    notify = true;
                    PostLikeText(pId,myUid,uid);

                }
            });

            ((PostTextViewHolder)holder).pText.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Intent intent = new Intent(mActivity, PostTextDetailActivity.class);
                    intent.putExtra("postId",pId);
                    mActivity.startActivity(intent);
                }
            });
            ((AdapterPosts.PostTextViewHolder)holder).commentBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Intent intent = new Intent(mActivity, PostTextDetailActivity.class);
                    intent.putExtra("postId",pId);
                    mActivity.startActivity(intent);
                }
            });
            ((AdapterPosts.PostTextViewHolder)holder).uPictureIv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Intent intent = new Intent(mActivity, AllUserProfileActivity.class);
                    intent.putExtra("user_id",uid);
                    mActivity.startActivity(intent);

                }
            });
            ((AdapterPosts.PostTextViewHolder)holder).pLikesTv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Intent intent = new Intent(mActivity, PostLikedByActivity.class);
                    intent.putExtra("postId",pId);
                    mActivity.startActivity(intent);
                }
            });
            ((AdapterPosts.PostTextViewHolder)holder).shareBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    sharePostText(postItem.getpText());

                }
            });


        } else if (getItemViewType(position) == POST_IMAGE){


            String pTimestamp = postItem.getpTime();
            Calendar cal = Calendar.getInstance(Locale.FRENCH);
            cal.setTimeInMillis(Long.parseLong(pTimestamp));
            String pTime = DateFormat.format("dd MMMM, yyyy HH:mm ",cal).toString();
            ((PostImageViewHolder)holder).pTimeTv.setText(pTime);
            ((PostImageViewHolder)holder).pDescriptionTv.setText(postItem.getpDescr());

            ((PostImageViewHolder)holder).setLikes(holder,postItem.getpId());

            ((PostImageViewHolder)holder).pCommentTv.setText(postItem.getpComments() +" "+ mActivity.getString(R.string.comment));

            setUserNameImage(holder,postItem);

            Glide.with(mActivity.getApplicationContext())
                    .asBitmap()
                    .load(postItem.getpImage())
                    .placeholder(R.drawable.image_placeholder)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(((PostImageViewHolder)holder).pImageIv);

            ((AdapterPosts.PostImageViewHolder)holder).pImageIv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    new ImageViewer.Builder(v.getContext(), Collections.singletonList(postItem.getpImage()))
                            .setStartPosition(0)
                            .show();
                }
            });

            ((AdapterPosts.PostImageViewHolder)holder).moreBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    showMoreOptions(((AdapterPosts.PostImageViewHolder)holder).moreBtn,uid,myUid,pId,pImage);
                }
            });
            ((AdapterPosts.PostImageViewHolder)holder).likeBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {


                    mProcessLike = true;

                    likesRef.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                            if (mProcessLike){
                                // already liked, remove like
                                if (dataSnapshot.child(pId).hasChild(myUid)){

                                    likesRef.child(pId).child(myUid).removeValue();
                                    mProcessLike = false;
                                }
                                else {
                                    //not like, like it

                                    likesRef.child(pId).child(myUid).setValue(true);
                                    mProcessLike = false;

                                    addToHisNotification(""+uid,""+pId,mActivity.getString(R.string.liked));
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }
            });



            ((PostImageViewHolder)holder).comment.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Intent intent = new Intent(mActivity, PostDetailActivity.class);
                    intent.putExtra("postId",pId);
                    mActivity.startActivity(intent);
                }
            });
            ((AdapterPosts.PostImageViewHolder)holder).commentBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Intent intent = new Intent(mActivity, PostDetailActivity.class);
                    intent.putExtra("postId",pId);
                    mActivity.startActivity(intent);
                }
            });
            ((AdapterPosts.PostImageViewHolder)holder).uPictureIv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Intent intent = new Intent(mActivity, AllUserProfileActivity.class);
                    intent.putExtra("user_id",uid);
                    mActivity.startActivity(intent);

                }
            });
            ((AdapterPosts.PostImageViewHolder)holder).pLikesTv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Intent intent = new Intent(mActivity, PostLikedByActivity.class);
                    intent.putExtra("postId",pId);
                    mActivity.startActivity(intent);
                }
            });
            ((AdapterPosts.PostImageViewHolder)holder).shareBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    BitmapDrawable bitmapDrawable = (BitmapDrawable)((AdapterPosts.PostImageViewHolder)holder).pImageIv.getDrawable();
                    //convert image to bitmap
                    Bitmap bitmap = bitmapDrawable.getBitmap();
                    shareImageAndText(postItem.getpDescr(),bitmap);

                }
            });

        } else if (getItemViewType(position) == POST_VIDEO){

            String pTimestamp = postItem.getpTime();
            Calendar cal = Calendar.getInstance(Locale.FRENCH);
            cal.setTimeInMillis(Long.parseLong(pTimestamp));
            String pTime = DateFormat.format("dd MMMM, yyyy HH:mm ",cal).toString();
            ((PostVideoViewHolder)holder).pTimeTv.setText(pTime);
            ((PostVideoViewHolder)holder).pDescriptionTv.setText(postItem.getpDescr());
            ((PostVideoViewHolder)holder).video_timer.setText(postItem.getpVideoDuration());

            String pvDesc = postItem.getpDescr();
            String videoCover = postItem.getVideoCover();


            Glide.with(mActivity.getApplicationContext())
                    .asBitmap()
                    .load(videoCover)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(((PostVideoViewHolder)holder).post_video_cover);



            setUserNameVideo(holder,postItem);

            ((PostVideoViewHolder)holder).setLikesForVideo(holder,postItem.getpId());

            ((PostVideoViewHolder)holder).pCommentTv.setText(postItem.getpComments() +" "+ mActivity.getString(R.string.comment));
            ((AdapterPosts.PostVideoViewHolder)holder).moreBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    showMoreVideoOptions(((AdapterPosts.PostVideoViewHolder)holder).moreBtn,uid,myUid,pId,pVideo,pvDesc);
                }
            });
            ((AdapterPosts.PostVideoViewHolder)holder).likeBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {


                    mProcessLike = true;

                    likesRef.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                            if (mProcessLike){
                                // already liked, remove like
                                if (dataSnapshot.child(pId).hasChild(myUid)){
                                    likesRef.child(pId).child(myUid).removeValue();
                                    mProcessLike = false;
                                }
                                else {
                                    //not like, like it
                                    likesRef.child(pId).child(myUid).setValue(true);
                                    mProcessLike = false;

                                    addToHisNotificationVideo(""+uid,""+pId,mActivity.getString(R.string.liked));
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }
            });
            ((AdapterPosts.PostVideoViewHolder)holder).itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Intent intent = new Intent(mActivity, PostVideoDetailActivity.class);
                    intent.putExtra("postId",pId);
                    mActivity.startActivity(intent);
                }
            });

            ((AdapterPosts.PostVideoViewHolder)holder).commentBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Intent intent = new Intent(mActivity, PostVideoDetailActivity.class);
                    intent.putExtra("postId",pId);
                    mActivity.startActivity(intent);
                }
            });
            ((AdapterPosts.PostVideoViewHolder)holder).uPictureIv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Intent intent = new Intent(mActivity, AllUserProfileActivity.class);
                    intent.putExtra("user_id",uid);
                    mActivity.startActivity(intent);

                }
            });
            ((AdapterPosts.PostVideoViewHolder)holder).pLikesTv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Intent intent = new Intent(mActivity, PostLikedByActivity.class);
                    intent.putExtra("postId",pId);
                    mActivity.startActivity(intent);
                }
            });
            ((AdapterPosts.PostVideoViewHolder)holder).shareBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    sharePostVideoOnly(postItem.getpVideo(),postItem.getpId());

                }
            });

        } else if (getItemViewType(position) == POST_AUDIO){

            String pTimestamp = postItem.getpTime();
            Calendar cal = Calendar.getInstance(Locale.FRENCH);
            cal.setTimeInMillis(Long.parseLong(pTimestamp));
            String pTime = DateFormat.format("dd MMMM, yyyy HH:mm ",cal).toString();
            ((PostAudioViewHolder)holder).pTimeTv.setText(pTime);
            ((PostAudioViewHolder)holder).post_audio_artist.setText(postItem.getArtist());
            ((PostAudioViewHolder)holder).post_audio_title.setText(postItem.getTitle());
            ((PostAudioViewHolder)holder).post_audio_duration.setText(milliSecondsToTimer(postItem.getpAudioDuration()));
            ((PostAudioViewHolder)holder).pDescriptionTv.setText(postItem.getAudioDesc());


            Glide.with(mActivity.getApplicationContext())
                    .asBitmap()
                    .load(postItem.getpAudioCover())
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(((PostAudioViewHolder)holder).post_audio_cover);

            setUserNameAudio(holder,postItem);

            ((PostAudioViewHolder) holder).setLikesForAudio(holder,postItem.getpId());
            ((PostAudioViewHolder)holder).pCommentTv.setText(postItem.getpComments() +" "+ mActivity.getString(R.string.comment));

            ((AdapterPosts.PostAudioViewHolder)holder).moreBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    showMoreAudioOptions(((AdapterPosts.PostAudioViewHolder)holder).moreBtn,uid,myUid,pId,pAudio,postItem.getTitle());
                }
            });
            ((AdapterPosts.PostAudioViewHolder)holder).likeBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    mProcessLike = true;

                    likesRef.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                            if (mProcessLike){
                                // already liked, remove like
                                if (dataSnapshot.child(pId).hasChild(myUid)){

                                    likesRef.child(pId).child(myUid).removeValue();
                                    mProcessLike = false;
                                }
                                else {
                                    //not like, like it
                                    likesRef.child(pId).child(myUid).setValue(true);
                                    mProcessLike = false;

                                    addToHisNotificationAudio(""+uid,""+pId,mActivity.getString(R.string.liked));
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }
            });
            ((PostAudioViewHolder)holder).post_audio_cover.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Intent intent = new Intent(mActivity, PostAudioDetailActivity.class);
                    intent.putExtra("postId",pId);
                    mActivity.startActivity(intent);
                }
            });
            ((AdapterPosts.PostAudioViewHolder)holder).commentBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Intent intent = new Intent(mActivity, PostAudioDetailActivity.class);
                    intent.putExtra("postId",pId);
                    mActivity.startActivity(intent);
                }
            });
            ((AdapterPosts.PostAudioViewHolder)holder).uPictureIv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Intent intent = new Intent(mActivity, AllUserProfileActivity.class);
                    intent.putExtra("user_id",uid);
                    mActivity.startActivity(intent);

                }
            });
            ((AdapterPosts.PostAudioViewHolder)holder).pLikesTv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Intent intent = new Intent(mActivity, PostLikedByActivity.class);
                    intent.putExtra("postId",pId);
                    mActivity.startActivity(intent);
                }
            });
            ((AdapterPosts.PostAudioViewHolder)holder).shareBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    shortLink(postItem.getpAudio());

                }
            });


            ((PostAudioViewHolder)holder).post_audio_download.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Intent intent = new Intent(mActivity, DownloadSongActivity.class);
                    intent.putExtra("postId",pId);
                    mActivity.startActivity(intent);

                }
            });


        } else{

            NativeAd ad;

            if (mAdItems.size() > position / AD_DISPLAY_FREQUENCY) {
                ad = mAdItems.get(position / AD_DISPLAY_FREQUENCY);
            } else {
                ad = mNativeAdsManager.nextNativeAd();
                mAdItems.add(ad);

            }

            AdHolder adHolder = (AdHolder) holder;
            adHolder.adChoicesContainer.removeAllViews();

            if (ad != null) {

                adHolder.adLoading.setVisibility(View.VISIBLE);

                adHolder.tvAdTitle.setText(ad.getAdvertiserName());
                adHolder.tvAdBody.setText(ad.getAdBodyText());
                adHolder.tvAdSocialContext.setText(ad.getAdSocialContext());
                adHolder.tvAdSponsoredLabel.setText("sponsored");
                adHolder.btnAdCallToAction.setText(ad.getAdCallToAction());
                adHolder.btnAdCallToAction.setVisibility(
                        ad.hasCallToAction() ? View.VISIBLE : View.INVISIBLE);
                AdOptionsView adOptionsView =
                        new AdOptionsView(mActivity, ad, adHolder.nativeAdLayout);
                adHolder.adChoicesContainer.addView(adOptionsView, 0);

                List<View> clickableViews = new ArrayList<>();
                clickableViews.add(adHolder.ivAdIcon);
                clickableViews.add(adHolder.mvAdMedia);
                clickableViews.add(adHolder.btnAdCallToAction);
                ad.registerViewForInteraction(
                        adHolder.nativeAdLayout,
                        adHolder.mvAdMedia,
                        adHolder.ivAdIcon,
                        clickableViews);
            }
        }


    }

    private void PostLikeText(String pId, String myUid, String uid) {

        mProcessLike = true;

        likesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {


                if (mProcessLike){

                    // already liked, remove like
                    if (dataSnapshot.child(pId).hasChild(myUid))
                    {
                        likesRef.child(pId).child(myUid).removeValue();
                        mProcessLike = false;
                    }
                    else {
                        //not like, like it

                        likesRef.child(pId).child(myUid).setValue(true);
                        mProcessLike = false;

                        String msg = mActivity.getString(R.string.liked);
                        if (notify) {
                            sendNotifiaction(uid,myName,myDp,msg,pId);
                        }
                        notify = false;
                        addToHisNotificationText(""+uid,""+pId,mActivity.getString(R.string.liked));

                    }
                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    public  void sendNotifiaction(String hisUid, String myName, String myDp, String msg, String pId){
        DatabaseReference tokens = FirebaseDatabase.getInstance().getReference("Tokens");
        Query query = tokens.orderByKey().equalTo(hisUid);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Token token = snapshot.getValue(Token.class);
                    Data data = new Data(""+myUid,
                            ""+myDp,
                             R.drawable.ic_notif,
                            ""+ myName+": "+msg,
                             mActivity.getString(R.string.app_name),
                            ""+pId,
                            ""+ hisUid,
                            "CommentNotification",
                            ""+"text");

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


    private void setUserNameAudio(RecyclerView.ViewHolder holder, Post post) {

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("user");
        ref.orderByChild("uid").equalTo(post.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        for (DataSnapshot ds: dataSnapshot.getChildren()){

                            String name = "" + ds.child("name").getValue();
                            String image = "" + ds.child("image").getValue();

                            ((PostAudioViewHolder)holder).uNameTv.setText(name);
                            Glide.with(mActivity.getApplicationContext())
                                    .asBitmap()
                                    .load(image)
                                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                                    .into(((PostAudioViewHolder)holder).uPictureIv);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }
    private void setUserNameVideo(RecyclerView.ViewHolder holder, Post post) {

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("user");
        ref.orderByChild("uid").equalTo(post.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        for (DataSnapshot ds: dataSnapshot.getChildren()){

                            String name = "" + ds.child("name").getValue();
                            String image = "" + ds.child("image").getValue();

                            ((PostVideoViewHolder)holder).uNameTv.setText(name);
                            Glide.with(mActivity.getApplicationContext())
                                    .asBitmap()
                                    .load(image)
                                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                                    .into(((PostVideoViewHolder)holder).uPictureIv);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }
    private void setUserNameText(RecyclerView.ViewHolder holder, Post post) {

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("user");
        ref.orderByChild("uid").equalTo(post.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        for (DataSnapshot ds: dataSnapshot.getChildren()){

                            String name = "" + ds.child("name").getValue();
                            String image = "" + ds.child("image").getValue();

                            ((PostTextViewHolder)holder).uNameTv.setText(name);
                            Glide.with(mActivity.getApplicationContext())
                                    .asBitmap()
                                    .load(image)
                                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                                    .into(((PostTextViewHolder)holder).uPictureIv);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }
    private void setUserNameImage(RecyclerView.ViewHolder holder, Post post) {

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("user");
        ref.orderByChild("uid").equalTo(post.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        for (DataSnapshot ds: dataSnapshot.getChildren()){

                            String name = "" + ds.child("name").getValue();
                            String image = "" + ds.child("image").getValue();

                            ((PostImageViewHolder)holder).uNameTv.setText(name);
                            Glide.with(mActivity.getApplicationContext())
                                    .asBitmap()
                                    .load(image)
                                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                                    .into(((PostImageViewHolder)holder).uPictureIv);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

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

    // All post options
    private void showMoreOptions(ImageButton moreBtn, String uid, String myUid, String pId, String pImage) {

        PopupMenu popupMenu = new PopupMenu(mActivity,moreBtn, Gravity.END);

        //show delete option in only post(s) of currently signed-in user
        if (uid.equals(myUid)){
            //add item to menu
            popupMenu.getMenu().add(Menu.NONE,0,0,mActivity.getString(R.string.delete));
            popupMenu.getMenu().add(Menu.NONE,1,0,mActivity.getString(R.string.edit));

        }

        popupMenu.getMenu().add(Menu.NONE,2,0,mActivity.getString(R.string.view_detail));
        popupMenu.getMenu().add(Menu.NONE,3,0,mActivity.getString(R.string.download));

        //click listener
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int id = item.getItemId();
                if (id == 0){
                    //delete is click
                    beginDelete(pId,pImage);
                } else if (id == 1){

                    Intent intent = new Intent(mActivity, AddPostActivity.class);
                    intent.putExtra("key","editPost");
                    intent.putExtra("editPostId",pId);
                    mActivity.startActivity(intent);

                } else if (id == 2){

                    Intent intent = new Intent(mActivity, PostDetailActivity.class);
                    intent.putExtra("postId",pId);
                    mActivity.startActivity(intent);
                } else if (id == 3){

                    beginImageDownload(pImage,pId);
                }


                return false;
            }
        });
        //show menu
        popupMenu.show();
    }
    private void showMoreOptionsForText(ImageButton moreBtn, String uid, String myUid, String pId) {

        PopupMenu popupMenu = new PopupMenu(mActivity,moreBtn, Gravity.END);

        //show delete option in only post(s) of currently signed-in user
        if (uid.equals(myUid)){
            //add item to menu
            popupMenu.getMenu().add(Menu.NONE,0,0,mActivity.getString(R.string.delete));
            popupMenu.getMenu().add(Menu.NONE,1,0,mActivity.getString(R.string.edit));


        }
        popupMenu.getMenu().add(Menu.NONE,2,0,mActivity.getString(R.string.view_detail));


        //click listener
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int id = item.getItemId();
                if (id == 0){
                    //delete is click
                    beginTextDelete(pId);
                } else if (id == 1){

                    Intent intent = new Intent(mActivity, PostTextActivity.class);
                    intent.putExtra("key","editPost");
                    intent.putExtra("editPostId",pId);
                    mActivity.startActivity(intent);

                } else if (id == 2){

                    Intent intent = new Intent(mActivity, PostTextDetailActivity.class);
                    intent.putExtra("postId",pId);
                    mActivity.startActivity(intent);
                }


                return false;
            }
        });
        //show menu
        popupMenu.show();
    }
    private void showMoreAudioOptions(ImageButton moreBtn_audio, String uid, String myUid, String pId, String pAudio, String title) {

        PopupMenu popupMenu = new PopupMenu(mActivity,moreBtn_audio, Gravity.END);

        //show delete option in only post(s) of currently signed-in user
        if (uid.equals(myUid)){
            //add item to menu
            popupMenu.getMenu().add(Menu.NONE,0,0,mActivity.getString(R.string.delete));

        }

        popupMenu.getMenu().add(Menu.NONE,1,0,mActivity.getString(R.string.view_detail));
        popupMenu.getMenu().add(Menu.NONE,2,0,mActivity.getString(R.string.download));

        //click listener
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int id = item.getItemId();
                if (id == 0){
                    //delete is click
                    beginAudioDelete(pId,pAudio);
                } else if (id == 1){

                    Intent intent = new Intent(mActivity, PostAudioDetailActivity.class);
                    intent.putExtra("postId",pId);
                    mActivity.startActivity(intent);

                } else if (id == 2){

                    beginAudioDownload(pId,pAudio,title);
                }

                return false;
            }
        });
        //show menu
        popupMenu.show();
    }
    private void showMoreVideoOptions(ImageButton moreBtn_video, String uid, String myUid, String pId, String pVideo, String pvDesc) {

        PopupMenu popupMenu = new PopupMenu(mActivity,moreBtn_video, Gravity.END);

        //show delete option in only post(s) of currently signed-in user
        if (uid.equals(myUid)){
            //add item to menu
            popupMenu.getMenu().add(Menu.NONE,0,0,mActivity.getString(R.string.delete));

        }
        popupMenu.getMenu().add(Menu.NONE,1,0,mActivity.getString(R.string.view_detail));
        popupMenu.getMenu().add(Menu.NONE,2,0,mActivity.getString(R.string.download));

        //click listener
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int id = item.getItemId();
                if (id == 0){
                    //delete is click
                    beginVideoDelete(pId,pVideo);
                } else if (id == 1){

                    Intent intent = new Intent(mActivity, PostVideoDetailActivity.class);
                    intent.putExtra("postId",pId);
                    mActivity.startActivity(intent);

                } else if (id == 2){

                    beginVideoDownload(pId,pVideo,pvDesc);
                }

                return false;
            }
        });
        //show menu
        popupMenu.show();

    }

    //delete options
    private void beginDelete(String pId, String pImage) {
        //ProgressBar
        ProgressDialog pb = new ProgressDialog(mActivity);
        pb.setMessage("deleting");
        /*Steps:
        1) delete image using url
        2) delete from database using post id */

        StorageReference picRef = FirebaseStorage.getInstance().getReferenceFromUrl(pImage);
        picRef.delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        //image deleted, now delete database
                        Query fquery = FirebaseDatabase.getInstance().getReference("Posts").orderByChild("pId").equalTo(pId);
                        fquery.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                for (DataSnapshot ds: dataSnapshot.getChildren()){
                                    ds.getRef().removeValue();
                                }
                                Toast.makeText(mActivity, "Delete ", Toast.LENGTH_SHORT).show();
                                pb.dismiss();
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                Toast.makeText(mActivity, "", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        pb.dismiss();
                        Toast.makeText(mActivity, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
    private void beginTextDelete(String pId) {
        ProgressDialog pb = new ProgressDialog(mActivity);
        pb.setMessage("deleting");

        Query fquery = FirebaseDatabase.getInstance().getReference("Posts").orderByChild("pId").equalTo(pId);
        fquery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for (DataSnapshot ds: dataSnapshot.getChildren()){
                    ds.getRef().removeValue();
                }
                Toast.makeText(mActivity, "Delete ", Toast.LENGTH_SHORT).show();
                pb.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(mActivity, "", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void beginAudioDelete(String pId, String pAudio) {

        //ProgressBar
        ProgressDialog pb = new ProgressDialog(mActivity);
        pb.setMessage("deleting");
        /*Steps:
        1) delete image using url
        2) delete from database using post id */

        StorageReference picRef = FirebaseStorage.getInstance().getReferenceFromUrl(pAudio);
        picRef.delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        //image deleted, now delete database
                        Query fquery = FirebaseDatabase.getInstance().getReference("Posts").orderByChild("pId").equalTo(pId);
                        fquery.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                for (DataSnapshot ds: dataSnapshot.getChildren()){
                                    ds.getRef().removeValue();
                                }
                                Toast.makeText(mActivity, "Delete ", Toast.LENGTH_SHORT).show();
                                pb.dismiss();
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                Toast.makeText(mActivity, "", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        pb.dismiss();
                        Toast.makeText(mActivity, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
    private void beginVideoDelete(String pId, String pVideo) {
        //ProgressBar
        ProgressDialog pb = new ProgressDialog(mActivity);
        pb.setMessage("deleting");
        /*Steps:
        1) delete image using url
        2) delete from database using post id */

        StorageReference picRef = FirebaseStorage.getInstance().getReferenceFromUrl(pVideo);
        picRef.delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        //image deleted, now delete database
                        Query fquery = FirebaseDatabase.getInstance().getReference("Posts").orderByChild("pId").equalTo(pId);
                        fquery.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                for (DataSnapshot ds: dataSnapshot.getChildren()){
                                    ds.getRef().removeValue();
                                }
                                Toast.makeText(mActivity, "Delete ", Toast.LENGTH_SHORT).show();
                                pb.dismiss();
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                Toast.makeText(mActivity, "", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        pb.dismiss();
                        Toast.makeText(mActivity, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }



    //download options
    private void beginImageDownload(String pImage, String pId) {

            //Your read write code.
            StorageReference picRef = FirebaseStorage.getInstance().getReferenceFromUrl(pImage);
            picRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {

                    String url = uri.toString();

                    downloadImageFiles(url,"P-"+pId,".jpg");
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                }
            });


    }
    private void downloadImageFiles(String url,String fileName,String fileExtension) {


// Create request for android download manager
        DownloadManager downloadManager = (DownloadManager) mActivity.getSystemService(Context.DOWNLOAD_SERVICE);
        Uri uri = Uri.parse(url);
        DownloadManager.Request request = new DownloadManager.Request(uri);
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI |
                DownloadManager.Request.NETWORK_MOBILE);

// set title and description
        request.setTitle(mActivity.getString(R.string.app_name));
        request.setDescription(fileName + fileExtension);

        request.allowScanningByMediaScanner();
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);

//set the local destination for download file to a path within the application's external files directory
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS,fileName + fileExtension);

        downloadManager.enqueue(request);
    }
    private void beginAudioDownload(String pId, String pAudio, String title){

            StorageReference Ref = FirebaseStorage.getInstance().getReferenceFromUrl(pAudio);
            Ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {

                    String url = uri.toString();

                    downloadAudioFiles(url,title,".mp3");
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                }
            });


    }
    private void downloadAudioFiles(String url,String fileName,String fileExtension) {

// Create request for android download manager
        DownloadManager downloadManager = (DownloadManager) mActivity.getSystemService(Context.DOWNLOAD_SERVICE);
        Uri uri = Uri.parse(url);
        DownloadManager.Request request = new DownloadManager.Request(uri);
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI |
                DownloadManager.Request.NETWORK_MOBILE);

// set title and description
        request.setTitle(mActivity.getString(R.string.app_name));
        request.setDescription(fileName + fileExtension);

        request.allowScanningByMediaScanner();
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);

//set the local destination for download file to a path within the application's external files directory
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS,fileName + fileExtension);
        request.setMimeType("*/*");
        downloadManager.enqueue(request);
    }
    private void beginVideoDownload(String pId, String pVideo, String pvDesc) {

            StorageReference picRef = FirebaseStorage.getInstance().getReferenceFromUrl(pVideo);
            picRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {

                    String url = uri.toString();

                    downloadVideoFiles(url,pvDesc,".mp4");
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                }
            });


    }
    private void downloadVideoFiles(String url,String fileName,String fileExtension) {


        // Create request for android download manager
        DownloadManager downloadManager = (DownloadManager) mActivity.getSystemService(Context.DOWNLOAD_SERVICE);
        Uri uri = Uri.parse(url);
        DownloadManager.Request request = new DownloadManager.Request(uri);
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI |
                DownloadManager.Request.NETWORK_MOBILE);

// set title and description
        request.setTitle(mActivity.getString(R.string.app_name));
        request.setDescription(fileName + fileExtension);

        request.allowScanningByMediaScanner();
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);

//set the local destination for download file to a path within the application's external files directory
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName + fileExtension);
        request.setMimeType("video/*");
        downloadManager.enqueue(request);
    }

    //add notification
    private void addToHisNotification(String hisUid,String pId,String notification){

        String timestamp = ""+System.currentTimeMillis();

        HashMap<Object,String> hashMap = new HashMap<>();
        hashMap.put("pId",pId);
        hashMap.put("timestamp",timestamp);
        hashMap.put("pUid",hisUid);
        hashMap.put("type","image");
        hashMap.put("notification",notification);
        hashMap.put("sUid",myUid);


        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("user");
        ref.child(hisUid).child("Notifications").child(timestamp).setValue(hashMap)
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
    private void addToHisNotificationText(String hisUid,String pId,String notification){

        String timestamp = ""+System.currentTimeMillis();

        HashMap<Object,String> hashMap = new HashMap<>();
        hashMap.put("pId",pId);
        hashMap.put("timestamp",timestamp);
        hashMap.put("pUid",hisUid);
        hashMap.put("type","text");
        hashMap.put("notification",notification);
        hashMap.put("sUid",myUid);


        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("user");
        ref.child(hisUid).child("Notifications").child(timestamp).setValue(hashMap)
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
    private void addToHisNotificationVideo(String hisUid,String pId,String notification){

        String timestamp = ""+System.currentTimeMillis();

        HashMap<Object,String> hashMap = new HashMap<>();
        hashMap.put("pId",pId);
        hashMap.put("timestamp",timestamp);
        hashMap.put("pUid",hisUid);
        hashMap.put("type","audio");
        hashMap.put("notification",notification);
        hashMap.put("sUid",myUid);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("user");
        ref.child(hisUid).child("Notifications").child(timestamp).setValue(hashMap)
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
    private void addToHisNotificationAudio(String hisUid,String pId,String notification){

        String timestamp = ""+System.currentTimeMillis();

        HashMap<Object,String> hashMap = new HashMap<>();
        hashMap.put("pId",pId);
        hashMap.put("timestamp",timestamp);
        hashMap.put("pUid",hisUid);
        hashMap.put("type","video");
        hashMap.put("notification",notification);
        hashMap.put("sUid",myUid);


        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("user");
        ref.child(hisUid).child("Notifications").child(timestamp).setValue(hashMap)
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
    //share options
    private void shareImageAndText(String pDescription, Bitmap bitmap) {
        String shareBody = pDescription;
        // save image in cache , get saved image uri
        Uri uri = saveImageToShare(bitmap);

        //share intent
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.putExtra(Intent.EXTRA_STREAM,uri);
        intent.putExtra(Intent.EXTRA_TEXT,shareBody);
        intent.putExtra(Intent.EXTRA_SUBJECT,"subject here");
        intent.setType("image/png");
        mActivity.startActivity(Intent.createChooser(intent,"Share via"));

    }
    private void sharePostText(String pText) {

        String shareBody = pText;
        //share intent
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_SUBJECT,"subject here");
        intent.putExtra(Intent.EXTRA_TEXT,shareBody);
        mActivity.startActivity(Intent.createChooser(intent,"Share via"));
    }
    private void shortLink(String pAudio) {

        Task<ShortDynamicLink> dynamicLinkUri = FirebaseDynamicLinks.getInstance().createDynamicLink()
                .setLink(Uri.parse(pAudio))
                .setDynamicLinkDomain("chacks.page.link")
                .buildShortDynamicLink()
                .addOnCompleteListener(new OnCompleteListener<ShortDynamicLink>() {
                    @Override
                    public void onComplete(@NonNull Task<ShortDynamicLink> task) {

                        if (task.isSuccessful()) {
                            // Short link created
                            Uri shortLink = task.getResult().getShortLink();
                            Uri flowchartLink = task.getResult().getPreviewLink();
                            Intent intent = new Intent();
                            intent.setAction(Intent.ACTION_SEND);
                            intent.putExtra(Intent.EXTRA_TEXT,  shortLink.toString());
                            intent.setType("text/plain");
                            mActivity.startActivity(Intent.createChooser(intent, "Share audio!"));
                        } else {
                            // Error
                            // ...
                        }
                    }
                });

    }
    private void sharePostVideoOnly(String pVideo, String pId) {

        Task<ShortDynamicLink> dynamicLinkUri = FirebaseDynamicLinks.getInstance().createDynamicLink()
                .setLink(Uri.parse(pVideo))
                .setDynamicLinkDomain("chacks.page.link")
                .buildShortDynamicLink()
                .addOnCompleteListener(new OnCompleteListener<ShortDynamicLink>() {
                    @Override
                    public void onComplete(@NonNull Task<ShortDynamicLink> task) {

                        if (task.isSuccessful()) {
                            // Short link created
                            Uri shortLink = task.getResult().getShortLink();
                            Uri flowchartLink = task.getResult().getPreviewLink();
                            String msg = "Hey, check out this  video I found on ChaCks ";
                            Intent intent = new Intent();
                            intent.setAction(Intent.ACTION_SEND);
                            intent.putExtra(Intent.EXTRA_TEXT,  msg + flowchartLink + pId);
                            intent.setType("text/plain");
                            mActivity.startActivity(Intent.createChooser(intent, "Share video!"));
                        } else {
                            // Error
                            // ...
                        }
                    }
                });


    }
    private Uri saveImageToShare(Bitmap bitmap) {
        File imageFolder = new File(mActivity.getCacheDir(),"images");
        Uri uri = null;

        try {

            imageFolder.mkdirs();
            File file = new File(imageFolder,"shared_image.png");
            FileOutputStream stream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG,90,stream);
            stream.flush();
            stream.close();
            uri = FileProvider.getUriForFile(mActivity,"com.ornoiragency.chacks.fileprovider",file);

        } catch (Exception e){
            Toast.makeText(mActivity, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
        }

        return uri;
    }

    @Override
    public int getItemCount() {
       return postList.size();
    }

    @Override
    public int getItemViewType(int position) {

        if (postList.get(position).getType().equals("image")) {
            Log.e("TAG", "getItemViewType position : " + position);
            Log.e("TAG", "getItemViewType position% : " + position % 5);
           // if (position % 5 == 0)
             //   return position % AD_DISPLAY_FREQUENCY != 0 ? POST_IMAGE : AD_TYPE;
            return POST_IMAGE;
        } else if (postList.get(position).getType().equals("text")){
            Log.e("TAG", "getItemViewType position : " + position);
            Log.e("TAG", "getItemViewType position% : " + position % 5);
           // if (position % 5 == 0)
             //   return position % AD_DISPLAY_FREQUENCY != 0 ? POST_TEXT : AD_TYPE;
            return POST_TEXT;
        } else if (postList.get(position).getType().equals("video")){
            Log.e("TAG", "getItemViewType position : " + position);
            Log.e("TAG", "getItemViewType position% : " + position % 5);
          //  if (position % 5 == 0)
            //    return position % AD_DISPLAY_FREQUENCY != 0 ? POST_VIDEO : AD_TYPE;
            return POST_VIDEO;

        } else if (postList.get(position).getType().equals("audio")){
            Log.e("TAG", "getItemViewType position : " + position);
            Log.e("TAG", "getItemViewType position% : " + position % 5);
           // if (position % 5 == 0)
             //   return position % AD_DISPLAY_FREQUENCY != 0 ? POST_AUDIO : AD_TYPE;
            return POST_AUDIO;
        }
        return getItemViewType(position);
    }

    public class PostImageViewHolder extends RecyclerView.ViewHolder{

        //post image view
        public  ImageView uPictureIv,pImageIv;
        public TextView uNameTv,pTimeTv,pLikesTv,pCommentTv;
        public EmojiTextView pDescriptionTv;
        public ImageButton moreBtn;
        public ImageView likeBtn,commentBtn,shareBtn;

        LinearLayout like,comment;

        int countLikes;
        String mUid;
        DatabaseReference likesRef;

        public PostImageViewHolder(@NonNull View itemView) {
            super(itemView);

            //post image image
            uPictureIv = itemView.findViewById(R.id.uPictureIv);
            pImageIv = itemView.findViewById(R.id.post_image);
            uNameTv = itemView.findViewById(R.id.uNameTv);
            pTimeTv = itemView.findViewById(R.id.pTimeTv);

            like = itemView.findViewById(R.id.like);
            comment = itemView.findViewById(R.id.comment);

            //post imgae button,text
            pDescriptionTv = itemView.findViewById(R.id.pDescriptionTv);
            moreBtn = itemView.findViewById(R.id.moreBtn);
            likeBtn = itemView.findViewById(R.id.likeBtn);
            commentBtn = itemView.findViewById(R.id.commentBtn);
            shareBtn = itemView.findViewById(R.id.shareBtn);
            pLikesTv = itemView.findViewById(R.id.pLikesTv);
            pCommentTv = itemView.findViewById(R.id.pCommentTv);

            likesRef = FirebaseDatabase.getInstance().getReference().child("Likes");
            mUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        }

        private void setLikes(RecyclerView.ViewHolder holder, String postKey) {

            likesRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    if (dataSnapshot.child(postKey).hasChild(mUid)){

                        countLikes = (int) dataSnapshot.child(postKey).getChildrenCount();
                        ((PostImageViewHolder)holder).likeBtn.setImageResource(R.drawable.ic_liked__home_btn);
                        ((PostImageViewHolder)holder).pLikesTv.setText((countLikes +(" "+ mActivity.getString(R.string.like))));

                    } else {
                        countLikes = (int) dataSnapshot.child(postKey).getChildrenCount();
                        ((PostImageViewHolder)holder).likeBtn.setImageResource(R.drawable.ic_like_home_icon);
                        ((PostImageViewHolder)holder).pLikesTv.setText((countLikes +(" "+ mActivity.getString(R.string.like))));
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
    }

    public class PostTextViewHolder extends RecyclerView.ViewHolder{

        //post image view
        public  ImageView uPictureIv;
        public TextView uNameTv,pTimeTv,pLikesTv,pCommentTv;
        public EmojiTextView pText;
        public ImageButton moreBtn;
        public ImageView likeBtn,commentBtn,shareBtn;

        int countLikes;
        String mUid;
        DatabaseReference likesRef;

        public PostTextViewHolder(@NonNull View itemView) {
            super(itemView);

            //post text
            uPictureIv = itemView.findViewById(R.id.uPictureIv);
            uNameTv = itemView.findViewById(R.id.uNameTv);
            pTimeTv = itemView.findViewById(R.id.pTimeTv);
            pText = itemView.findViewById(R.id.post_text);
            //post imgae button,text
            moreBtn = itemView.findViewById(R.id.moreBtn);
            likeBtn = itemView.findViewById(R.id.likeBtn);
            commentBtn = itemView.findViewById(R.id.commentBtn);
            shareBtn = itemView.findViewById(R.id.shareBtn);
            pLikesTv = itemView.findViewById(R.id.pLikesTv);
            pCommentTv = itemView.findViewById(R.id.pCommentTv);

            likesRef = FirebaseDatabase.getInstance().getReference().child("Likes");
            mUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        }

        public void setLikesForText(RecyclerView.ViewHolder holder, String postKey) {

            likesRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    if (dataSnapshot.child(postKey).hasChild(mUid)){

                        countLikes = (int) dataSnapshot.child(postKey).getChildrenCount();
                        ((PostTextViewHolder)holder).likeBtn.setImageResource(R.drawable.ic_liked__home_btn);
                        ((PostTextViewHolder)holder).pLikesTv.setText((countLikes +(" "+ mActivity.getString(R.string.like))));

                    } else {
                        countLikes = (int) dataSnapshot.child(postKey).getChildrenCount();
                        ((PostTextViewHolder)holder).likeBtn.setImageResource(R.drawable.ic_like_home_icon);
                       ((PostTextViewHolder)holder).pLikesTv.setText((countLikes +(" "+ mActivity.getString(R.string.like))));
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }

    }

    public class PostVideoViewHolder extends RecyclerView.ViewHolder{

        //post image view
        public  ImageView uPictureIv;
        public TextView uNameTv,pTimeTv,pLikesTv,pCommentTv;
        public EmojiTextView pDescriptionTv;
        public ImageButton moreBtn;
        public ImageView likeBtn,commentBtn,shareBtn;

        public VideoView post_video;
        public TextView video_timer;
        public ImageView post_video_play,post_video_cover;
        public ProgressBar post_video_progress;


        int countLikes;
        String mUid;
        DatabaseReference likesRef;

        public PostVideoViewHolder(@NonNull View itemView) {
            super(itemView);

            //post video
            uPictureIv = itemView.findViewById(R.id.uPictureIv);
            post_video = itemView.findViewById(R.id.post_video);
            video_timer = itemView.findViewById(R.id.post_video_timer);
            post_video_play = itemView.findViewById(R.id.post_video_play);
            post_video_cover = itemView.findViewById(R.id.post_video_cover);
            post_video_progress = itemView.findViewById(R.id.post_video_progress);
            uNameTv = itemView.findViewById(R.id.uNameTv);
            pTimeTv = itemView.findViewById(R.id.pTimeTv);

            //post imgae button,text
            pDescriptionTv = itemView.findViewById(R.id.pDescriptionTv);
            moreBtn = itemView.findViewById(R.id.moreBtn);
            likeBtn = itemView.findViewById(R.id.likeBtn);
            commentBtn = itemView.findViewById(R.id.commentBtn);
            shareBtn = itemView.findViewById(R.id.shareBtn);
            pLikesTv = itemView.findViewById(R.id.pLikesTv);
            pCommentTv = itemView.findViewById(R.id.pCommentTv);

            likesRef = FirebaseDatabase.getInstance().getReference().child("Likes");
            mUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        }

        private void setLikesForVideo(RecyclerView.ViewHolder holder, String postKey) {

            likesRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    if (dataSnapshot.child(postKey).hasChild(mUid)){

                        countLikes = (int) dataSnapshot.child(postKey).getChildrenCount();
                        ((PostVideoViewHolder)holder).likeBtn.setImageResource(R.drawable.ic_liked__home_btn);
                        ((PostVideoViewHolder)holder).pLikesTv.setText((countLikes +(" "+ mActivity.getString(R.string.like))));

                    } else {
                        countLikes = (int) dataSnapshot.child(postKey).getChildrenCount();
                        ((PostVideoViewHolder)holder).likeBtn.setImageResource(R.drawable.ic_like_home_icon);
                        ((PostVideoViewHolder)holder).pLikesTv.setText((countLikes +(" "+ mActivity.getString(R.string.like))));
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
    }

    public class PostAudioViewHolder extends RecyclerView.ViewHolder{

        //post image view
        public  ImageView uPictureIv;
        public TextView uNameTv,pTimeTv,pLikesTv,pCommentTv;
        public EmojiTextView pDescriptionTv;
        public ImageButton moreBtn;
        public ImageView likeBtn,commentBtn,shareBtn;

        public TextView post_audio_artist,post_audio_title,post_audio_duration;
        public CircleImageView post_audio_cover;
        public ProgressBar post_audio_progress,post_audi_load;
        public ImageView post_audi_play;

        public AvatarImageView post_audio_download;

        int countLikes;
        String mUid;
        DatabaseReference likesRef;

        ConstraintLayout global_container;

        public PostAudioViewHolder(@NonNull View itemView) {
            super(itemView);

            //post audio
            pDescriptionTv = itemView.findViewById(R.id.pDescriptionTv);
            uPictureIv = itemView.findViewById(R.id.uPictureIv);
            uNameTv = itemView.findViewById(R.id.uNameTv);
            pTimeTv = itemView.findViewById(R.id.pTimeTv);
            post_audio_artist = itemView.findViewById(R.id.post_audio_artist);
            post_audio_title = itemView.findViewById(R.id.post_audio_title);
            post_audio_duration = itemView.findViewById(R.id.post_audio_duration);
            post_audio_cover = itemView.findViewById(R.id.post_audio_cover);
            post_audio_progress = itemView.findViewById(R.id.progressBar);
            post_audi_load = itemView.findViewById(R.id.post_audi_load);
            post_audi_play = itemView.findViewById(R.id.post_audi_play);

            post_audio_download = itemView.findViewById(R.id.post_audio_download);

            global_container = itemView.findViewById(R.id.global_container);
            //post imgae button,text
            moreBtn = itemView.findViewById(R.id.moreBtn);
            likeBtn = itemView.findViewById(R.id.likeBtn);
            commentBtn = itemView.findViewById(R.id.commentBtn);
            shareBtn = itemView.findViewById(R.id.shareBtn);
            pLikesTv = itemView.findViewById(R.id.pLikesTv);
            pCommentTv = itemView.findViewById(R.id.pCommentTv);

            likesRef = FirebaseDatabase.getInstance().getReference().child("Likes");
            mUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        }

        private void setLikesForAudio(RecyclerView.ViewHolder holder, String postKey) {

            likesRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    if (dataSnapshot.child(postKey).hasChild(mUid)){

                        countLikes = (int) dataSnapshot.child(postKey).getChildrenCount();
                        ((PostAudioViewHolder)holder).likeBtn.setImageResource(R.drawable.ic_liked__home_btn);
                        ((PostAudioViewHolder)holder).pLikesTv.setText((countLikes +(" "+ mActivity.getString(R.string.like))));


                    } else {
                         countLikes = (int) dataSnapshot.child(postKey).getChildrenCount();
                        ((PostAudioViewHolder)holder).likeBtn.setImageResource(R.drawable.ic_like_home_icon);
                        ((PostAudioViewHolder)holder).pLikesTv.setText((countLikes +(" "+ mActivity.getString(R.string.like))));
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
    }

    private static class AdHolder extends RecyclerView.ViewHolder {

        NativeAdLayout nativeAdLayout;
        MediaView mvAdMedia;
        MediaView ivAdIcon;
        TextView tvAdTitle;
        TextView tvAdBody;
        TextView tvAdSocialContext;
        TextView tvAdSponsoredLabel;
        Button btnAdCallToAction;
        LinearLayout adChoicesContainer;
        LinearLayout adLoading;

        AdHolder(NativeAdLayout adLayout) {
            super(adLayout);

            adLoading = adLayout.findViewById(R.id.adLoading);

            nativeAdLayout = adLayout;
            mvAdMedia = adLayout.findViewById(R.id.native_ad_media);
            tvAdTitle = adLayout.findViewById(R.id.native_ad_title);
            tvAdBody = adLayout.findViewById(R.id.native_ad_body);
            tvAdSocialContext = adLayout.findViewById(R.id.native_ad_social_context);
            tvAdSponsoredLabel = adLayout.findViewById(R.id.native_ad_sponsored_label);
            btnAdCallToAction = adLayout.findViewById(R.id.native_ad_call_to_action);
            ivAdIcon = adLayout.findViewById(R.id.native_ad_icon);
            adChoicesContainer = adLayout.findViewById(R.id.ad_choices_container);
        }
    }

}
