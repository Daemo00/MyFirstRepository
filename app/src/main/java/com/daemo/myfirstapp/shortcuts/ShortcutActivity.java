package com.daemo.myfirstapp.shortcuts;

import android.content.Intent;
import android.content.pm.ShortcutInfo;
import android.content.pm.ShortcutManager;
import android.graphics.drawable.Icon;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.daemo.myfirstapp.MySuperActivity;
import com.daemo.myfirstapp.R;

import java.util.Collections;

public class ShortcutActivity extends MySuperActivity {

    private TextView isMultiWindow_tv2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shortcut);
        isMultiWindow_tv2 = (TextView) findViewById(R.id.textView2);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            isMultiWindow_tv2.setText(String.valueOf(isInMultiWindowMode()));
        }
    }

    @Override
    public void onMultiWindowModeChanged(boolean isInMultiWindowMode) {
        super.onMultiWindowModeChanged(isInMultiWindowMode);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            ((TextView) findViewById(R.id.textView)).setText(String.valueOf(isInMultiWindowMode));
        }
    }

    public void addShortcut(View view) {
        ShortcutManager shortcutManager = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            shortcutManager = getSystemService(ShortcutManager.class);
        }

        ShortcutInfo shortcut = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            shortcut = new ShortcutInfo.Builder(this, "id1")
                    .setShortLabel("Google site")
                    .setLongLabel("Open Google site")
                    .setIcon(Icon.createWithResource(this, R.drawable.ic_action_add))
                    .setIntent(new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/")))
                    .build();
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            if (shortcutManager != null) {
                shortcutManager.addDynamicShortcuts(Collections.singletonList(shortcut));
            }
        }
    }

    public void resetDynamicShortcuts(View view) {
        ShortcutManager shortcutManager;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            shortcutManager = getSystemService(ShortcutManager.class);
            shortcutManager.removeAllDynamicShortcuts();
        }
    }

    public void launchAdjacent(View view) {
        Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse("http://maps.google.com/"));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
            i.setFlags(Intent.FLAG_ACTIVITY_LAUNCH_ADJACENT);
        startActivity(i);
    }
}