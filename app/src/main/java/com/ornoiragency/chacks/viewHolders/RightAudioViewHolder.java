package com.ornoiragency.chacks.viewHolders;

import android.content.Context;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.Guideline;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.ornoiragency.chacks.R;


public class RightAudioViewHolder extends RecyclerView.ViewHolder{

    public  TextView voiceTimer;
    public ProgressBar progressBar,voiceLoading;
    public ImageView playVoice;
    public LottieAnimationView audioAnimation;
    public SeekBar audioSeekBar;
    public TextView timeTv,isSeenTv;

    public RightAudioViewHolder(View rightAudioMessageView) {
        super(rightAudioMessageView);

        timeTv = itemView.findViewById(R.id.timeTv);
        isSeenTv = itemView.findViewById(R.id.isSeenTv);
        audioSeekBar = itemView.findViewById(R.id.audioSeekBar);
        audioAnimation = itemView.findViewById(R.id.audio_animation);

        voiceLoading = (ProgressBar) itemView.findViewById(R.id.voice_loading);
        voiceTimer = (TextView) itemView.findViewById(R.id.voiceTime);
        playVoice = (ImageView) itemView.findViewById(R.id.playVoice);
        progressBar = (ProgressBar) itemView.findViewById(R.id.progressBar);
    }
}
