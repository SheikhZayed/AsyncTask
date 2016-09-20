package com.example.techjini.threadbasics.adapter;


import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.techjini.threadbasics.R;
import com.example.techjini.threadbasics.helper.DownloadClickListener;
import com.example.techjini.threadbasics.model.Songs;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by techjini on 19/9/16.
 */
public class SongAdapter extends RecyclerView.Adapter<SongAdapter.ViewHolder> {

    private ArrayList<Songs> songsArrayList;
    private Context mContext;
    private DownloadClickListener downloadClickListener;

    public SongAdapter(ArrayList<Songs> songsArrayList, Context mContext, DownloadClickListener downloadClickListener) {
        this.mContext = mContext;
        this.songsArrayList = songsArrayList;
        this.downloadClickListener = downloadClickListener;
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_items, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        holder.textArtistName.setText(songsArrayList.get(position).getArtist());
        holder.textSongName.setText(songsArrayList.get(position).getName());
        Picasso.with(mContext).load(songsArrayList.get(position).getImage()).into(holder.imgThumbnail);
        holder.imgDownloadArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                downloadClickListener.onDownloadClicked(songsArrayList.get(position).getId(), songsArrayList.get(position).getUrl());
            }
        });
    }

    @Override
    public int getItemCount() {
        return songsArrayList.size();
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
}
