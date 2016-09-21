package com.example.techjini.threadbasics;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.techjini.threadbasics.adapter.SongAdapter;
import com.example.techjini.threadbasics.helper.DownloadClickListener;
import com.example.techjini.threadbasics.helper.NetworkRequest;
import com.example.techjini.threadbasics.helper.StaticUtils;
import com.example.techjini.threadbasics.model.Songs;

import org.json.JSONArray;
import org.json.JSONException;

import java.net.URL;
import java.util.ArrayList;

public class AsyncActivity extends Activity{

    private NetworkRequest networkRequest;
    private ArrayList<Songs> songsArrayList;
    private ProgressDialog progressDialog;
    private RecyclerView mRvSongs;
    private RecyclerView.LayoutManager mLayoutManager;
    private SongAdapter songAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_async);
        initWidgets();
        initObjects();
        new DownloadSongsList().execute(StaticUtils.API_URL);
    }

    private void initWidgets() {
        mLayoutManager = new LinearLayoutManager(this);
        mRvSongs = (RecyclerView) findViewById(R.id.xRvSongs);
        mRvSongs.setLayoutManager(mLayoutManager);
        mRvSongs.setHasFixedSize(true);
    }

    private void initObjects() {
        networkRequest = new NetworkRequest();
        songsArrayList= new ArrayList<>();
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait...");
        progressDialog.setMessage("While fetching data from server");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCancelable(false);
        progressDialog.setIndeterminate(true);
    }


    private class DownloadSongsList extends AsyncTask<String,Integer,ArrayList<Songs>> implements DownloadClickListener {

        @Override
        protected void onPreExecute() {
            progressDialog.show();
        }

        @Override
        protected ArrayList<Songs> doInBackground(String... params) {
            String response = networkRequest.callServer();
            switch (response) {
                case NetworkRequest.ERROR_HTTP_CLIENT_TIMEOUT:
                    displayToast("Connection Time out");
                    break;
                case NetworkRequest.ERROR_IO_EXCEPTION:
                    displayToast("Error in IO Connection");
                    break;
                case NetworkRequest.ERROR_UNKNOWN:
                    displayToast("Unknown Error");
                    break;
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
            return songsArrayList;
        }

        @Override
        protected void onPostExecute(ArrayList<Songs> songsArrayList) {
            super.onPostExecute(songsArrayList);
            progressDialog.dismiss();
            if (null == songAdapter) {
                songAdapter = new SongAdapter(songsArrayList,AsyncActivity.this,this);
                mRvSongs.setAdapter(songAdapter);
            } else
                songAdapter.notifyDataSetChanged();

        }

        @Override
        public void onDownloadClicked(int songId, String downloadUrl) {
            System.out.println("Song Id : " + songId + " : Download URL : " + downloadUrl);
        }
    }

    private void displayToast(String msg) {
        Toast.makeText(getApplicationContext(),msg,Toast.LENGTH_SHORT).show();
    }
}