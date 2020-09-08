package com.ornoiragency.chacks.Utils;

import android.content.Context;
import android.content.res.Resources;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.util.DisplayMetrics;
import android.util.Log;

import com.ornoiragency.chacks.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class CommonUtils {

    static ConnectivityManager cm = null;
    private static final String TAG = "CommonUtils";

    public static boolean isConnected(Context context) {

        if (cm == null) {
            cm = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        }

        NetworkInfo var0 = cm.getActiveNetworkInfo();
        return null != var0 && var0.isConnectedOrConnecting();
    }

    public static String getRandomElement() {
        List<String> list=new ArrayList<>();
        list.add("superhero1");
        list.add("superhero2");
        list.add("superhero3");
        list.add("superhero4");
        list.add("superhero5");
        list.add("testuser128");
        list.add("testuser12");
        list.add("testuser13");
        list.add("testuser11");
        list.add("testuser15");
        list.add("testuser25");
        list.add("testuser26");
        list.add("testuser28");
        list.add("testuser27");
        list.add("testuser30");
        list.add("testuser31");
        list.add("testuser33");

        Random rand = new Random();
        String s=list.get(rand.nextInt(list.size()));
        Log.d(TAG, "getRandomElement: "+s);
        return s;
    }




    public static float dpToPx(Context context, float valueInDp) {
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float px = valueInDp * ((float)metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
        return px;
    }

    public static SpannableString setTitle(String title, Context context)
    {
        SpannableString ss = new SpannableString(title);
        ss.setSpan(new ForegroundColorSpan(context.getResources().getColor(R.color.secondaryColor)),
                0, title.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        return ss;
    }

    public static AudioManager getAudioManager(Context context) {
        return (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
    }
}
