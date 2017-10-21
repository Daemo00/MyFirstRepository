package com.daemo.myfirstapp.chatHeads;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.v4.os.ResultReceiver;
import android.view.View;
import android.widget.Button;

import com.daemo.myfirstapp.activities.MySuperActivity;
import com.daemo.myfirstapp.R;
import com.daemo.myfirstapp.common.Constants;
import com.daemo.myfirstapp.common.Utils;

public class ChatHeadsActivity extends MySuperActivity {

    private Button startService;
    private Button stopService;
    private ResultReceiver resultReceiver = new ResultReceiver(new Handler()) {
        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            super.onReceiveResult(resultCode, resultData);
            refreshUI();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_heads);
        startService = (Button) findViewById(R.id.startService);
        stopService = (Button) findViewById(R.id.stopService);

        startService.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (checkDrawOverlayPermission()) {
                    Intent intent = new Intent(getApplication(), ChatHeadService.class);
                    intent.putExtra(Constants.Location.RECEIVER, resultReceiver);
                    startService(intent);
                }
            }
        });
        stopService.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (checkDrawOverlayPermission()) {
                    Intent intent = new Intent(getApplication(), ChatHeadService.class);
                    intent.putExtra(Constants.Location.RECEIVER, resultReceiver);
                    stopService(intent);
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        refreshUI();
        if (Utils.isMyServiceRunning(this, ChatHeadService.class))
            startService.performClick();
    }

    private void refreshUI() {
        boolean service_started = Utils.isMyServiceRunning(this, ChatHeadService.class);
        startService.setEnabled(!service_started);
        stopService.setEnabled(service_started);
    }

    public boolean checkDrawOverlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, Constants.REQUEST_CODE_OVERLAY_PERMISSION);
            } else
                return true;
        }
        return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Constants.REQUEST_CODE_OVERLAY_PERMISSION) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (Settings.canDrawOverlays(this)) {
                    if (!Utils.isMyServiceRunning(this, ChatHeadService.class))
                        startService(new Intent(getApplication(), ChatHeadService.class));
                    else
                        stopService(new Intent(getApplication(), ChatHeadService.class));
                }
            }
        }
    }
}