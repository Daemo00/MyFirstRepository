package com.daemo.myfirstapp.multimedia.audio;

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
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import com.daemo.myfirstapp.common.Constants;
import com.daemo.myfirstapp.common.Utils;
import com.daemo.myfirstapp.multimedia.MultimediaActivity;

import java.io.IOException;

public class MyMediaService extends Service implements MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, AudioManager.OnAudioFocusChangeListener {

    private MediaPlayer mMediaPlayer = null;
    private Intent mIntent;
    private MusicIntentReceiver musicIntentReceiver;
    private AudioManager audioManager;
    private static MyMediaService inst = null;

    public static MyMediaService getInstance() {
        return inst;
    }


    public MediaPlayer getMediaPlayer() {
        return mMediaPlayer;
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(Utils.getTag(this), "onStartCommand(" + Utils.debugIntent(intent) + ", " + flags + ", " + startId + ")");
        if (intent == null) return super.onStartCommand(intent, flags, startId);
        inst = MyMediaService.this;

        if (intent.getAction().equals(Constants.ACTION_PLAY)) {
            Log.d(Utils.getTag(this), "onStartCommand, PLAY");
            mIntent = intent;
            audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
            int result = audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);

            if (result != AudioManager.AUDIOFOCUS_REQUEST_GRANTED)
                return super.onStartCommand(intent, flags, startId);

            onAudioFocusChange(AudioManager.AUDIOFOCUS_GAIN);
        } else if (intent.getAction().equals(Constants.ACTION_STOP)) {
            Log.d(Utils.getTag(this), "onStartCommand, STOP");
            onAudioFocusChange(AudioManager.AUDIOFOCUS_LOSS);
        } else if (intent.getAction().equals(AudioManager.ACTION_AUDIO_BECOMING_NOISY)) {
            Log.d(Utils.getTag(this), "onStartCommand, BECOMING_NOISY");
            onAudioFocusChange(AudioManager.AUDIOFOCUS_LOSS_TRANSIENT);
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
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
                if (mMediaPlayer == null) break;
                if (mMediaPlayer.isPlaying()) mMediaPlayer.stop();
                mMediaPlayer.release();
                mMediaPlayer = null;
                audioManager.abandonAudioFocus(this);
                unregisterReceiver(musicIntentReceiver);
                stopForeground(true);
                break;

            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                if (mMediaPlayer == null) break;
                // Lost focus for a short time, but we have to stop playback.
                // We don't release the media player because playback is likely to resume
                if (mMediaPlayer.isPlaying()) mMediaPlayer.pause();
                break;

            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                if (mMediaPlayer == null) break;
                // Lost focus for a short time, but it's ok to keep playing at an attenuated level
                if (mMediaPlayer.isPlaying()) mMediaPlayer.setVolume(0.1f, 0.1f);
                break;
        }
    }

    private void initMediaPlayer() {
        Log.d(this.getClass().getSimpleName(), "initMediaPlayer");
        mMediaPlayer = AudioFragment.setupMediaPlayer(this, this);
        try {
            mMediaPlayer.setDataSource(getApplicationContext(), mIntent.getData());
        } catch (IOException e) {
            e.printStackTrace();
        }
        mMediaPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        mMediaPlayer.prepareAsync();

        musicIntentReceiver = new MusicIntentReceiver();
        IntentFilter intentFilter = new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
        registerReceiver(musicIntentReceiver, intentFilter);
        configForeground();
    }

    private void configForeground() {
        Log.d(Utils.getTag(this), "configForeground");

        String songName;
        MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
        mediaMetadataRetriever.setDataSource(getApplicationContext(), mIntent.getData());
        songName = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);

        Intent i = new Intent(getApplicationContext(), MultimediaActivity.class);
        PendingIntent pi = PendingIntent.getActivity(getApplicationContext(), Constants.REQUEST_CODE_MUSIC, i, PendingIntent.FLAG_UPDATE_CURRENT);

        Notification notification = new NotificationCompat.Builder(getApplicationContext())
                .setContentText("Playing " + songName)
                .setSmallIcon(android.R.drawable.ic_media_play)
                .setContentIntent(pi)
                .build();
        notification.flags |= Notification.FLAG_ONGOING_EVENT;
        startForeground(Constants.NOTIFICATION_ID_MUSIC, notification);
    }

    public IBinder onBind(Intent intent) {
        Log.d(Utils.getTag(this), "onBind(" + Utils.debugIntent(intent) + ")");
        return null;
    }

    public void onPrepared(MediaPlayer player) {
        Log.d(Utils.getTag(this), "onPrepared(" + player.toString() + ")");
        player.start();
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        Log.e(Utils.getTag(this), "onError(" + mp.toString() + ", " + what + ", " + extra + ")");
        return false;
    }

    @Override
    public void onDestroy() {
        if (mMediaPlayer != null) mMediaPlayer.release();
        inst = null;
    }


    public void audioNoisy() {
        onAudioFocusChange(AudioManager.AUDIOFOCUS_LOSS_TRANSIENT);
    }
}

