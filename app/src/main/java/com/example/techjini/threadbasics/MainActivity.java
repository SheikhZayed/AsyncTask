package com.example.techjini.threadbasics;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.techjini.threadbasics.adapter.SongAdapter;
import com.example.techjini.threadbasics.helper.DownloadClickListener;
import com.example.techjini.threadbasics.helper.NetStatus;
import com.example.techjini.threadbasics.helper.NetworkRequest;
import com.example.techjini.threadbasics.model.Songs;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;

public class MainActivity extends Activity implements Handler.Callback, View.OnClickListener, DownloadClickListener {

    private Handler handler;
    private NetworkRequest networkRequest;
    private final int ERROR_HTTP_CLIENT_TIMEOUT = 1;
    private final int ERROR_IO_EXCEPTION = 2;
    private final int ERROR_UNKNOWN = 3;
    private final int SUCCESS = 4;
    private ProgressDialog progressDialog;
    private ArrayList<Songs> songs;
    private RecyclerView mRvSongs;
    private RecyclerView.LayoutManager mLayoutManager;
    private SongAdapter songAdapter;
    private NetStatus networkStatus;
    private ImageView mIvRefresh;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initWidgets();
        initObjects();
        if (networkStatus.isNetworkAvailable()) {
            startServerConn();
        } else
            Toast.makeText(getApplicationContext(), "No Internet Available", Toast.LENGTH_SHORT).show();
    }

    private void initObjects() {
        networkRequest = new NetworkRequest();
        handler = new Handler(this);
        songs = new ArrayList<>();
        networkStatus = new NetStatus(this);
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait...");
        progressDialog.setMessage("While fetching data from server");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCancelable(false);
        progressDialog.setIndeterminate(true);
    }

    private void initWidgets() {
        mLayoutManager = new LinearLayoutManager(this);
        mRvSongs = (RecyclerView) findViewById(R.id.xRvSongs);
        mRvSongs.setLayoutManager(mLayoutManager);
        mRvSongs.setHasFixedSize(true);
        mIvRefresh = (ImageView) findViewById(R.id.xIvRefresh);
        mIvRefresh.setOnClickListener(this);
    }

    private void startServerConn() {
        progressDialog.show();
        Thread myThread = new Thread(new Runnable() {
            @Override
            public void run() {
                String response = networkRequest.callServer();
                switch (response) {
                    case NetworkRequest.ERROR_HTTP_CLIENT_TIMEOUT:
                        handler.sendEmptyMessage(ERROR_HTTP_CLIENT_TIMEOUT);
                        break;
                    case NetworkRequest.ERROR_IO_EXCEPTION:
                        handler.sendEmptyMessage(ERROR_IO_EXCEPTION);
                        break;
                    case NetworkRequest.ERROR_UNKNOWN:
                        handler.sendEmptyMessage(ERROR_UNKNOWN);
                        break;
                    default:
                        try {
                            JSONArray songsArray = new JSONArray(response);
                            songs.removeAll(songs);
                            for (int i = 0; i < songsArray.length(); i++) {
                                Songs songs1 = new Songs();
                                songs1.setId(songsArray.getJSONObject(i).getInt("id"));
                                songs1.setArtist(songsArray.getJSONObject(i).getString("artist"));
                                songs1.setName(songsArray.getJSONObject(i).getString("name"));
                                songs1.setUrl(songsArray.getJSONObject(i).getString("downloadUrl"));
                                songs1.setImage(songsArray.getJSONObject(i).getString("image"));
                                songs.add(songs1);
                            }
                            handler.sendEmptyMessage(SUCCESS);
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
        progressDialog.dismiss();
        switch (msg.what) {
            case ERROR_HTTP_CLIENT_TIMEOUT:
                Toast.makeText(getApplicationContext(), "time out occurred", Toast.LENGTH_SHORT).show();
                break;
            case ERROR_IO_EXCEPTION:
                Toast.makeText(getApplicationContext(), "io exception occurred", Toast.LENGTH_SHORT).show();
                break;
            case ERROR_UNKNOWN:
                Toast.makeText(getApplicationContext(), "unknown error", Toast.LENGTH_SHORT).show();
                break;
            default:
                if (null == songAdapter) {
                    songAdapter = new SongAdapter(songs, this, this);
                    mRvSongs.setAdapter(songAdapter);
                    break;
                } else
                    songAdapter.notifyDataSetChanged();
        }

        return false;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.xIvRefresh: {
                if (networkStatus.isNetworkAvailable()) {
                    startServerConn();
                } else {
                    Toast.makeText(this, "There is no Internet available, please check", Toast.LENGTH_LONG).show();
                }
                break;
            }
            default: {
                break;
            }
        }
    }


    @Override
    public void onDownloadClicked(int songId, String downloadUrl) {
        System.out.println("Song Id : " + songId + " : Download URL : " + downloadUrl);
    }
}