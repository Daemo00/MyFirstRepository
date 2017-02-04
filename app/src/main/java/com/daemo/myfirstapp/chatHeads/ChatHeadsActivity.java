package com.daemo.myfirstapp.chatHeads;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;

import com.daemo.myfirstapp.MySuperActivity;
import com.daemo.myfirstapp.R;

public class ChatHeadsActivity extends MySuperActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_heads);
        checkDrawOverlayPermission();
        Button startService = (Button) findViewById(R.id.startService);
        Button stopService = (Button) findViewById(R.id.stopService);
        startService.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                startService(new Intent(getApplication(), ChatHeadService.class));

            }
        });
        stopService.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                stopService(new Intent(getApplication(), ChatHeadService.class));

            }
        });
    }

    @Override
    protected int getLayoutResID() {
        return R.layout.activity_chat_heads;
    }

    public final static int REQUEST_CODE = 1;

    public void checkDrawOverlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, REQUEST_CODE);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (Settings.canDrawOverlays(this)) {
                    // continue here - permission was granted
                }
            }
        }
    }
}