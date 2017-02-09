package com.daemo.myfirstapp.search;

import android.os.Bundle;
import android.view.View;

import com.daemo.myfirstapp.MySuperActivity;
import com.daemo.myfirstapp.R;

public class OtherActivity extends MySuperActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected int getLayoutResID() {
        return R.layout.activity_other;
    }


    public void search_click(View view) {
        onSearchRequested();
    }
}
