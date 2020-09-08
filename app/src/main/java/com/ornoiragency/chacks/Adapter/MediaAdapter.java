package com.ornoiragency.chacks.Adapter;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import androidx.recyclerview.widget.RecyclerView.Adapter;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;
import com.bumptech.glide.Glide;
import com.ornoiragency.chacks.R;

import java.util.ArrayList;

public class MediaAdapter extends Adapter<MediaAdapter.MediaViewHolder> {
    Context context;
    ArrayList<String> mediaList;

    public class MediaViewHolder extends ViewHolder {
        FrameLayout mLayout;
        ImageView mMedia;

        public MediaViewHolder(View itemView) {
            super(itemView);
            this.mMedia = (ImageView) itemView.findViewById(R.id.media);
            this.mLayout = (FrameLayout) itemView.findViewById(R.id.layout);
        }
    }

    public MediaAdapter(Context context2, ArrayList<String> mediaList2) {
        this.context = context2;
        this.mediaList = mediaList2;
    }

    public MediaViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new MediaViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_media, null, false));
    }

    public void onBindViewHolder(final MediaViewHolder holder, int position) {
        Glide.with(this.context).load(Uri.parse((String) this.mediaList.get(position))).into(holder.mMedia);
        holder.mLayout.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (!MediaAdapter.this.mediaList.isEmpty()) {
                    MediaAdapter.this.mediaList.remove(holder.getAdapterPosition());
                    MediaAdapter.this.notifyDataSetChanged();
                }
            }
        });
    }

    public int getItemCount() {
        return this.mediaList.size();
    }
}
