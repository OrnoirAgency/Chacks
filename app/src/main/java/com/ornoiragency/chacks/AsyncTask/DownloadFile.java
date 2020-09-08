package com.ornoiragency.chacks.AsyncTask;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DownloadFile extends AsyncTask<String, Integer, String> {

    private String mediaUrl;
    String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
    String tempFilterAudioName = timeStamp + ".mp3";
    private String type;
    static String directory =  "/" + "ChaCks" + "/"
            + "audio/";

    @SuppressLint("StaticFieldLeak")
    private Context context;

    public DownloadFile(Context context, String type, String mediaUrl){
        this.type=type;
        this.context=context;
        this.mediaUrl=mediaUrl;

    }

    @Override
    protected String doInBackground(String... strings) {
        InputStream inputStream=null;
        OutputStream outputStream=null;
        HttpURLConnection connection=null;
        File file;
        try{
            URL url=new URL(mediaUrl);
            connection= (HttpURLConnection) url.openConnection();
            connection.connect();

            if (connection.getResponseCode()!= HttpURLConnection.HTTP_OK){
                return "server "+connection.getResponseCode() +" "+connection.getResponseMessage();
            }

            int fileLength=connection.getContentLength();

            inputStream=connection.getInputStream();


            File externalStorage = Environment.getExternalStorageDirectory();
           // file= new File(FileUtils.getPath(context,type)+ tempFilterAudioName);
            externalStorage.setReadable(true,false);
             file = new File(externalStorage.getAbsolutePath() + directory + tempFilterAudioName);
            outputStream= new FileOutputStream(file);


            byte []data=new byte[4096];

            long total=0;

            int count=0;

            while ((count = inputStream.read(data))!=-1){

                if (isCancelled()) {
                    inputStream.close();
                    return null;

                }
                total += count;

                if (fileLength > 0) {
                    publishProgress((int) (total * 100 / fileLength));
                    outputStream.write(data, 0, count);
                }
            }

        }catch (Exception e){
          e.printStackTrace();
        }
        finally {
            try {
                if (outputStream != null) {
                    outputStream.close();
                }
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException io) {

                io.printStackTrace();
            }

            if (connection != null) {
                connection.disconnect();
            }
        }
        return null;
    }




}
