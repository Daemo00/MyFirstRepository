package com.daemo.myfirstapp.settings;

import android.os.Bundle;

import com.daemo.myfirstapp.MySuperActivity;
import com.daemo.myfirstapp.R;
import com.daemo.myfirstapp.common.Utils;
import com.daemo.myfirstapp.common.logger.Log;

public class SettingsActivity extends MySuperActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e(Utils.getTag(this), "Creating activity");
        getFragmentManager().beginTransaction()
                .replace(R.id.content_frame, new SettingsFragment())
                .commit();
    }

    @Override
    protected int getLayoutResID() {
        return R.layout.activity_settings;
    }
}
