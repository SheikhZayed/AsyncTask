package com.example.techjini.threadbasics.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by techjini on 23/9/16.
 */
public class DownloadModel implements Parcelable{

    private int progress;
    private int currentFileSize;
    private int totalFileSize;

    public DownloadModel(){

    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    public int getCurrentFileSize() {
        return currentFileSize;
    }

    public void setCurrentFileSize(int currentFileSize) {
        this.currentFileSize = currentFileSize;
    }

    public int getTotalFileSize() {
        return totalFileSize;
    }

    public void setTotalFileSize(int totalFileSize) {
        this.totalFileSize = totalFileSize;
    }

    @Override
    public int describeContents() {
        return 0;
    }


    @Override
    public void writeToParcel(Parcel dest, int flags) {

        dest.writeInt(progress);
        dest.writeInt(currentFileSize);
        dest.writeInt(totalFileSize);
    }

    private DownloadModel(Parcel in) {

        progress = in.readInt();
        currentFileSize = in.readInt();
        totalFileSize = in.readInt();
    }

    public static final Parcelable.Creator<DownloadModel> CREATOR = new Parcelable.Creator<DownloadModel>() {
        public DownloadModel createFromParcel(Parcel in) {
            return new DownloadModel(in);
        }

        public DownloadModel[] newArray(int size) {
            return new DownloadModel[size];
        }
    };
}
