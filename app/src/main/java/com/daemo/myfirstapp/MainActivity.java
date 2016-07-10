package com.daemo.myfirstapp;

import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.daemo.myfirstapp.building.DisplayMessageActivity;

public class MainActivity extends AppCompatActivity {

    public static final String EXTRA_MESSAGE = MainActivity.class.getPackage().getName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try {
            fillRadioActivities();
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    private ActivityInfo[] list = new ActivityInfo[5];

    private void fillRadioActivities() throws PackageManager.NameNotFoundException {
        RadioGroup radio_activities = (RadioGroup) findViewById(R.id.radio_activities);
        RadioButton rb = null;
        int i = 0;
        for (ActivityInfo ai : getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_ACTIVITIES).activities) {
            if (ai.parentActivityName != null){
                rb = new RadioButton(this);
                list[i] = ai;
                rb.setText(ai.name);
                radio_activities.addView(rb, i);
                i++;
            }
        }

        if (rb != null) {
            rb.setChecked(true);
        }
    }

    public void sendMessage(View view) {
        Intent intent = new Intent(this, DisplayMessageActivity.class);
        EditText editText = (EditText) findViewById(R.id.edit_message);
        String message = editText.getText().toString();
        intent.putExtra(EXTRA_MESSAGE, message);
        startActivity(intent);
    }

    public void goToActivity(View view) {
        RadioGroup radio_activities = (RadioGroup) findViewById(R.id.radio_activities);
        RadioButton selectedRadioButton = (RadioButton) findViewById(radio_activities.getCheckedRadioButtonId());
        ActivityInfo selectedAI = list[radio_activities.indexOfChild(selectedRadioButton)];
        ComponentName name = new ComponentName(selectedAI.packageName, selectedAI.name);
        Intent i = new Intent()
                .setComponent(name);
        startActivity(i);
    }
}
