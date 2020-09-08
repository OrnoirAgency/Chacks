package com.ornoiragency.chacks.Adapter;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.cometchat.pro.constants.CometChatConstants;
import com.cometchat.pro.helpers.Logger;
import com.danikula.videocache.HttpProxyCacheServer;
import com.google.firebase.auth.FirebaseAuth;
import com.ornoiragency.chacks.Activity.PlayMessageVideoActivity;
import com.ornoiragency.chacks.R;
import com.ornoiragency.chacks.Utils.FontUtils;
import com.ornoiragency.chacks.models.Chat;
import com.ornoiragency.chacks.viewHolders.LeftAudioViewHolder;
import com.ornoiragency.chacks.viewHolders.LeftFileViewHolder;
import com.ornoiragency.chacks.viewHolders.LeftImageVideoViewHolder;
import com.ornoiragency.chacks.viewHolders.LeftMessageTextViewHolder;
import com.ornoiragency.chacks.viewHolders.LeftVideoViewHolder;
import com.ornoiragency.chacks.viewHolders.RightAudioViewHolder;
import com.ornoiragency.chacks.viewHolders.RightFileViewHolder;
import com.ornoiragency.chacks.viewHolders.RightImageVideoViewHolder;
import com.ornoiragency.chacks.viewHolders.RightMessageTextViewHolder;
import com.ornoiragency.chacks.viewHolders.RightVideoViewHolder;
import com.stfalcon.frescoimageviewer.ImageViewer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Locale;

import static com.facebook.react.bridge.UiThreadUtil.runOnUiThread;
import static com.ornoiragency.chacks.Chacks.getProxy;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final String TAG = "ChatAdapter";

    public static final int RIGHT_TEXT_MESSAGE =0 ;
    public static final int RIGHT_IMAGE_MESSAGE =1 ;
    public static final int RIGHT_VIDEO_MESSAGE =2 ;
    public static final int RIGHT_AUDIO_MESSAGE =3 ;
    public static final int RIGHT_FILE_MESSAGE =4 ;
    private static final int LEFT_TEXT_MESSAGE =6 ;
    private static final int LEFT_IMAGE_MESSAGE = 7;
    private static final int LEFT_VIDEO_MESSAGE = 8;
    private static final int LEFT_AUDIO_MESSAGE = 9;
    private static final int LEFT_FILE_MESSAGE = 10;


    private Activity mActivity;
    private ArrayList<Chat> ChatList;

    private FirebaseAuth firebaseAuth;

    // media player
    private static MediaPlayer mediaPlayer;
    Runnable run;
    String proxyUrl;
    private boolean firstLaunch = true;
    private Handler seekHandler = new Handler(Looper.getMainLooper());

    private boolean isPlaying = false;
    //  private RecyclerItemClickListener listener;
    private ChatAdapter.OnItemClickListener mOnItemClickListener;

    public ChatAdapter(Activity activity, ArrayList<Chat> ChatList) {
        this.mActivity = activity;
        this.ChatList = ChatList;

        firebaseAuth = FirebaseAuth.getInstance();

        //initialse mediaplayer
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
    }

    public interface OnItemClickListener {
        void onItemClick(View view, RecyclerView.ViewHolder holder, Chat chat, int position);
    }

    public void setOnItemClickListener(final ChatAdapter.OnItemClickListener mItemClickListener) {
        this.mOnItemClickListener = mItemClickListener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {

        switch (i){

            case RIGHT_TEXT_MESSAGE:
                View rightTextMessageView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.row_chat_message_text_right, parent, false);
                return new RightMessageTextViewHolder(rightTextMessageView);

            case LEFT_TEXT_MESSAGE:
                View leftTextMessageView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.row_chat_message_text_left, parent, false);
                return new LeftMessageTextViewHolder(leftTextMessageView);

            case LEFT_IMAGE_MESSAGE:
                View leftImageMessageView = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_message_image_left, parent, false);
                return new LeftImageVideoViewHolder(leftImageMessageView);

            case RIGHT_IMAGE_MESSAGE:
                View rightImageMessageView = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_message_image_right, parent, false);
                return new RightImageVideoViewHolder(rightImageMessageView);

            case LEFT_AUDIO_MESSAGE:
                View leftAudioMessageView = LayoutInflater.from(parent.getContext()).inflate(R.layout.cc_audionote_layout_left, parent, false);
                return new LeftAudioViewHolder(leftAudioMessageView);

            case RIGHT_AUDIO_MESSAGE:
                View rightAudioMessageView = LayoutInflater.from(parent.getContext()).inflate(R.layout.cc_audionote_layout_right, parent, false);
                return new RightAudioViewHolder(rightAudioMessageView);

            case RIGHT_VIDEO_MESSAGE:
                View rightVideoMessageView = LayoutInflater.from(parent.getContext()).inflate(R.layout.cc_image_video_layout_right, parent, false);
                return new RightVideoViewHolder(rightVideoMessageView);

            case LEFT_VIDEO_MESSAGE:
                View leftVideoMessageView = LayoutInflater.from(parent.getContext()).inflate(R.layout.cc_image_video_layout_left, parent, false);
                return new LeftImageVideoViewHolder(leftVideoMessageView);

            case RIGHT_FILE_MESSAGE:
                View rightFileMessage = LayoutInflater.from(parent.getContext()).inflate(R.layout.right_file_layout, parent, false);
                return new RightFileViewHolder(rightFileMessage);

            case LEFT_FILE_MESSAGE:
                View leftFileMessage = LayoutInflater.from(parent.getContext()).inflate(R.layout.left_file_layout, parent, false);
                return new LeftFileViewHolder(leftFileMessage);

            default:
                return null;
        }

    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        Chat model = ChatList.get(position);

        String timestamp = model.getTimestamp();
        String message = model.getMessage();
        String videoLegend = model.getVideoLegend();
        String videoMessage = model.getVideo();
        String videoCover = model.getVideoCover();
        String messagePdf = model.getPdf();
        String messageDoc = model.getDocFile();
        String audioDuration = model.getAudioDuration();
        String messageType = model.getType();

        Calendar cal = Calendar.getInstance(Locale.FRENCH);
        cal.setTimeInMillis(Long.parseLong(timestamp));
        String dateTime = DateFormat.format("dd MMMM,yy | HH:mm |",cal).toString();


        switch (holder.getItemViewType()) {

            case LEFT_TEXT_MESSAGE:
                LeftMessageTextViewHolder leftMessageViewHolder = (LeftMessageTextViewHolder) holder;
                leftMessageViewHolder.timeTv.setText(dateTime);
                leftMessageViewHolder.messageTv.setText(message);
                if (model.isIsseen()){
                    leftMessageViewHolder.isSeenTv.setBackgroundResource(R.drawable.ic_double_tick_blue);
                } else {
                    leftMessageViewHolder.isSeenTv.setBackgroundResource(R.drawable.ic_double_tick);
                }
                break;

            case RIGHT_TEXT_MESSAGE:
                RightMessageTextViewHolder rightCustomMessageViewHolder = (RightMessageTextViewHolder) holder;
                rightCustomMessageViewHolder.timeTv.setText(dateTime);
                rightCustomMessageViewHolder.messageTv.setText(message);
                if (model.isIsseen()){
                    rightCustomMessageViewHolder.isSeenTv.setBackgroundResource(R.drawable.ic_double_tick_blue);
                } else {
                    rightCustomMessageViewHolder.isSeenTv.setBackgroundResource(R.drawable.ic_double_tick);
                }
                break;

            case LEFT_IMAGE_MESSAGE:
                LeftImageVideoViewHolder leftImageViewHolder = (LeftImageVideoViewHolder) holder;
                leftImageViewHolder.timeTv.setText(dateTime);
                if (model.isIsseen()){
                    leftImageViewHolder.isSeenTv.setBackgroundResource(R.drawable.ic_double_tick_blue);
                } else {
                    leftImageViewHolder.isSeenTv.setBackgroundResource(R.drawable.ic_double_tick);
                }
                Glide.with(mActivity.getApplicationContext())
                        .load(model.getImageFile())
                        .into(leftImageViewHolder.messageImage);

                leftImageViewHolder.messageImage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        new ImageViewer.Builder(v.getContext(), Collections.singletonList(model.getImageFile()))
                                .setStartPosition(0)
                                .show();
                    }
                });

                break;

            case RIGHT_IMAGE_MESSAGE:
                RightImageVideoViewHolder rightImageVideoViewHolder = (RightImageVideoViewHolder) holder;
                rightImageVideoViewHolder.timeTv.setText(dateTime);
                if (model.isIsseen()){
                    rightImageVideoViewHolder.isSeenTv.setBackgroundResource(R.drawable.ic_double_tick_blue);
                } else {
                    rightImageVideoViewHolder.isSeenTv.setBackgroundResource(R.drawable.ic_double_tick);
                }
                Glide.with(mActivity.getApplicationContext())
                        .load(model.getImageFile())
                        .into(rightImageVideoViewHolder.messageImage);

                rightImageVideoViewHolder.messageImage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        new ImageViewer.Builder(v.getContext(), Collections.singletonList(model.getImageFile()))
                                .setStartPosition(0)
                                .show();
                    }
                });

                break;

            case RIGHT_AUDIO_MESSAGE:
                RightAudioViewHolder rightAudioViewHolder = (RightAudioViewHolder) holder;
                rightAudioViewHolder.voiceTimer.setText(milliSecondsToTimer(Long.parseLong(audioDuration)));
                rightAudioViewHolder.timeTv.setText(dateTime);
                if (model.isIsseen()){
                    rightAudioViewHolder.isSeenTv.setBackgroundResource(R.drawable.ic_double_tick_blue);
                } else {
                    rightAudioViewHolder.isSeenTv.setBackgroundResource(R.drawable.ic_double_tick);
                }

                HttpProxyCacheServer proxy = getProxy(mActivity.getApplicationContext());
                proxyUrl = proxy.getProxyUrl(model.getAudioFile());

                rightAudioViewHolder.playVoice.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {


                        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                            @Override
                            public void onPrepared(MediaPlayer mp) {
                                //lancer la note vocal
                                togglePlay(mp,rightAudioViewHolder.progressBar,rightAudioViewHolder.playVoice,
                                        rightAudioViewHolder.voiceTimer,rightAudioViewHolder.audioAnimation,rightAudioViewHolder.voiceLoading,holder);

                            }
                        });

                        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                            @Override
                            public void onCompletion(MediaPlayer mediaPlayer) {


                                rightAudioViewHolder.audioAnimation.setVisibility(View.GONE);
                                rightAudioViewHolder.playVoice.setImageResource(R.drawable.ic_play);
                                rightAudioViewHolder.voiceTimer.setText(milliSecondsToTimer(Long.parseLong(model.getAudioDuration())));
                                seekHandler.removeCallbacks(run);

                            }
                        });

                        HttpProxyCacheServer proxy = getProxy(mActivity.getApplicationContext());
                        proxyUrl = proxy.getProxyUrl(model.getAudioFile());

                        firstLaunch = false;
                        prepareSong(proxyUrl,rightAudioViewHolder.voiceLoading,holder);
                        //Controle de la note vocal
                        pushPlay(proxyUrl,rightAudioViewHolder.playVoice, rightAudioViewHolder.voiceLoading,rightAudioViewHolder.audioAnimation,holder);

                    }
                });

                break;

            case LEFT_AUDIO_MESSAGE:

                LeftAudioViewHolder leftAudioViewHolder = (LeftAudioViewHolder) holder;
                leftAudioViewHolder.voiceTimer.setText(milliSecondsToTimer(Long.parseLong(audioDuration)));
                leftAudioViewHolder.timeTv.setText(dateTime);

                leftAudioViewHolder.playVoice.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                            @Override
                            public void onPrepared(MediaPlayer mp) {
                                //lancer la note vocal
                                togglePlay(mp,leftAudioViewHolder.progressBar,leftAudioViewHolder.playVoice,
                                        leftAudioViewHolder.voiceTimer,leftAudioViewHolder.audioAnimation,leftAudioViewHolder.voiceLoading,holder);

                            }
                        });

                        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                            @Override
                            public void onCompletion(MediaPlayer mediaPlayer) {

                                leftAudioViewHolder.audioAnimation.setVisibility(View.GONE);
                                leftAudioViewHolder.playVoice.setImageResource(R.drawable.ic_play);
                                leftAudioViewHolder.voiceTimer.setText(milliSecondsToTimer(Long.parseLong(model.getAudioDuration())));
                                seekHandler.removeCallbacks(run);

                            }
                        });

                        HttpProxyCacheServer proxy = getProxy(mActivity.getApplicationContext());
                        proxyUrl = proxy.getProxyUrl(model.getAudioFile());

                        firstLaunch = false;
                        prepareSong(proxyUrl, leftAudioViewHolder.voiceLoading, holder);
                        //Controle de la note vocal
                        pushPlay(proxyUrl,leftAudioViewHolder.playVoice, leftAudioViewHolder.voiceLoading,leftAudioViewHolder.audioAnimation,holder);
                    }
                });

                break;

            case LEFT_VIDEO_MESSAGE:
                final LeftVideoViewHolder leftVideoViewHolder = (LeftVideoViewHolder) holder;
                leftVideoViewHolder.btnPlayVideo.setVisibility(View.VISIBLE);
                leftVideoViewHolder.timeTv.setText(dateTime);
                if (model.isIsseen()){
                    leftVideoViewHolder.isSeenTv.setBackgroundResource(R.drawable.ic_double_tick_blue);
                } else {
                    leftVideoViewHolder.isSeenTv.setBackgroundResource(R.drawable.ic_double_tick);
                }

                RequestOptions requestOptions = new RequestOptions().fitCenter()
                        .placeholder(R.drawable.ic_broken_image);
                Glide.with(mActivity.getApplicationContext())
                        .load(videoCover)
                        .apply(requestOptions)
                        .into(leftVideoViewHolder.messageVideo);

                break;

            case RIGHT_VIDEO_MESSAGE:

                RightVideoViewHolder rightVideoViewHolder = (RightVideoViewHolder) holder;
                rightVideoViewHolder.btnPlayVideo.setVisibility(View.VISIBLE);
                rightVideoViewHolder.timeTv.setText(dateTime);
                if (model.isIsseen()){
                    rightVideoViewHolder.isSeenTv.setBackgroundResource(R.drawable.ic_double_tick_blue);
                } else {
                    rightVideoViewHolder.isSeenTv.setBackgroundResource(R.drawable.ic_double_tick);
                }

                Glide.with(mActivity.getApplicationContext())
                        .load(videoCover)
                        .into(rightVideoViewHolder.messageVideo);

                rightVideoViewHolder.btnPlayVideo.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(mActivity, PlayMessageVideoActivity.class);
                        intent.putExtra("videoId",videoMessage);
                        mActivity.startActivity(intent);
                    }
                });

                break;

            case RIGHT_FILE_MESSAGE:
                try {
                    RightFileViewHolder rightFileViewHolder = (RightFileViewHolder) holder;

                    rightFileViewHolder.fileType.setTypeface(FontUtils.robotoRegular);
                    rightFileViewHolder.fileName.setTypeface(FontUtils.robotoRegular);

                    rightFileViewHolder.timeTv.setText(dateTime);
                    if (model.isIsseen()){
                        rightFileViewHolder.isSeenTv.setBackgroundResource(R.drawable.ic_double_tick_blue);
                    } else {
                        rightFileViewHolder.isSeenTv.setBackgroundResource(R.drawable.ic_double_tick);
                    }

                    rightFileViewHolder.fileName.setText(model.getFileName());
                    rightFileViewHolder.fileType.setText(model.getFileType());

                    rightFileViewHolder.fileName.setOnClickListener(view -> mActivity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(model.getDocFile()))));


                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;

            case LEFT_FILE_MESSAGE:
                try {

                    LeftFileViewHolder leftFileViewHolder = (LeftFileViewHolder) holder;
                    leftFileViewHolder.avatar.setVisibility(View.GONE);
                    leftFileViewHolder.senderName.setVisibility(View.GONE);
                    leftFileViewHolder.fileType.setTypeface(FontUtils.robotoRegular);
                    leftFileViewHolder.fileName.setTypeface(FontUtils.robotoRegular);
                    leftFileViewHolder.timeTv.setText(dateTime);
                    leftFileViewHolder.fileName.setText(model.getFileName());
                    leftFileViewHolder.fileType.setText(model.getFileType());
                   leftFileViewHolder.fileName.setOnClickListener(view -> mActivity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(model.getDocFile()))));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;


        }
    }

    private void togglePlay(MediaPlayer mp, ProgressBar progressBar, ImageView playVoice, TextView voiceTimer,
                            LottieAnimationView audioAnimation, ProgressBar voiceLoading, RecyclerView.ViewHolder holder) {
        if (mp.isPlaying()){
            mp.stop();
            mp.reset();

        }else {
            mp.start();
            voiceLoading.setVisibility(View.GONE);
            playVoice.setImageDrawable(ContextCompat.getDrawable(mActivity,R.drawable.ic_pause));
            audioAnimation.setVisibility(View.VISIBLE);

            seekHandler = new Handler();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    progressBar.setMax(100);
                    progressBar.setProgress((int)(((float)mediaPlayer.getCurrentPosition()/mediaPlayer.getDuration())*100));
                    voiceTimer.setText(milliSecondsToTimer(mediaPlayer.getCurrentPosition()));
                    seekHandler.postDelayed(this,100);
                }
            });
        }
    }
    private void pushPlay(String proxyUrl, ImageView playVoice, ProgressBar voiceLoading,
                          LottieAnimationView audioAnimation, RecyclerView.ViewHolder holder) {

     playVoice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (mediaPlayer.isPlaying() && mediaPlayer != null){
                   playVoice.setImageResource(R.drawable.ic_play);
                   audioAnimation.setVisibility(View.GONE);
                    mediaPlayer.pause();

                } else {

                    if (firstLaunch){
                        // Post post = postList.get(0);
                        prepareSong(proxyUrl,voiceLoading, holder);
                    } else {

                        mediaPlayer.start();
                        firstLaunch = false;
                    }
                    playVoice.setImageResource(R.drawable.ic_pause);
                    audioAnimation.setVisibility(View.VISIBLE);

                }
            }
        });
    }
    private void prepareSong(String proxyUrl, ProgressBar voiceLoading, RecyclerView.ViewHolder holder) {

        String stream = proxyUrl;
        voiceLoading.setVisibility(View.VISIBLE);

        mediaPlayer.reset();
        try {
            mediaPlayer.setDataSource(stream);
            mediaPlayer.prepareAsync();
        } catch (IOException e) {
            e.printStackTrace();
        }
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
    public void onDestroy() {
      //  tts.shutdown();
        try {
            if (mediaPlayer != null) {
                mediaPlayer.stop();
                mediaPlayer.reset();

            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    public void stopPlayer() {
        try {
            if (mediaPlayer != null) {
                mediaPlayer.stop();
                mediaPlayer.reset();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return ChatList.size();
    }

    @Override
    public int getItemViewType(int position) {

        if (ChatList.get(position).getSender().equals(firebaseAuth.getUid())){

            switch (ChatList.get(position).getType()) {

                case CometChatConstants.MESSAGE_TYPE_TEXT:

                    return RIGHT_TEXT_MESSAGE;

                case CometChatConstants.MESSAGE_TYPE_IMAGE:

                    return RIGHT_IMAGE_MESSAGE;

                case CometChatConstants.MESSAGE_TYPE_VIDEO:

                    return RIGHT_VIDEO_MESSAGE;

                case CometChatConstants.MESSAGE_TYPE_AUDIO:

                    return RIGHT_AUDIO_MESSAGE;

                case CometChatConstants.MESSAGE_TYPE_FILE:
                    return RIGHT_FILE_MESSAGE;

            }
        } else {

            switch (ChatList.get(position).getType()) {

                case CometChatConstants.MESSAGE_TYPE_TEXT:

                    return LEFT_TEXT_MESSAGE;

                case CometChatConstants.MESSAGE_TYPE_IMAGE:

                    return LEFT_IMAGE_MESSAGE;

                case CometChatConstants.MESSAGE_TYPE_VIDEO:

                    return LEFT_VIDEO_MESSAGE;

                case CometChatConstants.MESSAGE_TYPE_AUDIO:

                    return LEFT_AUDIO_MESSAGE;

                case CometChatConstants.MESSAGE_TYPE_FILE:
                    return LEFT_FILE_MESSAGE;
            }
        }

        return super.getItemViewType(position);
    }

}
