package com.example.techjini.threadbasics.service;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Environment;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import com.example.techjini.threadbasics.MainActivity;
import com.example.techjini.threadbasics.R;
import com.example.techjini.threadbasics.adapter.SongAdapter;
import com.example.techjini.threadbasics.helper.APIInterface;
import com.example.techjini.threadbasics.model.Download;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Retrofit;

public class DownloadService extends IntentService {

    private NotificationCompat.Builder mNotificationBuilder;
    private NotificationManager mNotificationManager;
    private int mTotalFileSize;
    private String mUrl,mSongName;
    private String mSongUri;
    private Call<ResponseBody> call;

    public DownloadService() {
        super("DownloadService");
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        mUrl = (String) intent.getExtras().get("URL");
        mSongName = (String) intent.getExtras().get("NAME");

        Intent notifIntent = new Intent(Intent.ACTION_VIEW);
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        String type = mime.getMimeTypeFromExtension("mp3");
        notifIntent.setDataAndType(Uri.fromFile(new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), mSongName+".mp3")), type);
        notifIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        PendingIntent pIntent = PendingIntent.getActivity(this, 7, notifIntent, 0);
        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_download)
                .setContentTitle("Downloading")
                .setContentText("Downloading " + mSongName +"...")
                .setAutoCancel(true)
                .addAction(R.mipmap.ic_play, "Play", pIntent);
        mNotificationManager.notify(0, mNotificationBuilder.build());

        initDownload();
    }

    private void initDownload() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://2015.downloadming1.com")
                .build();
        APIInterface apiInterface = retrofit.create(APIInterface.class);
        call = apiInterface.downloadFile(mUrl);
        try {
            downloadFile(call.execute().body());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void downloadFile(ResponseBody body) throws IOException {
        int count;
        byte data[] = new byte[1024 * 4];
        long fileSize = body.contentLength();
        InputStream bis = new BufferedInputStream(body.byteStream(), 1024 * 8);
        File outputFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), mSongName+".mp3");
        mSongUri = outputFile.getAbsolutePath().toString();
        OutputStream output = new FileOutputStream(outputFile);
        long total = 0;
        long startTime = System.currentTimeMillis();
        int timeCount = 1;
        while ((count = bis.read(data)) != -1) {

            total += count;
            mTotalFileSize = (int) (fileSize / (Math.pow(1024, 2)));
            double current = Math.round(total / (Math.pow(1024, 2)));

            int progress = (int) ((total * 100) / fileSize);

            long currentTime = System.currentTimeMillis() - startTime;

            Download download = new Download();
            download.setTotalFileSize(mTotalFileSize);

            if (currentTime > 1000 * timeCount) {
                download.setCurrentFileSize((int) current);
                download.setProgress(progress);
                sendNotification(download);
                timeCount++;
            }
            output.write(data, 0, count);
        }
        onDownloadComplete();
        output.flush();
        output.close();
        bis.close();
    }

    private void sendNotification(Download download) {
        sendIntent(download);
        mNotificationBuilder.setProgress(100, download.getProgress(), false);
        mNotificationBuilder.setContentText("Downloading " + mSongName  + download.getCurrentFileSize() + "/" + mTotalFileSize + " MB");
        mNotificationManager.notify(0, mNotificationBuilder.build());
    }

    private void onDownloadComplete() {
        Download download = new Download();
        download.setProgress(100);
        sendIntent(download);

        mNotificationManager.cancel(0);
        mNotificationBuilder.setProgress(0, 0, false);
        mNotificationBuilder.setContentText(mSongName + " Downloaded");
        mNotificationManager.notify(0, mNotificationBuilder.build());
    }

    private void sendIntent(Download download) {
        Intent intent = new Intent(MainActivity.MESSAGE_PROGRESS);
        intent.putExtra("download", download);
        intent.putExtra("uri",mSongUri);
        intent.putExtra("url",mUrl);
        LocalBroadcastManager.getInstance(DownloadService.this).sendBroadcast(intent);
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        mNotificationManager.cancel(0);
    }
}
