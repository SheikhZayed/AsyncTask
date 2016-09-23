package com.example.techjini.threadbasics;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.Toast;

import com.example.techjini.threadbasics.adapter.SongAdapter;
import com.example.techjini.threadbasics.helper.APIInterface;
import com.example.techjini.threadbasics.helper.DownloadClickListener;
import com.example.techjini.threadbasics.helper.NetStatus;
import com.example.techjini.threadbasics.model.Songs;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

public class RetrofitActivity extends Activity implements DownloadClickListener {

    private NetStatus netStatus;
    private ArrayList<Songs> myArraylist;
    private RecyclerView mRvSongs;
    private RecyclerView.LayoutManager mLayoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_retrofit);

        netStatus = new NetStatus(this);
        myArraylist = new ArrayList<>();
        mRvSongs = (RecyclerView) findViewById(R.id.xRvSongs);
        mLayoutManager = new LinearLayoutManager(this);
        mRvSongs.setLayoutManager(mLayoutManager);
        if (netStatus.isNetworkAvailable()){
            makeAPICall();
        }
        else
        Toast.makeText(getApplicationContext(),"No Internet Available,Please verify",Toast.LENGTH_SHORT).show();
    }

    private void makeAPICall() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://tools.techjini.com/")
                .addConverterFactory(JacksonConverterFactory.create())
                .build();

        APIInterface apiInterface = retrofit.create(APIInterface.class);
        Call<List<Songs>> call = apiInterface.getSongsList();
        call.enqueue(new Callback<List<Songs>>() {
            @Override
            public void onResponse(Call<List<Songs>> call, Response<List<Songs>> response) {
                if (response.isSuccessful()) {
                    myArraylist.addAll(response.body());
                    mRvSongs.setAdapter(new SongAdapter(myArraylist, RetrofitActivity.this, RetrofitActivity.this));
                }
            }

            @Override
            public void onFailure(Call<List<Songs>> call, Throwable t) {
                Log.d("error", t.getMessage());
            }
        });
    }

    @Override
    public void onDownloadClicked(int songId, String downloadUrl,String songName) {
        System.out.println("Song Id : " + songId + " : Download URL : " + downloadUrl);
    }
}
