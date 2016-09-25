package com.daemo.myfirstapp.multimedia;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.annotation.Nullable;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import com.daemo.myfirstapp.R;

import java.io.IOException;

public class MyService extends Service implements MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, AudioManager.OnAudioFocusChangeListener {
    public static final String ACTION_PLAY = "com.daemo.myfirstapp.PLAY";
    public static final String ACTION_STOP = "com.daemo.myfirstapp.STOP";
    private static final int NOTIFICATION_ID = 1;
    private MediaPlayer mMediaPlayer = null;
    private Intent mIntent;
    private MusicIntentReceiver musicIntentReceiver;

    public MediaPlayer getMediaPlayer() {
        return mMediaPlayer;
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(this.getClass().getSimpleName(), "onStartCommand");
        if (intent == null) return super.onStartCommand(intent, flags, startId);
        inst = MyService.this;

        if (intent.getAction().equals(ACTION_PLAY)) {
            Log.d(this.getClass().getSimpleName(), "onStartCommand, PLAY");
            mIntent = intent;
            AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
            int result = audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);

            if (result != AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                return super.onStartCommand(intent, flags, startId);
            }
            onAudioFocusChange(AudioManager.AUDIOFOCUS_GAIN);
        } else if (intent.getAction().equals(ACTION_STOP)) {
            Log.d(this.getClass().getSimpleName(), "onStartCommand, STOP");
            onAudioFocusChange(AudioManager.AUDIOFOCUS_LOSS);
            stopForeground(true);
        }
        return super.onStartCommand(intent, flags, startId);
    }

    private void configForeground() {
        Log.d(this.getClass().getSimpleName(), "configForeground");

        String songName;
        MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
        mediaMetadataRetriever.setDataSource(getApplicationContext(), mIntent.getData());
        songName = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);

// assign the song name to songName
        PendingIntent pi = PendingIntent.getActivity(getApplicationContext(), 0,
                new Intent(getApplicationContext(), MultimediaActivity.class),
                PendingIntent.FLAG_UPDATE_CURRENT);
        Notification notification = new NotificationCompat.Builder(getApplicationContext())
                .setContentText("Playing " + songName)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentIntent(pi)
                .build();
        notification.flags |= Notification.FLAG_ONGOING_EVENT;
        startForeground(NOTIFICATION_ID, notification);
    }

    public void onAudioFocusChange(int focusChange) {
        switch (focusChange) {
            case AudioManager.AUDIOFOCUS_GAIN:
                // resume playback
                if (mMediaPlayer == null) initMediaPlayer();
                else if (!mMediaPlayer.isPlaying()) mMediaPlayer.start();
                mMediaPlayer.setVolume(1.0f, 1.0f);
                break;

            case AudioManager.AUDIOFOCUS_LOSS:
                // Lost focus for an unbounded amount of time: stop playback and release media player
                unregisterReceiver(musicIntentReceiver);
                stopForeground(true);
                if (mMediaPlayer.isPlaying()) mMediaPlayer.stop();
                mMediaPlayer.release();
                mMediaPlayer = null;
                break;

            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                // Lost focus for a short time, but we have to stop playback. We don't release the media player because playback is likely to resume
                if (mMediaPlayer.isPlaying()) mMediaPlayer.pause();
                break;

            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                // Lost focus for a short time, but it's ok to keep playing at an attenuated level
                if (mMediaPlayer.isPlaying()) mMediaPlayer.setVolume(0.1f, 0.1f);
                break;
        }
    }

    private void initMediaPlayer() {
        Log.d(this.getClass().getSimpleName(), "initMediaPlayer");
        mMediaPlayer = new MediaPlayer();
        try {
            mMediaPlayer.setDataSource(getApplicationContext(), mIntent.getData());
        } catch (IOException e) {
            e.printStackTrace();
        }
        mMediaPlayer.setOnPreparedListener(this);
        mMediaPlayer.setOnErrorListener(this);
        mMediaPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        mMediaPlayer.prepareAsync();

        musicIntentReceiver = new MusicIntentReceiver();
        IntentFilter intentFilter = new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
        registerReceiver(musicIntentReceiver, intentFilter);
        configForeground();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.d(this.getClass().getSimpleName(), "onBind");
        return null;
    }

    public void onPrepared(MediaPlayer player) {
        Log.d(this.getClass().getSimpleName(), "onPrepared");
        player.start();
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        Log.d(this.getClass().getSimpleName(), "onError");
        return false;
    }

    @Override
    public void onDestroy() {
        if (mMediaPlayer != null) mMediaPlayer.release();
        inst = null;
    }

    private static MyService inst = null;

    public static MyService getInstance() {
        return inst;
    }

    public void audioNoisy() {
        onAudioFocusChange(AudioManager.AUDIOFOCUS_LOSS_TRANSIENT);
    }
}

