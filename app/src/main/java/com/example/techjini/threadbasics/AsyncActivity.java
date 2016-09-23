package com.example.techjini.threadbasics;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.Toast;

import com.example.techjini.threadbasics.adapter.SongAdapter;
import com.example.techjini.threadbasics.helper.DownloadClickListener;
import com.example.techjini.threadbasics.helper.NetStatus;
import com.example.techjini.threadbasics.helper.NetworkRequest;
import com.example.techjini.threadbasics.helper.StaticUtils;
import com.example.techjini.threadbasics.model.Songs;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;

public class AsyncActivity extends Activity {

    private NetworkRequest networkRequest;
    private NetStatus netStatus;
    private ArrayList<Songs> songsArrayList;
    private ProgressDialog progressDialog;
    private RecyclerView mRvSongs;
    private RecyclerView.LayoutManager mLayoutManager;
    private SongAdapter songAdapter;
    private final int ERROR_HTTP_CLIENT_TIMEOUT = 1;
    private final int ERROR_IO_EXCEPTION = 2;
    private final int ERROR_UNKNOWN = 3;
    private final int SUCCESS = 4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_async);
        initWidgets();
        initObjects();
        if (netStatus.isNetworkAvailable())
        new DownloadSongsList().execute(StaticUtils.API_URL);
        else
            Toast.makeText(getApplicationContext(),"error in establishing connection",Toast.LENGTH_SHORT).show();
    }

    private void initWidgets() {
        mLayoutManager = new LinearLayoutManager(this);
        mRvSongs = (RecyclerView) findViewById(R.id.xRvSongs);
        mRvSongs.setLayoutManager(mLayoutManager);
        mRvSongs.setHasFixedSize(true);
    }
    private void initObjects() {
        netStatus = new NetStatus(this);
        networkRequest = new NetworkRequest();
        songsArrayList = new ArrayList<>();
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait...");
        progressDialog.setMessage("While fetching data from server");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCancelable(false);
        progressDialog.setIndeterminate(true);
    }
    private class DownloadSongsList extends AsyncTask<String, Integer, Integer> implements DownloadClickListener {

        @Override
        protected void onPreExecute() {
            progressDialog.show();
        }

        @Override
        protected Integer doInBackground(String... params) {
            String response = networkRequest.callServer();
            switch (response) {
                case NetworkRequest.ERROR_HTTP_CLIENT_TIMEOUT:
                    return ERROR_HTTP_CLIENT_TIMEOUT;
                case NetworkRequest.ERROR_IO_EXCEPTION:
                    return ERROR_IO_EXCEPTION;
                case NetworkRequest.ERROR_UNKNOWN:
                    return ERROR_UNKNOWN;
                default:
                    try {
                        JSONArray songsArray = new JSONArray(response);
                        songsArrayList.removeAll(songsArrayList);
                        for (int i = 0; i < songsArray.length(); i++) {
                            Songs songs1 = new Songs();
                            songs1.setId(songsArray.getJSONObject(i).getInt("id"));
                            songs1.setArtist(songsArray.getJSONObject(i).getString("artist"));
                            songs1.setName(songsArray.getJSONObject(i).getString("name"));
                            songs1.setUrl(songsArray.getJSONObject(i).getString("downloadUrl"));
                            songs1.setImage(songsArray.getJSONObject(i).getString("image"));
                            songsArrayList.add(songs1);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
            }
            return SUCCESS;
        }

        @Override
        protected void onPostExecute(Integer status) {
            super.onPostExecute(status);
            progressDialog.dismiss();

            switch (status) {
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
                    songAdapter = new SongAdapter(songsArrayList, AsyncActivity.this, this);
                    mRvSongs.setAdapter(songAdapter);
                } else
                    songAdapter.notifyDataSetChanged();

            }
        }
        @Override
        public void onDownloadClicked(int songId, String downloadUrl,String songName) {
            System.out.println("Song Id : " + songId + " : Download URL : " + downloadUrl);
        }
    }
}