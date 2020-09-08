package com.ornoiragency.chacks.Post;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AbsListView;
import android.widget.VideoView;

import com.facebook.ads.AdError;
import com.facebook.ads.NativeAdsManager;
import com.ornoiragency.chacks.R;
import com.ornoiragency.chacks.Utils.DividerItemDecoration;
import com.ornoiragency.chacks.models.Post;

import java.util.List;

public class AllVideoPostActivity extends AppCompatActivity implements NativeAdsManager.Listener{

    VideoView vv_dashboard;
    private RecyclerView recyclerView;
    private View currentFocusedLayout, oldFocusedLayout;

    List<Post> postList;

    private NativeAdsManager mNativeAdsManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_video_post);

        recyclerView = findViewById(R.id.recyclerview);

        String placement_id = "188086015840585_188087219173798";
        mNativeAdsManager = new NativeAdsManager(this, placement_id, 5);
        mNativeAdsManager.loadAds();
        mNativeAdsManager.setListener(this);

        StaggeredGridLayoutManager gaggeredGridLayoutManager  = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        gaggeredGridLayoutManager.setGapStrategy(StaggeredGridLayoutManager.GAP_HANDLING_MOVE_ITEMS_BETWEEN_SPANS);
        gaggeredGridLayoutManager.setReverseLayout(true);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setLayoutManager(gaggeredGridLayoutManager);
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                gaggeredGridLayoutManager.invalidateSpanAssignments();
                super.onScrolled(recyclerView, dx, dy);


            }
        });

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

                if (newState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {
                    //get the recyclerview position which is completely visible and first
                    int positionView = ((LinearLayoutManager) recyclerView.getLayoutManager()).findFirstCompletelyVisibleItemPosition();
                    Log.i("VISISBLE", positionView + "");
                    if (positionView >= 0) {
                        if (oldFocusedLayout != null) {
                            //Stop the previous video playback after new scroll
                            vv_dashboard = (VideoView) oldFocusedLayout.findViewById(R.id.pVideo_video);
                            // vv_dashboard.stopPlayback();
                        }

                        currentFocusedLayout = ((LinearLayoutManager) recyclerView.getLayoutManager()).findViewByPosition(positionView);
                        vv_dashboard = (VideoView) currentFocusedLayout.findViewById(R.id.pVideo_video);
                        //to play video of selected recylerview, videosData is an array-list which is send to recyclerview adapter to fill the view. Here we getting that specific video which is displayed through recyclerview.
                        //  playVideo(postList.get(positionView));
                        oldFocusedLayout = currentFocusedLayout;
                    }
                }

            }
        });
    }

    @Override
    public void onAdsLoaded() {
        if (getApplicationContext() == null) {
            return;
        }

        RecyclerView.ItemDecoration itemDecoration = new DividerItemDecoration(getApplicationContext(),
                DividerItemDecoration.VERTICAL_LIST);
        recyclerView.addItemDecoration(itemDecoration);

    }

    @Override
    public void onAdError(AdError error) {
    }


    private void playVideo(Post post) {

        vv_dashboard.setVideoPath(post.getpVideo());
        vv_dashboard.requestFocus();
        vv_dashboard.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                // mediaPlayer.setLooping(true);
            }
        });
        vv_dashboard.start();
    }
}
