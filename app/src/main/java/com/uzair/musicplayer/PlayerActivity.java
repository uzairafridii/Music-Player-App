package com.uzair.musicplayer;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;


public class PlayerActivity extends AppCompatActivity implements SeekBar.OnSeekBarChangeListener , MediaPlayer.OnCompletionListener {

    public static final int REQUEST_CODE = 110;
    private String title, path;
    private int duration, currentPosition;
    private SeekBar songProgressBar;
    private TextView songTitle, totoalDuration, currentDuration;
    private MediaPlayer mediaPlayer;
    private ImageView playBtn, pauseBtn;
    private Handler mHandler = new Handler();
    private int seekForwardTime = 5000; // 5000 milliseconds
    private int seekBackwardTime = 5000; // 5000 milliseconds
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_player);

        initViews();
    }

    private void initViews() {

        mediaPlayer = new MediaPlayer();
        title = getIntent().getStringExtra("title");
        path = getIntent().getStringExtra("path");

        songTitle = findViewById(R.id.songTitle);
        currentDuration = findViewById(R.id.currentDuration);
        totoalDuration = findViewById(R.id.totalDuration);
        playBtn = findViewById(R.id.playButton);
        songProgressBar = findViewById(R.id.seekBar);
        songProgressBar.setOnSeekBarChangeListener(this);

    }

    private void playSong(String songPath) {
        // create a music... pass the music path to the player
        mediaPlayer.reset();
        mediaPlayer = MediaPlayer.create(this, Uri.parse(songPath));
        // get the current and total duration of music
        duration = mediaPlayer.getDuration();
        currentPosition = mediaPlayer.getCurrentPosition();

        currentDuration.setText(""+milliSecondsToTimer(currentPosition));
        totoalDuration.setText("" +milliSecondsToTimer(duration));

        mediaPlayer.start();
        playBtn.setImageResource(R.drawable.pause_music_icon);

        // set Progress bar values
        songProgressBar.setProgress(0);
        songProgressBar.setMax(100);

        songTitle.setText(title);
        updateProgressBar();
    }

    // play button click
    public void PlayMusicButtonClick(View view) throws IOException {
        // if music is not playing then start else stop the music when click on button
        // check for already playing
        if (mediaPlayer.isPlaying()) {
            if (mediaPlayer != null) {
                mediaPlayer.pause();
                // Changing button image to play button
                playBtn.setImageResource(R.drawable.music_play_icon);
            }
        } else {
            // Resume song
            if (mediaPlayer != null) {
                mediaPlayer.start();
                // Changing button image to pause button
                playBtn.setImageResource(R.drawable.pause_music_icon);
            }
        }

    }


    // backward the music
    public void backwardButtonClick(View view)
    {
        // get current song position
        int currentPosition = mediaPlayer.getCurrentPosition();
        // check if seekBackward time is greater than 0 sec
        if(currentPosition - seekBackwardTime >= 0){
            // backward song
            mediaPlayer.seekTo(currentPosition - seekBackwardTime);
        }else{
            // backward to starting position
            mediaPlayer.seekTo(0);
        }

    }

    // forward the music
    public void forwardButtonClick(View view)
    {
        int currentPosition = mediaPlayer.getCurrentPosition();
        // check if seekForward time is lesser than song duration
        if(currentPosition + seekForwardTime <= mediaPlayer.getDuration())
        {
            // forward song
            mediaPlayer.seekTo(currentPosition + seekForwardTime);
        }else
        {
            // forward to end position
            mediaPlayer.seekTo(mediaPlayer.getDuration());
        }
    }


    // click on playlist icon button to show playlist and get the music to play
    public void playListButtonClick(View view)
    {

        Intent intent = new Intent(this , MainActivity.class);
        startActivityForResult(intent, REQUEST_CODE);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == REQUEST_CODE && resultCode == RESULT_OK && data != null)
        {
            path = data.getExtras().getString("path");
            title = data.getExtras().getString("title");

            playSong(path);
        }
        else
        {
            Toast.makeText(this, "Error in data", Toast.LENGTH_SHORT).show();
        }

    }

    // convert millisecond to time
    private String milliSecondsToTimer(long milliseconds){
        String finalTimerString = "";
        String secondsString = "";

        // Convert total duration into time
        int hours = (int)( milliseconds / (1000*60*60));
        int minutes = (int)(milliseconds % (1000*60*60)) / (1000*60);
        int seconds = (int) ((milliseconds % (1000*60*60)) % (1000*60) / 1000);
        // Add hours if there
        if(hours > 0){
            finalTimerString = hours + ":";
        }

        // Prepending 0 to seconds if it is one digit
        if(seconds < 10){
            secondsString = "0" + seconds;
        }else{
            secondsString = "" + seconds;}

        finalTimerString = finalTimerString + minutes + ":" + secondsString;

        // return timer string
        return finalTimerString;
    }

    private int getProgressPercentage(long currentDuration, long totalDuration){
        Double percentage = (double) 0;

        long currentSeconds = (int) (currentDuration / 1000);
        long totalSeconds = (int) (totalDuration / 1000);

        // calculating percentage
        percentage =(((double)currentSeconds)/totalSeconds)*100;

        // return percentage
        return percentage.intValue();
    }

    private int progressToTimer(int progress, int totalDuration) {
        int currentDuration = 0;
        totalDuration = (int) (totalDuration / 1000);
        currentDuration = (int) ((((double)progress) / 100) * totalDuration);

        // return current duration in milliseconds
        return currentDuration * 1000;
    }

    public void updateProgressBar() {
        mHandler.postDelayed(mUpdateTimeTask, 100);
    }

    /**
     * Background Runnable thread
     * */
    private Runnable mUpdateTimeTask = new Runnable() {
        public void run() {

            long totalDuration = mediaPlayer.getDuration();
            long cDuration = mediaPlayer.getCurrentPosition();

            // Displaying Total Duration time
            totoalDuration.setText(""+milliSecondsToTimer(totalDuration));
            // Displaying time completed playing
            currentDuration.setText(""+milliSecondsToTimer(cDuration));

            // Updating progress bar
            int progress = (int)(getProgressPercentage(cDuration, totalDuration));
            //Log.d("Progress", ""+progress);
            songProgressBar.setProgress(progress);

            // Running this thread after 100 milliseconds
            mHandler.postDelayed(this, 100);
        }
    };

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

    }

    // when forward the song with seekbar touch
    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        // remove message Handler from updating progress bar
        mHandler.removeCallbacks(mUpdateTimeTask);
    }

    // when forward song with seekbar touch and hold to the current position
    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        mHandler.removeCallbacks(mUpdateTimeTask);
        int totalDuration = mediaPlayer.getDuration();
        int currentPosition = progressToTimer(seekBar.getProgress(), totalDuration);

        // forward or backward to certain seconds
        mediaPlayer.seekTo(currentPosition);

        // update timer progress again
        updateProgressBar();
    }

    @Override
    public void onCompletion(MediaPlayer mp)
    {
        mediaPlayer.pause();
        playBtn.setImageResource(R.drawable.music_play_icon);

    }
}
