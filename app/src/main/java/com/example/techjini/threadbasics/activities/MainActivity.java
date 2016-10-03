package com.example.techjini.threadbasics.activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.databinding.DataBindingUtil;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.techjini.threadbasics.R;
import com.example.techjini.threadbasics.adapter.MusicAdapter;
import com.example.techjini.threadbasics.constants.AppConstants;
import com.example.techjini.threadbasics.databinding.ActivityMainBinding;
import com.example.techjini.threadbasics.helper.NetworkStatus;
import com.example.techjini.threadbasics.helper.NetworkRequest;
import com.example.techjini.threadbasics.helper.RecyclerItemClickListener;
import com.example.techjini.threadbasics.interfaces.DownloadClickListener;
import com.example.techjini.threadbasics.model.DownloadModel;
import com.example.techjini.threadbasics.model.SongsModel;
import com.example.techjini.threadbasics.service.DownloadService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends Activity implements Handler.Callback, View.OnClickListener, DownloadClickListener{

    private Handler mHandler;
    private NetworkRequest mNetworkRequest;
    private ProgressDialog mProgressDialog;
    private ArrayList<SongsModel> mSongs;
    private RecyclerView.LayoutManager mLayoutManager;
    private MusicAdapter mSongAdapter;
    private NetworkStatus mNetworkStatus;
    private ActivityMainBinding mBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
        mBinding = DataBindingUtil.setContentView(this,R.layout.activity_main);
        initWidgets();
        initObjects();
        registerReceiver();
        if (mNetworkStatus.isNetworkAvailable()) {
            startServerConn();
        } else
            Toast.makeText(getApplicationContext(), R.string.error_no_internet, Toast.LENGTH_SHORT).show();
    }

    private void registerReceiver() {
        LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(this);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(AppConstants.MESSAGE_PROGRESS);
        broadcastManager.registerReceiver(broadcastReceiver,intentFilter);
    }

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(AppConstants.MESSAGE_PROGRESS)){
                DownloadModel download = intent.getParcelableExtra("download");
                String songName = intent.getStringExtra("name");
                if (download.getProgress() == 100){
                    for(SongsModel songList : mSongs)
                    {
                        if (songName.equals(songList.getName())) {
                            //setting state as downloaded
                            HashMap<String, Boolean> statusMap = new HashMap<>();
                            statusMap.put(songName, true);
                            updateMap(statusMap);
                            songList.setState(1);
                            mSongAdapter.notifyDataSetChanged();
                        }
                    }
                    File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), songName+".mp3");
                    String songUri = file.getAbsolutePath();
                    Log.d("info","playing song");
                    Toast.makeText(getApplicationContext(),getString(R.string.info_playing_song) + songUri,Toast.LENGTH_SHORT).show();
                    MediaPlayer mediaPlayer = MediaPlayer.create(MainActivity.this,Uri.parse(songUri));
                    if (mediaPlayer.isPlaying()){
                        mediaPlayer.stop();
                    }
                    else
                        mediaPlayer.start();
                }
            }
        }
    };

    private void updateMap(Map<String, Boolean> statusMap) {
        SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("MyPref", Context.MODE_PRIVATE);
        if (sharedPreferences != null) {
            JSONObject jsonObject = new JSONObject(statusMap);
            String jsonString = jsonObject.toString();
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("map_contents", jsonString);
            editor.commit();
        }
    }

    private void initObjects() {
        mNetworkRequest = new NetworkRequest();
        mHandler = new Handler(this);
        mSongs = new ArrayList<>();
        mNetworkStatus = new NetworkStatus(this);
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setTitle(getString(R.string.progress_title));
        mProgressDialog.setMessage(getString(R.string.progress_content));
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgressDialog.setCancelable(false);
        mProgressDialog.setIndeterminate(true);

        mBinding.xRvSongs.addOnItemTouchListener(new RecyclerItemClickListener(this, mBinding.xRvSongs, new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Intent intent = new Intent(MainActivity.this,SongDetailsActivity.class);
                intent.putExtra("name", mSongs.get(position).getName());
                intent.putExtra("id", mSongs.get(position).getId());
                intent.putExtra("artist", mSongs.get(position).getArtist());
                startActivity(intent);
                overridePendingTransition(R.anim.slide_up,R.anim.slide_down);
            }

            @Override
            public void onItemLongClick(View view, int position) {

            }
        }));
    }

    private void initWidgets() {
        mLayoutManager = new LinearLayoutManager(this);
        mBinding.xRvSongs.setLayoutManager(mLayoutManager);
        mBinding.xRvSongs.setHasFixedSize(true);
        mBinding.xIvRefresh.setOnClickListener(this);
        mBinding.xIvAsync.setOnClickListener(this);
    }

    public void startServerConn() {
        mProgressDialog.show();
        Thread myThread = new Thread(new Runnable() {
            @Override
            public void run() {
                String response = mNetworkRequest.callServer();
                switch (response) {
                    case NetworkRequest.ERROR_HTTP_CLIENT_TIMEOUT:
                        mHandler.sendEmptyMessage(AppConstants.ERROR_HTTP_CLIENT_TIMEOUT);
                        break;
                    case NetworkRequest.ERROR_IO_EXCEPTION:
                        mHandler.sendEmptyMessage(AppConstants.ERROR_IO_EXCEPTION);
                        break;
                    case NetworkRequest.ERROR_UNKNOWN:
                        mHandler.sendEmptyMessage(AppConstants.ERROR_UNKNOWN);
                        break;
                    default:
                        try {
                            JSONArray songsArray = new JSONArray(response);
                            mSongs.removeAll(mSongs);
                            for (int i = 0; i < songsArray.length(); i++) {
                                SongsModel songs1 = new SongsModel();
                                songs1.setId(songsArray.getJSONObject(i).getInt("id"));
                                songs1.setArtist(songsArray.getJSONObject(i).getString("artist"));
                                songs1.setName(songsArray.getJSONObject(i).getString("name"));
                                songs1.setUrl(songsArray.getJSONObject(i).getString("downloadUrl"));
                                songs1.setImage(songsArray.getJSONObject(i).getString("image"));
                                mSongs.add(songs1);
                            }
                            mHandler.sendEmptyMessage(AppConstants.SUCCESS);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                }
            }
        });
        myThread.start();
    }
    @Override
    public boolean handleMessage(Message msg) {
        mProgressDialog.dismiss();
        switch (msg.what) {
            case AppConstants.ERROR_HTTP_CLIENT_TIMEOUT:
                Toast.makeText(getApplicationContext(), getString(R.string.error_timeout), Toast.LENGTH_SHORT).show();
                break;
            case AppConstants.ERROR_IO_EXCEPTION:
                Toast.makeText(getApplicationContext(), getString(R.string.error_io), Toast.LENGTH_SHORT).show();
                break;
            case AppConstants.ERROR_UNKNOWN:
                Toast.makeText(getApplicationContext(), getString(R.string.error_unknown), Toast.LENGTH_SHORT).show();
                break;
            default:
                if (null == mSongAdapter) {
                    mSongAdapter = new MusicAdapter(mSongs, this, this);
                    mBinding.xRvSongs.setAdapter(mSongAdapter);
                    break;
                } else
                    mSongAdapter.notifyDataSetChanged();
        }
        return false;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.xIvRefresh: {
                if (mNetworkStatus.isNetworkAvailable()) {
                    startServerConn();
                } else {
                    Toast.makeText(this, getString(R.string.error_no_internet), Toast.LENGTH_LONG).show();
                }
                break;
            }
            case R.id.xIvAsync:
                Intent intent = new Intent(MainActivity.this, RetrofitActivity.class);
                startActivity(intent);
                break;
        }
    }


    @Override
    public void onDownloadClicked(int songId, String downloadUrl, String songName, int position) {

        if (mSongAdapter.checkFileExists(mSongs.get(position).getName())) {
            File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), songName+".mp3");
            String songUri = file.getAbsolutePath();
            MediaPlayer mediaPlayer = MediaPlayer.create(MainActivity.this, Uri.parse(songUri));
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            } else
                mediaPlayer.start();
        } else {
            Intent serviceIntent = new Intent(this, DownloadService.class);
            serviceIntent.putExtra("URL", downloadUrl);
            serviceIntent.putExtra("NAME", songName);
            this.startService(serviceIntent);
        }
    }


}
