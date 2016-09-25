package com.daemo.myfirstapp.multimedia;

import android.support.v4.media.session.MediaControllerCompat;
import android.util.Log;

public class MediaCallback extends MediaControllerCompat.Callback {

    @Override
    public void onAudioInfoChanged(MediaControllerCompat.PlaybackInfo info) {
        super.onAudioInfoChanged(info);
        Log.d("MediaCallback", "onAudioInfoChanged( " + info + ")");

    }
}