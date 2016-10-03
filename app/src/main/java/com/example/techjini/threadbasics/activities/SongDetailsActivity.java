package com.example.techjini.threadbasics.activities;

import android.databinding.DataBindingUtil;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Toast;

import com.example.techjini.threadbasics.R;
import com.example.techjini.threadbasics.databinding.ActivitySongDetailsBinding;

import java.io.File;

public class SongDetailsActivity extends AppCompatActivity implements View.OnClickListener {

    private String mSongname, mSongArtist;
    private int mSongID;
    private MediaPlayer mMediaPlayer;
    private ActivitySongDetailsBinding mBinding;
    private boolean mIsClicked;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_song_details);
        mBinding.buttonPause.setEnabled(false);
        mBinding.buttonPlay.setOnClickListener(this);
        mBinding.buttonPause.setOnClickListener(this);
        Animation animation = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.zoom_in);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            mSongname = bundle.getString("name");
            mSongArtist = bundle.getString("artist");
            mSongID = bundle.getInt("id");
        }

        mBinding.textSongname.setText(getString(R.string.text_song_name) + String.valueOf(mSongname));
        mBinding.textSongname.startAnimation(animation);
        mBinding.textArtistname.setText(getString(R.string.text_song_artist) + String.valueOf(mSongArtist));
        mBinding.textSongid.setText(getString(R.string.text_song_id) + String.valueOf(mSongID));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_play:
                mBinding.buttonPause.setEnabled(true);
                File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), mSongname + ".mp3");
                String songUri = file.getAbsolutePath();
                Log.d("info", "playing the song");
                mMediaPlayer = MediaPlayer.create(SongDetailsActivity.this, Uri.parse(songUri));
                if (mMediaPlayer.isPlaying()) {
                    mMediaPlayer.stop();
                } else
                    mMediaPlayer.start();
                break;
            case R.id.button_pause:
                mMediaPlayer.pause();
                mBinding.buttonPause.setEnabled(false);
                break;
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.options_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_play) {
            if (item.getTitle().equals("Pause")){
                Toast.makeText(getApplicationContext(),getString(R.string.info_pausing_song),Toast.LENGTH_SHORT).show();
                mIsClicked = false;
            }else {
                Toast.makeText(getApplicationContext(), getString(R.string.info_playing_song), Toast.LENGTH_SHORT).show();
                mIsClicked = true;
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (mIsClicked){
            menu.getItem(0).setTitle(getString(R.string.action_pause));
        }else{
            menu.getItem(0).setTitle(getString(R.string.text_button_play));
        }

        return super.onPrepareOptionsMenu(menu);
    }
}
