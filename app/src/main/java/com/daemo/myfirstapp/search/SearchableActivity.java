package com.daemo.myfirstapp.search;

import android.os.Bundle;
import android.view.View;

import com.daemo.myfirstapp.MySuperActivity;
import com.daemo.myfirstapp.R;

public class SearchableActivity extends MySuperActivity {

    public static final String JARGON = "JARGON";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_searchable);
    }

    public void search_click(View view) {
//        pauseSomeStuff();
        Bundle appData = new Bundle();
        appData.putBoolean(SearchableActivity.JARGON, true);
        startSearch("initial query", true, appData, false);

//        onSearchRequested();
    }
}