package com.daemo.myfirstapp.notification;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

import com.daemo.myfirstapp.R;

public class NotificationSpecialActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_special);
    }

    public void finishDialog(View view) {
        finish();
    }
}
