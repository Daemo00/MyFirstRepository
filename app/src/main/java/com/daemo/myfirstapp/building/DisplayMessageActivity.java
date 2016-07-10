package com.daemo.myfirstapp.building;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.daemo.myfirstapp.MainActivity;
import com.daemo.myfirstapp.R;

public class DisplayMessageActivity extends AppCompatActivity {

    private static final String TAG = DisplayMessageActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_message);

        Intent intent = getIntent();
        String message = intent.getStringExtra(MainActivity.EXTRA_MESSAGE);

        TextView textView = new TextView(this);
        textView.setTextSize(40);
        textView.setText(message);
        RelativeLayout layout = (RelativeLayout) findViewById(R.id.displaymessagelayout);
        layout.addView(textView);
    }
}
