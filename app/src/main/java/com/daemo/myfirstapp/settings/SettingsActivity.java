package com.daemo.myfirstapp.settings;

import android.os.Bundle;

import com.daemo.myfirstapp.MySuperActivity;
import com.daemo.myfirstapp.R;

public class SettingsActivity extends MySuperActivity {

    private SettingsFragment fragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        if (savedInstanceState == null) {
            this.fragment = new SettingsFragment();
            this.fragment.setActivityIntent(getIntent());
            getFragmentManager().beginTransaction()
                    .replace(R.id.content_frame, this.fragment)
                    .commit();
        }
    }
}