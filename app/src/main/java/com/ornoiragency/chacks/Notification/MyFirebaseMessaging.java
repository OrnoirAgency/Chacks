package com.ornoiragency.chacks.Notification;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.ornoiragency.chacks.Activity.ChatActivity;
import com.ornoiragency.chacks.Post.PostAudioDetailActivity;
import com.ornoiragency.chacks.Post.PostDetailActivity;
import com.ornoiragency.chacks.Post.PostTextDetailActivity;
import com.ornoiragency.chacks.Post.PostVideoDetailActivity;
import com.ornoiragency.chacks.R;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Random;

public class MyFirebaseMessaging extends FirebaseMessagingService {

    private static final String ADMIN_CHANNEL_ID = "admin_channel";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        SharedPreferences sp = getSharedPreferences("SP_USER", MODE_PRIVATE);
        String savedCurrentUser = sp.getString("Current_USERID", "None");

        String sent = remoteMessage.getData().get("sented");
        String user = remoteMessage.getData().get("user");
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        String notificationType = remoteMessage.getData().get("notificationType");
        if (notificationType.equals("PostNotification")){

            //post notification
            String sender = remoteMessage.getData().get("sender");
            String pId = remoteMessage.getData().get("pId");
            String pTitle = remoteMessage.getData().get("pTitle");
            String pDescription = remoteMessage.getData().get("pDescription");
            String user_image = remoteMessage.getData().get("user_image");
            String fromType = remoteMessage.getData().get("type");


            if (!sender.equals(savedCurrentUser)) {

                showPostNotification(""+pId,""+pTitle,""+pDescription,""+fromType,user_image);

            }

        } else if (notificationType.equals("ChatNotification")){
            //chat notification
            String user_id = remoteMessage.getData().get("user");
            String title = remoteMessage.getData().get("title");
            String body = remoteMessage.getData().get("body");
            String user_image = remoteMessage.getData().get("image");

            if (firebaseUser != null && sent.equals(firebaseUser.getUid())){
                if (!savedCurrentUser.equals(user)) {
                   sendNotification(user_id,title,body,user_image);
                }
            }
        } else if (notificationType.equals("CommentNotification")){

            //Comment Notifications
            String body = remoteMessage.getData().get("body");
            String title = remoteMessage.getData().get("title");
            String user_image = remoteMessage.getData().get("image");
            String pId = remoteMessage.getData().get("pId");
            String fromType = remoteMessage.getData().get("type");

            if (firebaseUser != null && sent.equals(firebaseUser.getUid())){
                if (!savedCurrentUser.equals(user)) {

                    showCommentNotification(""+pId,""+fromType,title,body,user_image);
                }
            }


        }


    }

    private void sendNotification(String user_id,String title, String body, String user_image) {

        NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);

        int notificationId = new Random().nextInt(3000);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            setupNotificationChannel(notificationManager);
        }


        Intent intent = new Intent(this, ChatActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("user_id", user_id);
        intent.putExtras(bundle);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);

        Bitmap  bitmap = getBitmapfromUrl(user_image);


        Uri notificationSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this,""+ ADMIN_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_logo_chacks_bleue)
                .setLargeIcon(bitmap)
                .setLights(Color.BLUE, 500, 500)
                .setColor(getResources().getColor(R.color.icon_checked))
                .setShowWhen(true)
                .setAutoCancel(true)
                .setContentTitle(title)
                .setContentText(body)
                .setSound(notificationSoundUri)
                .setContentIntent(pendingIntent);

        notificationManager.notify(notificationId,notificationBuilder.build());
    }

    private void showCommentNotification(String pId, String fromType,String title,String body, String user_image) {

        NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);

        int notificationId = new Random().nextInt(3000);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            setupNotificationChannel(notificationManager);
        }

        PendingIntent pendingIntent = null;

        if (fromType.equals("image")){
            Intent intent = new Intent(this, PostDetailActivity.class);
            intent.putExtra("postId",pId);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            pendingIntent = PendingIntent.getActivity(this,0,intent,PendingIntent.FLAG_ONE_SHOT);

        }

        if (fromType.equals("text")){
            Intent intent = new Intent(this, PostTextDetailActivity.class);
            intent.putExtra("postId",pId);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            pendingIntent = PendingIntent.getActivity(this,0,intent,PendingIntent.FLAG_ONE_SHOT);

        }

        if (fromType.equals("audio")){
            Intent intent = new Intent(this, PostAudioDetailActivity.class);
            intent.putExtra("postId",pId);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            pendingIntent = PendingIntent.getActivity(this,0,intent,PendingIntent.FLAG_ONE_SHOT);
        }


        if (fromType.equals("video")){
            Intent intent = new Intent(this, PostVideoDetailActivity.class);
            intent.putExtra("postId",pId);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            pendingIntent = PendingIntent.getActivity(this,0,intent,PendingIntent.FLAG_ONE_SHOT);
        }

        //Large icon
        // Bitmap largeIcon = BitmapFactory.decodeResource(getResources(),R.drawable.ic_notif);
        Bitmap  bitmap = getBitmapfromUrl(user_image);


        Uri notificationSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this,""+ ADMIN_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_logo_chacks_bleue)
                .setLargeIcon(bitmap)
                .setLights(Color.BLUE, 500, 500)
                .setColor(getResources().getColor(R.color.icon_checked))
                .setShowWhen(true)
                .setAutoCancel(true)
                .setContentTitle(title)
                .setContentText(body)
                .setSound(notificationSoundUri)
                .setContentIntent(pendingIntent);

        notificationManager.notify(notificationId,notificationBuilder.build());
    }

    private void showPostNotification(String pId, String pTitle, String pDescription,String fromType,String user_image) {

        NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);

        int notificationId = new Random().nextInt(3000);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
          setupNotificationChannel(notificationManager);
        }

        PendingIntent pendingIntent = null;

        if (fromType.equals("image")){
            Intent intent = new Intent(this, PostDetailActivity.class);
            intent.putExtra("postId",pId);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            pendingIntent = PendingIntent.getActivity(this,0,intent,PendingIntent.FLAG_ONE_SHOT);

        }

        if (fromType.equals("text")){
            Intent intent = new Intent(this, PostTextDetailActivity.class);
            intent.putExtra("postId",pId);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
             pendingIntent = PendingIntent.getActivity(this,0,intent,PendingIntent.FLAG_ONE_SHOT);

        }

        if (fromType.equals("audio")){
            Intent intent = new Intent(this, PostAudioDetailActivity.class);
            intent.putExtra("postId",pId);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
             pendingIntent = PendingIntent.getActivity(this,0,intent,PendingIntent.FLAG_ONE_SHOT);
        }


        if (fromType.equals("video")){
            Intent intent = new Intent(this, PostVideoDetailActivity.class);
            intent.putExtra("postId",pId);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            pendingIntent = PendingIntent.getActivity(this,0,intent,PendingIntent.FLAG_ONE_SHOT);
        }

        //Large icon
      // Bitmap largeIcon = BitmapFactory.decodeResource(getResources(),R.drawable.ic_notif);
        Bitmap  bitmap = getBitmapfromUrl(user_image);


        Uri notificationSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this,""+ ADMIN_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_logo_chacks_bleue)
                .setLargeIcon(bitmap)
                .setLights(Color.BLUE, 500, 500)
                .setColor(getResources().getColor(R.color.icon_checked))
                .setShowWhen(true)
                .setAutoCancel(true)
                .setContentTitle(pTitle)
                .setContentText(pDescription)
                .setSound(notificationSoundUri)
                .setContentIntent(pendingIntent);

        notificationManager.notify(notificationId,notificationBuilder.build());

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void setupNotificationChannel(NotificationManager notificationManager) {

        CharSequence channelName = "New notification";
        String channelDescription = "Device to device post notification";

        NotificationChannel adminChannel = new NotificationChannel(ADMIN_CHANNEL_ID,channelName,NotificationManager.IMPORTANCE_HIGH);
        adminChannel.setDescription(channelDescription);
        adminChannel.enableLights(true);
        adminChannel.setLightColor(Color.BLUE);
        adminChannel.enableVibration(true);
        if (notificationManager != null){
            notificationManager.createNotificationChannel(adminChannel);
        }
    }

    public Bitmap getBitmapfromUrl(String user_image) {
        try {
            URL url = new URL(user_image);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            Bitmap bitmap = BitmapFactory.decodeStream(input);
            return bitmap;

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;

        }
    }



}

