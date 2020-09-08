package com.ornoiragency.chacks.Adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ornoiragency.chacks.Activity.PlayMessageVideoActivity;
import com.ornoiragency.chacks.R;
import com.ornoiragency.chacks.models.Chat;

import com.stfalcon.frescoimageviewer.ImageViewer;
import com.vanniktech.emoji.EmojiTextView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Locale;

import me.alvince.android.avatarimageview.AvatarImageView;

public class AdapterChat extends RecyclerView.Adapter<AdapterChat.holderChat> {

    private static final int MSG_TYPE_LEFT = 0;
    private static final int MSG_TYPE_RIGHT = 1;

    private Context context;
    private ArrayList<Chat> ChatList;

    private FirebaseAuth firebaseAuth;

    //  private RecyclerItemClickListener listener;
    private OnItemClickListener mOnItemClickListener;
    private OnLOngItemClickListener mOnLongItemClickListener;

    public AdapterChat(Context context, ArrayList<Chat> ChatList) {
        this.context = context;
        this.ChatList = ChatList;

        firebaseAuth = FirebaseAuth.getInstance();
    }

    public interface OnItemClickListener {
        void onItemClick(View view, holderChat holder, Chat chat, int position);
    }

    public interface OnLOngItemClickListener {
        void onLongItemClick(View view, holderChat holder, Chat chat, int position);
    }

    public void setOnItemClickListener(final OnItemClickListener mItemClickListener) {
        this.mOnItemClickListener = mItemClickListener;
    }

    public void setOnLongItemClickListener(final OnLOngItemClickListener mItemClickListener) {
        this.mOnLongItemClickListener = mItemClickListener;
    }

    @NonNull
    @Override
    public holderChat onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {


        if (viewType == MSG_TYPE_RIGHT){
            View view = LayoutInflater.from(context).inflate(R.layout.row_chat_right,parent,false);
            return new holderChat(view);
        } else {

            View view = LayoutInflater.from(context).inflate(R.layout.row_chat_left,parent,false);
            return new holderChat(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull holderChat holder, int position) {

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
        holder.timeTv.setText(dateTime);
        holder.messageTv.setText(message);

        if (model.isIsseen()){
            holder.isSeenTv.setBackgroundResource(R.drawable.ic_double_tick_blue);
        } else {
            holder.isSeenTv.setBackgroundResource(R.drawable.ic_double_tick);
        }

        if (messageType.equals("text")){
            //text message
            holder.messageTv.setVisibility(View.VISIBLE);
            holder.videoLayout.setVisibility(View.GONE);
            holder.audioMessage.setVisibility(View.GONE);
            holder.voiceMessage.setVisibility(View.GONE);
            holder.pdfMessage.setVisibility(View.GONE);
            holder.docMessage.setVisibility(View.GONE);
            holder.messageTv.setText(message);
        } else {
            holder.messageTv.setVisibility(View.GONE);
        }

        if (messageType.equals("pdf")){
            holder.message_pdf.setText(message);
            holder.message_pdf.setOnClickListener(view -> context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(messagePdf))));

        } else {

            holder.pdfMessage.setVisibility(View.GONE);
        }

        if (messageType.equals("doc")){
            holder.message_doc.setText(message);
            holder.message_doc.setOnClickListener(view -> context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(messageDoc))));

        } else {

            holder.docMessage.setVisibility(View.GONE);
        }

        if (messageType.equals("voice")){

            holder.voiceMessage.setVisibility(View.VISIBLE);
            holder.voiceTimer.setText(milliSecondsToTimer(Long.parseLong(audioDuration)));


        } else {
            holder.voiceMessage.setVisibility(View.GONE);
        }

        if (messageType.equals("audio")){

            holder.audioMessage.setVisibility(View.VISIBLE);
            holder.message_audio.setText(message);
            holder.audioDuration.setText(milliSecondsToTimer(Long.parseLong(audioDuration)));
        } else {
            holder.audioMessage.setVisibility(View.GONE);
        }


        if (messageType.equals("video")){
            holder.videoLayout.setVisibility(View.VISIBLE);
            holder.videoLegend.setText(videoLegend);

            Glide.with(context)
                    .asBitmap()
                    .load(videoCover)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(holder.videoMessage);


            holder.playVideo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Intent intent = new Intent(context, PlayMessageVideoActivity.class);
                    intent.putExtra("videoId",videoMessage);
                    context.startActivity(intent);
                }
            });

        } else {
            holder.videoLayout.setVisibility(View.GONE);
        }


        //message image
        if (messageType.equals("image")){

            holder.imageMessage.setVisibility(View.VISIBLE);
            Glide.with(context.getApplicationContext())
                    .load(model.getImageFile())
                    .into(holder.messageImage);


            holder.messageImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    new ImageViewer.Builder(v.getContext(), Collections.singletonList(model.getImageFile()))
                            .setStartPosition(0)
                            .show();
                }
            });


            verifyImage(holder,model);

        } else {
            holder.imageMessage.setVisibility(View.GONE);
        }


        if(ChatList.get(holder.getAdapterPosition()).getMediaUrlList().isEmpty()){
            holder.mMediaLayout.setVisibility(View.GONE);
        }
        else{
            holder.mMediaLayout.setVisibility(View.VISIBLE);
            Glide.with(context.getApplicationContext())
                    .load(ChatList.get(position).getMediaUrlList().get(0))
                    .into(holder.mMedia);
            holder.mMediaAmount.setText(String.valueOf(ChatList.get(position).getMediaUrlList().size()));

        }

        holder.mMediaLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new ImageViewer.Builder(v.getContext(), ChatList.get(holder.getAdapterPosition()).getMediaUrlList())
                        .setStartPosition(0)
                        .show();
            }
        });

        holder.playVoice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (mOnItemClickListener != null) {
                    mOnItemClickListener.onItemClick(v, holder, model, position);
                }
            }
        });

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {

                if (mOnLongItemClickListener != null) {
                    mOnLongItemClickListener.onLongItemClick(view, holder, model, position);
                }

                return false;
            }
        });

    }

    private void verifyImage(holderChat holder, Chat model) {

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Chats");
        ref.orderByChild("messageId").equalTo(model.getMessageId())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        for (DataSnapshot ds: dataSnapshot.getChildren()){

                            String image = "" + ds.child("imageFile").getValue();

                            if (image != null){
                                holder.imageMessage.setVisibility(View.VISIBLE);
                            } else {
                                holder.imageMessage.setVisibility(View.GONE);
                            }
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

    @Override
    public int getItemCount() {

        return ChatList.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (ChatList.get(position).getSender().equals(firebaseAuth.getUid())){
            return MSG_TYPE_RIGHT;
        } else {
            return MSG_TYPE_LEFT;
        }
    }



    public class holderChat extends RecyclerView.ViewHolder{

        LinearLayout messageLayout;

        private TextView timeTv,isSeenTv;
        private EmojiTextView messageTv;
        //init reply
        AvatarImageView mMedia;
        TextView mMediaAmount;
        ConstraintLayout mMediaLayout;

        public ConstraintLayout imageMessage;
        public AvatarImageView messageImage;

        ConstraintLayout videoLayout,pdfMessage,docMessage,audioMessage,voiceMessage;
        TextView message_pdf,message_doc;
        TextView videoLegend;
        AvatarImageView videoMessage;
        ImageView playVideo;
        public ImageView playVoice;

        public  TextView message_audio,audioDuration,voiceTimer;
        public ProgressBar progressBar,voiceLoding;

        public holderChat(@NonNull View itemView) {
            super(itemView);

            this.messageLayout = (LinearLayout) itemView.findViewById(R.id.messageLayout);

            this.mMediaAmount = (TextView) itemView.findViewById(R.id.mediaAmount);
            this.mMediaLayout = (ConstraintLayout) itemView.findViewById(R.id.mediaLayout);
            this.mMedia = (AvatarImageView) itemView.findViewById(R.id.media);

            messageTv = itemView.findViewById(R.id.messageTv);
            timeTv = itemView.findViewById(R.id.timeTv);
            isSeenTv = itemView.findViewById(R.id.isSeenTv);

            this.imageMessage = (ConstraintLayout) itemView.findViewById(R.id.imageMessage);
            this.messageImage = (AvatarImageView) itemView.findViewById(R.id.messageImage);

            this.videoLegend = (TextView) itemView.findViewById(R.id.videoLegend);
            this.playVideo = (ImageView) itemView.findViewById(R.id.playVideo);
            this.videoLayout = (ConstraintLayout) itemView.findViewById(R.id.videoLayout);
            this.videoMessage = (AvatarImageView) itemView.findViewById(R.id.videoMessage);

            this.pdfMessage = (ConstraintLayout) itemView.findViewById(R.id.pdfMessage);
            this.message_pdf = (TextView) itemView.findViewById(R.id.message_pdf);
            this.docMessage = (ConstraintLayout) itemView.findViewById(R.id.docMessage);
            this.message_doc = (TextView) itemView.findViewById(R.id.message_doc);

            this.audioMessage = (ConstraintLayout) itemView.findViewById(R.id.audioMessage);
            this.message_audio = (TextView) itemView.findViewById(R.id.message_audio);
            this.audioDuration = (TextView) itemView.findViewById(R.id.audioDuration);

            this.voiceTimer = (TextView) itemView.findViewById(R.id.voiceTime);
            this.playVoice = (ImageView) itemView.findViewById(R.id.playVoice);
            this.progressBar = (ProgressBar) itemView.findViewById(R.id.progressBar);
            this.voiceLoding = (ProgressBar) itemView.findViewById(R.id.voice_loading);
            this.voiceMessage = (ConstraintLayout) itemView.findViewById(R.id.voiceMessage);

        }
    }
}
