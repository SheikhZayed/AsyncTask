package com.example.techjini.threadbasics.activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.databinding.DataBindingUtil;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.Toast;

import com.example.techjini.threadbasics.R;
import com.example.techjini.threadbasics.adapter.MusicAdapter;
import com.example.techjini.threadbasics.constants.AppConstants;
import com.example.techjini.threadbasics.databinding.ActivityAsyncBinding;
import com.example.techjini.threadbasics.helper.NetworkStatus;
import com.example.techjini.threadbasics.helper.NetworkRequest;
import com.example.techjini.threadbasics.interfaces.DownloadClickListener;
import com.example.techjini.threadbasics.model.SongsModel;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;

public class AsyncActivity extends Activity {

    private NetworkRequest mNetworkRequest;
    private NetworkStatus mNetStatus;
    private ArrayList<SongsModel> mSongsArrayList;
    private ProgressDialog mProgressDialog;
    private RecyclerView.LayoutManager mLayoutManager;
    private MusicAdapter mSongAdapter;
    private ActivityAsyncBinding mBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this,R.layout.activity_async);
        initWidgets();
        initObjects();
        if (mNetStatus.isNetworkAvailable())
        new DownloadSongsList().execute(AppConstants.API_URL);
        else
            Toast.makeText(getApplicationContext(), R.string.error_no_internet,Toast.LENGTH_SHORT).show();
    }

    private void initWidgets() {
        mLayoutManager = new LinearLayoutManager(this);
        mBinding.xRvSongs.setLayoutManager(mLayoutManager);
        mBinding.xRvSongs.setHasFixedSize(true);
    }
    private void initObjects() {
        mNetStatus = new NetworkStatus(this);
        mNetworkRequest = new NetworkRequest();
        mSongsArrayList = new ArrayList<>();
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setTitle(getString(R.string.progress_title));
        mProgressDialog.setMessage(getString(R.string.progress_content));
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgressDialog.setCancelable(false);
        mProgressDialog.setIndeterminate(true);
    }
    private class DownloadSongsList extends AsyncTask<String, Integer, Integer> implements DownloadClickListener {

        @Override
        protected void onPreExecute() {
            mProgressDialog.show();
        }

        @Override
        protected Integer doInBackground(String... params) {
            String response = mNetworkRequest.callServer();
            switch (response) {
                case NetworkRequest.ERROR_HTTP_CLIENT_TIMEOUT:
                    return AppConstants.ERROR_HTTP_CLIENT_TIMEOUT;
                case NetworkRequest.ERROR_IO_EXCEPTION:
                    return AppConstants.ERROR_IO_EXCEPTION;
                case NetworkRequest.ERROR_UNKNOWN:
                    return AppConstants.ERROR_UNKNOWN;
                default:
                    try {
                        JSONArray songsArray = new JSONArray(response);
                        mSongsArrayList.removeAll(mSongsArrayList);
                        for (int i = 0; i < songsArray.length(); i++) {
                            SongsModel songs1 = new SongsModel();
                            songs1.setId(songsArray.getJSONObject(i).getInt("id"));
                            songs1.setArtist(songsArray.getJSONObject(i).getString("artist"));
                            songs1.setName(songsArray.getJSONObject(i).getString("name"));
                            songs1.setUrl(songsArray.getJSONObject(i).getString("downloadUrl"));
                            songs1.setImage(songsArray.getJSONObject(i).getString("image"));
                            mSongsArrayList.add(songs1);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
            }
            return AppConstants.SUCCESS;
        }

        @Override
        protected void onPostExecute(Integer status) {
            super.onPostExecute(status);
            mProgressDialog.dismiss();

            switch (status) {
                case AppConstants.ERROR_HTTP_CLIENT_TIMEOUT:
                    Toast.makeText(getApplicationContext(), R.string.error_timeout, Toast.LENGTH_SHORT).show();
                    break;
                case AppConstants.ERROR_IO_EXCEPTION:
                    Toast.makeText(getApplicationContext(), R.string.error_io, Toast.LENGTH_SHORT).show();
                    break;
                case AppConstants.ERROR_UNKNOWN:
                    Toast.makeText(getApplicationContext(), R.string.error_unknown, Toast.LENGTH_SHORT).show();
                    break;
                default:
                if (null == mSongAdapter) {
                    mSongAdapter = new MusicAdapter(mSongsArrayList, AsyncActivity.this, this);
                    mBinding.xRvSongs.setAdapter(mSongAdapter);
                } else
                    mSongAdapter.notifyDataSetChanged();

            }
        }
        @Override
        public void onDownloadClicked(int songId, String downloadUrl,String songName,int position) {
            System.out.println(getString(R.string.log_songID) + songId + getString(R.string.log_downloadurl) + downloadUrl);
        }
    }
}