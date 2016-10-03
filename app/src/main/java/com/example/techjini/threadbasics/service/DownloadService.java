package com.example.techjini.threadbasics.service;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.webkit.MimeTypeMap;

import com.example.techjini.threadbasics.R;
import com.example.techjini.threadbasics.constants.AppConstants;
import com.example.techjini.threadbasics.interfaces.APIInterface;
import com.example.techjini.threadbasics.model.DownloadModel;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Retrofit;

public class DownloadService extends IntentService {

    private NotificationCompat.Builder mNotificationBuilder;
    private NotificationManager mNotificationManager;
    private int mTotalFileSize;
    private String mUrl,mSongName;
    private Call<ResponseBody> mCall;

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
                .setContentTitle(getString(R.string.notif_title))
                .setContentText(getString(R.string.notif_content) + mSongName +"...")
                .setAutoCancel(true)
                .addAction(R.drawable.ic_play_circle_filled_black_24dp, getString(R.string.notif_button), pIntent);
        mNotificationManager.notify(0, mNotificationBuilder.build());

        initDownload();
    }

    private void initDownload() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(AppConstants.MUSIC_URL)
                .build();
        APIInterface apiInterface = retrofit.create(APIInterface.class);
        mCall = apiInterface.downloadFile(mUrl);
        try {
            downloadFile(mCall.execute().body());
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

            DownloadModel download = new DownloadModel();
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

    private void sendNotification(DownloadModel download) {
        sendIntent(download);
        mNotificationBuilder.setProgress(100, download.getProgress(), false);
        mNotificationBuilder.setContentText(getString(R.string.progress_downloading_title) + mSongName  + download.getCurrentFileSize() + "/" + mTotalFileSize + " MB");
        mNotificationManager.notify(0, mNotificationBuilder.build());
    }

    private void onDownloadComplete() {
        DownloadModel download = new DownloadModel();
        download.setProgress(100);
        sendIntent(download);

        mNotificationManager.cancel(0);
        mNotificationBuilder.setProgress(0, 0, false);
        mNotificationBuilder.setContentText(mSongName + " Downloaded");
        mNotificationManager.notify(0, mNotificationBuilder.build());
    }

    private void sendIntent(DownloadModel download) {
        Intent intent = new Intent(AppConstants.MESSAGE_PROGRESS);
        intent.putExtra("download", download);
        intent.putExtra("name",mSongName);
        LocalBroadcastManager.getInstance(DownloadService.this).sendBroadcast(intent);
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        mNotificationManager.cancel(0);
    }
}
