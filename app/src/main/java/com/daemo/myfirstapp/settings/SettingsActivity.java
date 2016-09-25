package com.daemo.myfirstapp.settings;

import android.os.Bundle;

import com.daemo.myfirstapp.MySuperActivity;
import com.daemo.myfirstapp.R;

public class SettingsActivity extends MySuperActivity {

    public static String pref_key_audio_stream = "pref_key_audio_stream";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected int getLayoutResID() {
        return R.layout.activity_settings;
    }
}
