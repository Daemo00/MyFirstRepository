package com.daemo.myfirstapp.settings;


import android.os.Bundle;
import android.preference.PreferenceFragment;

import com.daemo.myfirstapp.R;
import com.daemo.myfirstapp.Utils;
import com.daemo.myfirstapp.common.logger.Log;

public class SettingsFragment extends PreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e(Utils.getTag(this), "Creating fragment");

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);
    }
}