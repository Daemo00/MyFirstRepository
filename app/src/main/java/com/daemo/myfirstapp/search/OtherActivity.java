package com.daemo.myfirstapp.search;

import android.os.Bundle;
import android.view.View;

import com.daemo.myfirstapp.activities.MySuperActivity;
import com.daemo.myfirstapp.R;

public class OtherActivity extends MySuperActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_other);
    }

    public void search_click(View view) {
        onSearchRequested();
    }
}
