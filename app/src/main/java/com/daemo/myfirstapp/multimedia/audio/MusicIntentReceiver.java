package com.daemo.myfirstapp.multimedia.audio;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;

public class MusicIntentReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context ctx, Intent intent) {
        if (intent.getAction().equals(AudioManager.ACTION_AUDIO_BECOMING_NOISY)) {
            // signal your service to stop playback (via an Intent, for instance)
            Intent i = new Intent(ctx, MyMediaService.class);
            i.setAction(intent.getAction());
            ctx.startService(i);
//            MyMediaService.getInstance().audioNoisy();
        }
    }
}
