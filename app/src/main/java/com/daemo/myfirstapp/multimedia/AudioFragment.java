package com.daemo.myfirstapp.multimedia;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
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

import com.daemo.myfirstapp.R;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class AudioFragment extends Fragment implements View.OnClickListener {

    private MultimediaActivity multimediaActivity;
    private final static String mediaSessionTag = "TAG";
    private MediaSessionCompat mediaSessionCompat;
    private View root;
    private Map<String, Integer> streams;
    private MediaPlayer mediaPlayer;
    private Intent mReceivedIntent;

    public AudioFragment() {
        // Required empty public constructor
        Log.d(this.getClass().getSimpleName(), "onConstructor");

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(this.getClass().getSimpleName(), "onCreate");
        multimediaActivity = (MultimediaActivity) getActivity();
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mp.start();
                Log.d(this.getClass().getSimpleName(), "mediaPlayer started");
                ((TextView) root.findViewById(R.id.btnPlay)).setText(R.string.stop);
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        Log.d(this.getClass().getSimpleName(), "onCreateView");

        mediaSessionCompat = new MediaSessionCompat(multimediaActivity, mediaSessionTag);
        mediaSessionCompat.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS | MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);
        mediaSessionCompat.setActive(true);

        root = inflater.inflate(R.layout.fragment_audio, container, false);

        AppCompatSpinner spinner = (AppCompatSpinner) root.findViewById(R.id.spinner);
        spinner.setAdapter(new ArrayAdapter<>(multimediaActivity, android.R.layout.simple_list_item_1, getStreamsList()));

        root.findViewById(R.id.btnFocus).setOnClickListener(this);
        root.findViewById(R.id.btnStream).setOnClickListener(this);
        root.findViewById(R.id.btnSelectFile).setOnClickListener(this);
        root.findViewById(R.id.btnPlay).setOnClickListener(this);

        createAFListener();
//        MediaPlayer mediaPlayer = MediaPlayer.create(multimediaActivity, R.raw.sound_file_1);
//        mediaPlayer.start();
        return root;
    }

    AudioManager.OnAudioFocusChangeListener afChangeListener;

    private void createAFListener() {
        final AudioManager am = (AudioManager) multimediaActivity.getSystemService(Context.AUDIO_SERVICE);

        afChangeListener = new AudioManager.OnAudioFocusChangeListener() {
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
                Log.d(this.getClass().getSimpleName(), "onAudioFocusChange(" + focusString + ")");
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK)
                        am.adjustVolume(AudioManager.ADJUST_MUTE, AudioManager.FLAG_SHOW_UI);
                    else if (focusChange == AudioManager.AUDIOFOCUS_GAIN)
                        am.adjustVolume(AudioManager.ADJUST_UNMUTE, AudioManager.FLAG_SHOW_UI);
                }
            }
        };
    }

    @NonNull
    private String[] getStreamsList() {
        streams = new HashMap<>();
        try {
            Class am = Class.forName(AudioManager.class.getName());
            for (Field f : am.getFields())
                if (f.getName().toUpperCase().startsWith("STREAM") && f.getType() == int.class)
                    streams.put(f.getName().toUpperCase().replace("STREAM_", ""), f.getInt(null));

        } catch (ClassNotFoundException | IllegalAccessException e) {
            e.printStackTrace();
        }
        Set<String> ks = streams.keySet();
        return ks.toArray(new String[ks.size()]);
    }

    private void setStream() {
        AppCompatSpinner spinner = (AppCompatSpinner) root.findViewById(R.id.spinner);
        String key = (String) spinner.getSelectedItem();
        multimediaActivity.setVolumeControlStream(streams.get(key));
        multimediaActivity.showToast("set stream: " + key);
    }

    private void focus() {
        final AudioManager am = (AudioManager) multimediaActivity.getSystemService(Context.AUDIO_SERVICE);

        // Request audio focus for playback

        int result = am.requestAudioFocus(afChangeListener,
                // Use the music stream.
                AudioManager.STREAM_MUSIC,
                // Request permanent focus.
                AudioManager.AUDIOFOCUS_GAIN);

        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            multimediaActivity.showToast("Audio's focus granted");
        }
        // Abandon audio focus when playback complete
        am.abandonAudioFocus(afChangeListener);
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(this.getClass().getSimpleName(), "onPause");

    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(this.getClass().getSimpleName(), "onStart");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(this.getClass().getSimpleName(), "onDestroy");

        mediaSessionCompat.release();
        mediaPlayer.release();
        mediaPlayer = null;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnStream:
                setStream();
                break;
            case R.id.btnFocus:
                focus();
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
        Log.d(this.getClass().getSimpleName(), "onActivityResult");

        if (resultCode == Activity.RESULT_CANCELED) return;
        mReceivedIntent = data;

        try {
            mediaPlayer.setDataSource(multimediaActivity, data.getData());
        } catch (IOException e) {
            File myFile = new File(data.getData().getPath());

            Log.e(this.getClass().getSimpleName(), "Uri is: " + data.getData().toString());
            Log.e(this.getClass().getSimpleName(), "Absolute path is: " + myFile.getAbsolutePath());
            e.printStackTrace();
        }

        ((TextView) root.findViewById(R.id.tvSelectedURI)).setText(data.getData().toString());
        ((TextView) root.findViewById(R.id.btnPlay)).setText(R.string.play);
    }

    public void play() {


        boolean isService = ((SwitchCompat) root.findViewById(R.id.switchService)).isChecked();

        if (mediaPlayer.isPlaying() ||
                (MyService.getInstance() != null &&
                        MyService.getInstance().getMediaPlayer() != null &&
                        MyService.getInstance().getMediaPlayer().isPlaying())) {
            root.findViewById(R.id.switchService).setClickable(true);
            if (isService) {
                Intent i = new Intent(multimediaActivity, MyService.class);
                i.setAction(MyService.ACTION_STOP);
                multimediaActivity.startService(i);
            } else
                mediaPlayer.stop();
            ((TextView) root.findViewById(R.id.btnPlay)).setText(R.string.play);
        } else {
            if (TextUtils.isEmpty(((TextView) root.findViewById(R.id.tvSelectedURI)).getText())) {
                multimediaActivity.showToast("Select a file");
                return;
            }

            root.findViewById(R.id.switchService).setClickable(false);
            if (isService) {
                Intent i = new Intent(multimediaActivity, MyService.class);
                i.setAction(MyService.ACTION_PLAY);
                i.setData(mReceivedIntent.getData());
                multimediaActivity.startService(i);
            } else {
                mediaPlayer.prepareAsync();
            }
        }
    }
}