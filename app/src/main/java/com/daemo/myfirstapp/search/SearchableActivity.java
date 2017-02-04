package com.daemo.myfirstapp.search;

import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;

import com.daemo.myfirstapp.MySuperActivity;
import com.daemo.myfirstapp.R;

public class SearchableActivity extends MySuperActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Get the intent, verify the action and get the query
        Intent intent = getIntent();
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            doMySearch(query);
        }
    }

    private void doMySearch(String query) {
        super.showToast("Searching " + query);
    }

    @Override
    protected int getLayoutResID() {
        return R.layout.activity_searchable;
    }
}
