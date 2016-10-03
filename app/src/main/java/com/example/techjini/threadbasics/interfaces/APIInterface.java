package com.example.techjini.threadbasics.interfaces;

import com.example.techjini.threadbasics.model.SongsModel;

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Streaming;
import retrofit2.http.Url;

/**
 * Created by techjini on 21/9/16.
 */
public interface APIInterface {
    @GET("assignment.json")
    Call<List<SongsModel>> getSongsList();

    @GET
    @Streaming
    Call<ResponseBody> downloadFile(@Url String url);
}
