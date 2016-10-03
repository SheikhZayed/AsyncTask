package com.example.techjini.threadbasics.adapter;


import android.content.Context;
import android.os.Environment;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.techjini.threadbasics.R;
import com.example.techjini.threadbasics.interfaces.DownloadClickListener;
import com.example.techjini.threadbasics.model.SongsModel;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by techjini on 19/9/16.
 */
public class SongAdapter extends RecyclerView.Adapter<SongAdapter.ViewHolder> {

    private ArrayList<SongsModel> mSongsArrayList;
    private Context mContext;
    private DownloadClickListener mDownloadClickListener;

    public SongAdapter(ArrayList<SongsModel> songsArrayList, Context mContext, DownloadClickListener downloadClickListener) {
        this.mContext = mContext;
        this.mSongsArrayList = songsArrayList;
        this.mDownloadClickListener = downloadClickListener;
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_items, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {

        holder.textArtistName.setText(mSongsArrayList.get(position).getArtist());
        holder.textSongName.setText(mSongsArrayList.get(position).getName());
         if(checkFileExists(mSongsArrayList.get(position).getName())){
            holder.imgDownloadArrow.setImageResource(R.drawable.ic_play_circle_filled_black_24dp);
        }else if (mSongsArrayList.get(position).getState() == 0) {
            holder.imgDownloadArrow.setImageResource(R.drawable.ic_download);
        }
        else
            holder.imgDownloadArrow.setImageResource(R.drawable.ic_download);
        Picasso.with(mContext).load(mSongsArrayList.get(position).getImage()).into(holder.imgThumbnail);
        holder.imgDownloadArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDownloadClickListener.onDownloadClicked(mSongsArrayList.get(position).getId(), mSongsArrayList.get(position).getUrl(), mSongsArrayList.get(position).getName(),position);
            }
        });
    }
    @Override
    public int getItemCount() {
        return mSongsArrayList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        ImageView imgThumbnail, imgDownloadArrow;
        TextView textSongName, textArtistName;

        public ViewHolder(View itemView) {
            super(itemView);
            imgThumbnail = (ImageView) itemView.findViewById(R.id.xIvSongImage);
            imgDownloadArrow = (ImageView) itemView.findViewById(R.id.xIvDownload);
            textSongName = (TextView) itemView.findViewById(R.id.xTvSongName);
            textArtistName = (TextView) itemView.findViewById(R.id.xTvSongArtist);

        }
    }
    public boolean checkFileExists(String name){
        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), name+".mp3");
        if (file.exists()){
            Log.d("info",name+ " exists");
            return true;
        }
        else
            return false;
    }
}
