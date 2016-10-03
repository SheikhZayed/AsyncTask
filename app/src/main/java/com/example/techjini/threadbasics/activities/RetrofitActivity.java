package com.example.techjini.threadbasics.activities;

import android.app.Activity;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.Toast;

import com.example.techjini.threadbasics.R;
import com.example.techjini.threadbasics.adapter.MusicAdapter;
import com.example.techjini.threadbasics.constants.AppConstants;
import com.example.techjini.threadbasics.databinding.ActivityRetrofitBinding;
import com.example.techjini.threadbasics.helper.NetworkStatus;
import com.example.techjini.threadbasics.interfaces.APIInterface;
import com.example.techjini.threadbasics.interfaces.DownloadClickListener;
import com.example.techjini.threadbasics.model.SongsModel;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

public class RetrofitActivity extends Activity implements DownloadClickListener {

    private NetworkStatus mNetStatus;
    private ArrayList<SongsModel> mArraylist;
    private RecyclerView.LayoutManager mLayoutManager;
    private ActivityRetrofitBinding mBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this,R.layout.activity_retrofit);

        mNetStatus = new NetworkStatus(this);
        mArraylist = new ArrayList<>();
        mLayoutManager = new LinearLayoutManager(this);
        mBinding.xRvSongs.setLayoutManager(mLayoutManager);
        if (mNetStatus.isNetworkAvailable()){
            makeAPICall();
        }
        else
        Toast.makeText(getApplicationContext(),getString(R.string.error_no_internet),Toast.LENGTH_SHORT).show();
    }

    private void makeAPICall() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(AppConstants.TJ_URL)
                .addConverterFactory(JacksonConverterFactory.create())
                .build();

        APIInterface apiInterface = retrofit.create(APIInterface.class);
        Call<List<SongsModel>> call = apiInterface.getSongsList();
        call.enqueue(new Callback<List<SongsModel>>() {
            @Override
            public void onResponse(Call<List<SongsModel>> call, Response<List<SongsModel>> response) {
                if (response.isSuccessful()) {
                    mArraylist.addAll(response.body());
                    mBinding.xRvSongs.setAdapter(new MusicAdapter(mArraylist, RetrofitActivity.this, RetrofitActivity.this));
                }
            }

            @Override
            public void onFailure(Call<List<SongsModel>> call, Throwable t) {
                Log.d("error", t.getMessage());
            }
        });
    }

    @Override
    public void onDownloadClicked(int songId, String downloadUrl, String songName, int position) {
        System.out.println(getString(R.string.log_songID) + songId + getString(R.string.log_downloadurl) + downloadUrl);
    }
}
