package com.example.techjini.threadbasics.adapter;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.os.Environment;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.techjini.threadbasics.R;
import com.example.techjini.threadbasics.databinding.RowItemsBinding;
import com.example.techjini.threadbasics.interfaces.DownloadClickListener;
import com.example.techjini.threadbasics.model.SongsModel;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by techjini on 28/9/16.
 */
public class MusicAdapter extends RecyclerView.Adapter{

    private ArrayList<SongsModel> mSongsArrayList;
    private Context mContext;
    private DownloadClickListener mDownloadClickListener;

    public MusicAdapter(ArrayList<SongsModel> mSongsArrayList, Context mContext, DownloadClickListener mDownloadClickListener) {
        this.mSongsArrayList = mSongsArrayList;
        this.mContext = mContext;
        this.mDownloadClickListener = mDownloadClickListener;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RowItemsBinding binding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.row_items,parent,false);
        return new SongViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        ((SongViewHolder)holder).mBinding.setSongs(mSongsArrayList.get(position));
        if(checkFileExists(mSongsArrayList.get(position).getName())){
            ((SongViewHolder) holder).mBinding.xIvDownload.setImageResource(R.drawable.ic_play_circle_filled_black_24dp);
        }else if (mSongsArrayList.get(position).getState() == 0) {
            ((SongViewHolder) holder).mBinding.xIvDownload.setImageResource(R.drawable.ic_download);
        }
        else
            ((SongViewHolder) holder).mBinding.xIvDownload.setImageResource(R.drawable.ic_download);

        Picasso.with(mContext).load(mSongsArrayList.get(position).getImage()).into(((SongViewHolder) holder).mBinding.xIvSongImage);
        ((SongViewHolder) holder).mBinding.xIvDownload.setOnClickListener(new View.OnClickListener() {
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

    private class SongViewHolder extends RecyclerView.ViewHolder {

        RowItemsBinding mBinding;

        public SongViewHolder(RowItemsBinding binding) {
            super(binding.getRoot());
            this.mBinding = binding;

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
