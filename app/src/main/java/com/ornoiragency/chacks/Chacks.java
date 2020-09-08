package com.ornoiragency.chacks;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.danikula.videocache.HttpProxyCacheServer;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.OkHttp3Downloader;
import com.squareup.picasso.Picasso;
import com.vanniktech.emoji.EmojiManager;
import com.vanniktech.emoji.ios.IosEmojiProvider;

public class Chacks extends Application {

    public static final String NIGHT_MODE = "NIGHT_MODE";
    private boolean isNightModeEnabled = false;

    private static Chacks singleton = null;

    public static Chacks getInstance() {

        if(singleton == null)
        {
            singleton = new Chacks();
        }
        return singleton;
    }

    @Override
    public void onCreate()
    {
        super.onCreate();

        singleton = this;
        SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        this.isNightModeEnabled = mPrefs.getBoolean(NIGHT_MODE, false);

        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        EmojiManager.install(new IosEmojiProvider());
        //load picture offline - picasso
        Picasso.Builder builder = new Picasso.Builder(this);
        builder.downloader(new OkHttp3Downloader(this,Integer.MAX_VALUE));
        Picasso built = builder.build();
        built.setIndicatorsEnabled(true);
        built.setLoggingEnabled(true);
        Picasso.setSingletonInstance(built);


    }

    private HttpProxyCacheServer proxy;

    public static HttpProxyCacheServer getProxy(Context context) {
        Chacks app = (Chacks) context.getApplicationContext();
        return app.proxy == null ? (app.proxy = app.newProxy()) : app.proxy;
    }

    private HttpProxyCacheServer newProxy() {
        return new HttpProxyCacheServer(this);
    }

    public boolean isNightModeEnabled() {
        return isNightModeEnabled;
    }

    public void setIsNightModeEnabled(boolean isNightModeEnabled) {
        this.isNightModeEnabled = isNightModeEnabled;

        SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = mPrefs.edit();
        editor.putBoolean(NIGHT_MODE, isNightModeEnabled);
        editor.apply();
    }
}