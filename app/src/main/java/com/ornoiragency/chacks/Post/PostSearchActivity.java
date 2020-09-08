package com.ornoiragency.chacks.Post;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.miguelcatalan.materialsearchview.MaterialSearchView;
import com.ornoiragency.chacks.R;

public class PostSearchActivity extends AppCompatActivity {

    Toolbar toolbar;
    MaterialSearchView searchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_search);

        toolbar = findViewById(R.id.toolbar);
        searchView = findViewById(R.id.search_view);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);

        menu.findItem(R.id.action_logout).setVisible(false);
        menu.findItem(R.id.action_groupinfo).setVisible(false);
        menu.findItem(R.id.action_search).setVisible(true);
        menu.findItem(R.id.action_settings).setVisible(false);

        MenuItem search = menu.findItem(R.id.action_search);
        searchView.setMenuItem(search);

        searchView.setOnQueryTextListener(new MaterialSearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                //Do some magic
                if (!TextUtils.isEmpty(s)){
                    // searchChatList(s);
                } else {
                    // chatList();
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                //Do some magic
                if (!TextUtils.isEmpty(s)){
                    // searchChatList(s);
                } else {
                    // chatList();
                }
                return false;
            }
        });

        searchView.setOnSearchViewListener(new MaterialSearchView.SearchViewListener() {
            @Override
            public void onSearchViewShown() {
                //Do some magic
            }

            @Override
            public void onSearchViewClosed() {
                //Do some magic
            }
        });

        return (super.onCreateOptionsMenu(menu));
    }




}