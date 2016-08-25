package com.daemo.myfirstapp;

import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;

public class MainActivity extends MySuperActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            fillRadioActivities();
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected int getLayoutResID() {
        return R.layout.activity_main;
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

    public void goToActivity(View view) {
        RadioGroup radio_activities = (RadioGroup) findViewById(R.id.radio_activities);
        RadioButton selectedRadioButton = (RadioButton) findViewById(radio_activities.getCheckedRadioButtonId());
        ActivityInfo selectedAI = list[radio_activities.indexOfChild(selectedRadioButton)];
        ComponentName name = new ComponentName(selectedAI.packageName, selectedAI.name);
        Intent i = new Intent()
                .setComponent(name);
        startActivity(i);
    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setMessage("Are you sure you want to exit?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        MainActivity.this.finish();
                        MainActivity.super.onBackPressed();
                    }
                })
                .setNegativeButton("No", null)
                .show();
    }
}
