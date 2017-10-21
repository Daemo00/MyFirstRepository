package com.daemo.myfirstapp.performance;

import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v7.widget.AppCompatTextView;
import android.util.Pair;
import android.view.View;

import com.daemo.myfirstapp.activities.MySuperActivity;
import com.daemo.myfirstapp.R;

public class PerformanceActivity extends MySuperActivity {

    @Override
    public void onCreate(Bundle savedInstanceState, PersistableBundle persistentState) {
        super.onCreate(savedInstanceState, persistentState);
        setContentView(R.layout.activity_performance);
    }

    public void batteryStats(View view) {
        Pair<Integer, String> batteryInfo = MyBatteryStatusReceiver.buildMessage(this);
        ((AppCompatTextView) findViewById(R.id.textView)).setText(batteryInfo.second);
    }
}