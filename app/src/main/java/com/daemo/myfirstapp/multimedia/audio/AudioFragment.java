package com.daemo.myfirstapp.multimedia.audio;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v7.widget.AppCompatSpinner;
import android.support.v7.widget.SwitchCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.daemo.myfirstapp.MySuperFragment;
import com.daemo.myfirstapp.R;
import com.daemo.myfirstapp.common.Constants;
import com.daemo.myfirstapp.common.Utils;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class AudioFragment extends MySuperFragment implements View.OnClickListener, MediaPlayer.OnPreparedListener, AudioManager.OnAudioFocusChangeListener, MediaPlayer.OnErrorListener {

    private MediaSessionCompat mediaSessionCompat;
    private Map<String, Integer> streams;
    private MediaPlayer mediaPlayer;
    private Intent mReceivedIntent;

    private static AudioFragment inst;
    private AudioManager audioManager;
    private MediaSessionCompat.Callback mediaSessionCallback = new MediaSessionCompat.Callback() {
        @Override
        public boolean onMediaButtonEvent(Intent mediaButtonEvent) {
            Log.d(Utils.getTag(this), "onMediaButtonEvent(" + Utils.debugIntent(mediaButtonEvent) + ")");
            return super.onMediaButtonEvent(mediaButtonEvent);
        }
    };

    public static AudioFragment getInstance(Bundle args) {
        if (inst == null)
            inst = new AudioFragment();
        inst.setArguments(args);
        return inst;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        audioManager = (AudioManager) getContext().getSystemService(Context.AUDIO_SERVICE);
        mediaSessionCompat = setupMediaSession(getContext(), mediaSessionCallback);
        mediaPlayer = setupMediaPlayer(this, this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_audio, (ViewGroup) super.onCreateView(inflater, container, savedInstanceState), true);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        AppCompatSpinner spinner = (AppCompatSpinner) view.findViewById(R.id.spinner);
        spinner.setAdapter(new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, getStreamsList()));

        view.findViewById(R.id.btnToggleFocus).setOnClickListener(this);
        view.findViewById(R.id.btnStream).setOnClickListener(this);
        view.findViewById(R.id.btnSelectFile).setOnClickListener(this);
        view.findViewById(R.id.btnPlay).setOnClickListener(this);
    }

    @NonNull
    private String[] getStreamsList() {
        streams = new HashMap<>();
        try {
            Class am = Class.forName(AudioManager.class.getName());
            for (Field f : am.getFields())
                if (f.getName().toUpperCase().startsWith("STREAM_") && f.getType() == int.class)
                    streams.put(f.getName().toUpperCase().replace("STREAM_", ""), f.getInt(null));

        } catch (ClassNotFoundException | IllegalAccessException e) {
            e.printStackTrace();
        }
        Set<String> ks = streams.keySet();
        return ks.toArray(new String[ks.size()]);
    }

    public static MediaPlayer setupMediaPlayer(MediaPlayer.OnPreparedListener onPreparedListener, MediaPlayer.OnErrorListener onErrorListener) {
        MediaPlayer mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mediaPlayer.setOnPreparedListener(onPreparedListener);
        mediaPlayer.setOnErrorListener(onErrorListener);
        return mediaPlayer;
    }

    public static MediaSessionCompat setupMediaSession(Context ctx, MediaSessionCompat.Callback mediaSessionCallback) {
        MediaSessionCompat mediaSessionCompat = new MediaSessionCompat(ctx, Constants.mediaSessionTag);
        mediaSessionCompat.setCallback(mediaSessionCallback);
        mediaSessionCompat.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS | MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);
        mediaSessionCompat.setActive(true);
        return mediaSessionCompat;
    }

    private String getSelectedStream() {
        if (getView() == null) return "";
        AppCompatSpinner spinner = (AppCompatSpinner) getView().findViewById(R.id.spinner);
        return (String) spinner.getSelectedItem();
    }

    private void setStream() {
        getActivity().setVolumeControlStream(streams.get(getSelectedStream()));
        getMySuperActivity().showToast("Set stream " + getSelectedStream());
    }

    private void requestFocus() {
        // Request audio requestFocus for playback
        if (audioManager.requestAudioFocus(this, streams.get(getSelectedStream()), AudioManager.AUDIOFOCUS_GAIN)
                == AudioManager.AUDIOFOCUS_REQUEST_GRANTED)
            getMySuperActivity().showToast("Audio requestFocus granted for stream " + getSelectedStream());
    }

    private void abandonFocus() {
        // Abandon audio requestFocus when playback complete
        if (audioManager.abandonAudioFocus(this) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED)
            getMySuperActivity().showToast("Audio requestFocus lost");
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnStream:
                setStream();
                break;
            case R.id.btnToggleFocus:
                if (((ToggleButton) v).isChecked())
                    requestFocus();
                else
                    abandonFocus();
                break;
            case R.id.btnSelectFile:
                startActivityForResult((new Intent(Intent.ACTION_PICK)).setType("*/*"), 0);
                break;
            case R.id.btnPlay:
                play();
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(Utils.getTag(this), "onActivityResult");

        if (resultCode == Activity.RESULT_CANCELED) return;
        mReceivedIntent = data;

        try {
            mediaPlayer.setDataSource(getContext(), data.getData());
        } catch (IOException e) {
            File myFile = new File(data.getData().getPath());

            Log.e(Utils.getTag(this), "Uri is: " + data.getData().toString());
            Log.e(Utils.getTag(this), "Absolute path is: " + myFile.getAbsolutePath());
            e.printStackTrace();
        }
        if (getView() == null) return;
        ((TextView) getView().findViewById(R.id.tvSelectedURI)).setText(data.getData().getLastPathSegment());
        ((TextView) getView().findViewById(R.id.btnPlay)).setText(R.string.play);
    }

    public void play() {
        if (getView() == null) return;
        boolean isService = ((SwitchCompat) getView().findViewById(R.id.switchService)).isChecked();

        if (mediaPlayer.isPlaying() ||
                (MyMediaService.getInstance() != null &&
                        MyMediaService.getInstance().getMediaPlayer() != null &&
                        MyMediaService.getInstance().getMediaPlayer().isPlaying())) {
            getView().findViewById(R.id.switchService).setClickable(true);
            if (isService) {
                Intent i = new Intent(getContext(), MyMediaService.class);
                i.setAction(Constants.ACTION_STOP);
                getContext().startService(i);
            } else
                mediaPlayer.stop();
            ((TextView) getView().findViewById(R.id.btnPlay)).setText(R.string.play);
        } else {
            if (TextUtils.isEmpty(((TextView) getView().findViewById(R.id.tvSelectedURI)).getText())) {
                getMySuperActivity().showToast("Select a file");
                return;
            }

            getView().findViewById(R.id.switchService).setClickable(false);
            if (isService) {
                Intent i = new Intent(getContext(), MyMediaService.class);
                i.setAction(Constants.ACTION_PLAY);
                i.setData(mReceivedIntent.getData());
                getContext().startService(i);
            } else {
                mediaPlayer.prepareAsync();
            }
        }
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        mp.start();
        Log.d(Utils.getTag(this), "mediaPlayer started");
        if (getView() == null) return;
        ((TextView) getView().findViewById(R.id.btnPlay)).setText(R.string.stop);
    }

    @Override
    public void onAudioFocusChange(int focusChange) {
        String focusString = "";
        switch (focusChange) {
            case AudioManager.AUDIOFOCUS_GAIN:
                focusString = "Gain";
                break;
            case AudioManager.AUDIOFOCUS_LOSS:
                focusString = "Loss";
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                focusString = "Loss transient";
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                focusString = "Loss transient can duck";
                break;
        }
        Log.d(Utils.getTag(this), "onAudioFocusChange(" + focusString + ")");
        if (Utils.hasMarshmallow()) {
            if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK)
                audioManager.adjustVolume(AudioManager.ADJUST_MUTE, AudioManager.FLAG_SHOW_UI);
            else if (focusChange == AudioManager.AUDIOFOCUS_GAIN)
                audioManager.adjustVolume(AudioManager.ADJUST_UNMUTE, AudioManager.FLAG_SHOW_UI);
        }
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        Log.e(this.getClass().getSimpleName(), "onError(" + mp.toString() + ", " + what + ", " + extra + ")");
        return false;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mediaSessionCompat.release();
        mediaPlayer.release();
        mediaPlayer = null;
    }
}