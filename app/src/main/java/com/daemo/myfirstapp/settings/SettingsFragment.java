package com.daemo.myfirstapp.settings;


import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.text.TextUtils;
import android.util.Log;

import com.daemo.myfirstapp.R;
import com.daemo.myfirstapp.common.Utils;

public class SettingsFragment extends PreferenceFragment {

    private String page;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);
        if (!TextUtils.isEmpty(page)) {
            openPreferenceScreen(page);
        }
    }

    public void setActivityIntent(final Intent intent) {
        if (intent != null) {
            if (Intent.ACTION_VIEW.equals(intent.getAction())) {
                if (intent.getExtras() != null) {
                    page = intent.getExtras().getString("page");
                }
            }
        }
    }

    private void openPreferenceScreen(final String screenName) {
        final Preference pref = findPreference(screenName);
        Log.d(Utils.getTag(this), "pref is " + pref);
        if (pref instanceof PreferenceScreen) {
            final PreferenceScreen preferenceScreen = (PreferenceScreen) pref;
            getActivity().setTitle(preferenceScreen.getTitle());
            setPreferenceScreen((PreferenceScreen) pref);
        }
    }
}